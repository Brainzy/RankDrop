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
    public Flyway flyway(DataSource dataSource) {
        var resource = getClass().getClassLoader()
                .getResource("db/migration/V1__initial_schema.sql");
        log.info("Migration file found: {}", resource);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:/db/migration")
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