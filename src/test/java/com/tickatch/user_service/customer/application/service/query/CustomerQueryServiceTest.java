package com.tickatch.user_service.customer.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.config.QueryDslTestConfig;
import com.tickatch.user_service.customer.application.service.query.dto.CustomerResponse;
import com.tickatch.user_service.customer.application.service.query.dto.CustomerSearchRequest;
import com.tickatch.user_service.customer.domain.Customer;
import com.tickatch.user_service.customer.domain.CustomerRepository;
import com.tickatch.user_service.customer.domain.exception.CustomerErrorCode;
import com.tickatch.user_service.customer.domain.exception.CustomerException;
import com.tickatch.user_service.customer.domain.repository.CustomerRepositoryImpl;
import com.tickatch.user_service.customer.domain.vo.CustomerGrade;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
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
@Import({QueryDslTestConfig.class, CustomerRepositoryImpl.class, CustomerQueryService.class})
@DisplayName("CustomerQueryService 테스트")
class CustomerQueryServiceTest {

  @Autowired
  private CustomerQueryService customerQueryService;

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private EntityManager entityManager;

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @Nested
  @DisplayName("getCustomer 테스트")
  class GetCustomerTest {

    @Test
    @DisplayName("ID로 고객을 조회한다")
    void getCustomer_success() {
      // given
      Customer customer = Customer.create(
          UUID.randomUUID(), "test@example.com", "홍길동", "010-1234-5678", LocalDate.of(1990, 1, 1)
      );
      customerRepository.save(customer);
      flushAndClear();

      UUID customerId = customer.getId();

      // when
      CustomerResponse response = customerQueryService.getCustomer(customerId);

      // then
      assertThat(response.id()).isEqualTo(customerId);
      assertThat(response.email()).isEqualTo("test@example.com");
      assertThat(response.name()).isEqualTo("홍길동");
      assertThat(response.phone()).isEqualTo("01012345678");
      assertThat(response.birthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
      assertThat(response.grade()).isEqualTo(CustomerGrade.NORMAL);
      assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 ID면 예외가 발생한다")
    void getCustomer_notFound_throwsException() {
      // given
      UUID customerId = UUID.randomUUID();

      // when & then
      assertThatThrownBy(() -> customerQueryService.getCustomer(customerId))
          .isInstanceOf(CustomerException.class)
          .satisfies(ex -> {
            CustomerException customerException = (CustomerException) ex;
            assertThat(customerException.getErrorCode()).isEqualTo(CustomerErrorCode.CUSTOMER_NOT_FOUND);
          });
    }
  }

  @Nested
  @DisplayName("getCustomerByEmail 테스트")
  class GetCustomerByEmailTest {

    @Test
    @DisplayName("이메일로 고객을 조회한다")
    void getCustomerByEmail_success() {
      // given
      String email = "test@example.com";
      Customer customer = Customer.create(
          UUID.randomUUID(), email, "홍길동", "010-1234-5678", null
      );
      customerRepository.save(customer);
      flushAndClear();

      // when
      CustomerResponse response = customerQueryService.getCustomerByEmail(email);

      // then
      assertThat(response.email()).isEqualTo(email);
      assertThat(response.name()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 예외가 발생한다")
    void getCustomerByEmail_notFound_throwsException() {
      // given
      String email = "notfound@example.com";

      // when & then
      assertThatThrownBy(() -> customerQueryService.getCustomerByEmail(email))
          .isInstanceOf(CustomerException.class)
          .satisfies(ex -> {
            CustomerException customerException = (CustomerException) ex;
            assertThat(customerException.getErrorCode()).isEqualTo(CustomerErrorCode.CUSTOMER_NOT_FOUND);
          });
    }
  }

  @Nested
  @DisplayName("searchCustomers 테스트")
  class SearchCustomersTest {

    @Test
    @DisplayName("조건으로 고객을 검색한다")
    void searchCustomers_success() {
      // given
      Customer customer1 = Customer.create(UUID.randomUUID(), "hong1@example.com", "홍길동", "010-1111-1111", null);
      Customer customer2 = Customer.create(UUID.randomUUID(), "hong2@example.com", "홍길순", "010-2222-2222", null);
      Customer customer3 = Customer.create(UUID.randomUUID(), "kim@example.com", "김철수", "010-3333-3333", null);
      customerRepository.save(customer1);
      customerRepository.save(customer2);
      customerRepository.save(customer3);
      flushAndClear();

      CustomerSearchRequest request = new CustomerSearchRequest(
          null, "홍", null, UserStatus.ACTIVE, null
      );
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<CustomerResponse> result = customerQueryService.searchCustomers(request, pageable);

      // then
      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getContent()).extracting(CustomerResponse::name)
          .containsExactlyInAnyOrder("홍길동", "홍길순");
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 페이지를 반환한다")
    void searchCustomers_empty() {
      // given
      Customer customer = Customer.create(UUID.randomUUID(), "test@example.com", "홍길동", "010-1234-5678", null);
      customerRepository.save(customer);
      flushAndClear();

      CustomerSearchRequest request = new CustomerSearchRequest(
          null, null, null, null, CustomerGrade.VIP
      );
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<CustomerResponse> result = customerQueryService.searchCustomers(request, pageable);

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
      Customer customer = Customer.create(UUID.randomUUID(), email, "홍길동", "010-1234-5678", null);
      customerRepository.save(customer);
      flushAndClear();

      // when
      boolean result = customerQueryService.existsByEmail(email);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 false를 반환한다")
    void existsByEmail_false() {
      // given
      String email = "notexists@example.com";

      // when
      boolean result = customerQueryService.existsByEmail(email);

      // then
      assertThat(result).isFalse();
    }
  }
}