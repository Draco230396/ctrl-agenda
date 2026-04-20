package com.spiid.login.service.infrastructure.outbound.persistence.repository;

import com.spiid.login.service.domain.model.RoleCatalogItem;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.CatalogRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatalogRoleJpaRepository extends JpaRepository<CatalogRoleEntity, Short> {
  List<CatalogRoleEntity> findAllByActiveTrueOrderByCodeAsc();

  Optional<CatalogRoleEntity> findByKey(String key);
}
