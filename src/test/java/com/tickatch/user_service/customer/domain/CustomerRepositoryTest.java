package com.tickatch.user_service.customer.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.config.QueryDslTestConfig;
import com.tickatch.user_service.customer.domain.repository.CustomerRepositoryImpl;
import com.tickatch.user_service.customer.domain.repository.dto.CustomerSearchCondition;
import com.tickatch.user_service.customer.domain.vo.CustomerGrade;
import java.time.LocalDate;
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
@Import({QueryDslTestConfig.class, CustomerRepositoryImpl.class})
@DisplayName("CustomerRepository 테스트")
class CustomerRepositoryTest {

  @Autowired private CustomerRepository customerRepository;

  private Customer customer1;
  private Customer customer2;
  private Customer customer3;

  @BeforeEach
  void setUp() {
    customer1 =
        Customer.create(
            UUID.randomUUID(), "hong@test.com", "홍길동", "01012345678", LocalDate.of(1990, 1, 1));

    customer2 =
        Customer.create(
            UUID.randomUUID(), "kim@test.com", "김철수", "01087654321", LocalDate.of(1985, 5, 15));

    customer3 =
        Customer.create(
            UUID.randomUUID(), "lee@test.com", "이영희", "01011112222", LocalDate.of(1995, 12, 25));
  }

  @Nested
  class Save_테스트 {

    @Test
    void Customer_정보를_저장한다() {
      Customer saved = customerRepository.save(customer1);

      assertThat(saved.getId()).isEqualTo(customer1.getId());
      assertThat(saved.getEmail()).isEqualTo("hong@test.com");
      assertThat(saved.getProfile().getName()).isEqualTo("홍길동");
      assertThat(saved.getGrade()).isEqualTo(CustomerGrade.NORMAL);
      assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void Customer_정보를_수정한다() {
      Customer saved = customerRepository.save(customer1);
      saved.updateProfile("홍길동수정", "01099998888");
      saved.upgradeGrade(CustomerGrade.VIP);

      Optional<Customer> found = customerRepository.findById(saved.getId());

      assertThat(found).isPresent();
      assertThat(found.get().getProfile().getName()).isEqualTo("홍길동수정");
      assertThat(found.get().getProfile().getPhone()).isEqualTo("01099998888");
      assertThat(found.get().getGrade()).isEqualTo(CustomerGrade.VIP);
    }
  }

  @Nested
  class FindById_테스트 {

    @Test
    void ID로_Customer_정보를_조회한다() {
      customerRepository.save(customer1);

      Optional<Customer> found = customerRepository.findById(customer1.getId());

      assertThat(found).isPresent();
      assertThat(found.get().getEmail()).isEqualTo("hong@test.com");
    }

    @Test
    void 존재하지_않는_ID_조회_시_empty를_반환한다() {
      Optional<Customer> found = customerRepository.findById(UUID.randomUUID());

      assertThat(found).isEmpty();
    }
  }

  @Nested
  class findByEmail_테스트 {

    @Test
    void 이메일로_Customer정보를_조회한다() {
      customerRepository.save(customer1);

      Optional<Customer> found = customerRepository.findByEmail("hong@test.com");

      assertThat(found).isPresent();
      assertThat(found.get().getProfile().getName()).isEqualTo("홍길동");
    }

    @Test
    void 존재하지_않는_이메일_조회_시_empty를_반환한다() {
      Optional<Customer> found = customerRepository.findByEmail("notfound@test.com");

      assertThat(found).isEmpty();
    }
  }

  @Nested
  class existsByEmail_테스트 {

    @Test
    void 이메일이_존재한다() {
      customerRepository.save(customer1);

      boolean exists = customerRepository.existsByEmail("hong@test.com");

      assertThat(exists).isTrue();
    }

    @Test
    void 이메일이_존재하지_않는다() {
      boolean exists = customerRepository.existsByEmail("notfound@test.com");

      assertThat(exists).isFalse();
    }
  }

  @Nested
  class findAllByCondition_테스트 {

    @BeforeEach
    void 데이터_초기화() {
      customerRepository.save(customer1);
      customerRepository.save(customer2);
      customer3.upgradeGrade(CustomerGrade.VIP);
      customerRepository.save(customer3);
    }

    @Test
    void 조건_없이_전체_조회한다() {
      CustomerSearchCondition condition = CustomerSearchCondition.builder().build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Customer> result = customerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void 이메일로_검색한다() {
      CustomerSearchCondition condition = CustomerSearchCondition.builder().email("hong").build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Customer> result = customerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getEmail()).isEqualTo("hong@test.com");
    }

    @Test
    void 이름으로_검색한다() {
      CustomerSearchCondition condition = CustomerSearchCondition.builder().name("김").build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Customer> result = customerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getProfile().getName()).isEqualTo("김철수");
    }

    @Test
    void 등급으로_검색한다() {
      CustomerSearchCondition condition =
          CustomerSearchCondition.builder().grade(CustomerGrade.VIP).build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Customer> result = customerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getProfile().getName()).isEqualTo("이영희");
    }

    @Test
    void 상태로_검색한다() {
      customer2.suspend();
      customerRepository.save(customer2);

      CustomerSearchCondition condition =
          CustomerSearchCondition.builder().status(UserStatus.SUSPENDED).build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Customer> result = customerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getProfile().getName()).isEqualTo("김철수");
    }

    @Test
    void 복합_조건으로_검색한다() {
      CustomerSearchCondition condition =
          CustomerSearchCondition.builder()
              .status(UserStatus.ACTIVE)
              .grade(CustomerGrade.NORMAL)
              .build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Customer> result = customerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void 페이징_기능이_동작한다() {
      CustomerSearchCondition condition = CustomerSearchCondition.builder().build();
      PageRequest pageable = PageRequest.of(0, 2);

      Page<Customer> result = customerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getTotalPages()).isEqualTo(2);
      assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void 정렬_기능이_동작한다() {
      CustomerSearchCondition condition = CustomerSearchCondition.builder().build();
      PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "email"));

      Page<Customer> result = customerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getContent().get(0).getEmail()).isEqualTo("hong@test.com");
      assertThat(result.getContent().get(1).getEmail()).isEqualTo("kim@test.com");
      assertThat(result.getContent().get(2).getEmail()).isEqualTo("lee@test.com");
    }
  }
}
