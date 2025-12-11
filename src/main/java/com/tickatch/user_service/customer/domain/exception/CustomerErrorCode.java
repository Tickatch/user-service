package com.tickatch.user_service.customer.domain.exception;

import io.github.tickatch.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 고객 에러 코드.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum CustomerErrorCode implements ErrorCode {

  // ========================================
  // 조회 (404)
  // ========================================
  CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "CUSTOMER_NOT_FOUND"),

  // ========================================
  // 검증 (400)
  // ========================================
  INVALID_BIRTH_DATE(HttpStatus.BAD_REQUEST.value(), "INVALID_BIRTH_DATE"),
  INVALID_CUSTOMER_GRADE(HttpStatus.BAD_REQUEST.value(), "INVALID_CUSTOMER_GRADE"),

  // ========================================
  // 권한 (403)
  // ========================================
  CUSTOMER_SUSPENDED(HttpStatus.FORBIDDEN.value(), "CUSTOMER_SUSPENDED"),
  CUSTOMER_WITHDRAWN(HttpStatus.FORBIDDEN.value(), "CUSTOMER_WITHDRAWN"),

  // ========================================
  // 비즈니스 규칙 (422)
  // ========================================
  CANNOT_WITHDRAW_WITH_ACTIVE_RESERVATIONS(HttpStatus.UNPROCESSABLE_ENTITY.value(), "CANNOT_WITHDRAW_WITH_ACTIVE_RESERVATIONS"),
  GRADE_DOWNGRADE_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY.value(), "GRADE_DOWNGRADE_NOT_ALLOWED");

  private final int status;
  private final String code;
}