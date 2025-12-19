package com.tickatch.user_service.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickatch.user_service.admin.domain.repository.AdminRepositoryImpl;
import com.tickatch.user_service.admin.domain.repository.dto.AdminSearchCondition;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.config.QueryDslTestConfig;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest
@Import({QueryDslTestConfig.class, AdminRepositoryImpl.class})
@DisplayName("AdminRepository 테스트")
class AdminRepositoryTest {

  @Autowired private AdminRepository adminRepository;

  private Admin admin1;
  private Admin admin2;
  private Admin admin3;

  @BeforeEach
  void setUp() {
    admin1 =
        Admin.create(
            UUID.randomUUID(), "admin1@test.com", "관리자1", "01012345678", "운영팀", AdminRole.ADMIN);

    admin2 =
        Admin.create(
            UUID.randomUUID(),
            "manager1@test.com",
            "매니저1",
            "01087654321",
            "운영팀",
            AdminRole.MANAGER);

    admin3 =
        Admin.create(
            UUID.randomUUID(),
            "manager2@test.com",
            "매니저2",
            "01011112222",
            "개발팀",
            AdminRole.MANAGER);
  }

  @Nested
  class 저장_테스트 {

    @Test
    void Admin_저장에_성공한다() {
      Admin saved = adminRepository.save(admin1);

      assertThat(saved.getId()).isEqualTo(admin1.getId());
      assertThat(saved.getEmail()).isEqualTo("admin1@test.com");
      assertThat(saved.getProfile().getName()).isEqualTo("관리자1");
      assertThat(saved.getProfile().getDepartment()).isEqualTo("운영팀");
      assertThat(saved.getAdminRole()).isEqualTo(AdminRole.ADMIN);
      assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void Admin_수정에_성공한다() {
      Admin saved = adminRepository.save(admin1);
      saved.updateProfile("관리자1수정", "01099998888", "기획팀");

      Optional<Admin> found = adminRepository.findById(saved.getId());

      assertThat(found).isPresent();
      assertThat(found.get().getProfile().getName()).isEqualTo("관리자1수정");
      assertThat(found.get().getProfile().getDepartment()).isEqualTo("기획팀");
    }
  }

  @Nested
  class FindById_테스트 {

    @Test
    void ID로_Admin_조회를_성공한다() {
      adminRepository.save(admin1);

      Optional<Admin> found = adminRepository.findById(admin1.getId());

      assertThat(found).isPresent();
      assertThat(found.get().getEmail()).isEqualTo("admin1@test.com");
    }

    @Test
    void 존재하지_않는_ID를_조회_시_empty를_반환한다() {
      Optional<Admin> found = adminRepository.findById(UUID.randomUUID());

      assertThat(found).isEmpty();
    }
  }

  @Nested
  class FindByEmail_테스트 {

    @Test
    void 이메일로_admin_조회를_성공한다() {
      adminRepository.save(admin1);

      Optional<Admin> found = adminRepository.findByEmail("admin1@test.com");

      assertThat(found).isPresent();
      assertThat(found.get().getProfile().getName()).isEqualTo("관리자1");
    }

    @Test
    void 존재하지_않는_이메일로_조회_시_empty를_반환한다() {
      Optional<Admin> found = adminRepository.findByEmail("notfound@test.com");

      assertThat(found).isEmpty();
    }
  }

  @Nested
  class ExistsByEmail_테스트 {

    @Test
    void 이메일_존재_여부를_확인한다() {
      adminRepository.save(admin1);

      boolean exists = adminRepository.existsByEmail("admin1@test.com");

      assertThat(exists).isTrue();
    }

    @Test
    void 이메일_존재_여부_확인_시_존재하지_않는다() {
      boolean exists = adminRepository.existsByEmail("notfound@test.com");

      assertThat(exists).isFalse();
    }
  }

  @Nested
  class CountActiveByRole_테스트 {

