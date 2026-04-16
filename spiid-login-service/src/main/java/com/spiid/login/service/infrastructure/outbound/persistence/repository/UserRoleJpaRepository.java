package com.spiid.login.service.infrastructure.outbound.persistence.repository;

import com.spiid.login.service.infrastructure.outbound.persistence.entity.UserRoleEntity;
import com.spiid.login.service.infrastructure.outbound.persistence.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, UserRoleId> {

  @Query("select ur.id.roleCode from UserRoleEntity ur where ur.id.userId = :userId")
  List<Short> findRoleCodesByUserId(UUID userId);

  @Modifying
  @Query("delete from UserRoleEntity ur where ur.id.userId = :userId")
  void deleteAllByUserId(UUID userId);
}
