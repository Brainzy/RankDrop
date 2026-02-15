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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreEntryRepository scoreRepository;
    private final LeaderboardRepository leaderboardRepository;

    @Transactional
    public ScoreEntry submitScore(String slug, String playerName, double value) {
        Leaderboard leaderboard = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

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

        Sort.Direction direction = (leaderboard.getSortOrder() == SortOrder.ASC)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(0, limit, Sort.by(direction, "scoreValue"));

        return scoreRepository.findByLeaderboard_Slug(slug, pageable).getContent();
    }
}