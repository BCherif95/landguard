package com.laboussole.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    OpenAPI laBoussoleOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("LA BOUSSOLE — Land Intelligence API")
                        .version("0.1.0")
                        .description("Backend API for the LA BOUSSOLE African land security and surveillance platform.")
                        .license(new License().name("Proprietary").url("https://laboussole.africa")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components().addSecuritySchemes(SCHEME, new SecurityScheme()
                        .name(SCHEME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
