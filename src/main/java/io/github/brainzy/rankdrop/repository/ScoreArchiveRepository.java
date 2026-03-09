package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.dto.ScoreArchiveSummary;
import io.github.brainzy.rankdrop.entity.ScoreArchive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreArchiveRepository extends JpaRepository<ScoreArchive, Long> {
    @Query("SELECT new io.github.brainzy.rankdrop.dto.ScoreArchiveSummary(s.leaderboardSlug, s.resetLabel, s.archivedAt, COUNT(s)) " +
            "FROM ScoreArchive s " +
            "GROUP BY s.leaderboardSlug, s.resetLabel, s.archivedAt " +
            "ORDER BY s.archivedAt DESC")
    List<ScoreArchiveSummary> findAllArchiveSummaries();

    @Query("SELECT s FROM ScoreArchive s WHERE s.leaderboardSlug = :slug AND s.resetLabel = :resetLabel")
    List<ScoreArchive> findByLeaderboardSlugAndResetLabel(
            @Param("slug") String slug,
            @Param("resetLabel") String resetLabel,
            Pageable pageable);
}