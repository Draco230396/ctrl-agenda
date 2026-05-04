package com.spiid.iam.service.infrastructure.outbound.persistence.adapter;

import com.spiid.iam.service.application.dto.Tenant;
import com.spiid.iam.service.domain.port.out.TenantRepositoryPort;
import com.spiid.iam.service.infrastructure.outbound.persistence.entity.TenantEntity;
import com.spiid.iam.service.infrastructure.outbound.persistence.repository.TenantJpaRepository;
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
