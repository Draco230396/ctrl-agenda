package com.spiid.login.service.application.dto;

import java.util.List;
import java.util.UUID;
/**
 * Vista de usuario expuesta al cliente.
 *
 * No debe contener información sensible
 * como contraseñas, hashes, etc.
 */
public record UserViewDto(
        UUID id,
        String email,
        boolean enabled,
        List<RoleCatalogItem> roles
) {}