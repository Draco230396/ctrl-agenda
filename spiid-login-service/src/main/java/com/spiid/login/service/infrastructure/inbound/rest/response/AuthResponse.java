package com.spiid.login.service.infrastructure.inbound.rest.response;

import java.util.List;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UserView user;

    public AuthResponse(String accessToken, String refreshToken, UserView user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public UserView getUser() {
        return user;
    }

    // 🔹 DTO interno para usuario
    public static class UserView {
        private String id;
        private String email;
        private boolean enabled;
        private List<RoleView> roles;

        public UserView(String id, String email, boolean enabled, List<RoleView> roles) {
            this.id = id;
            this.email = email;
            this.enabled = enabled;
            this.roles = roles;
        }

        public String getId() { return id; }
        public String getEmail() { return email; }
        public boolean isEnabled() { return enabled; }
        public List<RoleView> getRoles() { return roles; }
    }

    // 🔹 DTO interno para roles
    public static class RoleView {
        private short code;
        private String key;
        private String description;

        public RoleView(short code, String key, String description) {
            this.code = code;
            this.key = key;
            this.description = description;
        }

        public short getCode() { return code; }
        public String getKey() { return key; }
        public String getDescription() { return description; }
    }
}
