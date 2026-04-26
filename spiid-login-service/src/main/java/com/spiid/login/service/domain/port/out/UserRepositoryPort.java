package com.spiid.login.service.domain.port.out;

import com.spiid.login.service.application.dto.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    User save(User user);
    Optional<User> findByProviderId(String providerId);
}
