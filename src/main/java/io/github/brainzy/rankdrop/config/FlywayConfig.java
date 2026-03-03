package io.github.brainzy.rankdrop.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlNoDriversForInteractiveAuthException;
import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlNoIntegratedAuthException;
import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlServerUntrustedCertificateSqlException;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
@ImportRuntimeHints(FlywayConfig.FlywayHints.class)
public class FlywayConfig implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        BeanDefinition entityManagerFactory = beanFactory
                .getBeanDefinition("entityManagerFactory");
        entityManagerFactory.setDependsOn("flyway");
    }

    @Bean
    public Flyway flyway(DataSource dataSource) throws Exception {
        var resource = getClass().getClassLoader()
                .getResourceAsStream("db/migration/V1__initial_schema.sql");

        Path tempDir = Files.createTempDirectory("flyway-migrations");
        Path tempSql = tempDir.resolve("V1__initial_schema.sql");

        if (resource != null) {
            Files.copy(resource, tempSql);
        } else {
            log.info("Could not find resource,");
        }

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("filesystem:" + tempDir)
                .schemas("public")
                .defaultSchema("public")
                .createSchemas(true)
                .load();
        flyway.migrate();
        return flyway;
    }

    static class FlywayHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection().registerType(
                    FlywaySqlServerUntrustedCertificateSqlException.class,
                    MemberCategory.INVOKE_DECLARED_METHODS
            );
            hints.reflection().registerType(
                    FlywaySqlNoIntegratedAuthException.class,
                    MemberCategory.INVOKE_DECLARED_METHODS
            );
            hints.reflection().registerType(
                    FlywaySqlNoDriversForInteractiveAuthException.class,
                    MemberCategory.INVOKE_DECLARED_METHODS
            );
        }
    }
}