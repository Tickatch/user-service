package com.tickatch.user_service.customer.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.config.QueryDslTestConfig;
import com.tickatch.user_service.customer.application.service.command.dto.CreateCustomerCommand;
import com.tickatch.user_service.customer.application.service.command.dto.UpdateCustomerProfileCommand;
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

@DataJpaTest
@Import({QueryDslTestConfig.class, CustomerRepositoryImpl.class, CustomerCommandService.class})
@DisplayName("CustomerCommandService 테스트")
class CustomerCommandServiceTest {

  @Autowired
  private CustomerCommandService customerCommandService;

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private EntityManager entityManager;

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @Nested
  @DisplayName("createCustomer 테스트")
  class CreateCustomerTest {

    @Test
    @DisplayName("고객을 생성한다")
    void createCustomer_success() {
      // given
      UUID authId = UUID.randomUUID();
      CreateCustomerCommand command = CreateCustomerCommand.of(
          authId, "test@example.com", "홍길동", "010-1234-5678", LocalDate.of(1990, 1, 1)
      );

      // when
      UUID customerId = customerCommandService.createCustomer(command);
      flushAndClear();

      // then
      Customer saved = customerRepository.findById(customerId).orElseThrow();
      assertThat(saved.getEmail()).isEqualTo("test@example.com");
      assertThat(saved.getProfile().getName()).isEqualTo("홍길동");
      assertThat(saved.getProfile().getPhone()).isEqualTo("01012345678");
      assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
      assertThat(saved.getGrade()).isEqualTo(CustomerGrade.NORMAL);
    }

    @Test
    @DisplayName("이미 존재하는 이메일이면 예외가 발생한다")
    void createCustomer_duplicateEmail_throwsException() {
      // given
      Customer existing = Customer.create(
          UUID.randomUUID(), "test@example.com", "기존고객", "010-0000-0000", null
      );
      customerRepository.save(existing);
      flushAndClear();

      CreateCustomerCommand command = CreateCustomerCommand.of(
          UUID.randomUUID(), "test@example.com", "홍길동", "010-1234-5678", null
      );

      // when & then
      assertThatThrownBy(() -> customerCommandService.createCustomer(command))
          .isInstanceOf(CustomerException.class)
          .satisfies(ex -> {
            CustomerException customerException = (CustomerException) ex;
            assertThat(customerException.getErrorCode()).isEqualTo(CustomerErrorCode.CUSTOMER_ALREADY_EXISTS);
          });
    }
  }

  @Nested
  @DisplayName("updateProfile 테스트")
  class UpdateProfileTest {

    @Test
    @DisplayName("고객 프로필을 수정한다")
    void updateProfile_success() {
      // given
      Customer customer = Customer.create(
          UUID.randomUUID(), "test@example.com", "홍길동", "010-1234-5678", null
      );
      customerRepository.save(customer);
      flushAndClear();

      UUID customerId = customer.getId();
      UpdateCustomerProfileCommand command = UpdateCustomerProfileCommand.of(
          customerId, "김철수", "010-9999-8888", LocalDate.of(1995, 5, 5)
      );

      // when
      customerCommandService.updateProfile(command);
      flushAndClear();

      // then
      Customer updated = customerRepository.findById(customerId).orElseThrow();
      assertThat(updated.getProfile().getName()).isEqualTo("김철수");
      assertThat(updated.getProfile().getPhone()).isEqualTo("01099998888");
      assertThat(updated.getBirthDate()).isEqualTo(LocalDate.of(1995, 5, 5));
    }

    @Test
    @DisplayName("존재하지 않는 고객이면 예외가 발생한다")
    void updateProfile_notFound_throwsException() {
      // given
      UUID customerId = UUID.randomUUID();
      UpdateCustomerProfileCommand command = UpdateCustomerProfileCommand.of(
          customerId, "김철수", "010-9999-8888", null
      );

      // when & then
      assertThatThrownBy(() -> customerCommandService.updateProfile(command))
          .isInstanceOf(CustomerException.class)
          .satisfies(ex -> {
            CustomerException customerException = (CustomerException) ex;
            assertThat(customerException.getErrorCode()).isEqualTo(CustomerErrorCode.CUSTOMER_NOT_FOUND);
          });
    }
  }

