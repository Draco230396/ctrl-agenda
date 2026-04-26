package com.spiid.login.service.infrastructure.outbound.persistence.adapter;

import com.spiid.login.service.application.dto.RoleCatalogItem;
import com.spiid.login.service.domain.port.out.RoleCatalogRepositoryPort;
import com.spiid.login.service.infrastructure.outbound.persistence.repository.CatalogRoleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RoleCatalogRepositoryAdapter implements RoleCatalogRepositoryPort {

  private final CatalogRoleJpaRepository repo;

  public RoleCatalogRepositoryAdapter(CatalogRoleJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public Optional<RoleCatalogItem> findByCode(short code) {
    return repo.findById(code).map(e -> new RoleCatalogItem(e.getCode(), e.getKey(), e.getDescription()));
  }

  @Override
  public List<RoleCatalogItem> findAllActive() {
    return repo.findAllByActiveTrueOrderByCodeAsc()
        .stream()
        .map(e -> new RoleCatalogItem(e.getCode(), e.getKey(), e.getDescription()))
        .toList();
  }

  @Override
  public Optional<RoleCatalogItem> findByKey(String key) {

    String normalizedKey = key.trim().toUpperCase();

    return repo.findByKey(normalizedKey)
            .map(e -> new RoleCatalogItem(
                    e.getCode(),
                    e.getKey(),
                    e.getDescription()
            ));
  }
}
