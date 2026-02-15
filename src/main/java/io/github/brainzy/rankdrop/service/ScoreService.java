package io.github.brainzy.rankdrop.service;

import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.entity.ScoreEntry;
import io.github.brainzy.rankdrop.entity.SortOrder;
import io.github.brainzy.rankdrop.exception.LeaderboardNotFoundException;
import io.github.brainzy.rankdrop.repository.LeaderboardRepository;
import io.github.brainzy.rankdrop.repository.ScoreEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreEntryRepository scoreRepository;
    private final LeaderboardRepository leaderboardRepository;

    @Transactional
    public ScoreEntry submitScore(String slug, String playerName, double value) {
        Leaderboard leaderboard = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        if (!leaderboard.isAllowMultipleScores()) {
            return handleSingleScoreSubmission(leaderboard, playerName, value);
        }

        return createAndSaveScore(leaderboard, playerName, value);
    }

    public List<ScoreEntry> getPlayerScoreWithSurrounding(String slug, String playerAlias, int surrounding) {
        Leaderboard leaderboard = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        Sort.Direction bestSort = getSortDirection(leaderboard.getSortOrder());

        ScoreEntry bestEntry = scoreRepository.findTopByLeaderboard_SlugAndPlayerAlias(slug, playerAlias, Sort.by(bestSort, "scoreValue"))
                .orElseThrow(() -> new RuntimeException("Player not found: " + playerAlias));

        if (surrounding <= 0) {
            return Collections.singletonList(bestEntry);
        }

        Pageable limitHigher = PageRequest.of(0, surrounding + 1, Sort.by(Sort.Direction.ASC, "scoreValue"));
        Pageable limitLower = PageRequest.of(0, surrounding, Sort.by(Sort.Direction.DESC, "scoreValue"));

        List<ScoreEntry> higherScores = scoreRepository.findByLeaderboard_SlugAndScoreValueGreaterThanEqual(slug, bestEntry.getScoreValue(), limitHigher).getContent();
        List<ScoreEntry> lowerScores = scoreRepository.findByLeaderboard_SlugAndScoreValueLessThan(slug, bestEntry.getScoreValue(), limitLower).getContent();

        return Stream.concat(higherScores.stream(), lowerScores.stream())
                .sorted((e1, e2) -> (leaderboard.getSortOrder() == SortOrder.ASC)
                        ? Double.compare(e1.getScoreValue(), e2.getScoreValue())
                        : Double.compare(e2.getScoreValue(), e1.getScoreValue()))
                .collect(Collectors.toList());
    }

    private ScoreEntry handleSingleScoreSubmission(Leaderboard leaderboard, String playerName, double value) {
        Optional<ScoreEntry> existingOpt = scoreRepository.findTopByLeaderboard_SlugAndPlayerAlias(
                leaderboard.getSlug(), playerName, Sort.unsorted());

        if (existingOpt.isEmpty()) {
            return createAndSaveScore(leaderboard, playerName, value);
        }

        ScoreEntry existing = existingOpt.get();
        if (isNewScoreBetter(value, existing.getScoreValue(), leaderboard.getSortOrder())) {
            existing.setScoreValue(value);
            existing.setSubmittedAt(LocalDateTime.now());
            return scoreRepository.save(existing);
        }

        return existing;
    }

    private ScoreEntry createAndSaveScore(Leaderboard leaderboard, String playerName, double value) {
        ScoreEntry entry = ScoreEntry.builder()
                .playerAlias(playerName)
                .scoreValue(value)
                .leaderboard(leaderboard)
                .build();
        return scoreRepository.save(entry);
    }

    public List<ScoreEntry> getTopScores(String slug, int limit) {
        Leaderboard leaderboard = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        Sort.Direction direction = getSortDirection(leaderboard.getSortOrder());
        Pageable pageable = PageRequest.of(0, limit, Sort.by(direction, "scoreValue"));

        return scoreRepository.findByLeaderboard_Slug(slug, pageable).getContent();
    }

    private Sort.Direction getSortDirection(SortOrder sortOrder) {
        return (sortOrder == SortOrder.ASC) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private boolean isNewScoreBetter(double newScore, double currentScore, SortOrder sortOrder) {
        return (sortOrder == SortOrder.ASC) ? newScore < currentScore : newScore > currentScore;
    }
}