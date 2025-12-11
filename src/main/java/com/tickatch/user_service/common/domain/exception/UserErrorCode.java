package com.tickatch.user_service.common.domain.exception;

import io.github.tickatch.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 공통 사용자 에러 코드.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

  // ========================================
  // 검증 - 프로필 (400)
  // ========================================
  INVALID_NAME(HttpStatus.BAD_REQUEST.value(), "INVALID_NAME"),
  INVALID_PHONE(HttpStatus.BAD_REQUEST.value(), "INVALID_PHONE"),
  INVALID_ADDRESS(HttpStatus.BAD_REQUEST.value(), "INVALID_ADDRESS"),

  // ========================================
  // 상태 (422)
  // ========================================
  USER_ALREADY_SUSPENDED(HttpStatus.UNPROCESSABLE_ENTITY.value(), "USER_ALREADY_SUSPENDED"),
  USER_ALREADY_ACTIVE(HttpStatus.UNPROCESSABLE_ENTITY.value(), "USER_ALREADY_ACTIVE"),
  USER_ALREADY_WITHDRAWN(HttpStatus.UNPROCESSABLE_ENTITY.value(), "USER_ALREADY_WITHDRAWN"),

  // ========================================
  // 이벤트 (503)
  // ========================================
  EVENT_PUBLISH_FAILED(HttpStatus.SERVICE_UNAVAILABLE.value(), "EVENT_PUBLISH_FAILED");

  private final int status;
  private final String code;
}