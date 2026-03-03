package io.github.brainzy.rankdrop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Slf4j
public class DatabaseLogger implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired(required = false)
    private DataSource dataSource;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("=== DATABASE CONNECTION TEST ===");
        if (dataSource != null) {
            log.info("DataSource bean created successfully: {}", dataSource.getClass().getSimpleName());
            try {
                log.info("Database URL: {}", dataSource.getConnection().getMetaData().getURL());
                log.info("Database connection test: SUCCESS");
            } catch (Exception e) {
                log.error("Database connection test: FAILED", e);
            }
        } else {
            log.warn("DataSource bean NOT found");
        }
    }
}
