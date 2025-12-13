package com.tickatch.user_service.admin.application.service.query.dto;

import com.tickatch.user_service.admin.domain.Admin;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 관리자 조회 응답 DTO.
 */
public record AdminResponse(
    UUID id,
    String email,
    String name,
    String phone,
    String department,
    AdminRole adminRole,
    UserStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

  public static AdminResponse from(Admin admin) {
    return new AdminResponse(
        admin.getId(),
        admin.getEmail(),
        admin.getProfile().getName(),
        admin.getProfile().getPhone(),
        admin.getProfile().getDepartment(),
        admin.getAdminRole(),
        admin.getStatus(),
        admin.getCreatedAt(),
        admin.getUpdatedAt()
    );
  }
}