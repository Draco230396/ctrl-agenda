package com.spiid.login.service.infrastructure.outbound.persistence.adapter;

import com.spiid.login.service.application.dto.Tenant;
import com.spiid.login.service.domain.port.out.TenantRepositoryPort;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.TenantEntity;
import com.spiid.login.service.infrastructure.outbound.persistence.repository.TenantJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class TenantRepositoryAdapter implements TenantRepositoryPort {

    private final TenantJpaRepository repo;

    public TenantRepositoryAdapter(TenantJpaRepository repo) {
        this.repo = repo;
    }


    /**
     * @param tenant
     * @return
     */
    @Override
    public Tenant save(Tenant tenant) {

        TenantEntity entity = new TenantEntity();
        entity.setId(tenant.id());
        entity.setName(tenant.name());
        entity.setActive(tenant.active());

        repo.save(entity);

        return tenant;
    }
}
