package io.github.brainzy.rankdrop.config;

import io.github.brainzy.rankdrop.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackupScheduler {

    private final JdbcTemplate jdbcTemplate;
    private final SystemSettingService systemSettingService;

    @Scheduled(cron = "0 0 2 * * *")
    public void performBackup() {
        try {
            int retentionDays = Integer.parseInt(
                    systemSettingService.getSetting("BACKUP_RETENTION_DAYS", "3"));
            String backupPath = systemSettingService.getSetting("BACKUP_PATH", "./backups");

            Path backupDir = Paths.get(backupPath);
            Files.createDirectories(backupDir);

            String timestamp = LocalDate.now().toString();
            String destination = backupDir.resolve("rankdrop-" + timestamp + ".mv.db").toString();

            jdbcTemplate.execute("BACKUP TO '" + destination + "'");
            log.info("Backup created: {}", destination);

            deleteOldBackups(backupDir, retentionDays);
        } catch (Exception e) {
            log.error("Backup failed: {}", e.getMessage());
        }
    }

    private void deleteOldBackups(Path backupDir, int retentionDays) throws IOException {
        LocalDate cutoff = LocalDate.now().minusDays(retentionDays);

        try (Stream<Path> files = Files.list(backupDir)) {
            files.filter(p -> p.getFileName().toString().startsWith("rankdrop-"))
                    .filter(p -> {
                        String fileName = p.getFileName().toString();
                        String dateStr = fileName
                                .replace("rankdrop-", "")
                                .replace(".mv.db", "");
                        try {
                            return LocalDate.parse(dateStr).isBefore(cutoff);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                            log.info("Deleted old backup: {}", p);
                        } catch (IOException e) {
                            log.warn("Failed to delete old backup: {}", p);
                        }
                    });
        }
    }
}
