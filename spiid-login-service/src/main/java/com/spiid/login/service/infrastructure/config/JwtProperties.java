package com.spiid.login.service.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * Properties para JWT.
 * Se alimenta desde application.yml en:
 * app.security.jwt.*
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long accessTtlSeconds,
        long refreshTtlSeconds
) {}
