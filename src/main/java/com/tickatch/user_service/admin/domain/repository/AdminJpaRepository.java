package com.tickatch.user_service.admin.domain.repository;

import com.tickatch.user_service.admin.domain.Admin;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Admin JPA 리포지토리.
 *
 * <p>Spring Data JPA 기본 CRUD 기능을 제공한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public interface AdminJpaRepository extends JpaRepository<Admin, UUID> {

  /**
   * 이메일로 Admin을 조회한다.
   *
   * @param email 이메일
   * @return 조회된 Admin (없으면 empty)
   */
  Optional<Admin> findByEmail(String email);

  /**
   * 이메일로 Admin 존재 여부를 확인한다.
   *
   * @param email 이메일
   * @return 존재하면 true
   */
  boolean existsByEmail(String email);

  /**
   * 특정 역할과 상태의 Admin 수를 조회한다.
   *
   * @param role 역할
   * @param status 상태
   * @return Admin 수
   */
  long countByAdminRoleAndStatus(AdminRole role, UserStatus status);
}
