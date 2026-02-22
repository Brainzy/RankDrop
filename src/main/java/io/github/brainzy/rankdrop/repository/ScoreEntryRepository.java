package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.entity.ScoreEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ScoreEntryRepository extends JpaRepository<ScoreEntry, Long> {
    void deleteByLeaderboard(Leaderboard leaderboard);

    Page<ScoreEntry> findByLeaderboard_Slug(String slug, Pageable pageable);

    @Modifying
    @Query("UPDATE ScoreEntry s SET s.scoreValue = s.scoreValue + :value, s.submittedAt = :now WHERE s.leaderboard.slug = :slug AND s.playerAlias = :playerAlias")
    int incrementScore(@Param("slug") String slug, @Param("playerAlias") String playerAlias, @Param("value") double value, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE ScoreEntry s SET s.scoreValue = :value, s.submittedAt = :now " +
            "WHERE s.leaderboard.slug = :slug AND s.playerAlias = :playerAlias AND s.scoreValue < :value")
    int updateIfHigherScore(@Param("slug") String slug, @Param("playerAlias") String playerAlias, @Param("value") double value, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE ScoreEntry s SET s.scoreValue = :value, s.submittedAt = :now " +
            "WHERE s.leaderboard.slug = :slug AND s.playerAlias = :playerAlias AND s.scoreValue > :value")
    int updateIfLowerScore(@Param("slug") String slug, @Param("playerAlias") String playerAlias, @Param("value") double value, @Param("now") LocalDateTime now);

    Optional<ScoreEntry> findTopByLeaderboard_SlugAndPlayerAlias(String slug, String playerAlias, Sort sort);

    @Query("SELECT COUNT(s) FROM ScoreEntry s WHERE s.leaderboard.slug = :slug AND (s.scoreValue > :score OR (s.scoreValue = :score AND s.submittedAt < :submittedAt))")
    long countBetterScoresDesc(@Param("slug") String slug, @Param("score") double score, @Param("submittedAt") LocalDateTime submittedAt);

    @Query("SELECT COUNT(s) FROM ScoreEntry s WHERE s.leaderboard.slug = :slug AND (s.scoreValue < :score OR (s.scoreValue = :score AND s.submittedAt < :submittedAt))")
    long countBetterScoresAsc(@Param("slug") String slug, @Param("score") double score, @Param("submittedAt") LocalDateTime submittedAt);

    @Query("SELECT s FROM ScoreEntry s WHERE s.leaderboard.slug = :slug AND (s.scoreValue > :score OR (s.scoreValue = :score AND s.submittedAt < :submittedAt)) ORDER BY s.scoreValue ASC, s.submittedAt DESC")
    Slice<ScoreEntry> findHigherScores(@Param("slug") String slug, @Param("score") double score, @Param("submittedAt") LocalDateTime submittedAt, Pageable pageable);

    @Query("SELECT s FROM ScoreEntry s WHERE s.leaderboard.slug = :slug AND (s.scoreValue < :score OR (s.scoreValue = :score AND s.submittedAt > :submittedAt)) ORDER BY s.scoreValue DESC, s.submittedAt ASC")
    Slice<ScoreEntry> findLowerScores(@Param("slug") String slug, @Param("score") double score, @Param("submittedAt") LocalDateTime submittedAt, Pageable pageable);
}