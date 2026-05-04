package com.spiid.iam.service.domain.port.out;

import com.spiid.iam.service.application.dto.Tenant;

public interface TenantRepositoryPort {
    Tenant save(Tenant tenant);
}
