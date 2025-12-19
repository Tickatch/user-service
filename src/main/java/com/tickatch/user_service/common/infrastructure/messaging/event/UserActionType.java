package com.tickatch.user_service.common.infrastructure.messaging.event;

/**
 * 사용자 로그 액션 타입.
 *
 * <p>User Service에서 발생할 수 있는 모든 액션 타입을 정의한다. 로그 서비스에서 이 값을 기준으로 액션을 분류하고 저장한다.
 *
 * <p>사용자 유형별 접두어:
 *
 * <ul>
 *   <li>CUSTOMER_* : 고객 관련 액션
 *   <li>SELLER_* : 판매자 관련 액션
 *   <li>ADMIN_* : 관리자 관련 액션
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 * @see UserLogEvent
 */
public final class UserActionType {

  private UserActionType() {
    // 인스턴스화 방지
  }

  // ========================================
  // Customer 관련
  // ========================================

  /** 고객 생성 */
  public static final String CUSTOMER_CREATED = "CUSTOMER_CREATED";

  /** 고객 생성 실패 */
  public static final String CUSTOMER_CREATE_FAILED = "CUSTOMER_CREATE_FAILED";

  /** 고객 정보 수정 */
  public static final String CUSTOMER_UPDATED = "CUSTOMER_UPDATED";

  /** 고객 정보 수정 실패 */
  public static final String CUSTOMER_UPDATE_FAILED = "CUSTOMER_UPDATE_FAILED";

  /** 고객 탈퇴 */
  public static final String CUSTOMER_WITHDRAWN = "CUSTOMER_WITHDRAWN";

  /** 고객 탈퇴 실패 */
  public static final String CUSTOMER_WITHDRAW_FAILED = "CUSTOMER_WITHDRAW_FAILED";

  /** 고객 정지 */
  public static final String CUSTOMER_SUSPENDED = "CUSTOMER_SUSPENDED";

  /** 고객 정지 실패 */
  public static final String CUSTOMER_SUSPEND_FAILED = "CUSTOMER_SUSPEND_FAILED";

  /** 고객 활성화 */
  public static final String CUSTOMER_ACTIVATED = "CUSTOMER_ACTIVATED";

  /** 고객 활성화 실패 */
  public static final String CUSTOMER_ACTIVATE_FAILED = "CUSTOMER_ACTIVATE_FAILED";

  // ========================================
  // Seller 관련
  // ========================================

  /** 판매자 생성 */
  public static final String SELLER_CREATED = "SELLER_CREATED";

  /** 판매자 생성 실패 */
  public static final String SELLER_CREATE_FAILED = "SELLER_CREATE_FAILED";

  /** 판매자 정보 수정 */
  public static final String SELLER_UPDATED = "SELLER_UPDATED";

  /** 판매자 정보 수정 실패 */
  public static final String SELLER_UPDATE_FAILED = "SELLER_UPDATE_FAILED";

  /** 판매자 탈퇴 */
  public static final String SELLER_WITHDRAWN = "SELLER_WITHDRAWN";

  /** 판매자 탈퇴 실패 */
  public static final String SELLER_WITHDRAW_FAILED = "SELLER_WITHDRAW_FAILED";

  /** 판매자 정지 */
  public static final String SELLER_SUSPENDED = "SELLER_SUSPENDED";

  /** 판매자 정지 실패 */
  public static final String SELLER_SUSPEND_FAILED = "SELLER_SUSPEND_FAILED";

  /** 판매자 활성화 */
  public static final String SELLER_ACTIVATED = "SELLER_ACTIVATED";

  /** 판매자 활성화 실패 */
  public static final String SELLER_ACTIVATE_FAILED = "SELLER_ACTIVATE_FAILED";

  /** 판매자 승인 */
  public static final String SELLER_APPROVED = "SELLER_APPROVED";

  /** 판매자 승인 실패 */
  public static final String SELLER_APPROVE_FAILED = "SELLER_APPROVE_FAILED";

  /** 판매자 반려 */
  public static final String SELLER_REJECTED = "SELLER_REJECTED";

  /** 판매자 반려 실패 */
  public static final String SELLER_REJECT_FAILED = "SELLER_REJECT_FAILED";

  // ========================================
  // Admin 관련
  // ========================================

  /** 관리자 생성 */
  public static final String ADMIN_CREATED = "ADMIN_CREATED";

  /** 관리자 생성 실패 */
  public static final String ADMIN_CREATE_FAILED = "ADMIN_CREATE_FAILED";

  /** 관리자 정보 수정 */
  public static final String ADMIN_UPDATED = "ADMIN_UPDATED";

  /** 관리자 정보 수정 실패 */
  public static final String ADMIN_UPDATE_FAILED = "ADMIN_UPDATE_FAILED";

  /** 관리자 탈퇴 */
  public static final String ADMIN_WITHDRAWN = "ADMIN_WITHDRAWN";

  /** 관리자 탈퇴 실패 */
  public static final String ADMIN_WITHDRAW_FAILED = "ADMIN_WITHDRAW_FAILED";

  /** 관리자 정지 */
  public static final String ADMIN_SUSPENDED = "ADMIN_SUSPENDED";

  /** 관리자 정지 실패 */
  public static final String ADMIN_SUSPEND_FAILED = "ADMIN_SUSPEND_FAILED";

  /** 관리자 활성화 */
  public static final String ADMIN_ACTIVATED = "ADMIN_ACTIVATED";

  /** 관리자 활성화 실패 */
  public static final String ADMIN_ACTIVATE_FAILED = "ADMIN_ACTIVATE_FAILED";
}
