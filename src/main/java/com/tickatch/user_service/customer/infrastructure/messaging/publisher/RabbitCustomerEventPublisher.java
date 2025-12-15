package com.tickatch.user_service.customer.infrastructure.messaging.publisher;

import com.tickatch.user_service.common.domain.exception.UserErrorCode;
import com.tickatch.user_service.common.domain.exception.UserException;
import com.tickatch.user_service.common.infrastructure.messaging.event.UserStatusChangedEvent;
import com.tickatch.user_service.customer.application.messaging.CustomerEventPublisher;
import com.tickatch.user_service.customer.domain.Customer;
import io.github.tickatch.common.event.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 기반 고객 이벤트 발행 구현체.
 *
 * <p>고객 도메인에서 발생하는 상태 변경 이벤트를 RabbitMQ를 통해 Auth Service로 발행한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitCustomerEventPublisher implements CustomerEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Value("${spring.application.name:user-service}")
  private String serviceName;

  @Value("${messaging.exchange.user:tickatch.user}")
  private String userExchange;

  @Override
  public void publishWithdrawn(Customer customer) {
    publish(UserStatusChangedEvent.customerWithdrawn(customer.getId()), "탈퇴");
  }

  @Override
  public void publishSuspended(Customer customer) {
    publish(UserStatusChangedEvent.customerSuspended(customer.getId()), "정지");
  }

  @Override
  public void publishActivated(Customer customer) {
    publish(UserStatusChangedEvent.customerActivated(customer.getId()), "활성화");
  }

  private void publish(UserStatusChangedEvent event, String actionName) {
    log.info("고객 {} 이벤트 발행 시작. customerId: {}", actionName, event.getUserId());

    try {
      IntegrationEvent integrationEvent = IntegrationEvent.from(event, serviceName);
      rabbitTemplate.convertAndSend(userExchange, event.getRoutingKey(), integrationEvent);

      log.info("고객 {} 이벤트 발행 완료. customerId: {}, routingKey: {}",
          actionName, event.getUserId(), event.getRoutingKey());
    } catch (Exception e) {
      log.error("고객 {} 이벤트 발행 실패. customerId: {}", actionName, event.getUserId(), e);
      throw new UserException(UserErrorCode.EVENT_PUBLISH_FAILED, e, event.getUserId());
    }
  }
}