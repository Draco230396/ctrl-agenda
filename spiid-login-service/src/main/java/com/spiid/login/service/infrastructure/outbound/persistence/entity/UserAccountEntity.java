package com.spiid.login.service.infrastructure.outbound.persistence.entity;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "user_account", schema = "login")
public class UserAccountEntity {

  @Id
  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "email", nullable = false, unique = true, length = 320)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 200)
  private String passwordHash;

  @Column(name = "enabled", nullable = false)
  private boolean enabled;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;


}
