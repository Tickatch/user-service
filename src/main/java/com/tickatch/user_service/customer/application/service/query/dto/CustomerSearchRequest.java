package com.tickatch.user_service.customer.application.service.query.dto;

import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.customer.domain.repository.dto.CustomerSearchCondition;
import com.tickatch.user_service.customer.domain.vo.CustomerGrade;

/**
 * 고객 검색 요청 DTO.
 */
public record CustomerSearchRequest(
    String email,
    String name,
    String phone,
    UserStatus status,
    CustomerGrade grade
) {

  public CustomerSearchCondition toCondition() {
    return CustomerSearchCondition.builder()
        .email(email)
        .name(name)
        .phone(phone)
        .status(status)
        .grade(grade)
        .build();
  }
}