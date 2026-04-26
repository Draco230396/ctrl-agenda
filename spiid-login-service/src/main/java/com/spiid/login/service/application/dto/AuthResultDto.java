package com.spiid.login.service.application.dto;
/**
 * Resultado estándar de autenticación.
 *
 * Se reutiliza en:
 * - register
 * - login
 * - refresh
 *
 * Agrupa los tokens y la información del usuario.
 */
public record AuthResultDto(
        String accessToken,
        String refreshToken,
        UserViewDto user
) {}