package com.tickatch.user_service.admin.infrastructure.messaging.publisher;

import com.tickatch.user_service.admin.application.messaging.AdminEventPublisher;
import com.tickatch.user_service.admin.domain.Admin;
import com.tickatch.user_service.common.domain.exception.UserErrorCode;
import com.tickatch.user_service.common.domain.exception.UserException;
import com.tickatch.user_service.common.infrastructure.messaging.event.UserStatusChangedEvent;
import io.github.tickatch.common.event.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 기반 관리자 이벤트 발행 구현체.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitAdminEventPublisher implements AdminEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Value("${spring.application.name:user-service}")
  private String serviceName;

  @Value("${messaging.exchange.user:tickatch.user}")
  private String userExchange;

  @Override
  public void publishWithdrawn(Admin admin) {
    publish(UserStatusChangedEvent.adminWithdrawn(admin.getId()), "탈퇴");
  }

  @Override
  public void publishSuspended(Admin admin) {
    publish(UserStatusChangedEvent.adminSuspended(admin.getId()), "정지");
  }

  @Override
  public void publishActivated(Admin admin) {
    publish(UserStatusChangedEvent.adminActivated(admin.getId()), "활성화");
  }

  private void publish(UserStatusChangedEvent event, String actionName) {
    log.info("관리자 {} 이벤트 발행 시작. adminId: {}", actionName, event.getUserId());

    try {
      IntegrationEvent integrationEvent = IntegrationEvent.from(event, serviceName);
      rabbitTemplate.convertAndSend(userExchange, event.getRoutingKey(), integrationEvent);

      log.info("관리자 {} 이벤트 발행 완료. adminId: {}, routingKey: {}",
          actionName, event.getUserId(), event.getRoutingKey());
    } catch (Exception e) {
      log.error("관리자 {} 이벤트 발행 실패. adminId: {}", actionName, event.getUserId(), e);
      throw new UserException(UserErrorCode.EVENT_PUBLISH_FAILED, e, event.getUserId());
    }
  }
}