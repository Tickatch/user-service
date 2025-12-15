package com.tickatch.user_service.seller.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 판매자 승인 상태.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum SellerStatus {

  /**
   * 승인 대기.
   */
  PENDING("승인 대기 상태"),

  /**
   * 승인 완료.
   */
  APPROVED("승인 완료 상태"),

  /**
   * 승인 거절.
   */
  REJECTED("승인 거절 상태");

  private final String description;

  /**
   * 승인 대기 상태인지 확인.
   *
   * @return 승인 대기이면 true
   */
  public boolean isPending() {
    return this == PENDING;
  }

  /**
   * 승인 완료 상태인지 확인.
   *
   * @return 승인 완료이면 true
   */
  public boolean isApproved() {
    return this == APPROVED;
  }

  /**
   * 승인 거절 상태인지 확인.
   *
   * @return 승인 거절이면 true
   */
  public boolean isRejected() {
    return this == REJECTED;
  }

  /**
   * 승인 가능한 상태인지 확인.
   *
   * @return 승인 대기 상태이면 true
   */
  public boolean canApprove() {
    return this == PENDING;
  }

  /**
   * 거절 가능한 상태인지 확인.
   *
   * @return 승인 대기 상태이면 true
   */
  public boolean canReject() {
    return this == PENDING;
  }

  /**
   * 공연 등록 가능한 상태인지 확인.
   *
   * @return 승인 완료 상태이면 true
   */
  public boolean canRegisterPerformance() {
    return this == APPROVED;
  }

  /**
   * 정산 정보 수정 가능한 상태인지 확인.
   *
   * @return 승인 완료 상태이면 true
   */
  public boolean canUpdateSettlement() {
    return this == APPROVED;
  }

  /**
   * 최종 상태(더 이상 전이 불가)인지 확인.
   *
   * @return 승인 또는 거절 상태이면 true
   */
  public boolean isTerminal() {
    return this == APPROVED || this == REJECTED;
  }
}