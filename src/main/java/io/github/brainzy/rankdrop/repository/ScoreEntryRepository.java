package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.ScoreEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScoreEntryRepository extends JpaRepository<ScoreEntry, Long> {
    Page<ScoreEntry> findByLeaderboard_Slug(String slug, Pageable pageable);

    Optional<ScoreEntry> findTopByLeaderboard_SlugAndPlayerAlias(String slug, String playerAlias, Sort sort);

    Slice<ScoreEntry> findByLeaderboard_SlugAndScoreValueGreaterThanEqual(String slug, double scoreValue, Pageable pageable);

    Slice<ScoreEntry> findByLeaderboard_SlugAndScoreValueLessThan(String slug, double scoreValue, Pageable pageable);
}