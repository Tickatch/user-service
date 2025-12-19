package com.tickatch.user_service.admin.application.messaging;

import java.util.UUID;

/**
 * 관리자 로그 이벤트 발행 인터페이스.
 *
 * <p>Application 레이어에서 정의하고, Infrastructure 레이어에서 구현한다. 이를 통해 Application은 메시징 기술(RabbitMQ, Kafka
 * 등)에 의존하지 않는다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public interface AdminLogEventPublisher {

  /**
   * 관리자 생성 성공 로그를 발행한다.
   *
   * @param adminId 생성된 관리자 ID
   */
  void publishCreated(UUID adminId);

  /** 관리자 생성 실패 로그를 발행한다. */
  void publishCreateFailed();

  /**
   * 관리자 정보 수정 성공 로그를 발행한다.
   *
   * @param adminId 수정된 관리자 ID
   */
  void publishUpdated(UUID adminId);

  /**
   * 관리자 정보 수정 실패 로그를 발행한다.
   *
   * @param adminId 관리자 ID
   */
  void publishUpdateFailed(UUID adminId);

  /**
   * 관리자 탈퇴 성공 로그를 발행한다.
   *
   * @param adminId 탈퇴한 관리자 ID
   */
  void publishWithdrawn(UUID adminId);

  /**
   * 관리자 탈퇴 실패 로그를 발행한다.
   *
   * @param adminId 관리자 ID
   */
  void publishWithdrawFailed(UUID adminId);

  /**
   * 관리자 정지 성공 로그를 발행한다.
   *
   * @param adminId 정지된 관리자 ID
   */
  void publishSuspended(UUID adminId);

  /**
   * 관리자 정지 실패 로그를 발행한다.
   *
   * @param adminId 관리자 ID
   */
  void publishSuspendFailed(UUID adminId);

  /**
   * 관리자 활성화 성공 로그를 발행한다.
   *
   * @param adminId 활성화된 관리자 ID
   */
  void publishActivated(UUID adminId);

  /**
   * 관리자 활성화 실패 로그를 발행한다.
   *
   * @param adminId 관리자 ID
   */
  void publishActivateFailed(UUID adminId);
}
