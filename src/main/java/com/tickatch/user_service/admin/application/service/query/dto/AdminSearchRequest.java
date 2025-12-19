package com.tickatch.user_service.admin.application.service.query.dto;

import com.tickatch.user_service.admin.domain.repository.dto.AdminSearchCondition;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import com.tickatch.user_service.common.domain.vo.UserStatus;

/** 관리자 검색 요청 DTO. */
public record AdminSearchRequest(
    String email, String name, UserStatus status, AdminRole adminRole, String department) {

  public AdminSearchCondition toCondition() {
    return AdminSearchCondition.builder()
        .email(email)
        .name(name)
        .status(status)
        .adminRole(adminRole)
        .department(department)
        .build();
  }
}
