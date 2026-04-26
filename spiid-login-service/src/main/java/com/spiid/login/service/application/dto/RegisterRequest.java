package com.spiid.login.service.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        // roleCodes numéricos (catálogo); opcional -> se asigna PASSENGER por default
        List<Short> roleCodes
) {}