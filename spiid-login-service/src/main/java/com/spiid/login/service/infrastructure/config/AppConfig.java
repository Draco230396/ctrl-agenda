package com.spiid.login.service.infrastructure.config;

import com.spiid.login.service.application.dto.JwtProperties;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class})
public class AppConfig {
    // Centraliza @ConfigurationProperties.
}
