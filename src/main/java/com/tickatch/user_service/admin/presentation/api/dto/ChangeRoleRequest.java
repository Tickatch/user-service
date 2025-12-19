package com.tickatch.user_service.admin.presentation.api.dto;

import com.tickatch.user_service.admin.application.service.command.dto.ChangeAdminRoleCommand;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 관리자 역할 변경 요청 DTO.
 *
 * <p>관리자의 역할을 변경할 때 사용한다. ADMIN 역할만 다른 관리자의 역할을 변경할 수 있으며, 자기 자신의 역할은 변경할 수 없다.
 *
 * @param newRole 새 역할
 * @author Tickatch
 * @since 1.0.0
 */
public record ChangeRoleRequest(@NotNull(message = "새 역할은 필수입니다") AdminRole newRole) {

  /**
   * Command 객체로 변환한다.
   *
   * @param targetAdminId 대상 관리자 ID
   * @param changerAdminId 변경자 관리자 ID
   * @return ChangeAdminRoleCommand
   */
  public ChangeAdminRoleCommand toCommand(UUID targetAdminId, UUID changerAdminId) {
    return ChangeAdminRoleCommand.of(targetAdminId, changerAdminId, newRole);
  }
}
