package com.tickatch.user_service.customer.domain.exception;

import io.github.tickatch.common.error.BusinessException;

/**
 * 고객 예외.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public class CustomerException extends BusinessException {

  public CustomerException(CustomerErrorCode errorCode) {
    super(errorCode);
  }

  public CustomerException(CustomerErrorCode errorCode, Object... errorArgs) {
    super(errorCode, errorArgs);
  }

  public CustomerException(CustomerErrorCode errorCode, Throwable cause, Object... errorArgs) {
    super(errorCode, cause, errorArgs);
  }
}
