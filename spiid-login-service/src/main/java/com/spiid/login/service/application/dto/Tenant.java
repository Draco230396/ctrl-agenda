package com.spiid.login.service.application.dto;

import java.util.UUID;

public record Tenant(
        UUID id,
        String name,
        boolean active
) {
}
