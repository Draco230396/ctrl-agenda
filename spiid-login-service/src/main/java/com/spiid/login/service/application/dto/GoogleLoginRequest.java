package com.spiid.login.service.application.dto;

public record GoogleLoginRequest(
        String idToken,
        String role,  //OWNER -> ignora tenantId -> crea uno nuevo
        String tenantId  //Necesita tenantId
) {}
