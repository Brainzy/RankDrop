package io.github.brainzy.rankdrop.config;

import io.github.brainzy.rankdrop.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackupScheduler {

    private final SystemSettingService systemSettingService;

    @Value("${DB_HOST}")
    private String dbHost;

    @Value("${DB_NAME}")
    private String dbName;

    @Value("${DB_USERNAME}")
    private String dbUser;

    @Value("${DB_PASSWORD}")
    private String dbPassword;

    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    public void performBackup() {
        try {
            int retentionDays = Integer.parseInt(
                    systemSettingService.getSetting("BACKUP_RETENTION_DAYS", "3"));

            Path backupDir = Paths.get("/app/backups");
            Files.createDirectories(backupDir);

            String timestamp = LocalDate.now(ZoneOffset.UTC).toString();
            String destination = backupDir
                    .resolve("rankdrop-" + timestamp + ".dump")
                    .toString();

            ProcessBuilder pb = new ProcessBuilder(
                    "/usr/bin/pg_dump",
                    "-h", dbHost,
                    "-p", "5432",
                    "-U", dbUser,
                    "-d", dbName,
                    "--format=custom",
                    "--compress=9",
                    "--clean",
                    "--if-exists",
                    "-f", destination
            );

            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(log::info);
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Backup created: {}", destination);
                deleteOldBackups(backupDir, retentionDays);
            } else {
                log.error("Backup failed with exit code {}", exitCode);
            }

        } catch (Exception e) {
            log.error("Backup failed", e);
        }
    }

    private void deleteOldBackups(Path backupDir, int retentionDays) throws IOException {
        LocalDate cutoff = LocalDate.now(ZoneOffset.UTC).minusDays(retentionDays);

        try (Stream<Path> files = Files.list(backupDir)) {
            files.filter(p -> p.getFileName().toString().startsWith("rankdrop-"))
                    .filter(p -> p.getFileName().toString().endsWith(".dump"))
                    .filter(p -> {
                        String dateStr = p.getFileName().toString()
                                .replace("rankdrop-", "")
                                .replace(".dump", "");
                        try {
                            return LocalDate.parse(dateStr).isBefore(cutoff);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                            log.info("Deleted old backup: {}", p);
                        } catch (IOException e) {
                            log.warn("Failed to delete {}", p);
                        }
                    });
        }
    }
}