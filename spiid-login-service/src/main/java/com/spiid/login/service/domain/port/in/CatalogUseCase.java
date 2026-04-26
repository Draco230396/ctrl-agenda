package com.spiid.login.service.domain.port.in;

import com.spiid.login.service.application.dto.RoleCatalogItem;

import java.util.List;

public interface CatalogUseCase {

    List<RoleCatalogItem> listRoles();

    RoleCatalogItem getRoleByKey(String key);


}
