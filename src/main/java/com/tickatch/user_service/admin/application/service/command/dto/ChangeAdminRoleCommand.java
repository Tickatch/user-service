package com.tickatch.user_service.admin.application.service.command.dto;

import com.tickatch.user_service.admin.domain.vo.AdminRole;
import java.util.UUID;

/**
 * 관리자 역할 변경 커맨드.
 *
 * @param targetAdminId 변경 대상 관리자 ID
 * @param changerAdminId 변경 수행 관리자 ID
 * @param newRole 새 역할
 */
public record ChangeAdminRoleCommand(
    UUID targetAdminId,
    UUID changerAdminId,
    AdminRole newRole
) {

  public static ChangeAdminRoleCommand of(UUID targetAdminId, UUID changerAdminId, AdminRole newRole) {
    return new ChangeAdminRoleCommand(targetAdminId, changerAdminId, newRole);
  }
}