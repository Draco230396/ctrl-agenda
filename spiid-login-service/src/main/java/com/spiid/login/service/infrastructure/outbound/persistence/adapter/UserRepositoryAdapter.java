package com.spiid.login.service.infrastructure.outbound.persistence.adapter;

import com.spiid.login.service.infrastructure.outbound.persistence.entity.CatalogRoleEntity;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.UserAccountEntity;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.UserRoleEntity;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.UserRoleId;
import com.spiid.login.service.infrastructure.outbound.persistence.repository.CatalogRoleJpaRepository;
import com.spiid.login.service.infrastructure.outbound.persistence.repository.UserAccountJpaRepository;
import com.spiid.login.service.infrastructure.outbound.persistence.repository.UserRoleJpaRepository;
import com.spiid.login.service.application.dto.RoleCatalogItem;
import com.spiid.login.service.application.dto.User;
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
    return users.findByEmail(email)
            .map(this::mapToDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findById(UUID id) {
    return users.findById(id)
            .map(this::mapToDomain);
  }

  @Override
  @Transactional
  public User save(User user) {

    UserAccountEntity entity = users.findById(user.id())
            .orElseGet(UserAccountEntity::new);

    mapToEntity(entity, user);

    users.save(entity);

    saveRoles(entity.getId(), user.roles());

    return user;
  }


  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByProviderId(String providerId) {
    return users.findByProviderId(providerId)
            .map(this::mapToDomain);
  }
  private void mapToEntity(UserAccountEntity entity, User user) {

    Instant now = Instant.now();

    entity.setId(user.id());
    entity.setTenantId(user.tenantId());
    entity.setEmail(user.email());
    entity.setPasswordHash(user.passwordHash());
    entity.setEnabled(user.enabled());
    entity.setProvider(user.provider());
    entity.setProviderId(user.providerId());

    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(now);
    }

    entity.setUpdatedAt(now);
  }
  private void saveRoles(UUID userId, Set<RoleCatalogItem> roles) {

    userRoles.deleteAllByUserId(userId);

    if (roles == null || roles.isEmpty()) return;

    Instant now = Instant.now();

    for (RoleCatalogItem role : roles) {

      short code = role.code();

      catalogRoles.findById(code)
              .orElseThrow(() ->
                      new IllegalArgumentException("Role code inválido: " + code)
              );

      userRoles.save(
              new UserRoleEntity(
                      new UserRoleId(userId, code),
                      now
              )
      );
    }
  }
  private User mapToDomain(UserAccountEntity entity) {

    List<Short> codes = userRoles.findRoleCodesByUserId(entity.getId());

    if (codes.isEmpty()) {
      return buildUser(entity, Collections.emptySet());
    }

    List<CatalogRoleEntity> catalog = catalogRoles.findAllById(codes);

    Set<RoleCatalogItem> roles = new LinkedHashSet<>();

    for (CatalogRoleEntity cr : catalog) {
      roles.add(new RoleCatalogItem(
              cr.getCode(),
              cr.getKey(),
              cr.getDescription()
      ));
    }

    return buildUser(entity, roles);
  }
  private User buildUser(UserAccountEntity e, Set<RoleCatalogItem> roles) {

    return new User(
            e.getId(),
            e.getTenantId(),
            e.getEmail(),
            e.getPasswordHash(),
            e.isEnabled(),
            e.getCreatedAt(),
            e.getUpdatedAt(),
            roles,
            e.getProvider(),
            e.getProviderId()
    );
  }
}
