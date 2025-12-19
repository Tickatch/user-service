package com.tickatch.user_service.customer.application.messaging;

import java.util.UUID;

/**
 * 고객 로그 이벤트 발행 인터페이스.
 *
 * <p>Application 레이어에서 정의하고, Infrastructure 레이어에서 구현한다. 이를 통해 Application은 메시징 기술(RabbitMQ, Kafka
 * 등)에 의존하지 않는다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public interface CustomerLogEventPublisher {

  /**
   * 고객 생성 성공 로그를 발행한다.
   *
   * @param customerId 생성된 고객 ID
   */
  void publishCreated(UUID customerId);

  /** 고객 생성 실패 로그를 발행한다. */
  void publishCreateFailed();

  /**
   * 고객 정보 수정 성공 로그를 발행한다.
   *
   * @param customerId 수정된 고객 ID
   */
  void publishUpdated(UUID customerId);

  /**
   * 고객 정보 수정 실패 로그를 발행한다.
   *
   * @param customerId 고객 ID
   */
  void publishUpdateFailed(UUID customerId);

  /**
   * 고객 탈퇴 성공 로그를 발행한다.
   *
   * @param customerId 탈퇴한 고객 ID
   */
  void publishWithdrawn(UUID customerId);

  /**
   * 고객 탈퇴 실패 로그를 발행한다.
   *
   * @param customerId 고객 ID
   */
  void publishWithdrawFailed(UUID customerId);

  /**
   * 고객 정지 성공 로그를 발행한다.
   *
   * @param customerId 정지된 고객 ID
   */
  void publishSuspended(UUID customerId);

  /**
   * 고객 정지 실패 로그를 발행한다.
   *
   * @param customerId 고객 ID
   */
  void publishSuspendFailed(UUID customerId);

  /**
   * 고객 활성화 성공 로그를 발행한다.
   *
   * @param customerId 활성화된 고객 ID
   */
  void publishActivated(UUID customerId);

  /**
   * 고객 활성화 실패 로그를 발행한다.
   *
   * @param customerId 고객 ID
   */
  void publishActivateFailed(UUID customerId);
}
