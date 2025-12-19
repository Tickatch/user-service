package com.tickatch.user_service.customer.infrastructure.messaging.publisher;

import com.tickatch.user_service.common.infrastructure.messaging.config.RabbitMQConfig;
import com.tickatch.user_service.common.infrastructure.messaging.event.UserActionType;
import com.tickatch.user_service.common.infrastructure.messaging.event.UserLogEvent;
import com.tickatch.user_service.customer.application.messaging.CustomerLogEventPublisher;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 기반 고객 로그 이벤트 발행자.
 *
 * <p>고객 도메인에서 발생하는 주요 액션에 대한 로그 이벤트를 RabbitMQ를 통해 로그 서비스로 발행한다. 로그 발행 실패 시에도 비즈니스 로직에 영향을 주지 않도록
 * 예외를 던지지 않고 에러 로그로 기록한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitCustomerLogPublisher implements CustomerLogEventPublisher {

  private static final String USER_TYPE = "CUSTOMER";

  private final RabbitTemplate rabbitTemplate;

  @Value("${messaging.exchange.log:tickatch.log}")
  private String logExchange;

  @Override
  public void publishCreated(UUID customerId) {
    publish(customerId, UserActionType.CUSTOMER_CREATED);
    log.info("고객 생성 로그 발행. customerId: {}", customerId);
  }

  @Override
  public void publishCreateFailed() {
    publish(null, UserActionType.CUSTOMER_CREATE_FAILED);
    log.warn("고객 생성 실패 로그 발행.");
  }

  @Override
  public void publishUpdated(UUID customerId) {
    publish(customerId, UserActionType.CUSTOMER_UPDATED);
    log.info("고객 수정 로그 발행. customerId: {}", customerId);
  }

  @Override
  public void publishUpdateFailed(UUID customerId) {
    publish(customerId, UserActionType.CUSTOMER_UPDATE_FAILED);
    log.warn("고객 수정 실패 로그 발행. customerId: {}", customerId);
  }

  @Override
  public void publishWithdrawn(UUID customerId) {
    publish(customerId, UserActionType.CUSTOMER_WITHDRAWN);
    log.info("고객 탈퇴 로그 발행. customerId: {}", customerId);
  }

  @Override
  public void publishWithdrawFailed(UUID customerId) {
    publish(customerId, UserActionType.CUSTOMER_WITHDRAW_FAILED);
    log.warn("고객 탈퇴 실패 로그 발행. customerId: {}", customerId);
  }

  @Override
  public void publishSuspended(UUID customerId) {
    publish(customerId, UserActionType.CUSTOMER_SUSPENDED);
    log.info("고객 정지 로그 발행. customerId: {}", customerId);
  }

  @Override
  public void publishSuspendFailed(UUID customerId) {
    publish(customerId, UserActionType.CUSTOMER_SUSPEND_FAILED);
    log.warn("고객 정지 실패 로그 발행. customerId: {}", customerId);
  }

  @Override
  public void publishActivated(UUID customerId) {
    publish(customerId, UserActionType.CUSTOMER_ACTIVATED);
    log.info("고객 활성화 로그 발행. customerId: {}", customerId);
  }

  @Override
  public void publishActivateFailed(UUID customerId) {
    publish(customerId, UserActionType.CUSTOMER_ACTIVATE_FAILED);
    log.warn("고객 활성화 실패 로그 발행. customerId: {}", customerId);
  }

  private void publish(UUID customerId, String actionType) {
    try {
      UserLogEvent event = UserLogEvent.createSystemEvent(customerId, USER_TYPE, actionType);
      rabbitTemplate.convertAndSend(logExchange, RabbitMQConfig.ROUTING_KEY_USER_LOG, event);
      log.debug(
          "고객 로그 이벤트 발행 완료. eventId: {}, customerId: {}, actionType: {}",
          event.eventId(),
          customerId,
          actionType);
    } catch (Exception e) {
      log.error(
          "고객 로그 이벤트 발행 실패. customerId: {}, actionType: {}, error: {}",
          customerId,
          actionType,
          e.getMessage(),
          e);
    }
  }
}
