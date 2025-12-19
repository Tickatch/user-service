package com.tickatch.user_service.customer.application.service.query.dto;

import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.customer.domain.Customer;
import com.tickatch.user_service.customer.domain.vo.CustomerGrade;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** 고객 조회 응답 DTO. */
public record CustomerResponse(
    UUID id,
    String email,
    String name,
    String phone,
    LocalDate birthDate,
    CustomerGrade grade,
    UserStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static CustomerResponse from(Customer customer) {
    return new CustomerResponse(
        customer.getId(),
        customer.getEmail(),
        customer.getProfile().getName(),
        customer.getProfile().getPhone(),
        customer.getBirthDate(),
        customer.getGrade(),
        customer.getStatus(),
        customer.getCreatedAt(),
        customer.getUpdatedAt());
  }
}
