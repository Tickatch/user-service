package com.tickatch.user_service.customer.application.service.command.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 고객 생성 커맨드.
 *
 * @param authId Auth Service에서 발급된 ID
 * @param email 이메일
 * @param name 이름
 * @param phone 연락처
 * @param birthDate 생년월일 (선택)
 */
public record CreateCustomerCommand(
    UUID authId,
    String email,
    String name,
    String phone,
    LocalDate birthDate
) {

  public static CreateCustomerCommand of(UUID authId, String email, String name, String phone, LocalDate birthDate) {
    return new CreateCustomerCommand(authId, email, name, phone, birthDate);
  }
}