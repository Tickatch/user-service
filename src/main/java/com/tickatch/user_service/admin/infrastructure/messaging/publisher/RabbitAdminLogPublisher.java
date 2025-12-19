package com.tickatch.user_service.admin.infrastructure.messaging.publisher;

import com.tickatch.user_service.admin.application.messaging.AdminLogEventPublisher;
import com.tickatch.user_service.common.infrastructure.messaging.config.RabbitMQConfig;
import com.tickatch.user_service.common.infrastructure.messaging.event.UserActionType;
import com.tickatch.user_service.common.infrastructure.messaging.event.UserLogEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 기반 관리자 로그 이벤트 발행자.
 *
 * <p>관리자 도메인에서 발생하는 주요 액션에 대한 로그 이벤트를 RabbitMQ를 통해 로그 서비스로 발행한다. 로그 발행 실패 시에도 비즈니스 로직에 영향을 주지 않도록
 * 예외를 던지지 않고 에러 로그로 기록한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitAdminLogPublisher implements AdminLogEventPublisher {

  private static final String USER_TYPE = "ADMIN";

  private final RabbitTemplate rabbitTemplate;

  @Value("${messaging.exchange.log:tickatch.log}")
  private String logExchange;

  @Override
  public void publishCreated(UUID adminId) {
    publish(adminId, UserActionType.ADMIN_CREATED);
    log.info("관리자 생성 로그 발행. adminId: {}", adminId);
  }

  @Override
  public void publishCreateFailed() {
    publish(null, UserActionType.ADMIN_CREATE_FAILED);
    log.warn("관리자 생성 실패 로그 발행.");
  }

  @Override
  public void publishUpdated(UUID adminId) {
    publish(adminId, UserActionType.ADMIN_UPDATED);
    log.info("관리자 수정 로그 발행. adminId: {}", adminId);
  }

  @Override
  public void publishUpdateFailed(UUID adminId) {
    publish(adminId, UserActionType.ADMIN_UPDATE_FAILED);
    log.warn("관리자 수정 실패 로그 발행. adminId: {}", adminId);
  }

  @Override
  public void publishWithdrawn(UUID adminId) {
    publish(adminId, UserActionType.ADMIN_WITHDRAWN);
    log.info("관리자 탈퇴 로그 발행. adminId: {}", adminId);
  }

  @Override
  public void publishWithdrawFailed(UUID adminId) {
    publish(adminId, UserActionType.ADMIN_WITHDRAW_FAILED);
    log.warn("관리자 탈퇴 실패 로그 발행. adminId: {}", adminId);
  }

  @Override
  public void publishSuspended(UUID adminId) {
    publish(adminId, UserActionType.ADMIN_SUSPENDED);
    log.info("관리자 정지 로그 발행. adminId: {}", adminId);
  }

  @Override
  public void publishSuspendFailed(UUID adminId) {
    publish(adminId, UserActionType.ADMIN_SUSPEND_FAILED);
    log.warn("관리자 정지 실패 로그 발행. adminId: {}", adminId);
  }

  @Override
  public void publishActivated(UUID adminId) {
    publish(adminId, UserActionType.ADMIN_ACTIVATED);
    log.info("관리자 활성화 로그 발행. adminId: {}", adminId);
  }

  @Override
  public void publishActivateFailed(UUID adminId) {
    publish(adminId, UserActionType.ADMIN_ACTIVATE_FAILED);
    log.warn("관리자 활성화 실패 로그 발행. adminId: {}", adminId);
  }

  private void publish(UUID adminId, String actionType) {
    try {
      UserLogEvent event = UserLogEvent.createSystemEvent(adminId, USER_TYPE, actionType);
      rabbitTemplate.convertAndSend(logExchange, RabbitMQConfig.ROUTING_KEY_USER_LOG, event);
      log.debug(
          "관리자 로그 이벤트 발행 완료. eventId: {}, adminId: {}, actionType: {}",
          event.eventId(),
          adminId,
          actionType);
    } catch (Exception e) {
      log.error(
          "관리자 로그 이벤트 발행 실패. adminId: {}, actionType: {}, error: {}",
          adminId,
          actionType,
          e.getMessage(),
          e);
    }
  }
}
