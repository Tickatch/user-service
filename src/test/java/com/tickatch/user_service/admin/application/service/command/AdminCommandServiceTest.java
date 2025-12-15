package com.tickatch.user_service.admin.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.admin.application.service.command.dto.ChangeAdminRoleCommand;
import com.tickatch.user_service.admin.application.service.command.dto.CreateAdminCommand;
import com.tickatch.user_service.admin.application.service.command.dto.UpdateAdminProfileCommand;
import com.tickatch.user_service.admin.domain.Admin;
import com.tickatch.user_service.admin.domain.AdminRepository;
import com.tickatch.user_service.admin.domain.exception.AdminErrorCode;
import com.tickatch.user_service.admin.domain.exception.AdminException;
import com.tickatch.user_service.admin.domain.repository.AdminRepositoryImpl;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import com.tickatch.user_service.config.QueryDslTestConfig;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({QueryDslTestConfig.class, AdminRepositoryImpl.class, AdminCommandService.class})
@DisplayName("AdminCommandService 테스트")
class AdminCommandServiceTest {

  @Autowired
  private AdminCommandService adminCommandService;

  @Autowired
  private AdminRepository adminRepository;

  @Autowired
  private EntityManager entityManager;

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @Nested
  @DisplayName("createAdmin 테스트")
  class CreateAdminTest {

    @Test
    @DisplayName("관리자를 생성한다")
    void createAdmin_success() {
      // given
      UUID authId = UUID.randomUUID();
      CreateAdminCommand command = CreateAdminCommand.of(
          authId, "admin@example.com", "관리자", "010-1234-5678", "운영팀", AdminRole.MANAGER
      );

      // when
      UUID adminId = adminCommandService.createAdmin(command);
      flushAndClear();

      // then
      Admin saved = adminRepository.findById(adminId).orElseThrow();
      assertThat(saved.getEmail()).isEqualTo("admin@example.com");
      assertThat(saved.getProfile().getName()).isEqualTo("관리자");
      assertThat(saved.getProfile().getPhone()).isEqualTo("01012345678");
      assertThat(saved.getProfile().getDepartment()).isEqualTo("운영팀");
      assertThat(saved.getAdminRole()).isEqualTo(AdminRole.MANAGER);
    }

    @Test
    @DisplayName("이미 존재하는 이메일이면 예외가 발생한다")
    void createAdmin_duplicateEmail_throwsException() {
      // given
      Admin existing = Admin.create(
          UUID.randomUUID(), "admin@example.com", "기존관리자", "010-0000-0000", "기존팀", AdminRole.MANAGER
      );
      adminRepository.save(existing);
      flushAndClear();

      CreateAdminCommand command = CreateAdminCommand.of(
          UUID.randomUUID(), "admin@example.com", "관리자", "010-1234-5678", "운영팀", AdminRole.MANAGER
      );

      // when & then
      assertThatThrownBy(() -> adminCommandService.createAdmin(command))
          .isInstanceOf(AdminException.class)
          .satisfies(ex -> {
            AdminException adminException = (AdminException) ex;
            assertThat(adminException.getErrorCode()).isEqualTo(AdminErrorCode.ADMIN_ALREADY_EXISTS);
          });
    }
  }

  @Nested
  @DisplayName("updateProfile 테스트")
  class UpdateProfileTest {

    @Test
    @DisplayName("관리자 프로필을 수정한다")
    void updateProfile_success() {
      // given
      Admin admin = Admin.create(
          UUID.randomUUID(), "admin@example.com", "관리자", "010-1234-5678", "운영팀", AdminRole.MANAGER
      );
      adminRepository.save(admin);
      flushAndClear();

      UUID adminId = admin.getId();
      UpdateAdminProfileCommand command = UpdateAdminProfileCommand.of(
          adminId, "수정관리자", "010-9999-8888", "기술팀"
      );

      // when
      adminCommandService.updateProfile(command);
      flushAndClear();

      // then
      Admin updated = adminRepository.findById(adminId).orElseThrow();
      assertThat(updated.getProfile().getName()).isEqualTo("수정관리자");
      assertThat(updated.getProfile().getPhone()).isEqualTo("01099998888");
      assertThat(updated.getProfile().getDepartment()).isEqualTo("기술팀");
    }

    @Test
    @DisplayName("존재하지 않는 관리자면 예외가 발생한다")
    void updateProfile_notFound_throwsException() {
      // given
      UUID adminId = UUID.randomUUID();
      UpdateAdminProfileCommand command = UpdateAdminProfileCommand.of(
          adminId, "수정관리자", "010-9999-8888", "기술팀"
      );

      // when & then
      assertThatThrownBy(() -> adminCommandService.updateProfile(command))
          .isInstanceOf(AdminException.class)
          .satisfies(ex -> {
            AdminException adminException = (AdminException) ex;
            assertThat(adminException.getErrorCode()).isEqualTo(AdminErrorCode.ADMIN_NOT_FOUND);
          });
    }
  }

  @Nested
  @DisplayName("changeRole 테스트")
  class ChangeRoleTest {

