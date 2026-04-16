package com.spiid.login.service.infrastructure.outbound.persistence.repository;

import com.spiid.login.service.infrastructure.outbound.persistence.entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, UUID> {
  Optional<UserAccountEntity> findByEmail(String email);
}
