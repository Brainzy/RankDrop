package io.github.brainzy.rankdrop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Order(2)
public class DataInitializerDisabled implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("DataInitializer disabled - skipping database initialization");
    }

    /*
    private final LeaderboardRepository leaderboardRepository;
    private final SystemSettingService systemSettingService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Finding by slug in DataInitializer");
        if (leaderboardRepository.findBySlug("global-high-scores").isEmpty()) {
            Leaderboard lb = Leaderboard.builder()
                    .slug("global-high-scores")
                    .displayName("🏆 Global High Scores")
                    .build();
            leaderboardRepository.save(lb);
            log.info("Database seeded: Global Leaderboard created");
        }

        String gameKey = systemSettingService.getSetting("GAME_SECRET");
        if (gameKey != null && gameKey.length() < 16) {
            log.warn("GAME_SECRET is set but too short, consider rotating it");
        }

        String webhookUrl = systemSettingService.getSetting("WEBHOOK_URL");
        if (webhookUrl != null && !webhookUrl.startsWith("http")) {
            log.warn("WEBHOOK_URL looks invalid: {}", webhookUrl);
        }

        if (systemSettingService.getSetting("BACKUP_RETENTION_DAYS") == null) {
            systemSettingService.setSetting("BACKUP_RETENTION_DAYS", "3");
            log.info("Seeded default backup retention days: 3");
        }

        if (systemSettingService.getSetting("BACKUP_PATH") == null) {
            systemSettingService.setSetting("BACKUP_PATH", "./backups");
            log.info("Seeded default backup path: ./backups");
        }

        log.info("RankDrop started successfully");
    }
     */
}
