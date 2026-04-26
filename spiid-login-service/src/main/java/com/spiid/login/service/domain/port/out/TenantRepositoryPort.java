package com.spiid.login.service.domain.port.out;

import com.spiid.login.service.application.dto.Tenant;

public interface TenantRepositoryPort {
    Tenant save(Tenant tenant);
}
