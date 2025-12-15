package com.tickatch.user_service.common.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("UserException 단위 테스트")
class UserExceptionTest {

  @Test
  void 에러코드로_예외를_생성한다() {
    UserException exception = new UserException(UserErrorCode.INVALID_NAME);

    assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.INVALID_NAME);
    assertThat(exception.getErrorCode().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(exception.getErrorCode().getCode()).isEqualTo("INVALID_NAME");
  }

  @Test
  void 에러코드와_인자로_예외를_생성한다() {
    String userId = "test-user-Id";

    UserException exception = new UserException(UserErrorCode.EVENT_PUBLISH_FAILED, userId);

    assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.EVENT_PUBLISH_FAILED);
    assertThat(exception.getErrorArgs()).containsExactly(userId);
  }

  @Test
  void 에러코드와_원인_예외로_예외를_생성한다() {
    RuntimeException cause = new RuntimeException("원인 예외");

    UserException exception = new UserException(UserErrorCode.INVALID_PHONE, cause);

    assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.INVALID_PHONE);
    assertThat(exception.getCause()).isEqualTo(cause);
  }
}