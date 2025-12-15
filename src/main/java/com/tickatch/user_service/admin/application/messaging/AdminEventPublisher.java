package com.tickatch.user_service.admin.application.messaging;

import com.tickatch.user_service.admin.domain.Admin;

/**
 * 관리자 도메인 이벤트 발행 인터페이스.
 *
 * <p>Application 레이어에서 정의하고, Infrastructure 레이어에서 구현한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public interface AdminEventPublisher {

  /**
   * 관리자 탈퇴 이벤트를 발행한다.
   *
   * @param admin 탈퇴한 관리자 엔티티
   */
  void publishWithdrawn(Admin admin);

  /**
   * 관리자 정지 이벤트를 발행한다.
   *
   * @param admin 정지된 관리자 엔티티
   */
  void publishSuspended(Admin admin);

  /**
   * 관리자 활성화 이벤트를 발행한다.
   *
   * @param admin 활성화된 관리자 엔티티
   */
  void publishActivated(Admin admin);
}