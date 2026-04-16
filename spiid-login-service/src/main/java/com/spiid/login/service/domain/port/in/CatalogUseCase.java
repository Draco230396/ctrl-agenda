package com.spiid.login.service.domain.port.in;

import com.spiid.login.service.domain.model.RoleCatalogItem;

import java.util.List;

public interface CatalogUseCase {

    List<RoleCatalogItem> listRoles();
}
