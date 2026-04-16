package com.spiid.login.service.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class})
public class AppConfig {
    // Centraliza @ConfigurationProperties.
}
