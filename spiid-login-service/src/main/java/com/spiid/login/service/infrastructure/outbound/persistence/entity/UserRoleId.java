package com.spiid.login.service.infrastructure.outbound.persistence.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * PK compuesta de iam.user_role (user_id, role_code)
 */
@Embeddable
public class UserRoleId implements Serializable {

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "role_code", nullable = false)
  private short roleCode;

  public UserRoleId() {}

  public UserRoleId(UUID userId, short roleCode) {
    this.userId = userId;
    this.roleCode = roleCode;
  }

  public UUID getUserId() { return userId; }
  public short getRoleCode() { return roleCode; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserRoleId that = (UserRoleId) o;
    return roleCode == that.roleCode && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, roleCode);
  }
}
