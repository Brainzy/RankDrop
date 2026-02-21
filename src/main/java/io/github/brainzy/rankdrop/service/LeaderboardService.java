package io.github.brainzy.rankdrop.service;

import io.github.brainzy.rankdrop.dto.LeaderboardCreateRequest;
import io.github.brainzy.rankdrop.dto.LeaderboardResetRequest;
import io.github.brainzy.rankdrop.dto.ScoreArchiveSummary;
import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.entity.ResetFrequency;
import io.github.brainzy.rankdrop.entity.ScoreArchive;
import io.github.brainzy.rankdrop.entity.ScoreEntry;
import io.github.brainzy.rankdrop.exception.LeaderboardAlreadyExistsException;
import io.github.brainzy.rankdrop.exception.LeaderboardNotFoundException;
import io.github.brainzy.rankdrop.repository.LeaderboardRepository;
import io.github.brainzy.rankdrop.repository.ScoreArchiveRepository;
import io.github.brainzy.rankdrop.repository.ScoreEntryRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaderboardService {
    private final LeaderboardRepository leaderboardRepository;
    private final ScoreArchiveRepository scoreArchiveRepository;
    private final ScoreEntryRepository scoreEntryRepository;

    public LeaderboardService(LeaderboardRepository leaderboardRepository, ScoreArchiveRepository scoreArchiveRepository, ScoreEntryRepository scoreEntryRepository) {
        this.leaderboardRepository = leaderboardRepository;
        this.scoreArchiveRepository = scoreArchiveRepository;
        this.scoreEntryRepository = scoreEntryRepository;
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
                .cumulative(request.isCumulative())
                .minScore(request.minScore())
                .maxScore(request.maxScore())
                .resetFrequency(request.resetFrequency())
                .archiveOnReset(request.archiveOnReset())
                .build();

        calculateNextReset(lb);

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
        scoreEntryRepository.deleteByLeaderboard(board);
        leaderboardRepository.delete(board);
    }

    public List<Leaderboard> getAllLeaderboards() {
        return leaderboardRepository.findAll();
    }

    public void resetLeaderboard(String slug, LeaderboardResetRequest request) {
        Leaderboard board = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        performReset(board, request.archiveScores(), request.resetLabel());
    }

    public void performReset(Leaderboard board, boolean archive, String resetLabel) {
        if (archive) {
            if (resetLabel == null || resetLabel.isBlank()) {
                resetLabel = "Auto-Reset " + LocalDateTime.now();
            }
            archiveScores(board, resetLabel);
        }

        List<ScoreEntry> entries = scoreEntryRepository.findByLeaderboard_Slug(board.getSlug(), Pageable.unpaged()).getContent();
        if (!entries.isEmpty()) {
            scoreEntryRepository.deleteAllInBatch(entries);
        }

        if (board.getResetFrequency() != ResetFrequency.NONE) {
            calculateNextReset(board);
        }

        leaderboardRepository.save(board);
    }

    public List<ScoreArchiveSummary> getAllArchives() {
        return scoreArchiveRepository.findAllArchiveSummaries();
    }

    public List<ScoreArchive> getArchivedScoresByLabel(String slug, String resetLabel, int limit) {
        if (leaderboardRepository.findBySlug(slug).isEmpty()) {
            throw new LeaderboardNotFoundException(slug);
        }
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "scoreValue"));
        return scoreArchiveRepository.findByLeaderboardSlugAndResetLabel(slug, resetLabel, pageable);
    }

    private void archiveScores(Leaderboard board, String resetLabel) {
        LocalDateTime now = LocalDateTime.now();
        List<ScoreEntry> entries = scoreEntryRepository.findByLeaderboard_Slug(board.getSlug(), Pageable.unpaged()).getContent();

        List<ScoreArchive> archives = entries.stream()
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
                .metadata(entry.getMetadata())
                .build();
    }

    private void calculateNextReset(Leaderboard lb) {
        if (lb.getResetFrequency() == ResetFrequency.NONE) {
            lb.setNextResetAt(null);
            return;
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        LocalDateTime next = switch (lb.getResetFrequency()) {
            case DAILY -> atMidnight(now.plusDays(1));
            case WEEKLY -> atMidnight(now.with(TemporalAdjusters.next(DayOfWeek.MONDAY)));
            case MONTHLY -> atMidnight(now.with(TemporalAdjusters.firstDayOfNextMonth()));
            default -> null;
        };
        lb.setNextResetAt(next);
    }

    private static LocalDateTime atMidnight(LocalDateTime dt) {
        return dt.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
}