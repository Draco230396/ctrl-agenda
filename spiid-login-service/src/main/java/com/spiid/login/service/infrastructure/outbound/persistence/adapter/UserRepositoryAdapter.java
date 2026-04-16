package com.spiid.login.service.infrastructure.outbound.persistence.adapter;

import com.spiid.login.service.infrastructure.outbound.persistence.entity.UserAccountEntity;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.UserRoleEntity;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.UserRoleId;
import com.spiid.login.service.infrastructure.outbound.persistence.repository.CatalogRoleJpaRepository;
import com.spiid.login.service.infrastructure.outbound.persistence.repository.UserAccountJpaRepository;
import com.spiid.login.service.infrastructure.outbound.persistence.repository.UserRoleJpaRepository;
import com.spiid.login.service.domain.model.RoleCatalogItem;
import com.spiid.login.service.domain.model.User;
import com.spiid.login.service.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Adapter JPA -> Puerto de dominio.
 * Aquí es donde JPA vive (infraestructura).
 */
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

  private final UserAccountJpaRepository users;
  private final UserRoleJpaRepository userRoles;
  private final CatalogRoleJpaRepository catalogRoles;

  public UserRepositoryAdapter(UserAccountJpaRepository users, UserRoleJpaRepository userRoles, CatalogRoleJpaRepository catalogRoles) {
    this.users = users;
    this.userRoles = userRoles;
    this.catalogRoles = catalogRoles;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    return users.findByEmail(email).map(this::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findById(UUID id) {
    return users.findById(id).map(this::toDomain);
  }

  @Override
  @Transactional
  public User save(User user) {
    UserAccountEntity e = new UserAccountEntity();
    e.setTenantId(user.tenantId());
    e.setEmail(user.email());
    e.setPasswordHash(user.passwordHash());
    e.setEnabled(user.enabled());
    e.setCreatedAt(user.createdAt() == null ? Instant.now() : user.createdAt());
    e.setUpdatedAt(Instant.now());

    users.save(e);

    // Sin FK cross-service, pero aquí sí tenemos FK al catálogo (role_code).
    // Re-escribimos roles para dejar el estado consistente.
    userRoles.deleteAllByUserId(e.getTenantId());

    if (user.roles() != null) {
      Instant now = Instant.now();
      for (RoleCatalogItem r : user.roles()) {
        short code = r.code();
        // Validamos que exista en catálogo (si no existe, fallará también por FK en DB)
        catalogRoles.findById(code).orElseThrow(() -> new IllegalArgumentException("Role code inválido: " + code));
        userRoles.save(new UserRoleEntity(new UserRoleId(e.getTenantId(), code), now));
      }
    }

    return toDomain(e);
  }

  private User toDomain(UserAccountEntity e) {
    List<Short> codes = userRoles.findRoleCodesByUserId(e.getTenantId());
    Set<RoleCatalogItem> roles = new LinkedHashSet<>();
    for (Short c : codes) {
      if (c == null) continue;
      catalogRoles.findById(c).ifPresent(cr ->
          roles.add(new RoleCatalogItem(cr.getCode(), cr.getKey(), cr.getDescription()))
      );
    }

    return new User(
        e.getTenantId(),
        e.getEmail(),
        e.getPasswordHash(),
        e.isEnabled(),
        e.getCreatedAt(),
        e.getUpdatedAt(),
        roles
    );
  }
}
