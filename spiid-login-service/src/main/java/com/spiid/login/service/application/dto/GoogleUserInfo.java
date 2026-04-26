package com.spiid.login.service.application.dto;

/**
 * Se crea modelo de respuesta -> Dominio
 * @param email
 * @param name
 */
public record GoogleUserInfo(
        String email,
        String name,
        String sub
) {}
