package com.spiid.login.service.infrastructure.outbound.persistence.mapper;

import com.spiid.login.service.application.dto.RoleCatalogItem;
import com.spiid.login.service.application.dto.User;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.CatalogRoleEntity;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.UserAccountEntity;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.UserRoleEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    public static User toDomain(
            UserAccountEntity user,
            Set<UserRoleEntity> roles,
            Set<CatalogRoleEntity> catalogRoles
    ) {

        Set<RoleCatalogItem> domainRoles =
                roles.stream()
                        .map(r -> catalogRoles.stream()
                                .filter(c -> c.getCode() == r.getId().getRoleCode())
                                .findFirst()
                                .orElseThrow())
                        .map(c -> new RoleCatalogItem(
                                c.getCode(),
                                c.getKey(),
                                c.getDescription()
                        ))
                        .collect(Collectors.toSet());

        return new User(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                domainRoles,
                user.getProvider(),
                user.getProviderId()
        );
    }

    public static UserAccountEntity toEntity(User user) {
        UserAccountEntity e = new UserAccountEntity();
        e.setId(user.id());
        e.setTenantId(user.tenantId());
        e.setEmail(user.email());
        e.setPasswordHash(user.passwordHash());
        e.setEnabled(user.enabled());
        e.setCreatedAt(user.createdAt());
        e.setUpdatedAt(user.updatedAt());
        e.setProvider(user.provider());
        e.setProviderId(user.providerId());
        return e;
    }
}
