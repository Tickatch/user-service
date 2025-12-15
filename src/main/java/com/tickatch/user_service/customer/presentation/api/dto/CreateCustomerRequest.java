package com.tickatch.user_service.customer.presentation.api.dto;

import com.tickatch.user_service.customer.application.service.command.dto.CreateCustomerCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 고객 생성 요청 DTO.
 *
 * <p>새 고객을 생성할 때 필요한 정보를 담는다.
 *
 * @param email 이메일
 * @param name 이름
 * @param phone 연락처
 * @param birthDate 생년월일 (선택)
 * @author Tickatch
 * @since 1.0.0
 */
public record CreateCustomerRequest(
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    String email,

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    String name,

    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "연락처 형식이 올바르지 않습니다")
    String phone,

    @Past(message = "생년월일은 과거 날짜여야 합니다")
    LocalDate birthDate
) {

  /**
   * Command 객체로 변환한다.
   *
   * @param authId 인증 ID (Auth Service에서 발급)
   * @return CreateCustomerCommand
   */
  public CreateCustomerCommand toCommand(UUID authId) {
    return CreateCustomerCommand.of(authId, email, name, phone, birthDate);
  }
}