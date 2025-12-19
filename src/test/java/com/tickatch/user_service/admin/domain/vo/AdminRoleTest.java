package com.tickatch.user_service.admin.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("AdminRole 테스트")
class AdminRoleTest {

  @ParameterizedTest
  @EnumSource(AdminRole.class)
  void 모든_역할은_description과_level을_가진다(AdminRole role) {
    assertThat(role.getDescription()).isNotBlank();
    assertThat(role.getLevel()).isPositive();
  }

  @Nested
  class 역할_확인_테스트 {

    @Test
    void MANAGER_역할을_확인한다() {
      AdminRole role = AdminRole.MANAGER;

      assertThat(role.isManager()).isTrue();
      assertThat(role.isAdmin()).isFalse();
    }

    @Test
    void ADMIN_역할을_확인한다() {
      AdminRole role = AdminRole.ADMIN;

      assertThat(role.isManager()).isFalse();
      assertThat(role.isAdmin()).isTrue();
    }
  }

  @Nested
  class 역할_비교_테스트 {

    @Test
    void ADMIN이_MANAGER보다_높은_권한을_가진다() {
      assertThat(AdminRole.ADMIN.isHigherThan(AdminRole.MANAGER)).isTrue();
      assertThat(AdminRole.MANAGER.isHigherThan(AdminRole.ADMIN)).isFalse();
    }

    @Test
    void 같은_역할은_높지_않다() {
      assertThat(AdminRole.ADMIN.isHigherThan(AdminRole.ADMIN)).isFalse();
      assertThat(AdminRole.MANAGER.isHigherThan(AdminRole.MANAGER)).isFalse();
    }

    @Test
    void 같거나_높은_권한을_확인한다() {
      assertThat(AdminRole.ADMIN.isHigherOrEqualThan(AdminRole.MANAGER)).isTrue();
      assertThat(AdminRole.ADMIN.isHigherOrEqualThan(AdminRole.ADMIN)).isTrue();
      assertThat(AdminRole.MANAGER.isHigherOrEqualThan(AdminRole.ADMIN)).isFalse();
      assertThat(AdminRole.MANAGER.isHigherOrEqualThan(AdminRole.MANAGER)).isTrue();
    }
  }

  @Nested
  class 권한_확인_테스트 {

    @Test
    void ADMIN만_관리자_생성이_가능하다() {
      assertThat(AdminRole.ADMIN.canCreateAdmin()).isTrue();
      assertThat(AdminRole.MANAGER.canCreateAdmin()).isFalse();
    }

    @Test
    void ADMIN만_역할_변경이_가능하다() {
      assertThat(AdminRole.ADMIN.canChangeRole()).isTrue();
      assertThat(AdminRole.MANAGER.canChangeRole()).isFalse();
    }

    @Test
    void 모든_역할이_판매자_승인이_가능하다() {
      assertThat(AdminRole.ADMIN.canApproveSeller()).isTrue();
      assertThat(AdminRole.MANAGER.canApproveSeller()).isTrue();
    }

    @Test
    void 모든_역할이_사용자_정지_가능하다() {
      assertThat(AdminRole.ADMIN.canSuspendUser()).isTrue();
      assertThat(AdminRole.MANAGER.canSuspendUser()).isTrue();
    }
  }

  @Test
  void 역할_레벨_순서를_확인한다() {
    assertThat(AdminRole.MANAGER.getLevel()).isLessThan(AdminRole.ADMIN.getLevel());
  }
}
