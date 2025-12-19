package com.tickatch.user_service.customer.application.messaging;

import com.tickatch.user_service.customer.domain.Customer;

/**
 * 고객 도메인 이벤트 발행 인터페이스.
 *
 * <p>Application 레이어에서 정의하고, Infrastructure 레이어에서 구현한다. 이를 통해 Application은 메시징 기술(RabbitMQ, Kafka
 * 등)에 의존하지 않는다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public interface CustomerEventPublisher {

  /**
   * 고객 탈퇴 이벤트를 발행한다.
   *
   * <p>Auth Service에서 해당 인증 정보를 WITHDRAWN 상태로 변경한다.
   *
   * @param customer 탈퇴한 고객 엔티티
   */
  void publishWithdrawn(Customer customer);

  /**
   * 고객 정지 이벤트를 발행한다.
   *
   * <p>Auth Service에서 토큰을 무효화하고 로그인을 차단한다.
   *
   * @param customer 정지된 고객 엔티티
   */
  void publishSuspended(Customer customer);

  /**
   * 고객 활성화 이벤트를 발행한다.
   *
   * <p>Auth Service에서 로그인 차단을 해제한다.
   *
   * @param customer 활성화된 고객 엔티티
   */
  void publishActivated(Customer customer);
}
