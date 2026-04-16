package com.spiid.login.service.application.usecase;

import com.spiid.login.service.domain.model.RoleCatalogItem;
import com.spiid.login.service.domain.model.User;
import com.spiid.login.service.domain.port.in.AuthUseCase;
import com.spiid.login.service.domain.port.in.CatalogUseCase;
import com.spiid.login.service.domain.port.out.GoogleTokenVerifierPort;
import com.spiid.login.service.domain.port.out.RefreshTokenStorePort;
import com.spiid.login.service.domain.port.out.RoleCatalogRepositoryPort;
import com.spiid.login.service.domain.port.out.UserRepositoryPort;
import com.spiid.login.service.domain.valueobject.Email;
import com.spiid.login.service.infrastructure.security.JwtTokenService;
import com.spiid.login.service.infrastructure.security.TokenHashing;
import com.spiid.login.service.infrastructure.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.spiid.login.service.domain.valueobject.RoleCode;
import java.time.Instant;
import java.util.*;


/**
 * Implementación de los casos de uso de Auth
 * NOTA: Esta capa no interactua con JPA ni HTTP, solo usa puertos y servicios de utileria
 * */
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort users;
    private final RoleCatalogRepositoryPort roleCatalog;
    private final RefreshTokenStorePort refreshStore;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProps;

    private final GoogleTokenVerifierPort googleTokenVerifierPort;
    private final UserRepositoryPort userRepositoryPort;

    private final CatalogService catalogUseCase;


    @Override
    @Transactional
    public AuthResultDto register(String email, String password, List<Short> roleCodes, String userAgent, String ipAddress) {
        String normalizedEmail = normalizeEmail(email);

        users.findByEmail(normalizedEmail).ifPresent(u -> {
            throw new IllegalArgumentException("Email ya registrado: " + normalizedEmail);
        });

        // Validación de roleCodes -> catálogo (para poder mostrar key/description)
        List<Short> codes = (roleCodes == null || roleCodes.isEmpty())
                ? List.of((short) 1)  // 1 = PASSENGER (seed)
                : roleCodes;

        Set<RoleCatalogItem> roles = new LinkedHashSet<>();
        for (Short code : codes) {
            if (code == null) continue;
            RoleCatalogItem role = roleCatalog.findByCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("Role code inválido: " + code));
            roles.add(role);
        }

        Instant now = Instant.now();
        User user = new User(
                UUID.randomUUID(),
                normalizedEmail,
                passwordEncoder.encode(password),
                true,
                now,
                now,
                roles
        );

        User saved = users.save(user);

        String access = jwtTokenService.createAccessToken(saved);
        String refresh = jwtTokenService.createRefreshToken(saved);

        // Guardamos hash del refresh token (no el token en claro)
        String refreshHash = TokenHashing.sha256Hex(refresh);
        refreshStore.store(saved.tenantId(),refreshHash,  now.plusSeconds(jwtProps.refreshTtlSeconds()), userAgent, ipAddress);

        return new AuthResultDto(access, refresh, toUserView(saved));
    }

    @Override
    @Transactional
    public AuthResultDto login(String email, String password, String userAgent, String ipAddress) {
        String normalizedEmail = normalizeEmail(email);

        User user = users.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!user.enabled()) {
            throw new IllegalArgumentException("Usuario deshabilitado");
        }

        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        Instant now = Instant.now();

        String access = jwtTokenService.createAccessToken(user);
        String refresh = jwtTokenService.createRefreshToken(user);

        String refreshHash = TokenHashing.sha256Hex(refresh);
        refreshStore.store(user.tenantId(), refreshHash,  now.plusSeconds(jwtProps.refreshTtlSeconds()), userAgent, ipAddress);

        return new AuthResultDto(access, refresh, toUserView(user));
    }

    @Override
    @Transactional
    public AuthResultDto refresh(String refreshToken, String userAgent, String ipAddress) {
        String refreshHash = TokenHashing.sha256Hex(refreshToken);

        var rec = refreshStore.read(refreshHash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));

        if (rec.revokedAt() != null) {
            throw new IllegalArgumentException("Refresh token revocado");
        }

        if (Instant.now().isAfter(rec.expiresAt())) {
            refreshStore.revoke(refreshHash);
            throw new IllegalArgumentException("Refresh token expirado");
        }

        User user = users.findById(rec.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!user.enabled()) {
            throw new IllegalArgumentException("Usuario deshabilitado");
        }

        // Rotación simple: revoca el refresh anterior y entrega uno nuevo
        refreshStore.revoke(refreshHash);

        Instant now = Instant.now();
        String access = jwtTokenService.createAccessToken(user);
        String newRefresh = jwtTokenService.createRefreshToken(user);

        String newRefreshHash = TokenHashing.sha256Hex(newRefresh);
        refreshStore.store(user.tenantId(), newRefreshHash,  now.plusSeconds(jwtProps.refreshTtlSeconds()), userAgent, ipAddress);

        return new AuthResultDto(access, newRefresh, toUserView(user));
    }

    @Override
    @Transactional
    public UserViewDto me(UUID userId) {
        User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return toUserView(user);
    }

    private static String normalizeEmail(String email) {
        if (email == null) throw new IllegalArgumentException("Email requerido");
        String e = email.trim().toLowerCase(Locale.ROOT);
        if (e.isBlank()) throw new IllegalArgumentException("Email requerido");
        return e;
    }

    private static UserViewDto toUserView(User user) {
        return new UserViewDto(
                user.tenantId(),
                user.email(),
                user.enabled(),
                user.roles() == null ? List.of() : user.roles().stream().toList()
        );
    }

    @Override
    public String loginWithGoogle(String idToken, String role, String tenantIdInput) {

        var googleUser = googleTokenVerifierPort.verify(idToken);

        var email = new Email(googleUser.email());

        var userOpt = userRepositoryPort.findByEmail(email.toString());

        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();

        } else {

            UUID tenantId;

            if ("OWNER".equalsIgnoreCase(role)) {
                tenantId = UUID.randomUUID();
            } else {
                tenantId = UUID.fromString(tenantIdInput);
            }

            RoleCatalogItem roleItem = catalogUseCase.listRoles().getFirst();

            user = new User(
                    tenantId,
                    email.toString(),
                    null,
                    true,
                    Instant.now(),
                    Instant.now(),
                    Set.of(roleItem)
            );

            userRepositoryPort.save(user);
        }

        return jwtTokenService.generateToken(user);
    }


}
