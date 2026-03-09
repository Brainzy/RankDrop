package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {
    @Query("SELECT l FROM Leaderboard l WHERE l.slug = :slug")
    Optional<Leaderboard> findBySlug(@Param("slug") String slug);

    @Query("SELECT l FROM Leaderboard l WHERE l.nextResetAt < :dateTime")
    List<Leaderboard> findAllByNextResetAtBefore(@Param("dateTime") LocalDateTime dateTime);
}