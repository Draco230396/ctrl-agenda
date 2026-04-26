package com.spiid.login.service.infrastructure.outbound.persistence.adapter;

import com.spiid.login.service.application.dto.TokenRecord;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.RefreshTokenEntity;
import com.spiid.login.service.domain.port.out.RefreshTokenStorePort;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.UserAccountEntity;
import com.spiid.login.service.infrastructure.outbound.persistence.repository.RefreshTokenJpaRepository;
import com.spiid.login.service.infrastructure.outbound.persistence.repository.UserAccountJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenStoreAdapter implements RefreshTokenStorePort {

  private final RefreshTokenJpaRepository repo;

  private final UserAccountJpaRepository userAccountJpaRepository;
  public RefreshTokenStoreAdapter(RefreshTokenJpaRepository repo, UserAccountJpaRepository userAccountJpaRepository) {
    this.repo = repo;
    this.userAccountJpaRepository = userAccountJpaRepository;
  }

  @Override
  @Transactional
  public void store(UUID userId, UUID tenantId, String refreshTokenHash,  Instant expiresAt, String userAgent, String ipAddress) {
    //1. Obtener referencia del usuario (NO hace query real)
    UserAccountEntity user = userAccountJpaRepository.getReferenceById(userId);

    //2. Crear entidad
    RefreshTokenEntity e = new RefreshTokenEntity();

    //3. RELACIÓN JPA (esto soluciona el FK)
    e.setUser(user);

    //4. Datos del token
    e.setTenantId(tenantId);
    e.setTokenHash(refreshTokenHash);
    e.setIssuedAt(Instant.now());
    e.setExpiresAt(expiresAt);

    //5. Opcionales
    e.setRevokedAt(null);
    e.setUserAgent(userAgent);
    e.setIpAddress(ipAddress);
    //6. Persistir
    repo.save(e);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<TokenRecord> read(String refreshTokenHash) {
    return repo.findByTokenHash(refreshTokenHash)
        .map(e -> new TokenRecord(e.getUser().getId(), e.getExpiresAt(), e.getRevokedAt()));
  }

  @Override
  @Transactional
  public void revoke(String refreshTokenHash) {
    repo.findByTokenHash(refreshTokenHash).ifPresent(e -> {
      e.setRevokedAt(Instant.now());
      repo.save(e);
    });
  }
}
