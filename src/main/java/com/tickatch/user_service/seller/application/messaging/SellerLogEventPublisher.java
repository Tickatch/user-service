package com.tickatch.user_service.seller.application.messaging;

import java.util.UUID;

/**
 * 판매자 로그 이벤트 발행 인터페이스.
 *
 * <p>Application 레이어에서 정의하고, Infrastructure 레이어에서 구현한다. 이를 통해 Application은 메시징 기술(RabbitMQ, Kafka
 * 등)에 의존하지 않는다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public interface SellerLogEventPublisher {

  /**
   * 판매자 생성 성공 로그를 발행한다.
   *
   * @param sellerId 생성된 판매자 ID
   */
  void publishCreated(UUID sellerId);

  /** 판매자 생성 실패 로그를 발행한다. */
  void publishCreateFailed();

  /**
   * 판매자 정보 수정 성공 로그를 발행한다.
   *
   * @param sellerId 수정된 판매자 ID
   */
  void publishUpdated(UUID sellerId);

  /**
   * 판매자 정보 수정 실패 로그를 발행한다.
   *
   * @param sellerId 판매자 ID
   */
  void publishUpdateFailed(UUID sellerId);

  /**
   * 판매자 탈퇴 성공 로그를 발행한다.
   *
   * @param sellerId 탈퇴한 판매자 ID
   */
  void publishWithdrawn(UUID sellerId);

  /**
   * 판매자 탈퇴 실패 로그를 발행한다.
   *
   * @param sellerId 판매자 ID
   */
  void publishWithdrawFailed(UUID sellerId);

  /**
   * 판매자 정지 성공 로그를 발행한다.
   *
   * @param sellerId 정지된 판매자 ID
   */
  void publishSuspended(UUID sellerId);

  /**
   * 판매자 정지 실패 로그를 발행한다.
   *
   * @param sellerId 판매자 ID
   */
  void publishSuspendFailed(UUID sellerId);

  /**
   * 판매자 활성화 성공 로그를 발행한다.
   *
   * @param sellerId 활성화된 판매자 ID
   */
  void publishActivated(UUID sellerId);

  /**
   * 판매자 활성화 실패 로그를 발행한다.
   *
   * @param sellerId 판매자 ID
   */
  void publishActivateFailed(UUID sellerId);

  /**
   * 판매자 승인 성공 로그를 발행한다.
   *
   * @param sellerId 승인된 판매자 ID
   */
  void publishApproved(UUID sellerId);

  /**
   * 판매자 승인 실패 로그를 발행한다.
   *
   * @param sellerId 판매자 ID
   */
  void publishApproveFailed(UUID sellerId);

  /**
   * 판매자 반려 성공 로그를 발행한다.
   *
   * @param sellerId 반려된 판매자 ID
   */
  void publishRejected(UUID sellerId);

  /**
   * 판매자 반려 실패 로그를 발행한다.
   *
   * @param sellerId 판매자 ID
   */
  void publishRejectFailed(UUID sellerId);
}
