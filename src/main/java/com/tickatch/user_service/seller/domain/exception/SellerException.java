package com.tickatch.user_service.seller.domain.exception;

import io.github.tickatch.common.error.BusinessException;

/**
 * 판매자 예외.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public class SellerException extends BusinessException {

  public SellerException(SellerErrorCode errorCode) {
    super(errorCode);
  }

  public SellerException(SellerErrorCode errorCode, Object... errorArgs) {
    super(errorCode, errorArgs);
  }

  public SellerException(SellerErrorCode errorCode, Throwable cause, Object... errorArgs) {
    super(errorCode, cause, errorArgs);
  }
}