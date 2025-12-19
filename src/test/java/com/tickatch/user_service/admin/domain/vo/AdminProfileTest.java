package com.tickatch.user_service.admin.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.admin.domain.exception.AdminErrorCode;
import com.tickatch.user_service.admin.domain.exception.AdminException;
import com.tickatch.user_service.common.domain.exception.UserErrorCode;
import com.tickatch.user_service.common.domain.exception.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("AdminProfile 테스트")
class AdminProfileTest {

  @Nested
  class 생성_테스트 {

    @Test
    void 유효한_정보로_생성한다() {
      AdminProfile profile = AdminProfile.of("관리자", "01012345678", "운영팀");
      assertThat(profile.getName()).isEqualTo("관리자");
      assertThat(profile.getPhone()).isEqualTo("01012345678");
      assertThat(profile.getDepartment()).isEqualTo("운영팀");
    }

    @Test
    void 연락처와_부서_없이_생성한다() {
      AdminProfile profile = AdminProfile.of("관리자", null, null);
      assertThat(profile.getName()).isEqualTo("관리자");
      assertThat(profile.getPhone()).isNull();
      assertThat(profile.getDepartment()).isNull();
    }

    @Test
    void 연락처를_정규화한다() {
      AdminProfile profile = AdminProfile.of("관리자", "010-1234-5678", "운영팀");
      assertThat(profile.getPhone()).isEqualTo("01012345678");
    }
  }

  @Nested
  class 이름_검증_테스트 {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void 이름이_null이거나_빈_값이면_UserExcetpion이_발생한다(String invalidName) {
      assertThatThrownBy(() -> AdminProfile.of(invalidName, "01012345678", "운영팀"))
          .isInstanceOf(UserException.class)
          .satisfies(
              e -> {
                UserException ue = (UserException) e;
                assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.INVALID_NAME);
              });
    }

    @Test
    void 이름이_50자를_초과하면_예외가_발생한다() {
      String longName = "가".repeat(51);
      assertThatThrownBy(() -> AdminProfile.of(longName, "01012345678", "운영팀"))
          .isInstanceOf(UserException.class)
          .satisfies(
              e -> {
                UserException ue = (UserException) e;
                assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.INVALID_NAME);
              });
    }
  }

  @Nested
  class 연락처_검증_테스트 {

    @ParameterizedTest
    @ValueSource(strings = {"1234567890", "02-1234-5678", "abc12345678"})
    void 유효하지_않은_연락처는_UserException가_발생한다(String invalidPhone) {
      assertThatThrownBy(() -> AdminProfile.of("관리자", invalidPhone, "운영팀"))
          .isInstanceOf(UserException.class)
          .satisfies(
              e -> {
                UserException ue = (UserException) e;
                assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.INVALID_PHONE);
              });
    }
  }

  @Nested
  class 부서_검증_테스트 {

    @Test
    void 부서가_100자를_초과하면_AdminException이_발생한다() {
      String longDepartment = "가".repeat(101);
      assertThatThrownBy(() -> AdminProfile.of("관리자", "01012345678", longDepartment))
          .isInstanceOf(AdminException.class)
          .satisfies(
              e -> {
                AdminException ae = (AdminException) e;
                assertThat(ae.getErrorCode()).isEqualTo(AdminErrorCode.INVALID_DEPARTMENT);
              });
    }

    @Test
    void 부서가_100자이면_정상_생성한다() {
      String maxDepartment = "가".repeat(100);
      AdminProfile profile = AdminProfile.of("관리자", "01012345678", maxDepartment);
      assertThat(profile.getDepartment()).hasSize(100);
    }
  }

  @Nested
  class 수정_테스트 {

    @Test
    void 프로필을_수정할_수_있다() {
      AdminProfile original = AdminProfile.of("관리자", "01012345678", "운영팀");
      AdminProfile updated = original.update("새관리자", "01087654321", "개발팀");
      assertThat(updated.getName()).isEqualTo("새관리자");
      assertThat(updated.getPhone()).isEqualTo("01087654321");
      assertThat(updated.getDepartment()).isEqualTo("개발팀");
      // 원본 불변 확인
      assertThat(original.getName()).isEqualTo("관리자");
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_값이면_동등하다() {
      AdminProfile profile1 = AdminProfile.of("관리자", "01012345678", "운영팀");
      AdminProfile profile2 = AdminProfile.of("관리자", "010-1234-5678", "운영팀");
      assertThat(profile1).isEqualTo(profile2);
      assertThat(profile1.hashCode()).isEqualTo(profile2.hashCode());
    }

    @Test
    void 다른_값이면_동등하지_않다() {
      AdminProfile profile1 = AdminProfile.of("관리자", "01012345678", "운영팀");
      AdminProfile profile2 = AdminProfile.of("관리자", "01012345678", "개발팀");
      assertThat(profile1).isNotEqualTo(profile2);
    }
  }
}
