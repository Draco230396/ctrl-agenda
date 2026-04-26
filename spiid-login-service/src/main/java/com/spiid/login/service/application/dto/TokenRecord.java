package com.spiid.login.service.application.dto;

import java.time.Instant;
import java.util.UUID;

public record TokenRecord(UUID userId,
                          Instant expiresAt,
                          Instant revokedAt){}