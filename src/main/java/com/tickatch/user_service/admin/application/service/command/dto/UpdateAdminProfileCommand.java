package com.tickatch.user_service.admin.application.service.command.dto;

import java.util.UUID;

/**
 * 관리자 프로필 수정 커맨드.
 *
 * @param adminId 관리자 ID
 * @param name 이름
 * @param phone 연락처 (선택)
 * @param department 부서 (선택)
 */
public record UpdateAdminProfileCommand(
    UUID adminId, String name, String phone, String department) {

  public static UpdateAdminProfileCommand of(
      UUID adminId, String name, String phone, String department) {
    return new UpdateAdminProfileCommand(adminId, name, phone, department);
  }
}
