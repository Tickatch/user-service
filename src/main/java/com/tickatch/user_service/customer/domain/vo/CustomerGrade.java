package com.tickatch.user_service.customer.domain.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 고객 등급.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum CustomerGrade {

  /** 일반 등급. */
  NORMAL("일반 등급", 0),

  /** VIP 등급. */
  VIP("VIP 등급", 1);

  private final String description;
  private final int level;

  /**
   * 일반 등급인지 확인.
   *
   * @return 일반 등급이면 true
   */
  public boolean isNormal() {
    return this == NORMAL;
  }

  /**
   * VIP 등급인지 확인.
   *
   * @return VIP 등급이면 true
   */
  public boolean isVip() {
    return this == VIP;
  }

  /**
   * 다른 등급보다 높은지 확인.
   *
   * @param other 비교 대상 등급
   * @return 더 높으면 true
   */
  public boolean isHigherThan(CustomerGrade other) {
    return this.level > other.level;
  }

  /**
   * 다른 등급보다 낮은지 확인.
   *
   * @param other 비교 대상 등급
   * @return 더 낮으면 true
   */
  public boolean isLowerThan(CustomerGrade other) {
    return this.level < other.level;
  }

  /**
   * 해당 등급으로 업그레이드 가능한지 확인.
   *
   * @param targetGrade 목표 등급
   * @return 업그레이드 가능하면 true (현재보다 높거나 같은 등급)
   */
  public boolean canUpgradeTo(CustomerGrade targetGrade) {
    return targetGrade.level >= this.level;
  }
}
