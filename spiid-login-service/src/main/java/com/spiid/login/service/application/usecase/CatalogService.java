package com.spiid.login.service.application.usecase;

import com.spiid.login.service.domain.model.RoleCatalogItem;
import com.spiid.login.service.domain.port.in.CatalogUseCase;
import com.spiid.login.service.domain.port.out.RoleCatalogRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService implements CatalogUseCase {

    private final RoleCatalogRepositoryPort roles;

    public CatalogService(RoleCatalogRepositoryPort roles) {
        this.roles = roles;
    }

    @Override
    public List<RoleCatalogItem> listRoles() {
        return roles.findAllActive();
    }
}
