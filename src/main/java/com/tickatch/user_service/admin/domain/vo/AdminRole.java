package com.tickatch.user_service.admin.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 역할.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum AdminRole {

  /** 매니저 (일반 관리자). */
  MANAGER("일반 관리자", 1),

  /** 어드민 (최고 관리자). */
  ADMIN("최고 관리자", 2);

  private final String description;
  private final int level;

  /**
   * 매니저 역할인지 확인.
   *
   * @return MANAGER이면 true
   */
  public boolean isManager() {
    return this == MANAGER;
  }

  /**
   * 최고 관리자인지 확인.
   *
   * @return ADMIN이면 true
   */
  public boolean isAdmin() {
    return this == ADMIN;
  }

  /**
   * 다른 역할보다 권한이 높은지 확인.
   *
   * @param other 비교 대상 역할
   * @return 더 높으면 true
   */
  public boolean isHigherThan(AdminRole other) {
    return this.level > other.level;
  }

  /**
   * 다른 역할보다 권한이 같거나 높은지 확인.
   *
   * @param other 비교 대상 역할
   * @return 같거나 높으면 true
   */
  public boolean isHigherOrEqualThan(AdminRole other) {
    return this.level >= other.level;
  }

  /**
   * 관리자 생성 권한이 있는지 확인.
   *
   * @return ADMIN이면 true
   */
  public boolean canCreateAdmin() {
    return this == ADMIN;
  }

  /**
   * 역할 변경 권한이 있는지 확인.
   *
   * @return ADMIN이면 true
   */
  public boolean canChangeRole() {
    return this == ADMIN;
  }

  /**
   * 판매자 승인 권한이 있는지 확인.
   *
   * @return 모든 역할 가능 (MANAGER 이상)
   */
  public boolean canApproveSeller() {
    return this.level >= MANAGER.level;
  }

  /**
   * 사용자 정지 권한이 있는지 확인.
   *
   * @return 모든 역할 가능 (MANAGER 이상)
   */
  public boolean canSuspendUser() {
    return this.level >= MANAGER.level;
  }
}
