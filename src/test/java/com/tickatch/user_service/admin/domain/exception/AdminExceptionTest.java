package com.tickatch.user_service.admin.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;

@DisplayName("AdminException 테스트")
class AdminExceptionTest {

  @ParameterizedTest
  @EnumSource(AdminErrorCode.class)
  void 모든_에러코드는_status와_code를_가진다(AdminErrorCode errorCode) {
    assertThat(errorCode.getStatus()).isPositive();
    assertThat(errorCode.getCode()).isNotBlank();
  }

  @Test
  void 조회_에러코드는_404_상태코드를_가진다() {
    assertThat(AdminErrorCode.ADMIN_NOT_FOUND.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void 검증_에러코드는_400_상태코드를_가진다() {
    assertThat(AdminErrorCode.INVALID_ADMIN_ROLE.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(AdminErrorCode.INVALID_DEPARTMENT.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void 권한_에러코드는_403_상태코드를_가진다() {
    assertThat(AdminErrorCode.ADMIN_PERMISSION_DENIED.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(AdminErrorCode.ONLY_ADMIN_CAN_CREATE_ADMIN.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(AdminErrorCode.ONLY_ADMIN_CAN_CHANGE_ROLE.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(AdminErrorCode.ADMIN_SUSPENDED.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @Test
  void 비즈니스_규칙_에러코드는_422_상태코드를_가진다() {
    assertThat(AdminErrorCode.CANNOT_CHANGE_OWN_ROLE.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    assertThat(AdminErrorCode.CANNOT_DELETE_LAST_ADMIN.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    assertThat(AdminErrorCode.CANNOT_DEACTIVATE_LAST_ADMIN.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
  }

  @Test
  void 에러코드와_인자로_예외를_생성한다() {
    String adminId = "admin-123";
    AdminException exception = new AdminException(AdminErrorCode.ADMIN_NOT_FOUND, adminId);
    assertThat(exception.getErrorCode()).isEqualTo(AdminErrorCode.ADMIN_NOT_FOUND);
    assertThat(exception.getErrorArgs()).containsExactly(adminId);
  }
}