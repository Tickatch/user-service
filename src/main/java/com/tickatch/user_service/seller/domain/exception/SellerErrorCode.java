package com.tickatch.user_service.seller.domain.exception;

import io.github.tickatch.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 판매자 에러 코드.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum SellerErrorCode implements ErrorCode {

  // ========================================
  // 조회 (404)
  // ========================================
  SELLER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "SELLER_NOT_FOUND"),

  // ========================================
  // 중복 (409)
  // ========================================
  SELLER_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "SELLER_ALREADY_EXISTS"),
  BUSINESS_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "BUSINESS_NUMBER_ALREADY_EXISTS"),

  // ========================================
  // 검증 - 사업자 정보 (400)
  // ========================================
  INVALID_BUSINESS_NAME(HttpStatus.BAD_REQUEST.value(), "INVALID_BUSINESS_NAME"),
  INVALID_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST.value(), "INVALID_BUSINESS_NUMBER"),
  INVALID_REPRESENTATIVE_NAME(HttpStatus.BAD_REQUEST.value(), "INVALID_REPRESENTATIVE_NAME"),
  INVALID_BUSINESS_ADDRESS(HttpStatus.BAD_REQUEST.value(), "INVALID_BUSINESS_ADDRESS"),
  DUPLICATE_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST.value(), "DUPLICATE_BUSINESS_NUMBER"),

  // ========================================
  // 검증 - 정산 정보 (400)
  // ========================================
  INVALID_BANK_CODE(HttpStatus.BAD_REQUEST.value(), "INVALID_BANK_CODE"),
  INVALID_ACCOUNT_NUMBER(HttpStatus.BAD_REQUEST.value(), "INVALID_ACCOUNT_NUMBER"),
  INVALID_ACCOUNT_HOLDER(HttpStatus.BAD_REQUEST.value(), "INVALID_ACCOUNT_HOLDER"),

  // ========================================
  // 검증 - 승인 (400)
  // ========================================
  INVALID_REJECTION_REASON(HttpStatus.BAD_REQUEST.value(), "INVALID_REJECTION_REASON"),

  // ========================================
  // 권한 (403)
  // ========================================
  SELLER_NOT_APPROVED(HttpStatus.FORBIDDEN.value(), "SELLER_NOT_APPROVED"),
  SELLER_SUSPENDED(HttpStatus.FORBIDDEN.value(), "SELLER_SUSPENDED"),
  SELLER_WITHDRAWN(HttpStatus.FORBIDDEN.value(), "SELLER_WITHDRAWN"),

  // ========================================
  // 비즈니스 규칙 (422)
  // ========================================
  SELLER_NOT_PENDING(HttpStatus.UNPROCESSABLE_ENTITY.value(), "SELLER_NOT_PENDING"),
  SELLER_ALREADY_APPROVED(HttpStatus.UNPROCESSABLE_ENTITY.value(), "SELLER_ALREADY_APPROVED"),
  SELLER_ALREADY_REJECTED(HttpStatus.UNPROCESSABLE_ENTITY.value(), "SELLER_ALREADY_REJECTED"),
  CANNOT_UPDATE_SETTLEMENT_BEFORE_APPROVAL(
      HttpStatus.UNPROCESSABLE_ENTITY.value(), "CANNOT_UPDATE_SETTLEMENT_BEFORE_APPROVAL"),
  CANNOT_WITHDRAW_WITH_ACTIVE_PRODUCTS(
      HttpStatus.UNPROCESSABLE_ENTITY.value(), "CANNOT_WITHDRAW_WITH_ACTIVE_PRODUCTS"),
  CANNOT_REGISTER_PERFORMANCE(
      HttpStatus.UNPROCESSABLE_ENTITY.value(), "CANNOT_REGISTER_PERFORMANCE");

  private final int status;
  private final String code;
}
