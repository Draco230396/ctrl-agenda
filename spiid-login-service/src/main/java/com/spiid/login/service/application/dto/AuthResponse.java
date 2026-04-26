package com.spiid.login.service.application.dto;

public record AuthResponse(String accessToken, String refreshToken, UserView user) {}