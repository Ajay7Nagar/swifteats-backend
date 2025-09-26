package com.swifteats.config;

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
    public OpenAPI api() {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityScheme basic = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic");

        Components components = new Components()
                .addSecuritySchemes("bearer-jwt", bearer)
                .addSecuritySchemes("basic", basic);

        SecurityRequirement securityItem = new SecurityRequirement()
                .addList("bearer-jwt");

        return new OpenAPI()
                .components(components)
                .addSecurityItem(securityItem)
                .info(new Info()
                        .title("SwiftEats API")
                        .version("v1")
                        .description("Browse restaurants, manage orders, driver tracking, and auth"));
    }
}

