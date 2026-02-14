package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.entity.ScoreEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreEntryRepository extends JpaRepository<ScoreEntry, Long> {
    List<ScoreEntry> findTop10ByLeaderboardOrderByScoreValueDesc(Leaderboard leaderboard);
}