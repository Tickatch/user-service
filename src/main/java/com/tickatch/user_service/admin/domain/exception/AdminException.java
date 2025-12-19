package com.tickatch.user_service.admin.domain.exception;

import io.github.tickatch.common.error.BusinessException;

/**
 * 관리자 예외.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public class AdminException extends BusinessException {

  public AdminException(AdminErrorCode errorCode) {
    super(errorCode);
  }

  public AdminException(AdminErrorCode errorCode, Object... errorArgs) {
    super(errorCode, errorArgs);
  }

  public AdminException(AdminErrorCode errorCode, Throwable cause, Object... errorArgs) {
    super(errorCode, cause, errorArgs);
  }
}
