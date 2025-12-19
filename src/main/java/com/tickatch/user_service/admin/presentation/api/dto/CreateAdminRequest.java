package com.tickatch.user_service.admin.presentation.api.dto;

import com.tickatch.user_service.admin.application.service.command.dto.CreateAdminCommand;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * 관리자 생성 요청 DTO.
 *
 * <p>새 관리자를 생성할 때 필요한 정보를 담는다.
 *
 * @param email 이메일
 * @param name 이름
 * @param phone 연락처 (선택)
 * @param department 부서 (선택)
 * @param adminRole 역할
 * @author Tickatch
 * @since 1.0.0
 */
public record CreateAdminRequest(
    @NotBlank(message = "이메일은 필수입니다") @Email(message = "이메일 형식이 올바르지 않습니다") String email,
    @NotBlank(message = "이름은 필수입니다") @Size(max = 50, message = "이름은 50자 이하여야 합니다") String name,
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "연락처 형식이 올바르지 않습니다")
        String phone,
    @Size(max = 100, message = "부서명은 100자 이하여야 합니다") String department,
    @NotNull(message = "역할은 필수입니다") AdminRole adminRole) {

  /**
   * Command 객체로 변환한다.
   *
   * @param authId 인증 ID (Auth Service에서 발급)
   * @return CreateAdminCommand
   */
  public CreateAdminCommand toCommand(UUID authId) {
    return CreateAdminCommand.of(authId, email, name, phone, department, adminRole);
  }
}
