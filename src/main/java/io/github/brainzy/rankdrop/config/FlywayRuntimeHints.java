package io.github.brainzy.rankdrop.config;

import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlNoDriversForInteractiveAuthException;
import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlNoIntegratedAuthException;
import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlServerUntrustedCertificateSqlException;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(FlywayRuntimeHints.FlywayHints.class)
public class FlywayRuntimeHints {

    static class FlywayHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Add all Flyway SQL exception classes
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