    @BeforeEach
    void 데이터_초기화() {
      adminRepository.save(admin1);
      adminRepository.save(admin2);
      adminRepository.save(admin3);
    }

    @Test
    void ADMIN_역할_활성_관리자_수를_조회한다() {
      long count = adminRepository.countActiveByRole(AdminRole.ADMIN);

      assertThat(count).isEqualTo(1);
    }

    @Test
    void MANAGER_역할_활성_관리자_수를_조회한다() {
      long count = adminRepository.countActiveByRole(AdminRole.MANAGER);

      assertThat(count).isEqualTo(2);
    }

    @Test
    void 정지된_관리자는_카운트에서_제외한다() {
      admin2.suspend();
      adminRepository.save(admin2);

      long count = adminRepository.countActiveByRole(AdminRole.MANAGER);

      assertThat(count).isEqualTo(1);
    }
  }

  @Nested
  class FindAllByCondition_테스트 {

    @BeforeEach
    void 데이터_초기화() {
      adminRepository.save(admin1);
      adminRepository.save(admin2);
      adminRepository.save(admin3);
    }

    @Test
    void 조건_없이_전체_조회가_된다() {
      AdminSearchCondition condition = AdminSearchCondition.builder().build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Admin> result = adminRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void 이메일로_검색이_된다() {
      AdminSearchCondition condition = AdminSearchCondition.builder().email("admin1").build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Admin> result = adminRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getEmail()).isEqualTo("admin1@test.com");
    }

    @Test
    void 이름으로_검색이_된다() {
      AdminSearchCondition condition = AdminSearchCondition.builder().name("매니저").build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Admin> result = adminRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void Admin_역할로_검색한다() {
      AdminSearchCondition condition =
          AdminSearchCondition.builder().adminRole(AdminRole.ADMIN).build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Admin> result = adminRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getAdminRole()).isEqualTo(AdminRole.ADMIN);
    }

    @Test
    void MANAGER_역할로_검색한다() {
      AdminSearchCondition condition =
          AdminSearchCondition.builder().adminRole(AdminRole.MANAGER).build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Admin> result = adminRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void 부서로_검색한다() {
      AdminSearchCondition condition = AdminSearchCondition.builder().department("운영").build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Admin> result = adminRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void 상태로_검색한다() {
      admin2.suspend();
      adminRepository.save(admin2);

      AdminSearchCondition condition =
          AdminSearchCondition.builder().status(UserStatus.SUSPENDED).build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Admin> result = adminRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getProfile().getName()).isEqualTo("매니저1");
    }

    @Test
    void 복합_조건으로_검색한다() {
      AdminSearchCondition condition =
          AdminSearchCondition.builder().adminRole(AdminRole.MANAGER).department("운영").build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Admin> result = adminRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getProfile().getName()).isEqualTo("매니저1");
    }

    @Test
    void 페이징으로_필터링이_된다() {
      AdminSearchCondition condition = AdminSearchCondition.builder().build();
      PageRequest pageable = PageRequest.of(0, 2);

      Page<Admin> result = adminRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getTotalPages()).isEqualTo(2);
      assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void 이메일_오름차순으로_정렬이_된다() {
      AdminSearchCondition condition = AdminSearchCondition.builder().build();
      PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "email"));

      Page<Admin> result = adminRepository.findAllByCondition(condition, pageable);

      assertThat(result.getContent().get(0).getEmail()).isEqualTo("admin1@test.com");
      assertThat(result.getContent().get(1).getEmail()).isEqualTo("manager1@test.com");
      assertThat(result.getContent().get(2).getEmail()).isEqualTo("manager2@test.com");
    }

    @Test
    void 역할_내림차순으로_정렬이_된다() {
      AdminSearchCondition condition = AdminSearchCondition.builder().build();
      PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "adminRole"));

      Page<Admin> result = adminRepository.findAllByCondition(condition, pageable);

      assertThat(result.getContent().get(0).getAdminRole()).isEqualTo(AdminRole.MANAGER);
    }
  }
}
