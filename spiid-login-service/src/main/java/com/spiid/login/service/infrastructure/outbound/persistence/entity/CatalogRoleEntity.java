package com.spiid.login.service.infrastructure.outbound.persistence.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "catalog_role", schema = "login")
public class CatalogRoleEntity {

  public CatalogRoleEntity(){

  }
  @Id
  @Column(name = "code", nullable = false)
  private short code;

  @Column(name = "key", nullable = false, unique = true, length = 40)
  private String key;

  @Column(name = "description", nullable = false, length = 255)
  private String description;

  @Column(name = "active", nullable = false)
  private boolean active;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;


}
