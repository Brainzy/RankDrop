package io.github.brainzy.rankdrop.service;

import io.github.brainzy.rankdrop.dto.ScoreEntryResponse;
import io.github.brainzy.rankdrop.dto.ScoreSubmitResponse;
import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.entity.ScoreEntry;
import io.github.brainzy.rankdrop.entity.SortOrder;
import io.github.brainzy.rankdrop.exception.LeaderboardNotFoundException;
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

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreEntryRepository scoreRepository;
    private final LeaderboardRepository leaderboardRepository;

    @Transactional
    public ScoreSubmitResponse submitScore(String slug, String playerName, double value) {
        Leaderboard leaderboard = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        if (leaderboard.isCumulative()) {
            return handleCumulativeScoreSubmission(leaderboard, playerName, value);
        }

        if (!leaderboard.isAllowMultipleScores()) {
            return handleSingleScoreSubmission(leaderboard, playerName, value);
        }

        return createAndSaveScore(leaderboard, playerName, value);
    }

    private ScoreSubmitResponse handleCumulativeScoreSubmission(Leaderboard leaderboard, String playerName, double value) {
        Optional<ScoreEntry> existingOpt = scoreRepository.findTopByLeaderboard_SlugAndPlayerAlias(
                leaderboard.getSlug(), playerName, Sort.unsorted());

        if (existingOpt.isEmpty()) {
            return createAndSaveScore(leaderboard, playerName, value);
        }

        ScoreEntry existing = existingOpt.get();
        existing.setScoreValue(existing.getScoreValue() + value);
        existing.setSubmittedAt(LocalDateTime.now());

        return ScoreSubmitResponse.fromEntity(scoreRepository.save(existing));
    }

    private ScoreSubmitResponse handleSingleScoreSubmission(Leaderboard leaderboard, String playerName, double value) {
        Optional<ScoreEntry> existingOpt = scoreRepository.findTopByLeaderboard_SlugAndPlayerAlias(
                leaderboard.getSlug(), playerName, Sort.unsorted());

        if (existingOpt.isEmpty()) {
            return createAndSaveScore(leaderboard, playerName, value);
        }

        ScoreEntry existing = existingOpt.get();
        if (isNewScoreBetter(value, existing.getScoreValue(), leaderboard.getSortOrder())) {
            existing.setScoreValue(value);
            existing.setSubmittedAt(LocalDateTime.now());
            return ScoreSubmitResponse.fromEntity(scoreRepository.save(existing));
        }

        return ScoreSubmitResponse.fromEntity(existing);
    }

    private ScoreSubmitResponse createAndSaveScore(Leaderboard leaderboard, String playerName, double value) {
        ScoreEntry entry = ScoreEntry.builder()
                .playerAlias(playerName)
                .scoreValue(value)
                .leaderboard(leaderboard)
                .build();

        return ScoreSubmitResponse.fromEntity(scoreRepository.save(entry));
    }

    public List<ScoreEntryResponse> getTopScores(String slug, int limit) {
        Leaderboard leaderboard = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        Sort.Direction direction = resolveSortDirection(leaderboard.getSortOrder());
        Pageable pageable = PageRequest.of(0, limit, Sort.by(direction, "scoreValue"));

        List<ScoreEntry> entries = scoreRepository.findByLeaderboard_Slug(slug, pageable).getContent();

        List<ScoreEntryResponse> response = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            response.add(ScoreEntryResponse.fromEntity(entries.get(i), i + 1));
        }

        return response;
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

    private Sort.Direction resolveSortDirection(SortOrder sortOrder) {
        return (sortOrder == SortOrder.ASC) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private boolean isNewScoreBetter(double newScore, double currentScore, SortOrder sortOrder) {
        return (sortOrder == SortOrder.ASC) ? newScore < currentScore : newScore > currentScore;
    }
}