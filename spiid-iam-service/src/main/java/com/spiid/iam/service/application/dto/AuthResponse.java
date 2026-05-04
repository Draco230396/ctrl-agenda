package com.spiid.iam.service.application.dto;

public record AuthResponse(String accessToken, String refreshToken, UserView user) {}