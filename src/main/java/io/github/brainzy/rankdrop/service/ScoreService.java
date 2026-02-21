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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Transactional
    public ScoreSubmitResponse submitScore(String slug, String playerName, double value, String metadata) {
        Leaderboard leaderboard = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        if (playerService.isPlayerBanned(playerName)) {
            throw new PlayerBannedException(playerName);
        }

        validateScore(value, leaderboard);

        ScoreSubmitResponse response;
        if (leaderboard.isCumulative()) {
            response = handleCumulativeScoreSubmission(leaderboard, playerName, value, metadata);
        } else if (!leaderboard.isAllowMultipleScores()) {
            response = handleSingleScoreSubmission(leaderboard, playerName, value, metadata);
        } else {
            response = createAndSaveScore(leaderboard, playerName, value, metadata);
        }

        long betterScoresCount;
        if (leaderboard.getSortOrder() == SortOrder.ASC) {
            betterScoresCount = scoreRepository.countBetterScoresAsc(slug, response.scoreValue(), response.submittedAt());
        } else {
            betterScoresCount = scoreRepository.countBetterScoresDesc(slug, response.scoreValue(), response.submittedAt());
        }

        if (betterScoresCount < 100) {
            scoreCacheService.evictTopScoresCache(slug);
        }

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
        Optional<ScoreEntry> existingOpt = scoreRepository.findTopByLeaderboard_SlugAndPlayerAlias(
                leaderboard.getSlug(), playerName, Sort.unsorted());

        if (existingOpt.isEmpty()) {
            return createAndSaveScore(leaderboard, playerName, value, metadata);
        }

        ScoreEntry existing = existingOpt.get();
        existing.setScoreValue(existing.getScoreValue() + value);
        existing.setSubmittedAt(LocalDateTime.now());
        if (metadata != null) {
            existing.setMetadata(metadata);
        }

        return ScoreSubmitResponse.fromEntity(scoreRepository.save(existing));
    }

    private ScoreSubmitResponse handleSingleScoreSubmission(Leaderboard leaderboard, String playerName, double value, String metadata) {
        Optional<ScoreEntry> existingOpt = scoreRepository.findTopByLeaderboard_SlugAndPlayerAlias(
                leaderboard.getSlug(), playerName, Sort.unsorted());

        if (existingOpt.isEmpty()) {
            return createAndSaveScore(leaderboard, playerName, value, metadata);
        }

        ScoreEntry existing = existingOpt.get();
        if (isNewScoreBetter(value, existing.getScoreValue(), leaderboard.getSortOrder())) {
            existing.setScoreValue(value);
            existing.setSubmittedAt(LocalDateTime.now());
            if (metadata != null) {
                existing.setMetadata(metadata);
            }
            return ScoreSubmitResponse.fromEntity(scoreRepository.save(existing));
        }

        return ScoreSubmitResponse.fromEntity(existing);
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

    private boolean isNewScoreBetter(double newScore, double currentScore, SortOrder sortOrder) {
        return (sortOrder == SortOrder.ASC) ? newScore < currentScore : newScore > currentScore;
    }
}