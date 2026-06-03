package com.laboussole.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "laboussole.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
