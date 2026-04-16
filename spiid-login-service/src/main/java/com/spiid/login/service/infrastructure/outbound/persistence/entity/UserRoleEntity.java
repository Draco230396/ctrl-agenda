package com.spiid.login.service.infrastructure.outbound.persistence.entity;


import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "user_role", schema = "login")
public class UserRoleEntity {

  @EmbeddedId
  private UserRoleId id;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

}
