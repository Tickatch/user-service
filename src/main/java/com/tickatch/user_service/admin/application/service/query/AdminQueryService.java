package com.tickatch.user_service.admin.application.service.query;

import com.tickatch.user_service.admin.application.service.query.dto.AdminResponse;
import com.tickatch.user_service.admin.application.service.query.dto.AdminSearchRequest;
import com.tickatch.user_service.admin.domain.Admin;
import com.tickatch.user_service.admin.domain.AdminRepository;
import com.tickatch.user_service.admin.domain.exception.AdminErrorCode;
import com.tickatch.user_service.admin.domain.exception.AdminException;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 조회 서비스.
 *
 * <p>관리자 조회 작업을 처리한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQueryService {

  private final AdminRepository adminRepository;

  /**
   * ID로 관리자를 조회한다.
   *
   * @param adminId 관리자 ID
   * @return 관리자 응답
   * @throws AdminException 관리자를 찾을 수 없는 경우
   */
  public AdminResponse getAdmin(UUID adminId) {
    Admin admin =
        adminRepository
            .findById(adminId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.ADMIN_NOT_FOUND));
    return AdminResponse.from(admin);
  }

  /**
   * 이메일로 관리자를 조회한다.
   *
   * @param email 이메일
   * @return 관리자 응답
   * @throws AdminException 관리자를 찾을 수 없는 경우
   */
  public AdminResponse getAdminByEmail(String email) {
    Admin admin =
        adminRepository
            .findByEmail(email)
            .orElseThrow(() -> new AdminException(AdminErrorCode.ADMIN_NOT_FOUND));
    return AdminResponse.from(admin);
  }

  /**
   * 조건에 맞는 관리자 목록을 페이징하여 조회한다.
   *
   * @param request 검색 요청
   * @param pageable 페이징 정보
   * @return 페이징된 관리자 응답 목록
   */
  public Page<AdminResponse> searchAdmins(AdminSearchRequest request, Pageable pageable) {
    return adminRepository
        .findAllByCondition(request.toCondition(), pageable)
        .map(AdminResponse::from);
  }

  /**
   * 이메일 존재 여부를 확인한다.
   *
   * @param email 이메일
   * @return 존재하면 true
   */
  public boolean existsByEmail(String email) {
    return adminRepository.existsByEmail(email);
  }

  /**
   * 특정 역할의 활성 관리자 수를 조회한다.
   *
   * @param role 역할
   * @return 활성 관리자 수
   */
  public long countActiveByRole(AdminRole role) {
    return adminRepository.countActiveByRole(role);
  }
}
