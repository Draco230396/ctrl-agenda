package com.spiid.login.service.infrastructure.inbound.rest.request;

public record GoogleLoginRequest(
        String idToken,
        String role,  //OWNER -> ignora tenantId -> crea uno nuevo
        String tenantId  //Necesita tenantId
) {}
