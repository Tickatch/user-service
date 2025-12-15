package com.tickatch.user_service.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.admin.domain.exception.AdminErrorCode;
import com.tickatch.user_service.admin.domain.exception.AdminException;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Admin 테스트")
class AdminTest {

  private UUID authId;
  private String email;
  private String name;
  private String phone;
  private String department;

  @BeforeEach
  void setUp() {
    authId = UUID.randomUUID();
    email = "admin@test.com";
    name = "관리자";
    phone = "01012345678";
    department = "운영팀";
  }

  private Admin createAdmin(AdminRole role) {
    return Admin.create(authId, email, name, phone, department, role);
  }

  private Admin createManagerAdmin() {
    return createAdmin(AdminRole.MANAGER);
  }

  private Admin createSuperAdmin() {
    return createAdmin(AdminRole.ADMIN);
  }

  @Nested
  class 생성_테스트 {

    @Test
    void MANAGER_역할로_관리자를_생성한다() {
      Admin admin = createManagerAdmin();
      assertThat(admin.getId()).isEqualTo(authId);
      assertThat(admin.getEmail()).isEqualTo(email);
      assertThat(admin.getProfile().getName()).isEqualTo(name);
      assertThat(admin.getProfile().getPhone()).isEqualTo(phone);
      assertThat(admin.getProfile().getDepartment()).isEqualTo(department);
      assertThat(admin.getAdminRole()).isEqualTo(AdminRole.MANAGER);
      assertThat(admin.getStatus()).isEqualTo(UserStatus.ACTIVE);
      assertThat(admin.isManager()).isTrue();
      assertThat(admin.isAdmin()).isFalse();
    }

    @Test
    void ADMIN_역할로_관리자는_생성한다() {
      Admin admin = createSuperAdmin();
      assertThat(admin.getAdminRole()).isEqualTo(AdminRole.ADMIN);
      assertThat(admin.isManager()).isFalse();
      assertThat(admin.isAdmin()).isTrue();
    }

    @Test
    void 부서_없이_생성한다() {
      Admin admin = Admin.create(authId, email, name, phone, null, AdminRole.MANAGER);
      assertThat(admin.getProfile().getDepartment()).isNull();
    }
  }

  @Nested
  class 프로필_수정_테스트 {

    @Test
    void 프로필을_수정한다() {
      Admin admin = createManagerAdmin();
      String newName = "새관리자";
      String newPhone = "01087654321";
      String newDepartment = "개발팀";
      admin.updateProfile(newName, newPhone, newDepartment);
      assertThat(admin.getProfile().getName()).isEqualTo(newName);
      assertThat(admin.getProfile().getPhone()).isEqualTo(newPhone);
      assertThat(admin.getProfile().getDepartment()).isEqualTo(newDepartment);
    }
  }

  @Nested
  class 역할_변경_테스트 {

    @Test
    void ADMIN이_다른_관리자의_역할을_변경한다() {
      Admin superAdmin = createSuperAdmin();
      Admin targetAdmin = Admin.create(UUID.randomUUID(), "target@test.com", "대상", "01011112222", "운영팀", AdminRole.MANAGER);
      targetAdmin.changeRole(AdminRole.ADMIN, superAdmin);
      assertThat(targetAdmin.getAdminRole()).isEqualTo(AdminRole.ADMIN);
      assertThat(targetAdmin.isAdmin()).isTrue();
    }

    @Test
    void MANAGER가_역할_변경_시_예외가_발생한다() {
      Admin managerAdmin = createManagerAdmin();
      Admin targetAdmin = Admin.create(UUID.randomUUID(), "target@test.com", "대상", "01011112222", "운영팀", AdminRole.MANAGER);
      assertThatThrownBy(() -> targetAdmin.changeRole(AdminRole.ADMIN, managerAdmin))
          .isInstanceOf(AdminException.class)
          .satisfies(e -> {
            AdminException ae = (AdminException) e;
            assertThat(ae.getErrorCode()).isEqualTo(AdminErrorCode.ONLY_ADMIN_CAN_CHANGE_ROLE);
          });
    }

    @Test
    void 자기_자신의_역할_변경_시_예외가_발생한다() {
      Admin superAdmin = createSuperAdmin();
      assertThatThrownBy(() -> superAdmin.changeRole(AdminRole.MANAGER, superAdmin))
          .isInstanceOf(AdminException.class)
          .satisfies(e -> {
            AdminException ae = (AdminException) e;
            assertThat(ae.getErrorCode()).isEqualTo(AdminErrorCode.CANNOT_CHANGE_OWN_ROLE);
          });
    }
  }

  @Nested
  class 권한_확인_테스트 {

    @Test
    void ADMIN은_모든_권한을_보유한다() {
      Admin superAdmin = createSuperAdmin();
      assertThat(superAdmin.hasPermission(AdminRole.MANAGER)).isTrue();
      assertThat(superAdmin.hasPermission(AdminRole.ADMIN)).isTrue();
      assertThat(superAdmin.canCreateAdmin()).isTrue();
      assertThat(superAdmin.canApproveSeller()).isTrue();
    }

    @Test
    void MANAGER는_제한된_권한을_보유한다() {
      Admin managerAdmin = createManagerAdmin();
      assertThat(managerAdmin.hasPermission(AdminRole.MANAGER)).isTrue();
      assertThat(managerAdmin.hasPermission(AdminRole.ADMIN)).isFalse();
      assertThat(managerAdmin.canCreateAdmin()).isFalse();
      assertThat(managerAdmin.canApproveSeller()).isTrue();
    }
  }

  @Nested
  class 상태_변경_테스트 {

    @Test
    void 정지_처리한다() {
      Admin admin = createManagerAdmin();
      admin.suspend();
      assertThat(admin.isSuspended()).isTrue();
      assertThat(admin.isActive()).isFalse();
      assertThat(admin.getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    void 정지_해제한다() {
      Admin admin = createManagerAdmin();
      admin.suspend();
      admin.activate();
      assertThat(admin.isActive()).isTrue();
      assertThat(admin.isSuspended()).isFalse();
      assertThat(admin.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void 탈퇴_처리한다() {
      Admin admin = createManagerAdmin();
      admin.withdraw();
      assertThat(admin.isWithdrawn()).isTrue();
      assertThat(admin.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
    }
  }

  @Nested
  class 상태_확인_테스트 {

    @Test
    void 생성_시_ACTIVE_상태이다() {
      Admin admin = createManagerAdmin();
      assertThat(admin.isActive()).isTrue();
      assertThat(admin.isSuspended()).isFalse();
      assertThat(admin.isWithdrawn()).isFalse();
    }
  }
}