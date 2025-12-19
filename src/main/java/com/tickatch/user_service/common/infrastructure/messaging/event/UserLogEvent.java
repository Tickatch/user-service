package com.tickatch.user_service.common.infrastructure.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 로그 이벤트.
 *
 * <p>User Service에서 발생하는 주요 액션에 대한 로그 정보를 담는 이벤트 객체이다. 로그 서비스로 전송되어 사용자 관련 활동 이력을 기록한다.
 *
 * <p>이벤트 정보:
 *
 * <ul>
 *   <li>Exchange: tickatch.log
 *   <li>Routing Key: user.log
 *   <li>대상 서비스: log-service
 * </ul>
 *
 * @param eventId 이벤트 고유 ID
 * @param userId 대상 사용자 ID
 * @param userType 사용자 유형 (CUSTOMER, SELLER, ADMIN)
 * @param actionType 액션 타입 ({@link UserActionType} 참조)
 * @param actorType 액터 타입 (ADMIN, SELLER, CUSTOMER, SYSTEM)
 * @param actorUserId 액터 사용자 ID (SYSTEM인 경우 null)
 * @param occurredAt 이벤트 발생 시간
 * @author Tickatch
 * @since 1.0.0
 */
public record UserLogEvent(
    UUID eventId,
    UUID userId,
    String userType,
    String actionType,
    String actorType,
    UUID actorUserId,
    LocalDateTime occurredAt) {

  /**
   * 새로운 사용자 로그 이벤트를 생성한다.
   *
   * @param userId 대상 사용자 ID
   * @param userType 사용자 유형
   * @param actionType 액션 타입
   * @param actorType 액터 타입
   * @param actorUserId 액터 사용자 ID
   * @return 생성된 UserLogEvent
   */
  public static UserLogEvent create(
      UUID userId, String userType, String actionType, String actorType, UUID actorUserId) {
    return new UserLogEvent(
        UUID.randomUUID(),
        userId,
        userType,
        actionType,
        actorType,
        actorUserId,
        LocalDateTime.now());
  }

  /**
   * 시스템에 의한 사용자 로그 이벤트를 생성한다.
   *
   * @param userId 대상 사용자 ID
   * @param userType 사용자 유형
   * @param actionType 액션 타입
   * @return 생성된 UserLogEvent
   */
  public static UserLogEvent createSystemEvent(UUID userId, String userType, String actionType) {
    return new UserLogEvent(
        UUID.randomUUID(), userId, userType, actionType, "SYSTEM", null, LocalDateTime.now());
  }
}
