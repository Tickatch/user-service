package com.tickatch.user_service.customer.application.service.command.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 고객 프로필 수정 커맨드.
 *
 * @param customerId 고객 ID
 * @param name 이름
 * @param phone 연락처
 * @param birthDate 생년월일 (선택)
 */
public record UpdateCustomerProfileCommand(
    UUID customerId,
    String name,
    String phone,
    LocalDate birthDate
) {

  public static UpdateCustomerProfileCommand of(UUID customerId, String name, String phone, LocalDate birthDate) {
    return new UpdateCustomerProfileCommand(customerId, name, phone, birthDate);
  }
}