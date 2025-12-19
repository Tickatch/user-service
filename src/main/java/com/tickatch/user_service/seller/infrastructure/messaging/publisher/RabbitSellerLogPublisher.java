package com.tickatch.user_service.seller.infrastructure.messaging.publisher;

import com.tickatch.user_service.common.infrastructure.messaging.config.RabbitMQConfig;
import com.tickatch.user_service.common.infrastructure.messaging.event.UserActionType;
import com.tickatch.user_service.common.infrastructure.messaging.event.UserLogEvent;
import com.tickatch.user_service.seller.application.messaging.SellerLogEventPublisher;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 기반 판매자 로그 이벤트 발행자.
 *
 * <p>판매자 도메인에서 발생하는 주요 액션에 대한 로그 이벤트를 RabbitMQ를 통해 로그 서비스로 발행한다. 로그 발행 실패 시에도 비즈니스 로직에 영향을 주지 않도록
 * 예외를 던지지 않고 에러 로그로 기록한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitSellerLogPublisher implements SellerLogEventPublisher {

  private static final String USER_TYPE = "SELLER";

  private final RabbitTemplate rabbitTemplate;

  @Value("${messaging.exchange.log:tickatch.log}")
  private String logExchange;

  @Override
  public void publishCreated(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_CREATED);
    log.info("판매자 생성 로그 발행. sellerId: {}", sellerId);
  }

  @Override
  public void publishCreateFailed() {
    publish(null, UserActionType.SELLER_CREATE_FAILED);
    log.warn("판매자 생성 실패 로그 발행.");
  }

  @Override
  public void publishUpdated(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_UPDATED);
    log.info("판매자 수정 로그 발행. sellerId: {}", sellerId);
  }

  @Override
  public void publishUpdateFailed(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_UPDATE_FAILED);
    log.warn("판매자 수정 실패 로그 발행. sellerId: {}", sellerId);
  }

  @Override
  public void publishWithdrawn(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_WITHDRAWN);
    log.info("판매자 탈퇴 로그 발행. sellerId: {}", sellerId);
  }

  @Override
  public void publishWithdrawFailed(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_WITHDRAW_FAILED);
    log.warn("판매자 탈퇴 실패 로그 발행. sellerId: {}", sellerId);
  }

  @Override
  public void publishSuspended(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_SUSPENDED);
    log.info("판매자 정지 로그 발행. sellerId: {}", sellerId);
  }

  @Override
  public void publishSuspendFailed(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_SUSPEND_FAILED);
    log.warn("판매자 정지 실패 로그 발행. sellerId: {}", sellerId);
  }

  @Override
  public void publishActivated(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_ACTIVATED);
    log.info("판매자 활성화 로그 발행. sellerId: {}", sellerId);
  }

  @Override
  public void publishActivateFailed(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_ACTIVATE_FAILED);
    log.warn("판매자 활성화 실패 로그 발행. sellerId: {}", sellerId);
  }

  @Override
  public void publishApproved(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_APPROVED);
    log.info("판매자 승인 로그 발행. sellerId: {}", sellerId);
  }

  @Override
  public void publishApproveFailed(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_APPROVE_FAILED);
    log.warn("판매자 승인 실패 로그 발행. sellerId: {}", sellerId);
  }

  @Override
  public void publishRejected(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_REJECTED);
    log.info("판매자 반려 로그 발행. sellerId: {}", sellerId);
  }

  @Override
  public void publishRejectFailed(UUID sellerId) {
    publish(sellerId, UserActionType.SELLER_REJECT_FAILED);
    log.warn("판매자 반려 실패 로그 발행. sellerId: {}", sellerId);
  }

  private void publish(UUID sellerId, String actionType) {
    try {
      UserLogEvent event = UserLogEvent.createSystemEvent(sellerId, USER_TYPE, actionType);
      rabbitTemplate.convertAndSend(logExchange, RabbitMQConfig.ROUTING_KEY_USER_LOG, event);
      log.debug(
          "판매자 로그 이벤트 발행 완료. eventId: {}, sellerId: {}, actionType: {}",
          event.eventId(),
          sellerId,
          actionType);
    } catch (Exception e) {
      log.error(
          "판매자 로그 이벤트 발행 실패. sellerId: {}, actionType: {}, error: {}",
          sellerId,
          actionType,
          e.getMessage(),
          e);
    }
  }
}
