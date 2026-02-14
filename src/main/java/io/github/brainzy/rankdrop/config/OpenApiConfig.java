package io.github.brainzy.rankdrop.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RankDrop API")
                        .version("1.0.0")
                        .description("High-performance gaming leaderboard API"))
                .addSecurityItem(new SecurityRequirement()
                        .addList("AdminAuth")
                        .addList("GameAuth"))
                .components(new Components()
                        .addSecuritySchemes("AdminAuth", new SecurityScheme()
                                .name("X-Admin-Token")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER))
                        .addSecuritySchemes("GameAuth", new SecurityScheme()
                                .name("X-Game-Key")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)));
    }
}