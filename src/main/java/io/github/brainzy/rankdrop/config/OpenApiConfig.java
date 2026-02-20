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
                        .description("Built for indie teams who want to own their data and scale without the stress of recurring costs or complex setup."))
                .addSecurityItem(new SecurityRequirement().addList("AdminAuth"))
                .addSecurityItem(new SecurityRequirement().addList("GameAuth"))
                .components(new Components()
                        .addSecuritySchemes("AdminAuth", new SecurityScheme()
                                .name("X-Admin-Token")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Required for all /api/v1/admin/** endpoints"))
                        .addSecuritySchemes("GameAuth", new SecurityScheme()
                                .name("X-Game-Key")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Required for POST /scores endpoints")));
    }
}