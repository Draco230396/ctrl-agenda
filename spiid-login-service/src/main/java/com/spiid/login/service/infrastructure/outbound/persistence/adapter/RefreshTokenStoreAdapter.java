package com.spiid.login.service.infrastructure.outbound.persistence.adapter;

import com.spiid.login.service.infrastructure.outbound.persistence.entity.RefreshTokenEntity;
import com.spiid.login.service.domain.port.out.RefreshTokenStorePort;
import com.spiid.login.service.infrastructure.outbound.persistence.repository.RefreshTokenJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenStoreAdapter implements RefreshTokenStorePort {

  private final RefreshTokenJpaRepository repo;

  public RefreshTokenStoreAdapter(RefreshTokenJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  @Transactional
  public void store(UUID userId, String refreshTokenHash,  Instant expiresAt, String userAgent, String ipAddress) {
    RefreshTokenEntity e = new RefreshTokenEntity();
    e.setId(UUID.randomUUID());
    e.setUserId(userId);
    e.setTokenHash(refreshTokenHash);
    e.setIssuedAt(Instant.now());
    e.setExpiresAt(expiresAt);
    e.setRevokedAt(null);
    e.setUserAgent(userAgent);
    e.setIpAddress(ipAddress);
    repo.save(e);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<TokenRecord> read(String refreshTokenHash) {
    return repo.findByTokenHash(refreshTokenHash)
        .map(e -> new TokenRecord(e.getUserId(), e.getExpiresAt(), e.getRevokedAt()));
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
