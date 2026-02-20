package io.github.brainzy.rankdrop.service;

import io.github.brainzy.rankdrop.dto.LeaderboardCreateRequest;
import io.github.brainzy.rankdrop.dto.LeaderboardResetRequest;
import io.github.brainzy.rankdrop.dto.ScoreArchiveSummary;
import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.entity.ScoreArchive;
import io.github.brainzy.rankdrop.entity.ScoreEntry;
import io.github.brainzy.rankdrop.exception.LeaderboardAlreadyExistsException;
import io.github.brainzy.rankdrop.exception.LeaderboardNotFoundException;
import io.github.brainzy.rankdrop.repository.LeaderboardRepository;
import io.github.brainzy.rankdrop.repository.ScoreArchiveRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaderboardService {
    private final LeaderboardRepository leaderboardRepository;
    private final ScoreArchiveRepository scoreArchiveRepository;

    public LeaderboardService(LeaderboardRepository leaderboardRepository, ScoreArchiveRepository scoreArchiveRepository) {
        this.leaderboardRepository = leaderboardRepository;
        this.scoreArchiveRepository = scoreArchiveRepository;
    }

    public Leaderboard createNewLeaderboard(LeaderboardCreateRequest request) {
        if (leaderboardRepository.findBySlug(request.slug()).isPresent()) {
            throw new LeaderboardAlreadyExistsException(request.slug());
        }

        Leaderboard lb = Leaderboard.builder()
                .slug(request.slug())
                .displayName(request.displayName())
                .sortOrder(request.sortOrder())
                .allowMultipleScores(request.allowMultipleScores())
                .isCumulative(request.isCumulative())
                .minScore(request.minScore())
                .maxScore(request.maxScore())
                .build();
        return leaderboardRepository.save(lb);
    }

    public Leaderboard updateExistingLeaderboard(String slug, String displayName) {
        Leaderboard board = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        board.setDisplayName(displayName);
        return leaderboardRepository.save(board);
    }

    public void deleteLeaderboardBySlug(String slug) {
        Leaderboard board = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));
        leaderboardRepository.delete(board);
    }

    public List<Leaderboard> getAllLeaderboards() {
        return leaderboardRepository.findAll();
    }

    public void resetLeaderboard(String slug, LeaderboardResetRequest request) {
        Leaderboard board = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        if (request.archiveScores()) {
            if (request.resetLabel() == null || request.resetLabel().isBlank()) {
                throw new IllegalArgumentException("resetLabel is required when archiveScores is true");
            }
            archiveScores(board, request.resetLabel());
        }

        // Clear entries
        board.getEntries().clear();
        leaderboardRepository.save(board);
    }

    public List<ScoreArchiveSummary> getAllArchiveHistory() {
        return scoreArchiveRepository.findAllArchiveSummaries();
    }

    public List<ScoreArchive> getArchivedScoresSnapshot(String slug, LocalDateTime archivedAt, int limit) {
        if (leaderboardRepository.findBySlug(slug).isEmpty()) {
            throw new LeaderboardNotFoundException(slug);
        }
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "scoreValue"));
        return scoreArchiveRepository.findByLeaderboardSlugAndArchivedAt(slug, archivedAt, pageable);
    }

    private void archiveScores(Leaderboard board, String resetLabel) {
        LocalDateTime now = LocalDateTime.now();
        List<ScoreArchive> archives = board.getEntries().stream()
                .map(entry -> mapToArchive(entry, resetLabel, now))
                .collect(Collectors.toList());
        scoreArchiveRepository.saveAll(archives);
    }

    private ScoreArchive mapToArchive(ScoreEntry entry, String resetLabel, LocalDateTime archivedAt) {
        return ScoreArchive.builder()
                .leaderboardSlug(entry.getLeaderboard().getSlug())
                .playerAlias(entry.getPlayerAlias())
                .scoreValue(entry.getScoreValue())
                .submittedAt(entry.getSubmittedAt())
                .archivedAt(archivedAt)
                .resetLabel(resetLabel)
                .build();
    }
}