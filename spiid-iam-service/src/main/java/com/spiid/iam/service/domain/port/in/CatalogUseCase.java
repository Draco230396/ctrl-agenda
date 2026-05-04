package com.spiid.iam.service.domain.port.in;

import com.spiid.iam.service.application.dto.RoleCatalogItem;

import java.util.List;

public interface CatalogUseCase {

    List<RoleCatalogItem> listRoles();

    RoleCatalogItem getRoleByKey(String key);


}
