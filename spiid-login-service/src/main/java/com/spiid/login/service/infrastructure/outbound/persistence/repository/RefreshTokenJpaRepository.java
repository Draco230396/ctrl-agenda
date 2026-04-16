package com.spiid.login.service.infrastructure.outbound.persistence.repository;

import com.spiid.login.service.infrastructure.outbound.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {
  Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
}
