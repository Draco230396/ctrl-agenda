package com.spiid.iam.service.infrastructure.outbound.persistence.adapter;

import com.spiid.iam.service.application.dto.Tenant;
import com.spiid.iam.service.domain.port.out.TenantRepositoryPort;
import com.spiid.iam.service.infrastructure.outbound.persistence.entity.TenantEntity;
import com.spiid.iam.service.infrastructure.outbound.persistence.repository.TenantJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

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
        //entity.setId(tenant.id());
        entity.setName(tenant.name());
        entity.setActive(tenant.active());

        TenantEntity saved = repo.save(entity);
        //Devolver con id generado
        return mapToDomain(saved);
    }

    /**
     * @param tenantId
     * @return
     */
    @Override
    public Optional<Tenant> findById(UUID tenantId) {
        return repo.findById(tenantId)
                .map(this::mapToDomain);
    }

    private Tenant mapToDomain(TenantEntity entity) {

        return new Tenant(
                entity.getId(),
                entity.getName(),
                entity.isActive()
        );
    }
}
