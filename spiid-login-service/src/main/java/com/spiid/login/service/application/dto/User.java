package com.spiid.login.service.application.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Modelo de dominio (NO JPA) para un usuario
 * La persistencia se hace en un adapter (outbound) para no romper la arquitectura
 * hexagonal
 * */
public record User(
        UUID id,
        UUID tenantId,
        String email,
        String passwordHash,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt,
        Set<RoleCatalogItem> roles,
        String provider,
        String providerId
) {}
