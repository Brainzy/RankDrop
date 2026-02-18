package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.ScoreArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreArchiveRepository extends JpaRepository<ScoreArchive, Long> {
}