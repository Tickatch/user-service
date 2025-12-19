package com.tickatch.user_service.admin.domain;

import com.tickatch.user_service.admin.domain.repository.dto.AdminSearchCondition;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Admin 리포지토리 인터페이스.
 *
 * <p>도메인 레이어에서 정의하고, 인프라스트럭처 레이어에서 구현한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public interface AdminRepository {

  /**
   * Admin을 저장한다.
   *
   * @param admin 저장할 Admin 엔티티
   * @return 저장된 Admin 엔티티
   */
  Admin save(Admin admin);

  /**
   * ID로 Admin을 조회한다.
   *
   * @param id Admin ID (= Auth ID)
   * @return 조회된 Admin (없으면 empty)
   */
  Optional<Admin> findById(UUID id);

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
   * 특정 역할의 활성 Admin 수를 조회한다.
   *
   * @param role 역할
   * @return 활성 Admin 수
   */
  long countActiveByRole(AdminRole role);

  /**
   * 검색 조건에 맞는 Admin 목록을 페이징하여 조회한다.
   *
   * @param condition 검색 조건
   * @param pageable 페이징 정보
   * @return 페이징된 Admin 목록
   */
  Page<Admin> findAllByCondition(AdminSearchCondition condition, Pageable pageable);
}
