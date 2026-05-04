package com.spiid.iam.service.application.dto;

import java.util.UUID;

public record Tenant(
        UUID id,
        String name,
        boolean active
) {
}
