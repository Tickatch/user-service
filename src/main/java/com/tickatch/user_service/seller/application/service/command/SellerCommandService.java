package com.tickatch.user_service.seller.application.service.command;

import com.tickatch.user_service.seller.application.messaging.SellerLogEventPublisher;
import com.tickatch.user_service.seller.application.service.command.dto.CreateSellerCommand;
import com.tickatch.user_service.seller.application.service.command.dto.UpdateSellerProfileCommand;
import com.tickatch.user_service.seller.application.service.command.dto.UpdateSettlementInfoCommand;
import com.tickatch.user_service.seller.domain.Seller;
import com.tickatch.user_service.seller.domain.SellerRepository;
import com.tickatch.user_service.seller.domain.exception.SellerErrorCode;
import com.tickatch.user_service.seller.domain.exception.SellerException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 판매자 커맨드 서비스.
 *
 * <p>판매자 생성, 수정, 승인/거절, 상태 변경 등 상태를 변경하는 작업을 처리한다. 모든 주요 작업에 대해 성공/실패 로그를 로그 서비스로 발행한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SellerCommandService {

  private final SellerRepository sellerRepository;
  private final SellerLogEventPublisher logEventPublisher;

  /**
   * 판매자를 생성한다.
   *
   * <p>성공 시 SELLER_CREATED 로그를, 실패 시 SELLER_CREATE_FAILED 로그를 발행한다.
   *
   * @param command 생성 커맨드
   * @return 생성된 판매자 ID
   * @throws SellerException 이미 존재하는 이메일 또는 사업자등록번호인 경우
   */
  public UUID createSeller(CreateSellerCommand command) {
    try {
      if (sellerRepository.existsByEmail(command.email())) {
        throw new SellerException(SellerErrorCode.SELLER_ALREADY_EXISTS);
      }

      if (sellerRepository.existsByBusinessNumber(command.businessNumber())) {
        throw new SellerException(SellerErrorCode.BUSINESS_NUMBER_ALREADY_EXISTS);
      }

      Seller seller =
          Seller.create(
              command.authId(),
              command.email(),
              command.name(),
              command.phone(),
              command.businessName(),
              command.businessNumber(),
              command.representativeName(),
              command.businessAddress());

      UUID sellerId = sellerRepository.save(seller).getId();
      log.info("판매자 생성 완료. sellerId: {}", sellerId);

      logEventPublisher.publishCreated(sellerId);
      return sellerId;
    } catch (Exception e) {
      logEventPublisher.publishCreateFailed();
      log.error("판매자 생성 실패. email: {}, error: {}", command.email(), e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 판매자 프로필을 수정한다.
   *
   * <p>성공 시 SELLER_UPDATED 로그를, 실패 시 SELLER_UPDATE_FAILED 로그를 발행한다.
   *
   * @param command 프로필 수정 커맨드
   * @throws SellerException 판매자를 찾을 수 없는 경우
   */
  public void updateProfile(UpdateSellerProfileCommand command) {
    try {
      Seller seller = findSellerById(command.sellerId());
      seller.updateProfile(command.name(), command.phone());
      log.info("판매자 프로필 수정 완료. sellerId: {}", command.sellerId());

      logEventPublisher.publishUpdated(command.sellerId());
    } catch (Exception e) {
      logEventPublisher.publishUpdateFailed(command.sellerId());
      log.error("판매자 프로필 수정 실패. sellerId: {}, error: {}", command.sellerId(), e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 정산 정보를 수정한다.
   *
   * <p>성공 시 SELLER_UPDATED 로그를, 실패 시 SELLER_UPDATE_FAILED 로그를 발행한다.
   *
   * @param command 정산 정보 수정 커맨드
   * @throws SellerException 판매자를 찾을 수 없거나 승인되지 않은 경우
   */
  public void updateSettlementInfo(UpdateSettlementInfoCommand command) {
    try {
      Seller seller = findSellerById(command.sellerId());
      seller.updateSettlementInfo(
          command.bankCode(), command.accountNumber(), command.accountHolder());
      log.info("판매자 정산 정보 수정 완료. sellerId: {}", command.sellerId());

      logEventPublisher.publishUpdated(command.sellerId());
    } catch (Exception e) {
      logEventPublisher.publishUpdateFailed(command.sellerId());
      log.error("판매자 정산 정보 수정 실패. sellerId: {}, error: {}", command.sellerId(), e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 판매자를 승인한다.
   *
   * <p>성공 시 SELLER_APPROVED 로그를, 실패 시 SELLER_APPROVE_FAILED 로그를 발행한다.
   *
   * @param sellerId 판매자 ID
   * @param approvedBy 승인자
   * @throws SellerException 판매자를 찾을 수 없거나 승인할 수 없는 상태인 경우
   */
  public void approveSeller(UUID sellerId, String approvedBy) {
    try {
      Seller seller = findSellerById(sellerId);
      seller.approve(approvedBy);
      log.info("판매자 승인 완료. sellerId: {}, approvedBy: {}", sellerId, approvedBy);

      logEventPublisher.publishApproved(sellerId);
    } catch (Exception e) {
      logEventPublisher.publishApproveFailed(sellerId);
      log.error("판매자 승인 실패. sellerId: {}, error: {}", sellerId, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 판매자를 거절한다.
   *
   * <p>성공 시 SELLER_REJECTED 로그를, 실패 시 SELLER_REJECT_FAILED 로그를 발행한다.
   *
   * @param sellerId 판매자 ID
   * @param reason 거절 사유
   * @throws SellerException 판매자를 찾을 수 없거나 거절할 수 없는 상태인 경우
   */
  public void rejectSeller(UUID sellerId, String reason) {
    try {
      Seller seller = findSellerById(sellerId);
      seller.reject(reason);
      log.info("판매자 거절 완료. sellerId: {}, reason: {}", sellerId, reason);

      logEventPublisher.publishRejected(sellerId);
    } catch (Exception e) {
      logEventPublisher.publishRejectFailed(sellerId);
      log.error("판매자 거절 실패. sellerId: {}, error: {}", sellerId, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 판매자를 정지한다.
   *
   * <p>성공 시 SELLER_SUSPENDED 로그를, 실패 시 SELLER_SUSPEND_FAILED 로그를 발행한다.
   *
   * @param sellerId 판매자 ID
   * @throws SellerException 판매자를 찾을 수 없거나 정지할 수 없는 상태인 경우
   */
  public void suspendSeller(UUID sellerId) {
    try {
      Seller seller = findSellerById(sellerId);
      seller.suspend();
      log.info("판매자 정지 완료. sellerId: {}", sellerId);

      logEventPublisher.publishSuspended(sellerId);
    } catch (Exception e) {
      logEventPublisher.publishSuspendFailed(sellerId);
      log.error("판매자 정지 실패. sellerId: {}, error: {}", sellerId, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 판매자 정지를 해제한다.
   *
   * <p>성공 시 SELLER_ACTIVATED 로그를, 실패 시 SELLER_ACTIVATE_FAILED 로그를 발행한다.
   *
   * @param sellerId 판매자 ID
   * @throws SellerException 판매자를 찾을 수 없거나 활성화할 수 없는 상태인 경우
   */
  public void activateSeller(UUID sellerId) {
    try {
      Seller seller = findSellerById(sellerId);
      seller.activate();
      log.info("판매자 활성화 완료. sellerId: {}", sellerId);

      logEventPublisher.publishActivated(sellerId);
    } catch (Exception e) {
      logEventPublisher.publishActivateFailed(sellerId);
      log.error("판매자 활성화 실패. sellerId: {}, error: {}", sellerId, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 판매자를 탈퇴 처리한다.
   *
   * <p>성공 시 SELLER_WITHDRAWN 로그를, 실패 시 SELLER_WITHDRAW_FAILED 로그를 발행한다.
   *
   * @param sellerId 판매자 ID
   * @throws SellerException 판매자를 찾을 수 없거나 탈퇴할 수 없는 상태인 경우
   */
  public void withdrawSeller(UUID sellerId) {
    try {
      Seller seller = findSellerById(sellerId);
      seller.withdraw();
      log.info("판매자 탈퇴 완료. sellerId: {}", sellerId);

      logEventPublisher.publishWithdrawn(sellerId);
    } catch (Exception e) {
      logEventPublisher.publishWithdrawFailed(sellerId);
      log.error("판매자 탈퇴 실패. sellerId: {}, error: {}", sellerId, e.getMessage(), e);
      throw e;
    }
  }

  private Seller findSellerById(UUID sellerId) {
    return sellerRepository
        .findById(sellerId)
        .orElseThrow(() -> new SellerException(SellerErrorCode.SELLER_NOT_FOUND));
  }
}
