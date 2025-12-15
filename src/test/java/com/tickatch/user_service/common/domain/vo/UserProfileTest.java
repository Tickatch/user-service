package com.tickatch.user_service.common.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.common.domain.exception.UserErrorCode;
import com.tickatch.user_service.common.domain.exception.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("UserProfile 단위 테스트")
class UserProfileTest {

  @Nested
  class 생성_테스트 {

    @Test
    void 유효한_이름과_연락처로_생성한다() {
      UserProfile profile = UserProfile.of("홍길동", "010-1234-5678");
      assertThat(profile.getName()).isEqualTo("홍길동");
      assertThat(profile.getPhone()).isEqualTo("01012345678");
    }

    @Test
    void 연락처_없이_생성한다() {
      UserProfile profile = UserProfile.of("홍길동", null);
      assertThat(profile.getName()).isEqualTo("홍길동");
      assertThat(profile.getPhone()).isNull();
    }

    @Test
    void 빈_연락처로_생성한다() {
      UserProfile profile = UserProfile.of("홍길동", "");
      assertThat(profile.getName()).isEqualTo("홍길동");
      assertThat(profile.getPhone()).isNull();
    }
  }

  @Nested
  class 이름_검증_테스트 {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void 이름이_null이거나_빈_값이면_예외가_발생한다(String invalidName) {
      assertThatThrownBy(() -> UserProfile.of(invalidName, "01012345678"))
          .isInstanceOf(UserException.class)
          .satisfies(e -> {
            UserException ue = (UserException) e;
            assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.INVALID_NAME);
          });
    }

    @Test
    void 이름이_50자이면_정상적으로_생성된다() {
      String longName = "가".repeat(51);
      assertThatThrownBy(() -> UserProfile.of(longName, "01012345678"))
          .isInstanceOf(UserException.class)
          .satisfies(e -> {
            UserException ue = (UserException) e;
            assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.INVALID_NAME);
          });
    }

    @Test
    void 이름이_50자를_초과하면_예외가_발생한다() {
      String maxName = "가".repeat(50);
      UserProfile profile = UserProfile.of(maxName, null);
      assertThat(profile.getName()).hasSize(50);
    }
  }

  @Nested
  class 연락처_검증_테스트 {

    @ParameterizedTest
    @ValueSource(strings = {"01012345678", "010-1234-5678", "01112345678", "011-1234-5678"})
    void 유효한_연락처_형식이다(String validPhone) {
      UserProfile profile = UserProfile.of("홍길동", validPhone);
      assertThat(profile.getPhone()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567890", "02-1234-5678", "010-123-456", "abc12345678"})
    void 유효하지_않은_연락처_형식이다(String invalidPhone) {
      assertThatThrownBy(() -> UserProfile.of("홍길동", invalidPhone))
          .isInstanceOf(UserException.class)
          .satisfies(e -> {
            UserException ue = (UserException) e;
            assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.INVALID_PHONE);
          });
    }
  }

  @Nested
  class 수정_테스트 {

    @Test
    void 프로필을_수정할_수_있다() {
      UserProfile original = UserProfile.of("홍길동", "01012345678");
      UserProfile updated = original.update("김철수", "01087654321");
      assertThat(updated.getName()).isEqualTo("김철수");
      assertThat(updated.getPhone()).isEqualTo("01087654321");
      // 원본 불변 확인
      assertThat(original.getName()).isEqualTo("홍길동");
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_값이면_동등하다() {
      UserProfile profile1 = UserProfile.of("홍길동", "01012345678");
      UserProfile profile2 = UserProfile.of("홍길동", "010-1234-5678"); // 정규화 후 같음
      assertThat(profile1).isEqualTo(profile2);
      assertThat(profile1.hashCode()).isEqualTo(profile2.hashCode());
    }

    @Test
    void 다른_값이면_동등하지_않다() {
      UserProfile profile1 = UserProfile.of("홍길동", "01012345678");
      UserProfile profile2 = UserProfile.of("김철수", "01012345678");
      assertThat(profile1).isNotEqualTo(profile2);
    }
  }
}