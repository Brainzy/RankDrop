package io.github.brainzy.rankdrop.service;

import io.github.brainzy.rankdrop.dto.ScoreEntryResponse;
import io.github.brainzy.rankdrop.dto.ScoreSubmitResponse;
import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.entity.ScoreEntry;
import io.github.brainzy.rankdrop.entity.SortOrder;
import io.github.brainzy.rankdrop.exception.LeaderboardNotFoundException;
import io.github.brainzy.rankdrop.exception.PlayerBannedException;
import io.github.brainzy.rankdrop.exception.PlayerNotFoundException;
import io.github.brainzy.rankdrop.repository.LeaderboardRepository;
import io.github.brainzy.rankdrop.repository.ScoreEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreEntryRepository scoreRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final ScoreCacheService scoreCacheService;
    private final PlayerService playerService;
    private final WebhookService webhookService;

    @Transactional
    public ScoreSubmitResponse submitScore(String slug, String playerName, double value, String metadata) {
        Leaderboard leaderboard = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        if (playerService.isPlayerBanned(playerName)) {
            throw new PlayerBannedException(playerName);
        }

        validateScore(value, leaderboard);

        ScoreSubmitResponse response = switch (leaderboard.getScoreStrategy()) {
            case CUMULATIVE -> handleCumulativeScoreSubmission(leaderboard, playerName, value, metadata);
            case BEST_ONLY -> handleSingleScoreSubmission(leaderboard, playerName, value, metadata);
            case MULTIPLE_ENTRIES -> createAndSaveScore(leaderboard, playerName, value, metadata);
        };

        long betterScoresCount;
        if (leaderboard.getSortOrder() == SortOrder.ASC) {
            betterScoresCount = scoreRepository.countBetterScoresAsc(slug, response.scoreValue(), response.submittedAt());
        } else {
            betterScoresCount = scoreRepository.countBetterScoresDesc(slug, response.scoreValue(), response.submittedAt());
        }

        if (betterScoresCount < 100) {
            scoreCacheService.evictTopScoresCache(slug);
        }

        webhookService.fireTopScoreWebhookIfEligible(slug, playerName, value, (int) betterScoresCount + 1);

        return response;
    }

    private void validateScore(double value, Leaderboard leaderboard) {
        if (leaderboard.getMinScore() != null && value < leaderboard.getMinScore()) {
            throw new IllegalArgumentException("Score is below the minimum allowed value of " + leaderboard.getMinScore());
        }
        if (leaderboard.getMaxScore() != null && value > leaderboard.getMaxScore()) {
            throw new IllegalArgumentException("Score exceeds the maximum allowed value of " + leaderboard.getMaxScore());
        }
    }

    private ScoreSubmitResponse handleCumulativeScoreSubmission(Leaderboard leaderboard, String playerName, double value, String metadata) {
        int rowsUpdated = scoreRepository.incrementScore(
                leaderboard.getSlug(), playerName, value, LocalDateTime.now(ZoneOffset.UTC));

        if (rowsUpdated == 0) {
            return createAndSaveScore(leaderboard, playerName, value, metadata);
        }

        return scoreRepository.findTopByLeaderboard_SlugAndPlayerAlias(
                        leaderboard.getSlug(), playerName, Sort.unsorted())
                .map(ScoreSubmitResponse::fromEntity)
                .orElseThrow(() -> new IllegalStateException("Score not found after increment for player: " + playerName));
    }

    private ScoreSubmitResponse handleSingleScoreSubmission(Leaderboard leaderboard, String playerName, double value, String metadata) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        int rowsUpdated = leaderboard.getSortOrder() == SortOrder.ASC
                ? scoreRepository.updateIfLowerScore(leaderboard.getSlug(), playerName, value, now)
                : scoreRepository.updateIfHigherScore(leaderboard.getSlug(), playerName, value, now);

        if (rowsUpdated == 0) {
            Optional<ScoreEntry> existing = scoreRepository.findTopByLeaderboard_SlugAndPlayerAlias(
                    leaderboard.getSlug(), playerName, Sort.unsorted());

            return existing.map(ScoreSubmitResponse::fromEntity).orElseGet(() -> createAndSaveScore(leaderboard, playerName, value, metadata));

        }

        return scoreRepository.findTopByLeaderboard_SlugAndPlayerAlias(
                        leaderboard.getSlug(), playerName, Sort.unsorted())
                .map(ScoreSubmitResponse::fromEntity)
                .orElseThrow();
    }

    private ScoreSubmitResponse createAndSaveScore(Leaderboard leaderboard, String playerName, double value, String metadata) {
        ScoreEntry entry = ScoreEntry.builder()
                .playerAlias(playerName)
                .scoreValue(value)
                .leaderboard(leaderboard)
                .metadata(metadata)
                .build();

        return ScoreSubmitResponse.fromEntity(scoreRepository.save(entry));
    }

    public List<ScoreEntryResponse> getTopScores(String slug, int limit) {
        List<ScoreEntryResponse> top100 = scoreCacheService.getTop100(slug);
        return top100.stream().limit(limit).collect(Collectors.toList());
    }

    public List<ScoreEntryResponse> getPlayerScoreWithSurrounding(String slug, String playerAlias, int surrounding) {
        Leaderboard leaderboard = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        Sort.Direction bestSort = resolveSortDirection(leaderboard.getSortOrder());

        ScoreEntry bestEntry = scoreRepository.findTopByLeaderboard_SlugAndPlayerAlias(slug, playerAlias, Sort.by(bestSort, "scoreValue"))
                .orElseThrow(() -> new PlayerNotFoundException(playerAlias));

        long betterScoresCount;
        if (leaderboard.getSortOrder() == SortOrder.ASC) {
            betterScoresCount = scoreRepository.countBetterScoresAsc(slug, bestEntry.getScoreValue(), bestEntry.getSubmittedAt());
        } else {
            betterScoresCount = scoreRepository.countBetterScoresDesc(slug, bestEntry.getScoreValue(), bestEntry.getSubmittedAt());
        }

        long playerRank = betterScoresCount + 1;

        if (surrounding <= 0) {
            return Collections.singletonList(ScoreEntryResponse.fromEntity(bestEntry, playerRank));
        }

        Pageable limit = PageRequest.of(0, surrounding);
        List<ScoreEntry> higherScores = scoreRepository.findHigherScores(slug, bestEntry.getScoreValue(), bestEntry.getSubmittedAt(), limit).getContent();
        List<ScoreEntry> lowerScores = scoreRepository.findLowerScores(slug, bestEntry.getScoreValue(), bestEntry.getSubmittedAt(), limit).getContent();

        List<ScoreEntry> betterList;
        List<ScoreEntry> worseList;

        if (leaderboard.getSortOrder() == SortOrder.ASC) {
            betterList = new ArrayList<>(lowerScores);
            worseList = new ArrayList<>(higherScores);
        } else {
            betterList = new ArrayList<>(higherScores);
            worseList = new ArrayList<>(lowerScores);
        }

        Collections.reverse(betterList);

        List<ScoreEntryResponse> result = new ArrayList<>();

        for (int i = 0; i < betterList.size(); i++) {
            long rank = playerRank - betterList.size() + i;
            result.add(ScoreEntryResponse.fromEntity(betterList.get(i), rank));
        }

        result.add(ScoreEntryResponse.fromEntity(bestEntry, playerRank));

        long currentRank = playerRank + 1;
        for (ScoreEntry entry : worseList) {
            result.add(ScoreEntryResponse.fromEntity(entry, currentRank++));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<ScoreEntryResponse> getAllScoresForLeaderboard(String slug, int page, int size) {
        Leaderboard leaderboard = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        Sort.Direction direction = resolveSortDirection(leaderboard.getSortOrder());
        Sort sort = Sort.by(direction, "scoreValue").and(Sort.by(Sort.Direction.ASC, "submittedAt"));

        int safSize = Math.min(size, 1000);
        Pageable pageable = PageRequest.of(page, safSize, sort);

        Page<ScoreEntry> scorePage = scoreRepository.findByLeaderboard_Slug(slug, pageable);

        long pageOffset = (long) page * safSize;
        List<ScoreEntryResponse> result = new ArrayList<>();
        for (int i = 0; i < scorePage.getContent().size(); i++) {
            result.add(ScoreEntryResponse.fromEntity(scorePage.getContent().get(i), pageOffset + i + 1));
        }
        return result;
    }

    @Transactional
    public void removeScore(Long scoreId) {
        ScoreEntry scoreEntry = scoreRepository.findById(scoreId)
                .orElseThrow(() -> new IllegalArgumentException("Score entry not found with ID: " + scoreId));

        String leaderboardSlug = scoreEntry.getLeaderboard().getSlug();
        scoreRepository.delete(scoreEntry);
        scoreCacheService.evictTopScoresCache(leaderboardSlug);
    }

    private Sort.Direction resolveSortDirection(SortOrder sortOrder) {
        return (sortOrder == SortOrder.ASC) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}
