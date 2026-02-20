package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {
    Optional<Leaderboard> findBySlug(String slug);
    
    List<Leaderboard> findAllByNextResetAtBefore(LocalDateTime dateTime);
}