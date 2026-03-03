package io.github.brainzy.rankdrop.config;

import org.flywaydb.core.Flyway;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import javax.sql.DataSource;

@Configuration
@ImportRuntimeHints(FlywayConfig.FlywayHints.class)
public class FlywayConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .schemas("public")
                .defaultSchema("public")
                .createSchemas(true)
                .load();

        return flyway;
    }

    static class FlywayHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection().registerType(
                    org.flywaydb.core.internal.command.clean.CleanModeConfigurationExtension.class,
                    MemberCategory.INVOKE_DECLARED_METHODS
            );
            hints.reflection().registerType(
                    org.flywaydb.core.internal.database.base.BaseDatabaseType.class,
                    MemberCategory.INVOKE_DECLARED_METHODS
            );
            hints.reflection().registerType(Flyway.class, MemberCategory.INVOKE_DECLARED_METHODS);
        }
    }
}