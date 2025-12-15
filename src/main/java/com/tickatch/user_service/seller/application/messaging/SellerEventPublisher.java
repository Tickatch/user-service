package com.tickatch.user_service.seller.application.messaging;

import com.tickatch.user_service.seller.domain.Seller;

/**
 * 판매자 도메인 이벤트 발행 인터페이스.
 *
 * <p>Application 레이어에서 정의하고, Infrastructure 레이어에서 구현한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public interface SellerEventPublisher {

  /**
   * 판매자 탈퇴 이벤트를 발행한다.
   *
   * @param seller 탈퇴한 판매자 엔티티
   */
  void publishWithdrawn(Seller seller);

  /**
   * 판매자 정지 이벤트를 발행한다.
   *
   * @param seller 정지된 판매자 엔티티
   */
  void publishSuspended(Seller seller);

  /**
   * 판매자 활성화 이벤트를 발행한다.
   *
   * @param seller 활성화된 판매자 엔티티
   */
  void publishActivated(Seller seller);
}