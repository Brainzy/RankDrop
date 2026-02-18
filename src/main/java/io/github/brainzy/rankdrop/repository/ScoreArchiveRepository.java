package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.ScoreArchive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreArchiveRepository extends JpaRepository<ScoreArchive, Long> {
    List<ScoreArchive> findByLeaderboardSlug(String slug, Pageable pageable);
}