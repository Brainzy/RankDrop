package io.github.brainzy.rankdrop.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(NativeHints.NativeHintsRegistrar.class)
public class NativeHints {

    static class NativeHintsRegistrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.resources()
                .registerPattern("db/migration/*.sql");
            
            hints.reflection()
                .registerType(org.flywaydb.core.Flyway.class);
        }
    }
}
