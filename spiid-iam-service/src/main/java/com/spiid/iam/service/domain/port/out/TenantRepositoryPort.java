package com.spiid.iam.service.domain.port.out;

import com.spiid.iam.service.application.dto.Tenant;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepositoryPort {
    Tenant save(Tenant tenant);
    Optional<Tenant> findById(UUID tenantId);
}