    @Test
    @DisplayName("ADMIN이 다른 관리자의 역할을 변경한다")
    void changeRole_success() {
      // given
      Admin targetAdmin = Admin.create(
          UUID.randomUUID(), "target@example.com", "대상관리자", "010-1111-1111", "운영팀", AdminRole.MANAGER
      );
      Admin changerAdmin = Admin.create(
          UUID.randomUUID(), "changer@example.com", "변경관리자", "010-2222-2222", "기술팀", AdminRole.ADMIN
      );
      adminRepository.save(targetAdmin);
      adminRepository.save(changerAdmin);
      flushAndClear();

      UUID targetAdminId = targetAdmin.getId();
      UUID changerAdminId = changerAdmin.getId();

      ChangeAdminRoleCommand command = ChangeAdminRoleCommand.of(
          targetAdminId, changerAdminId, AdminRole.ADMIN
      );

      // when
      adminCommandService.changeRole(command);
      flushAndClear();

      // then
      Admin updated = adminRepository.findById(targetAdminId).orElseThrow();
      assertThat(updated.getAdminRole()).isEqualTo(AdminRole.ADMIN);
    }

    @Test
    @DisplayName("MANAGER가 역할 변경을 시도하면 예외가 발생한다")
    void changeRole_notAdmin_throwsException() {
      // given
      Admin targetAdmin = Admin.create(
          UUID.randomUUID(), "target@example.com", "대상관리자", "010-1111-1111", "운영팀", AdminRole.MANAGER
      );
      Admin changerAdmin = Admin.create(
          UUID.randomUUID(), "changer@example.com", "변경관리자", "010-2222-2222", "기술팀", AdminRole.MANAGER
      );
      adminRepository.save(targetAdmin);
      adminRepository.save(changerAdmin);
      flushAndClear();

      UUID targetAdminId = targetAdmin.getId();
      UUID changerAdminId = changerAdmin.getId();

      ChangeAdminRoleCommand command = ChangeAdminRoleCommand.of(
          targetAdminId, changerAdminId, AdminRole.ADMIN
      );

      // when & then
      assertThatThrownBy(() -> adminCommandService.changeRole(command))
          .isInstanceOf(AdminException.class)
          .satisfies(ex -> {
            AdminException adminException = (AdminException) ex;
            assertThat(adminException.getErrorCode()).isEqualTo(AdminErrorCode.ONLY_ADMIN_CAN_CHANGE_ROLE);
          });
    }

    @Test
    @DisplayName("자신의 역할은 변경할 수 없다")
    void changeRole_self_throwsException() {
      // given
      Admin admin = Admin.create(
          UUID.randomUUID(), "admin@example.com", "관리자", "010-1234-5678", "운영팀", AdminRole.ADMIN
      );
      adminRepository.save(admin);
      flushAndClear();

      UUID adminId = admin.getId();

      ChangeAdminRoleCommand command = ChangeAdminRoleCommand.of(
          adminId, adminId, AdminRole.MANAGER
      );

      // when & then
      assertThatThrownBy(() -> adminCommandService.changeRole(command))
          .isInstanceOf(AdminException.class)
          .satisfies(ex -> {
            AdminException adminException = (AdminException) ex;
            assertThat(adminException.getErrorCode()).isEqualTo(AdminErrorCode.CANNOT_CHANGE_OWN_ROLE);
          });
    }
  }

  @Nested
  @DisplayName("suspendAdmin 테스트")
  class SuspendAdminTest {

    @Test
    @DisplayName("관리자를 정지한다")
    void suspendAdmin_success() {
      // given
      Admin admin = Admin.create(
          UUID.randomUUID(), "admin@example.com", "관리자", "010-1234-5678", "운영팀", AdminRole.MANAGER
      );
      adminRepository.save(admin);
      flushAndClear();

      UUID adminId = admin.getId();

      // when
      adminCommandService.suspendAdmin(adminId);
      flushAndClear();

      // then
      Admin updated = adminRepository.findById(adminId).orElseThrow();
      assertThat(updated.isSuspended()).isTrue();
    }
  }

  @Nested
  @DisplayName("activateAdmin 테스트")
  class ActivateAdminTest {

    @Test
    @DisplayName("정지된 관리자를 활성화한다")
    void activateAdmin_success() {
      // given
      Admin admin = Admin.create(
          UUID.randomUUID(), "admin@example.com", "관리자", "010-1234-5678", "운영팀", AdminRole.MANAGER
      );
      admin.suspend();
      adminRepository.save(admin);
      flushAndClear();

      UUID adminId = admin.getId();

      // when
      adminCommandService.activateAdmin(adminId);
      flushAndClear();

      // then
      Admin updated = adminRepository.findById(adminId).orElseThrow();
      assertThat(updated.isActive()).isTrue();
    }
  }

  @Nested
  @DisplayName("withdrawAdmin 테스트")
  class WithdrawAdminTest {

    @Test
    @DisplayName("관리자를 탈퇴 처리한다")
    void withdrawAdmin_success() {
      // given
      Admin admin = Admin.create(
          UUID.randomUUID(), "admin@example.com", "관리자", "010-1234-5678", "운영팀", AdminRole.MANAGER
      );
      adminRepository.save(admin);
      flushAndClear();

      UUID adminId = admin.getId();

      // when
      adminCommandService.withdrawAdmin(adminId);
      flushAndClear();

      // then
      Admin updated = adminRepository.findById(adminId).orElseThrow();
      assertThat(updated.isWithdrawn()).isTrue();
    }
  }
}