  @Nested
  @DisplayName("changeGrade 테스트")
  class ChangeGradeTest {

    @Test
    @DisplayName("고객 등급을 변경한다")
    void changeGrade_success() {
      // given
      Customer customer = Customer.create(
          UUID.randomUUID(), "test@example.com", "홍길동", "010-1234-5678", null
      );
      customerRepository.save(customer);
      flushAndClear();

      UUID customerId = customer.getId();

      // when
      customerCommandService.changeGrade(customerId, CustomerGrade.VIP);
      flushAndClear();

      // then
      Customer updated = customerRepository.findById(customerId).orElseThrow();
      assertThat(updated.getGrade()).isEqualTo(CustomerGrade.VIP);
    }

    @Test
    @DisplayName("등급 하향은 예외가 발생한다")
    void changeGrade_downgrade_throwsException() {
      // given
      Customer customer = Customer.create(
          UUID.randomUUID(), "test@example.com", "홍길동", "010-1234-5678", null
      );
      customer.upgradeGrade(CustomerGrade.VIP);
      customerRepository.save(customer);
      flushAndClear();

      UUID customerId = customer.getId();

      // when & then
      assertThatThrownBy(() -> customerCommandService.changeGrade(customerId, CustomerGrade.NORMAL))
          .isInstanceOf(CustomerException.class)
          .satisfies(ex -> {
            CustomerException customerException = (CustomerException) ex;
            assertThat(customerException.getErrorCode()).isEqualTo(CustomerErrorCode.GRADE_DOWNGRADE_NOT_ALLOWED);
          });
    }
  }

  @Nested
  @DisplayName("suspendCustomer 테스트")
  class SuspendCustomerTest {

    @Test
    @DisplayName("고객을 정지한다")
    void suspendCustomer_success() {
      // given
      Customer customer = Customer.create(
          UUID.randomUUID(), "test@example.com", "홍길동", "010-1234-5678", null
      );
      customerRepository.save(customer);
      flushAndClear();

      UUID customerId = customer.getId();

      // when
      customerCommandService.suspendCustomer(customerId);
      flushAndClear();

      // then
      Customer updated = customerRepository.findById(customerId).orElseThrow();
      assertThat(updated.isSuspended()).isTrue();
    }
  }

  @Nested
  @DisplayName("activateCustomer 테스트")
  class ActivateCustomerTest {

    @Test
    @DisplayName("정지된 고객을 활성화한다")
    void activateCustomer_success() {
      // given
      Customer customer = Customer.create(
          UUID.randomUUID(), "test@example.com", "홍길동", "010-1234-5678", null
      );
      customer.suspend();
      customerRepository.save(customer);
      flushAndClear();

      UUID customerId = customer.getId();

      // when
      customerCommandService.activateCustomer(customerId);
      flushAndClear();

      // then
      Customer updated = customerRepository.findById(customerId).orElseThrow();
      assertThat(updated.isActive()).isTrue();
    }
  }

  @Nested
  @DisplayName("withdrawCustomer 테스트")
  class WithdrawCustomerTest {

    @Test
    @DisplayName("고객을 탈퇴 처리한다")
    void withdrawCustomer_success() {
      // given
      Customer customer = Customer.create(
          UUID.randomUUID(), "test@example.com", "홍길동", "010-1234-5678", null
      );
      customerRepository.save(customer);
      flushAndClear();

      UUID customerId = customer.getId();

      // when
      customerCommandService.withdrawCustomer(customerId);
      flushAndClear();

      // then
      Customer updated = customerRepository.findById(customerId).orElseThrow();
      assertThat(updated.isWithdrawn()).isTrue();
    }
  }
}