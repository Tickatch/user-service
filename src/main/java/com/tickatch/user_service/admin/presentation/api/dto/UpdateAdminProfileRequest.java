package com.tickatch.user_service.admin.presentation.api.dto;

import com.tickatch.user_service.admin.application.service.command.dto.UpdateAdminProfileCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * 관리자 프로필 수정 요청 DTO.
 *
 * <p>관리자 프로필을 수정할 때 필요한 정보를 담는다.
 *
 * @param name 이름
 * @param phone 연락처 (선택)
 * @param department 부서 (선택)
 * @author Tickatch
 * @since 1.0.0
 */
public record UpdateAdminProfileRequest(
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    String name,

    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "연락처 형식이 올바르지 않습니다")
    String phone,

    @Size(max = 100, message = "부서명은 100자 이하여야 합니다")
    String department
) {

  /**
   * Command 객체로 변환한다.
   *
   * @param adminId 관리자 ID
   * @return UpdateAdminProfileCommand
   */
  public UpdateAdminProfileCommand toCommand(UUID adminId) {
    return UpdateAdminProfileCommand.of(adminId, name, phone, department);
  }
}