package com.tickatch.user_service.admin.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.admin.application.service.query.dto.AdminResponse;
import com.tickatch.user_service.admin.application.service.query.dto.AdminSearchRequest;
import com.tickatch.user_service.admin.domain.Admin;
import com.tickatch.user_service.admin.domain.AdminRepository;
import com.tickatch.user_service.admin.domain.exception.AdminErrorCode;
import com.tickatch.user_service.admin.domain.exception.AdminException;
import com.tickatch.user_service.admin.domain.repository.AdminRepositoryImpl;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.config.QueryDslTestConfig;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import({QueryDslTestConfig.class, AdminRepositoryImpl.class, AdminQueryService.class})
@DisplayName("AdminQueryService 테스트")
class AdminQueryServiceTest {

  @Autowired
  private AdminQueryService adminQueryService;

  @Autowired
  private AdminRepository adminRepository;

  @Autowired
  private EntityManager entityManager;

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @Nested
  @DisplayName("getAdmin 테스트")
  class GetAdminTest {

    @Test
    @DisplayName("ID로 관리자를 조회한다")
    void getAdmin_success() {
      // given
      Admin admin = Admin.create(
          UUID.randomUUID(), "admin@example.com", "관리자", "010-1234-5678", "운영팀", AdminRole.MANAGER
      );
      adminRepository.save(admin);
      flushAndClear();

      UUID adminId = admin.getId();

      // when
      AdminResponse response = adminQueryService.getAdmin(adminId);

      // then
      assertThat(response.id()).isEqualTo(adminId);
      assertThat(response.email()).isEqualTo("admin@example.com");
      assertThat(response.name()).isEqualTo("관리자");
      assertThat(response.phone()).isEqualTo("01012345678");
      assertThat(response.department()).isEqualTo("운영팀");
      assertThat(response.adminRole()).isEqualTo(AdminRole.MANAGER);
      assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 ID면 예외가 발생한다")
    void getAdmin_notFound_throwsException() {
      // given
      UUID adminId = UUID.randomUUID();

      // when & then
      assertThatThrownBy(() -> adminQueryService.getAdmin(adminId))
          .isInstanceOf(AdminException.class)
          .satisfies(ex -> {
            AdminException adminException = (AdminException) ex;
            assertThat(adminException.getErrorCode()).isEqualTo(AdminErrorCode.ADMIN_NOT_FOUND);
          });
    }
  }

  @Nested
  @DisplayName("getAdminByEmail 테스트")
  class GetAdminByEmailTest {

    @Test
    @DisplayName("이메일로 관리자를 조회한다")
    void getAdminByEmail_success() {
      // given
      String email = "admin@example.com";
      Admin admin = Admin.create(
          UUID.randomUUID(), email, "관리자", "010-1234-5678", "운영팀", AdminRole.ADMIN
      );
      adminRepository.save(admin);
      flushAndClear();

      // when
      AdminResponse response = adminQueryService.getAdminByEmail(email);

      // then
      assertThat(response.email()).isEqualTo(email);
      assertThat(response.name()).isEqualTo("관리자");
      assertThat(response.adminRole()).isEqualTo(AdminRole.ADMIN);
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 예외가 발생한다")
    void getAdminByEmail_notFound_throwsException() {
      // given
      String email = "notfound@example.com";

      // when & then
      assertThatThrownBy(() -> adminQueryService.getAdminByEmail(email))
          .isInstanceOf(AdminException.class)
          .satisfies(ex -> {
            AdminException adminException = (AdminException) ex;
            assertThat(adminException.getErrorCode()).isEqualTo(AdminErrorCode.ADMIN_NOT_FOUND);
          });
    }
  }

  @Nested
  @DisplayName("searchAdmins 테스트")
  class SearchAdminsTest {

    @Test
    @DisplayName("조건으로 관리자를 검색한다")
    void searchAdmins_success() {
      // given
      Admin admin1 = Admin.create(UUID.randomUUID(), "admin1@example.com", "관리자1", "010-1111-1111", "운영팀", AdminRole.MANAGER);
      Admin admin2 = Admin.create(UUID.randomUUID(), "admin2@example.com", "관리자2", "010-2222-2222", "기술팀", AdminRole.MANAGER);
      Admin admin3 = Admin.create(UUID.randomUUID(), "admin3@example.com", "관리자3", "010-3333-3333", "운영팀", AdminRole.ADMIN);
      adminRepository.save(admin1);
      adminRepository.save(admin2);
      adminRepository.save(admin3);
      flushAndClear();

      AdminSearchRequest request = new AdminSearchRequest(
          null, null, UserStatus.ACTIVE, AdminRole.MANAGER, null
      );
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<AdminResponse> result = adminQueryService.searchAdmins(request, pageable);

      // then
      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getContent()).extracting(AdminResponse::name)
          .containsExactlyInAnyOrder("관리자1", "관리자2");
    }

