package com.tickatch.user_service.common.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.tickatch.common.event.DomainEvent;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/**
 * 사용자 상태 변경 이벤트.
 *
 * <p>User Service에서 사용자(Customer/Seller/Admin)의 상태가 변경되면 발행된다.
 * Auth Service는 이를 수신하여 인증 상태를 동기화한다.
 *
 * <p>이벤트 정보:
 * <ul>
 *   <li>Aggregate Type: User
 *   <li>발행 서비스: user-service
 *   <li>수신 서비스: auth-service
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
public class UserStatusChangedEvent extends DomainEvent {

  private static final String AGGREGATE_TYPE = "User";

  /** 사용자 ID (= Auth ID) */
  private final UUID userId;

  /** 사용자 유형 (CUSTOMER, SELLER, ADMIN) */
  private final String userType;

  /** 상태 변경 유형 (WITHDRAWN, SUSPENDED, ACTIVATED) */
  private final String statusChangeType;

  /** 라우팅 키 */
  private final String routingKey;

  /**
   * 이벤트 발행용 생성자.
   *
   * @param userId 사용자 ID
   * @param userType 사용자 유형
   * @param statusChangeType 상태 변경 유형
   * @param routingKey 라우팅 키
   */
  public UserStatusChangedEvent(
      UUID userId,
      String userType,
      String statusChangeType,
      String routingKey) {
    super();
    this.userId = userId;
    this.userType = userType;
    this.statusChangeType = statusChangeType;
    this.routingKey = routingKey;
  }

  /**
   * JSON 역직렬화용 생성자.
   */
  @JsonCreator
  public UserStatusChangedEvent(
      @JsonProperty("eventId") String eventId,
      @JsonProperty("occurredAt") Instant occurredAt,
      @JsonProperty("version") int version,
      @JsonProperty("userId") UUID userId,
      @JsonProperty("userType") String userType,
      @JsonProperty("statusChangeType") String statusChangeType,
      @JsonProperty("routingKey") String routingKey) {
    super(eventId, occurredAt, version);
    this.userId = userId;
    this.userType = userType;
    this.statusChangeType = statusChangeType;
    this.routingKey = routingKey;
  }

  // ========== 팩토리 메서드 - Customer ==========

  public static UserStatusChangedEvent customerWithdrawn(UUID customerId) {
    return new UserStatusChangedEvent(customerId, "CUSTOMER", "WITHDRAWN", "customer.withdrawn");
  }

  public static UserStatusChangedEvent customerSuspended(UUID customerId) {
    return new UserStatusChangedEvent(customerId, "CUSTOMER", "SUSPENDED", "customer.suspended");
  }

  public static UserStatusChangedEvent customerActivated(UUID customerId) {
    return new UserStatusChangedEvent(customerId, "CUSTOMER", "ACTIVATED", "customer.activated");
  }

  // ========== 팩토리 메서드 - Seller ==========

  public static UserStatusChangedEvent sellerWithdrawn(UUID sellerId) {
    return new UserStatusChangedEvent(sellerId, "SELLER", "WITHDRAWN", "seller.withdrawn");
  }

  public static UserStatusChangedEvent sellerSuspended(UUID sellerId) {
    return new UserStatusChangedEvent(sellerId, "SELLER", "SUSPENDED", "seller.suspended");
  }

  public static UserStatusChangedEvent sellerActivated(UUID sellerId) {
    return new UserStatusChangedEvent(sellerId, "SELLER", "ACTIVATED", "seller.activated");
  }

  // ========== 팩토리 메서드 - Admin ==========

  public static UserStatusChangedEvent adminWithdrawn(UUID adminId) {
    return new UserStatusChangedEvent(adminId, "ADMIN", "WITHDRAWN", "admin.withdrawn");
  }

  public static UserStatusChangedEvent adminSuspended(UUID adminId) {
    return new UserStatusChangedEvent(adminId, "ADMIN", "SUSPENDED", "admin.suspended");
  }

  public static UserStatusChangedEvent adminActivated(UUID adminId) {
    return new UserStatusChangedEvent(adminId, "ADMIN", "ACTIVATED", "admin.activated");
  }

  @Override
  public String getAggregateId() {
    return userId.toString();
  }

  @Override
  public String getAggregateType() {
    return AGGREGATE_TYPE;
  }

  @Override
  public String getRoutingKey() {
    return routingKey;
  }
}