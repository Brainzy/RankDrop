package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.dto.ScoreArchiveSummary;
import io.github.brainzy.rankdrop.entity.ScoreArchive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScoreArchiveRepository extends JpaRepository<ScoreArchive, Long> {
    @Query("SELECT new io.github.brainzy.rankdrop.dto.ScoreArchiveSummary(s.leaderboardSlug, s.resetLabel, s.archivedAt, COUNT(s)) " +
            "FROM ScoreArchive s " +
            "GROUP BY s.leaderboardSlug, s.resetLabel, s.archivedAt " +
            "ORDER BY s.archivedAt DESC")
    List<ScoreArchiveSummary> findAllArchiveSummaries();

    List<ScoreArchive> findByLeaderboardSlugAndArchivedAt(String slug, LocalDateTime archivedAt, Pageable pageable);
}