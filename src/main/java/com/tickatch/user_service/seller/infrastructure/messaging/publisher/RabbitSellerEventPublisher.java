package com.tickatch.user_service.seller.infrastructure.messaging.publisher;

import com.tickatch.user_service.common.domain.exception.UserErrorCode;
import com.tickatch.user_service.common.domain.exception.UserException;
import com.tickatch.user_service.common.infrastructure.messaging.event.UserStatusChangedEvent;
import com.tickatch.user_service.seller.application.messaging.SellerEventPublisher;
import com.tickatch.user_service.seller.domain.Seller;
import io.github.tickatch.common.event.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 기반 판매자 이벤트 발행 구현체.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitSellerEventPublisher implements SellerEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Value("${spring.application.name:user-service}")
  private String serviceName;

  @Value("${messaging.exchange.user:tickatch.user}")
  private String userExchange;

  @Override
  public void publishWithdrawn(Seller seller) {
    publish(UserStatusChangedEvent.sellerWithdrawn(seller.getId()), "탈퇴");
  }

  @Override
  public void publishSuspended(Seller seller) {
    publish(UserStatusChangedEvent.sellerSuspended(seller.getId()), "정지");
  }

  @Override
  public void publishActivated(Seller seller) {
    publish(UserStatusChangedEvent.sellerActivated(seller.getId()), "활성화");
  }

  private void publish(UserStatusChangedEvent event, String actionName) {
    log.info("판매자 {} 이벤트 발행 시작. sellerId: {}", actionName, event.getUserId());

    try {
      IntegrationEvent integrationEvent = IntegrationEvent.from(event, serviceName);
      rabbitTemplate.convertAndSend(userExchange, event.getRoutingKey(), integrationEvent);

      log.info("판매자 {} 이벤트 발행 완료. sellerId: {}, routingKey: {}",
          actionName, event.getUserId(), event.getRoutingKey());
    } catch (Exception e) {
      log.error("판매자 {} 이벤트 발행 실패. sellerId: {}", actionName, event.getUserId(), e);
      throw new UserException(UserErrorCode.EVENT_PUBLISH_FAILED, e, event.getUserId());
    }
  }
}