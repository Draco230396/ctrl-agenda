package com.spiid.login.service.domain.port.out;

import com.spiid.login.service.domain.model.RoleCatalogItem;

import java.util.List;
import java.util.Optional;

public interface RoleCatalogRepositoryPort {

    Optional<RoleCatalogItem> findByCode(short code);
    List<RoleCatalogItem> findAllActive();

    Optional<RoleCatalogItem> findByKey(String key);

}
