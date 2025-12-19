package com.tickatch.user_service.admin.application.service.command.dto;

import com.tickatch.user_service.admin.domain.vo.AdminRole;
import java.util.UUID;

/**
 * 관리자 생성 커맨드.
 *
 * @param authId Auth Service에서 발급된 ID
 * @param email 이메일
 * @param name 이름
 * @param phone 연락처 (선택)
 * @param department 부서 (선택)
 * @param adminRole 역할
 */
public record CreateAdminCommand(
    UUID authId, String email, String name, String phone, String department, AdminRole adminRole) {

  public static CreateAdminCommand of(
      UUID authId,
      String email,
      String name,
      String phone,
      String department,
      AdminRole adminRole) {
    return new CreateAdminCommand(authId, email, name, phone, department, adminRole);
  }
}
