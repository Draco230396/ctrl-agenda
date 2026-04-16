package com.spiid.login.service.infrastructure.inbound.rest.controller;

import com.spiid.login.service.domain.port.in.AuthUseCase;
import com.spiid.login.service.infrastructure.inbound.rest.dto.AuthDtos;
import com.spiid.login.service.infrastructure.inbound.rest.request.GoogleLoginRequest;
import com.spiid.login.service.infrastructure.inbound.rest.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints de autenticación.
 *
 * Flujo:
 * - register: crea usuario + roles + emite tokens
 * - login: valida credenciales y emite tokens
 * - refresh: rota refresh token (revoca el anterior) y emite nuevo access/refresh
 * - me: devuelve datos del usuario autenticado (por access token)
 */
@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

  private final AuthUseCase auth;

  public AuthController(AuthUseCase auth) {
    this.auth = auth;
  }

  @PostMapping("/register")
  public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest req, HttpServletRequest http) {
    var res = auth.register(req.email(), req.password(), req.roleCodes(), userAgent(http), clientIp(http));
    return toResponse(res);
  }

  @PostMapping("/login")
  public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest req, HttpServletRequest http) {
    var res = auth.login(req.email(), req.password(), userAgent(http), clientIp(http));
    return toResponse(res);
  }

  @PostMapping("/refresh")
  public AuthDtos.AuthResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest req, HttpServletRequest http) {
    var res = auth.refresh(req.refreshToken(), userAgent(http), clientIp(http));
    return toResponse(res);
  }

  @GetMapping("/me")
  public AuthDtos.UserView me(Authentication authentication) {
    // JwtAuthFilter pone el principal como UUID
    UUID userId = (UUID) authentication.getPrincipal();
    var user = auth.me(userId);
    return new AuthDtos.UserView(
        user.id().toString(),
        user.email(),
        user.enabled(),
        user.roles().stream().map(r -> new AuthDtos.RoleView(r.code(), r.key(), r.description())).toList()
    );
  }

  private static AuthDtos.AuthResponse toResponse(AuthUseCase.AuthResultDto res) {
    var u = res.user();
    List<AuthDtos.RoleView> roles = u.roles().stream().map(r -> new AuthDtos.RoleView(r.code(), r.key(), r.description())).toList();
    var uv = new AuthDtos.UserView(u.id().toString(), u.email(), u.enabled(), roles);
    return new AuthDtos.AuthResponse(res.accessToken(), res.refreshToken(), uv);
  }

  private static String userAgent(HttpServletRequest req) {
    String ua = req.getHeader("User-Agent");
    return ua == null ? null : ua.substring(0, Math.min(ua.length(), 255));
  }

  private static String clientIp(HttpServletRequest req) {
    // Si estás detrás de proxy/load balancer, se usa X-Forwarded-For (primer IP).
    String xff = req.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    String ip = req.getRemoteAddr();
    return ip == null ? null : ip;
  }

  @PostMapping("/google")
  public ResponseEntity<AuthResponse> loginWithGoogle(
          @RequestBody GoogleLoginRequest request
  ) {
    String token = authUseCase.loginWithGoogle(
            request.idToken(),
            request.role(),
            request.tenantId()
    );

    return ResponseEntity.ok(new AuthResponse(token));
  }
}
