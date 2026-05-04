package com.spiid.iam.service.infrastructure.config;

import com.spiid.iam.service.application.dto.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class})
public class AppConfig {
    // Centraliza @ConfigurationProperties.
}
