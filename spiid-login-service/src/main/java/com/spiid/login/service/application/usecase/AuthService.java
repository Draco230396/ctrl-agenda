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

    private final CatalogUseCase catalogUseCase;

    @Override
    @Transactional
    public AuthResultDto register(String email, String password, List<Short> roleCodes, String userAgent, String ipAddress) {

        // Normalizar email
        String normalizedEmail = normalizeEmail(email);

        // Validar duplicado
        users.findByEmail(normalizedEmail).ifPresent(u -> {
            throw new IllegalArgumentException("Email ya registrado: " + normalizedEmail);
        });

        // Validar password básica
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password muy corto");
        }

        Instant now = Instant.now();

        //1. Crear tenant (registro = nuevo negocio)
        UUID tenantId = UUID.randomUUID();

        //2. Obtener rol desde catálogo (NO hardcode)
        RoleCatalogItem roleItem = catalogUseCase.getRoleByKey("OWNER");

        //3. Asignar roles
        Set<RoleCatalogItem> roles = Set.of(roleItem);

        //4. Crear usuario correctamente (modelo SaaS)
        User user = new User(
                UUID.randomUUID(),                 // userId
                tenantId,                          // tenantId
                normalizedEmail,
                passwordEncoder.encode(password), // password hash
                true,
                now,
                now,
                roles,
                "LOCAL",                          // provider
                null                              // providerId
        );

        // Guardar usuario
        User saved = users.save(user);

        // Generar tokens
        String access = jwtTokenService.createAccessToken(saved);
        String refresh = jwtTokenService.createRefreshToken(saved);

        // IMPORTANTE: usar user.id(), NO tenantId
        String refreshHash = TokenHashing.sha256Hex(refresh);

        refreshStore.store(
                saved.id(), // correcto
                refreshHash,
                now.plusSeconds(jwtProps.refreshTtlSeconds()),
                userAgent,
                ipAddress
        );

        return new AuthResultDto(access, refresh, toUserView(saved));
    }

    @Override
    @Transactional
    public AuthResultDto login(String email, String password, String userAgent, String ipAddress) {
        String normalizedEmail = normalizeEmail(email);

        User user = users.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
        // evitar login password para usuarios Google
        if ("GOOGLE".equals(user.provider())) {
            throw new IllegalArgumentException("Use Google login");
        }
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
        refreshStore.store(user.id(), refreshHash,  now.plusSeconds(jwtProps.refreshTtlSeconds()), userAgent, ipAddress);

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
        refreshStore.store(user.id(), newRefreshHash,  now.plusSeconds(jwtProps.refreshTtlSeconds()), userAgent, ipAddress);

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
    public AuthResultDto loginWithGoogle(String idToken, String role, String tenantIdInput) {

        // 1. VALIDAR TOKEN GOOGLE
        var googleUser = googleTokenVerifierPort.verify(idToken);

        // 2. BUSCAR USUARIO POR PROVIDER ID (GOOGLE)
        var userOpt = users.findByProviderId(googleUser.sub());

        // 3. SI NO EXISTE, VALIDAR QUE EL EMAIL NO ESTÉ REGISTRADO CON OTRO MÉTODO
        if (userOpt.isEmpty()) {
            users.findByEmail(googleUser.email())
                    .ifPresent(u -> {
                        throw new RuntimeException("Email already registered with another method");
                    });
        }

        User user;

        if (userOpt.isPresent()) {
            // =========================
            // LOGIN
            // =========================
            user = userOpt.get();

            if (!user.enabled()) {
                throw new RuntimeException("Usuario deshabilitado");
            }

        } else {
            // =========================
            // REGISTRO
            // =========================

            // NORMALIZAR ROLE
            String normalizedRole = role.toUpperCase();

            // OBTENER ROLE DESDE CATÁLOGO (valida automáticamente)
            RoleCatalogItem roleItem = catalogUseCase.getRoleByKey(normalizedRole);

            // DEFINIR TENANT
            UUID tenantId;

            if ("OWNER".equals(normalizedRole)) {
                // OWNER crea su propio tenant
                tenantId = UUID.randomUUID();
            } else {
                if (tenantIdInput == null) {
                    throw new RuntimeException("tenantId required for role: " + normalizedRole);
                }

                tenantId = UUID.fromString(tenantIdInput);
            }

            // CREAR USUARIO
            user = new User(
                    UUID.randomUUID(),
                    tenantId,
                    googleUser.email(),
                    null, // sin password (Google)
                    true,
                    Instant.now(),
                    Instant.now(),
                    Set.of(roleItem),
                    "GOOGLE",
                    googleUser.sub()
            );

            // GUARDAR
            user = users.save(user); // importante reasignar
        }

        // =========================
        // GENERACIÓN DE TOKENS (CORRECTO)
        // =========================
        Instant now = Instant.now();

        String access = jwtTokenService.createAccessToken(user);
        String refresh = jwtTokenService.createRefreshToken(user);

        // HASH DEL REFRESH
        String refreshHash = TokenHashing.sha256Hex(refresh);

        // GUARDAR REFRESH TOKEN
        refreshStore.store(
                user.id(),
                refreshHash,
                now.plusSeconds(jwtProps.refreshTtlSeconds()),
                null,
                null
        );

        // =========================
        // RESPONSE FINAL CORRECTO
        // =========================
        return new AuthResultDto(
                access,
                refresh,
                toUserView(user)
        );
    }


}
