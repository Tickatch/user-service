package com.tickatch.user_service.customer.application.service.command;

import com.tickatch.user_service.customer.application.messaging.CustomerLogEventPublisher;
import com.tickatch.user_service.customer.application.service.command.dto.CreateCustomerCommand;
import com.tickatch.user_service.customer.application.service.command.dto.UpdateCustomerProfileCommand;
import com.tickatch.user_service.customer.domain.Customer;
import com.tickatch.user_service.customer.domain.CustomerRepository;
import com.tickatch.user_service.customer.domain.exception.CustomerErrorCode;
import com.tickatch.user_service.customer.domain.exception.CustomerException;
import com.tickatch.user_service.customer.domain.vo.CustomerGrade;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고객 커맨드 서비스.
 *
 * <p>고객 생성, 수정, 상태 변경 등 상태를 변경하는 작업을 처리한다. 모든 주요 작업에 대해 성공/실패 로그를 로그 서비스로 발행한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerCommandService {

  private final CustomerRepository customerRepository;
  private final CustomerLogEventPublisher logEventPublisher;

  /**
   * 고객을 생성한다.
   *
   * <p>성공 시 CUSTOMER_CREATED 로그를, 실패 시 CUSTOMER_CREATE_FAILED 로그를 발행한다.
   *
   * @param command 생성 커맨드
   * @return 생성된 고객 ID
   * @throws CustomerException 이미 존재하는 이메일인 경우
   */
  public UUID createCustomer(CreateCustomerCommand command) {
    try {
      if (customerRepository.existsByEmail(command.email())) {
        throw new CustomerException(CustomerErrorCode.CUSTOMER_ALREADY_EXISTS);
      }

      Customer customer =
          Customer.create(
              command.authId(),
              command.email(),
              command.name(),
              command.phone(),
              command.birthDate());

      UUID customerId = customerRepository.save(customer).getId();
      log.info("고객 생성 완료. customerId: {}", customerId);

      logEventPublisher.publishCreated(customerId);
      return customerId;
    } catch (Exception e) {
      logEventPublisher.publishCreateFailed();
      log.error("고객 생성 실패. email: {}, error: {}", command.email(), e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 고객 프로필을 수정한다.
   *
   * <p>성공 시 CUSTOMER_UPDATED 로그를, 실패 시 CUSTOMER_UPDATE_FAILED 로그를 발행한다.
   *
   * @param command 프로필 수정 커맨드
   * @throws CustomerException 고객을 찾을 수 없는 경우
   */
  public void updateProfile(UpdateCustomerProfileCommand command) {
    try {
      Customer customer = findCustomerById(command.customerId());
      customer.updateProfile(command.name(), command.phone());

      if (command.birthDate() != null) {
        customer.updateBirthDate(command.birthDate());
      }
      log.info("고객 프로필 수정 완료. customerId: {}", command.customerId());

      logEventPublisher.publishUpdated(command.customerId());
    } catch (Exception e) {
      logEventPublisher.publishUpdateFailed(command.customerId());
      log.error("고객 프로필 수정 실패. customerId: {}, error: {}", command.customerId(), e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 고객 등급을 변경한다.
   *
   * <p>성공 시 CUSTOMER_UPDATED 로그를, 실패 시 CUSTOMER_UPDATE_FAILED 로그를 발행한다.
   *
   * @param customerId 고객 ID
   * @param newGrade 새 등급
   * @throws CustomerException 고객을 찾을 수 없거나 등급 변경이 불가능한 경우
   */
  public void changeGrade(UUID customerId, CustomerGrade newGrade) {
    try {
      Customer customer = findCustomerById(customerId);
      customer.upgradeGrade(newGrade);
      log.info("고객 등급 변경 완료. customerId: {}, newGrade: {}", customerId, newGrade);

      logEventPublisher.publishUpdated(customerId);
    } catch (Exception e) {
      logEventPublisher.publishUpdateFailed(customerId);
      log.error(
          "고객 등급 변경 실패. customerId: {}, newGrade: {}, error: {}",
          customerId,
          newGrade,
          e.getMessage(),
          e);
      throw e;
    }
  }

  /**
   * 고객을 정지한다.
   *
   * <p>성공 시 CUSTOMER_SUSPENDED 로그를, 실패 시 CUSTOMER_SUSPEND_FAILED 로그를 발행한다.
   *
   * @param customerId 고객 ID
   * @throws CustomerException 고객을 찾을 수 없거나 정지할 수 없는 상태인 경우
   */
  public void suspendCustomer(UUID customerId) {
    try {
      Customer customer = findCustomerById(customerId);
      customer.suspend();
      log.info("고객 정지 완료. customerId: {}", customerId);

      logEventPublisher.publishSuspended(customerId);
    } catch (Exception e) {
      logEventPublisher.publishSuspendFailed(customerId);
      log.error("고객 정지 실패. customerId: {}, error: {}", customerId, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 고객 정지를 해제한다.
   *
   * <p>성공 시 CUSTOMER_ACTIVATED 로그를, 실패 시 CUSTOMER_ACTIVATE_FAILED 로그를 발행한다.
   *
   * @param customerId 고객 ID
   * @throws CustomerException 고객을 찾을 수 없거나 활성화할 수 없는 상태인 경우
   */
  public void activateCustomer(UUID customerId) {
    try {
      Customer customer = findCustomerById(customerId);
      customer.activate();
      log.info("고객 활성화 완료. customerId: {}", customerId);

      logEventPublisher.publishActivated(customerId);
    } catch (Exception e) {
      logEventPublisher.publishActivateFailed(customerId);
      log.error("고객 활성화 실패. customerId: {}, error: {}", customerId, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 고객을 탈퇴 처리한다.
   *
   * <p>성공 시 CUSTOMER_WITHDRAWN 로그를, 실패 시 CUSTOMER_WITHDRAW_FAILED 로그를 발행한다.
   *
   * @param customerId 고객 ID
   * @throws CustomerException 고객을 찾을 수 없거나 탈퇴할 수 없는 상태인 경우
   */
  public void withdrawCustomer(UUID customerId) {
    try {
      Customer customer = findCustomerById(customerId);
      customer.withdraw();
      log.info("고객 탈퇴 완료. customerId: {}", customerId);

      logEventPublisher.publishWithdrawn(customerId);
    } catch (Exception e) {
      logEventPublisher.publishWithdrawFailed(customerId);
      log.error("고객 탈퇴 실패. customerId: {}, error: {}", customerId, e.getMessage(), e);
      throw e;
    }
  }

  private Customer findCustomerById(UUID customerId) {
    return customerRepository
        .findById(customerId)
        .orElseThrow(() -> new CustomerException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
  }
}
