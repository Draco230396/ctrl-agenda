package com.spiid.login.service.application.dto;

import java.util.List;

public record UserView(String id, String email, boolean enabled, List<RoleView> roles) {}