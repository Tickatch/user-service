package com.tickatch.user_service.common.domain.exception;

import io.github.tickatch.common.error.BusinessException;

/**
 * 공통 사용자 예외.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public class UserException extends BusinessException {

  public UserException(UserErrorCode errorCode) {
    super(errorCode);
  }

  public UserException(UserErrorCode errorCode, Object... errorArgs) {
    super(errorCode, errorArgs);
  }

  public UserException(UserErrorCode errorCode, Throwable cause, Object... errorArgs) {
    super(errorCode, cause, errorArgs);
  }
}