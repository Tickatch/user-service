package com.tickatch.user_service.common.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 상태.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus {

  /** 활성 상태. */
  ACTIVE("활성"),

  /** 정지 상태. */
  SUSPENDED("정지"),

  /** 탈퇴 상태. */
  WITHDRAWN("탈퇴");

  private final String description;

  /**
   * 활성 상태인지 확인.
   *
   * @return 활성 상태이면 true
   */
  public boolean isActive() {
    return this == ACTIVE;
  }

  /**
   * 정지 상태인지 확인.
   *
   * @return 정지 상태이면 true
   */
  public boolean isSuspended() {
    return this == SUSPENDED;
  }

  /**
   * 탈퇴 상태인지 확인.
   *
   * @return 탈퇴 상태이면 true
   */
  public boolean isWithdrawn() {
    return this == WITHDRAWN;
  }

  /**
   * 최종 상태(더 이상 전이 불가)인지 확인.
   *
   * @return 탈퇴 상태이면 true
   */
  public boolean isTerminal() {
    return this == WITHDRAWN;
  }

  /**
   * 정지 가능한 상태인지 확인.
   *
   * @return 활성 상태이면 true
   */
  public boolean canSuspend() {
    return this == ACTIVE;
  }

  /**
   * 활성화 가능한 상태인지 확인.
   *
   * @return 정지 상태이면 true
   */
  public boolean canActivate() {
    return this == SUSPENDED;
  }

  /**
   * 탈퇴 가능한 상태인지 확인.
   *
   * @return 탈퇴 상태가 아니면 true
   */
  public boolean canWithdraw() {
    return this != WITHDRAWN;
  }
}
