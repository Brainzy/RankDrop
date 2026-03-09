package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.entity.ScoreEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ScoreEntryRepository extends JpaRepository<ScoreEntry, Long> {
    @Modifying
    @Query("DELETE FROM ScoreEntry s WHERE s.leaderboard = :leaderboard")
    void deleteByLeaderboard(@Param("leaderboard") Leaderboard leaderboard);

    @Query("SELECT s FROM ScoreEntry s WHERE s.leaderboard.slug = :slug")
    Page<ScoreEntry> findByLeaderboardSlug(@Param("slug") String slug, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE score_entries SET score_value = score_value + :value, submitted_at = :now WHERE leaderboard_id = :leaderboardId AND player_alias = :playerAlias", nativeQuery = true)
    int incrementScore(@Param("leaderboardId") Long leaderboardId, @Param("playerAlias") String playerAlias, @Param("value") double value, @Param("now") LocalDateTime now);

    @Modifying
    @Query(value = "UPDATE score_entries SET score_value = :value, submitted_at = :now WHERE leaderboard_id = :leaderboardId AND player_alias = :playerAlias AND score_value < :value", nativeQuery = true)
    int updateIfHigherScore(@Param("leaderboardId") Long leaderboardId, @Param("playerAlias") String playerAlias, @Param("value") double value, @Param("now") LocalDateTime now);

    @Modifying
    @Query(value = "UPDATE score_entries SET score_value = :value, submitted_at = :now WHERE leaderboard_id = :leaderboardId AND player_alias = :playerAlias AND score_value > :value", nativeQuery = true)
    int updateIfLowerScore(@Param("leaderboardId") Long leaderboardId, @Param("playerAlias") String playerAlias, @Param("value") double value, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM ScoreEntry s WHERE s.leaderboard.id = :leaderboardId AND s.playerAlias = :playerAlias")
    Optional<ScoreEntry> findByLeaderboardIdAndPlayerAlias(
            @Param("leaderboardId") Long leaderboardId,
            @Param("playerAlias") String playerAlias);

    @Query(value = "SELECT COUNT(*) FROM score_entries WHERE leaderboard_id = :leaderboardId AND (score_value > :score OR (score_value = :score AND submitted_at < :submittedAt))", nativeQuery = true)
    long countBetterScoresDesc(@Param("leaderboardId") Long leaderboardId, @Param("score") double score, @Param("submittedAt") LocalDateTime submittedAt);

    @Query(value = "SELECT COUNT(*) FROM score_entries WHERE leaderboard_id = :leaderboardId AND (score_value < :score OR (score_value = :score AND submitted_at < :submittedAt))", nativeQuery = true)
    long countBetterScoresAsc(@Param("leaderboardId") Long leaderboardId, @Param("score") double score, @Param("submittedAt") LocalDateTime submittedAt);

    @Query(value = "SELECT * FROM score_entries WHERE leaderboard_id = :leaderboardId AND (score_value > :score OR (score_value = :score AND submitted_at < :submittedAt)) ORDER BY score_value ASC, submitted_at DESC", nativeQuery = true)
    Slice<ScoreEntry> findHigherScores(@Param("leaderboardId") Long leaderboardId, @Param("score") double score, @Param("submittedAt") LocalDateTime submittedAt, Pageable pageable);

    @Query(value = "SELECT * FROM score_entries WHERE leaderboard_id = :leaderboardId AND (score_value < :score OR (score_value = :score AND submitted_at > :submittedAt)) ORDER BY score_value DESC, submitted_at ASC", nativeQuery = true)
    Slice<ScoreEntry> findLowerScores(@Param("leaderboardId") Long leaderboardId, @Param("score") double score, @Param("submittedAt") LocalDateTime submittedAt, Pageable pageable);
}
