package io.github.brainzy.rankdrop.config;

import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.repository.LeaderboardRepository;
import io.github.brainzy.rankdrop.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(LeaderboardRepository leaderboardRepository,
                                   SystemSettingService systemSettingService) {
        return args -> {
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
        };
    }
}