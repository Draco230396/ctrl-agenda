package com.spiid.login.service.domain.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenStorePort {

    void store(
            UUID userId,
            String refreshTokenHash,
            Instant expiresAt,
            String userAgent,
            String ipAddress);
    Optional<TokenRecord> read(String refreshTokenHash);
    void revoke(String refreshTokenHash);
    record TokenRecord(UUID userId,
                       Instant expiresAt,
                       Instant revokedAt){}
}
