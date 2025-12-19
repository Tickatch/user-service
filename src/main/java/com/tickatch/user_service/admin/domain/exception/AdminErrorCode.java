package com.tickatch.user_service.admin.domain.exception;

import io.github.tickatch.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 관리자 에러 코드.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum AdminErrorCode implements ErrorCode {

  // ========================================
  // 조회 (404)
  // ========================================
  ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "ADMIN_NOT_FOUND"),

  // ========================================
  // 중복 (409)
  // ========================================
  ADMIN_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "ADMIN_ALREADY_EXISTS"),

  // ========================================
  // 검증 (400)
  // ========================================
  INVALID_ADMIN_ROLE(HttpStatus.BAD_REQUEST.value(), "INVALID_ADMIN_ROLE"),
  INVALID_DEPARTMENT(HttpStatus.BAD_REQUEST.value(), "INVALID_DEPARTMENT"),

  // ========================================
  // 권한 (403)
  // ========================================
  ADMIN_PERMISSION_DENIED(HttpStatus.FORBIDDEN.value(), "ADMIN_PERMISSION_DENIED"),
  ONLY_ADMIN_CAN_CREATE_ADMIN(HttpStatus.FORBIDDEN.value(), "ONLY_ADMIN_CAN_CREATE_ADMIN"),
  ONLY_ADMIN_CAN_CHANGE_ROLE(HttpStatus.FORBIDDEN.value(), "ONLY_ADMIN_CAN_CHANGE_ROLE"),
  ADMIN_SUSPENDED(HttpStatus.FORBIDDEN.value(), "ADMIN_SUSPENDED"),

  // ========================================
  // 비즈니스 규칙 (422)
  // ========================================
  CANNOT_CHANGE_OWN_ROLE(HttpStatus.UNPROCESSABLE_ENTITY.value(), "CANNOT_CHANGE_OWN_ROLE"),
  CANNOT_DELETE_LAST_ADMIN(HttpStatus.UNPROCESSABLE_ENTITY.value(), "CANNOT_DELETE_LAST_ADMIN"),
  CANNOT_DEACTIVATE_LAST_ADMIN(
      HttpStatus.UNPROCESSABLE_ENTITY.value(), "CANNOT_DEACTIVATE_LAST_ADMIN");

  private final int status;
  private final String code;
}
