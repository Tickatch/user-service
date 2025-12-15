package com.tickatch.user_service.common.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("UserStatus 단위 테스트")
class UserStatusTest {

  @ParameterizedTest
  @EnumSource(UserStatus.class)
  void 모든_상태는_description을_가진다(UserStatus userStatus) {
    assertThat(userStatus.getDescription()).isNotBlank();
  }

  @Nested
  class 상태_확인_테스트 {

    @Test
    void ACTIVE_상태를_확인한다() {
      UserStatus status = UserStatus.ACTIVE;

      assertThat(status.isActive()).isTrue();
      assertThat(status.isSuspended()).isFalse();
      assertThat(status.isWithdrawn()).isFalse();
      assertThat(status.isTerminal()).isFalse();
    }

    @Test
    void SUSPENDED_상태를_확인한다() {
      UserStatus status = UserStatus.SUSPENDED;

      assertThat(status.isActive()).isFalse();
      assertThat(status.isSuspended()).isTrue();
      assertThat(status.isWithdrawn()).isFalse();
      assertThat(status.isTerminal()).isFalse();
    }

    @Test
    void WITHDRAWN_상태를_확인한다() {
      UserStatus status = UserStatus.WITHDRAWN;

      assertThat(status.isActive()).isFalse();
      assertThat(status.isSuspended()).isFalse();
      assertThat(status.isWithdrawn()).isTrue();
      assertThat(status.isTerminal()).isTrue();
    }
  }

  @Nested
  class 상태_전이_가능_여부_확인_테스트 {

    @Test
    void ACTIVE에서_정지_가능하다() {
      assertThat(UserStatus.ACTIVE.canSuspend()).isTrue();
      assertThat(UserStatus.SUSPENDED.canSuspend()).isFalse();
      assertThat(UserStatus.WITHDRAWN.canSuspend()).isFalse();
    }

    @Test
    void SUSPENDED에서_활성화가_가능하다() {
      assertThat(UserStatus.ACTIVE.canActivate()).isFalse();
      assertThat(UserStatus.SUSPENDED.canActivate()).isTrue();
      assertThat(UserStatus.WITHDRAWN.canActivate()).isFalse();
    }

    @Test
    void WITHDRAWN이_아닌_상태에서_탈퇴_가능하다() {
      assertThat(UserStatus.ACTIVE.canWithdraw()).isTrue();
      assertThat(UserStatus.SUSPENDED.canWithdraw()).isTrue();
      assertThat(UserStatus.WITHDRAWN.canWithdraw()).isFalse();
    }
  }
}