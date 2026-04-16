package com.spiid.login.service.infrastructure.inbound.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * DTOs REST para auth.
 * Se agrupan en un archivo por simplicidad.
 */
public class AuthDtos {

  public record RegisterRequest(
      @NotBlank @Email String email,
      @NotBlank String password,
      // roleCodes numéricos (catálogo); opcional -> se asigna PASSENGER por default
      List<Short> roleCodes
  ) {}

  public record LoginRequest(
      @NotBlank @Email String email,
      @NotBlank String password
  ) {}

  public record RefreshRequest(
      @NotBlank String refreshToken
  ) {}

  public record RoleView(short code, String key, String description) {}

  public record UserView(String id, String email, boolean enabled, List<RoleView> roles) {}

  public record AuthResponse(String accessToken, String refreshToken, UserView user) {}
}