    @Test
    @DisplayName("부서별로 관리자를 검색한다")
    void searchAdmins_byDepartment() {
      // given
      Admin admin1 = Admin.create(UUID.randomUUID(), "admin1@example.com", "관리자1", "010-1111-1111", "운영팀", AdminRole.MANAGER);
      Admin admin2 = Admin.create(UUID.randomUUID(), "admin2@example.com", "관리자2", "010-2222-2222", "기술팀", AdminRole.MANAGER);
      adminRepository.save(admin1);
      adminRepository.save(admin2);
      flushAndClear();

      AdminSearchRequest request = new AdminSearchRequest(
          null, null, null, null, "운영팀"
      );
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<AdminResponse> result = adminQueryService.searchAdmins(request, pageable);

      // then
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).department()).isEqualTo("운영팀");
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 페이지를 반환한다")
    void searchAdmins_empty() {
      // given
      Admin admin = Admin.create(UUID.randomUUID(), "admin@example.com", "관리자", "010-1234-5678", "운영팀", AdminRole.MANAGER);
      adminRepository.save(admin);
      flushAndClear();

      AdminSearchRequest request = new AdminSearchRequest(
          null, null, null, AdminRole.ADMIN, "존재하지않는부서"
      );
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<AdminResponse> result = adminQueryService.searchAdmins(request, pageable);

      // then
      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }
  }

  @Nested
  @DisplayName("existsByEmail 테스트")
  class ExistsByEmailTest {

    @Test
    @DisplayName("이메일이 존재하면 true를 반환한다")
    void existsByEmail_true() {
      // given
      String email = "exists@example.com";
      Admin admin = Admin.create(UUID.randomUUID(), email, "관리자", "010-1234-5678", "운영팀", AdminRole.MANAGER);
      adminRepository.save(admin);
      flushAndClear();

      // when
      boolean result = adminQueryService.existsByEmail(email);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 false를 반환한다")
    void existsByEmail_false() {
      // given
      String email = "notexists@example.com";

      // when
      boolean result = adminQueryService.existsByEmail(email);

      // then
      assertThat(result).isFalse();
    }
  }

  @Nested
  @DisplayName("countActiveByRole 테스트")
  class CountActiveByRoleTest {

    @Test
    @DisplayName("ADMIN 역할의 활성 관리자 수를 조회한다")
    void countActiveByRole_admin() {
      // given
      Admin admin1 = Admin.create(UUID.randomUUID(), "admin1@example.com", "관리자1", "010-1111-1111", "운영팀", AdminRole.ADMIN);
      Admin admin2 = Admin.create(UUID.randomUUID(), "admin2@example.com", "관리자2", "010-2222-2222", "기술팀", AdminRole.ADMIN);
      Admin manager = Admin.create(UUID.randomUUID(), "manager@example.com", "매니저", "010-3333-3333", "운영팀", AdminRole.MANAGER);
      adminRepository.save(admin1);
      adminRepository.save(admin2);
      adminRepository.save(manager);
      flushAndClear();

      // when
      long count = adminQueryService.countActiveByRole(AdminRole.ADMIN);

      // then
      assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("정지된 관리자는 카운트에서 제외된다")
    void countActiveByRole_excludeSuspended() {
      // given
      Admin active = Admin.create(UUID.randomUUID(), "active@example.com", "활성관리자", "010-1111-1111", "운영팀", AdminRole.ADMIN);
      Admin suspended = Admin.create(UUID.randomUUID(), "suspended@example.com", "정지관리자", "010-2222-2222", "운영팀", AdminRole.ADMIN);
      suspended.suspend();
      adminRepository.save(active);
      adminRepository.save(suspended);
      flushAndClear();

      // when
      long count = adminQueryService.countActiveByRole(AdminRole.ADMIN);

      // then
      assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("활성 관리자가 없으면 0을 반환한다")
    void countActiveByRole_zero() {
      // given - no admins

      // when
      long count = adminQueryService.countActiveByRole(AdminRole.ADMIN);

      // then
      assertThat(count).isZero();
    }
  }
}