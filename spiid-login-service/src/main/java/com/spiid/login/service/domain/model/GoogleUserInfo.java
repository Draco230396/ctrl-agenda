package com.spiid.login.service.domain.model;

/**
 * Se crea modelo de respuesta -> Dominio
 * @param email
 * @param name
 */
public record GoogleUserInfo(
        String email,
        String name
) {}
