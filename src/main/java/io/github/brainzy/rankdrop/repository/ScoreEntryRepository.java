package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.ScoreEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreEntryRepository extends JpaRepository<ScoreEntry, Long> {
    Page<ScoreEntry> findByLeaderboard_Slug(String slug, Pageable pageable);
}