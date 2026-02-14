package io.github.brainzy.rankdrop.config;

import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.repository.LeaderboardRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(LeaderboardRepository repository) {
        return args -> {
            if (repository.findBySlug("global-high-scores").isEmpty()) {
                Leaderboard lb = Leaderboard.builder()
                        .slug("global-high-scores")
                        .displayName("🏆 Global High Scores")
                        .build();

                repository.save(lb);
                System.out.println("✅ Database Seeded: Global Leaderboard Created!");
            }
        };
    }
}