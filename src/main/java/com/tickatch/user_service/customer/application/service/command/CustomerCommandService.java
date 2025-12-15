package com.tickatch.user_service.customer.application.service.command;

import com.tickatch.user_service.customer.application.service.command.dto.CreateCustomerCommand;
import com.tickatch.user_service.customer.application.service.command.dto.UpdateCustomerProfileCommand;
import com.tickatch.user_service.customer.domain.Customer;
import com.tickatch.user_service.customer.domain.CustomerRepository;
import com.tickatch.user_service.customer.domain.exception.CustomerErrorCode;
import com.tickatch.user_service.customer.domain.exception.CustomerException;
import com.tickatch.user_service.customer.domain.vo.CustomerGrade;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고객 커맨드 서비스.
 *
 * <p>고객 생성, 수정, 상태 변경 등 상태를 변경하는 작업을 처리한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerCommandService {

  private final CustomerRepository customerRepository;

  /**
   * 고객을 생성한다.
   *
   * @param command 생성 커맨드
   * @return 생성된 고객 ID
   * @throws CustomerException 이미 존재하는 이메일인 경우
   */
  public UUID createCustomer(CreateCustomerCommand command) {
    if (customerRepository.existsByEmail(command.email())) {
      throw new CustomerException(CustomerErrorCode.CUSTOMER_ALREADY_EXISTS);
    }

    Customer customer = Customer.create(
        command.authId(),
        command.email(),
        command.name(),
        command.phone(),
        command.birthDate()
    );

    return customerRepository.save(customer).getId();
  }

  /**
   * 고객 프로필을 수정한다.
   *
   * @param command 프로필 수정 커맨드
   * @throws CustomerException 고객을 찾을 수 없는 경우
   */
  public void updateProfile(UpdateCustomerProfileCommand command) {
    Customer customer = findCustomerById(command.customerId());
    customer.updateProfile(command.name(), command.phone());

    if (command.birthDate() != null) {
      customer.updateBirthDate(command.birthDate());
    }
  }

  /**
   * 고객 등급을 변경한다.
   *
   * @param customerId 고객 ID
   * @param newGrade 새 등급
   * @throws CustomerException 고객을 찾을 수 없거나 등급 변경이 불가능한 경우
   */
  public void changeGrade(UUID customerId, CustomerGrade newGrade) {
    Customer customer = findCustomerById(customerId);
    customer.upgradeGrade(newGrade);
  }

  /**
   * 고객을 정지한다.
   *
   * @param customerId 고객 ID
   * @throws CustomerException 고객을 찾을 수 없거나 정지할 수 없는 상태인 경우
   */
  public void suspendCustomer(UUID customerId) {
    Customer customer = findCustomerById(customerId);
    customer.suspend();
  }

  /**
   * 고객 정지를 해제한다.
   *
   * @param customerId 고객 ID
   * @throws CustomerException 고객을 찾을 수 없거나 활성화할 수 없는 상태인 경우
   */
  public void activateCustomer(UUID customerId) {
    Customer customer = findCustomerById(customerId);
    customer.activate();
  }

  /**
   * 고객을 탈퇴 처리한다.
   *
   * @param customerId 고객 ID
   * @throws CustomerException 고객을 찾을 수 없거나 탈퇴할 수 없는 상태인 경우
   */
  public void withdrawCustomer(UUID customerId) {
    Customer customer = findCustomerById(customerId);
    customer.withdraw();
  }

  private Customer findCustomerById(UUID customerId) {
    return customerRepository.findById(customerId)
        .orElseThrow(() -> new CustomerException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
  }
}