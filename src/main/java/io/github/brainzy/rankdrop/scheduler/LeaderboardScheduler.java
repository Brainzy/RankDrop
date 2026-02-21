package io.github.brainzy.rankdrop.scheduler;

import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.repository.LeaderboardRepository;
import io.github.brainzy.rankdrop.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaderboardScheduler {

    private final LeaderboardRepository leaderboardRepository;
    private final LeaderboardService leaderboardService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");


    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void processScheduledResets() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<Leaderboard> dueResets = leaderboardRepository.findAllByNextResetAtBefore(now);

        for (Leaderboard lb : dueResets) {
            log.info("Processing automatic reset for leaderboard: {}", lb.getSlug());
            try {
                String label = "Auto-Reset " + lb.getResetFrequency() + " " + now.format(DATE_FORMATTER);
                leaderboardService.performReset(lb, lb.isArchiveOnReset(), label);
            } catch (Exception e) {
                log.error("Failed to reset leaderboard: {}", lb.getSlug(), e);
            }
        }
    }
}