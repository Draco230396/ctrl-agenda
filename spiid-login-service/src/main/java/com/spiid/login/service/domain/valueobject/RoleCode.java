package com.spiid.login.service.domain.valueobject;

public enum RoleCode {

    OWNER((short) 1),
    CLIENT((short) 2),
    ADMIN((short) 3);

    private final short code;

    RoleCode(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }

    public static RoleCode from(String value) {
        try {
            return RoleCode.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid role: " + value);
        }
    }
}