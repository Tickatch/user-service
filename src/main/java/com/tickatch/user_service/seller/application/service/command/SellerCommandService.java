package com.tickatch.user_service.seller.application.service.command;

import com.tickatch.user_service.seller.application.service.command.dto.CreateSellerCommand;
import com.tickatch.user_service.seller.application.service.command.dto.UpdateSellerProfileCommand;
import com.tickatch.user_service.seller.application.service.command.dto.UpdateSettlementInfoCommand;
import com.tickatch.user_service.seller.domain.Seller;
import com.tickatch.user_service.seller.domain.SellerRepository;
import com.tickatch.user_service.seller.domain.exception.SellerErrorCode;
import com.tickatch.user_service.seller.domain.exception.SellerException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 판매자 커맨드 서비스.
 *
 * <p>판매자 생성, 수정, 승인/거절, 상태 변경 등 상태를 변경하는 작업을 처리한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SellerCommandService {

  private final SellerRepository sellerRepository;

  /**
   * 판매자를 생성한다.
   *
   * @param command 생성 커맨드
   * @return 생성된 판매자 ID
   * @throws SellerException 이미 존재하는 이메일 또는 사업자등록번호인 경우
   */
  public UUID createSeller(CreateSellerCommand command) {
    if (sellerRepository.existsByEmail(command.email())) {
      throw new SellerException(SellerErrorCode.SELLER_ALREADY_EXISTS);
    }

    if (sellerRepository.existsByBusinessNumber(command.businessNumber())) {
      throw new SellerException(SellerErrorCode.BUSINESS_NUMBER_ALREADY_EXISTS);
    }

    Seller seller = Seller.create(
        command.authId(),
        command.email(),
        command.name(),
        command.phone(),
        command.businessName(),
        command.businessNumber(),
        command.representativeName(),
        command.businessAddress()
    );

    return sellerRepository.save(seller).getId();
  }

  /**
   * 판매자 프로필을 수정한다.
   *
   * @param command 프로필 수정 커맨드
   * @throws SellerException 판매자를 찾을 수 없는 경우
   */
  public void updateProfile(UpdateSellerProfileCommand command) {
    Seller seller = findSellerById(command.sellerId());
    seller.updateProfile(command.name(), command.phone());
  }

  /**
   * 정산 정보를 수정한다.
   *
   * @param command 정산 정보 수정 커맨드
   * @throws SellerException 판매자를 찾을 수 없거나 승인되지 않은 경우
   */
  public void updateSettlementInfo(UpdateSettlementInfoCommand command) {
    Seller seller = findSellerById(command.sellerId());
    seller.updateSettlementInfo(
        command.bankCode(),
        command.accountNumber(),
        command.accountHolder()
    );
  }

  /**
   * 판매자를 승인한다.
   *
   * @param sellerId 판매자 ID
   * @param approvedBy 승인자
   * @throws SellerException 판매자를 찾을 수 없거나 승인할 수 없는 상태인 경우
   */
  public void approveSeller(UUID sellerId, String approvedBy) {
    Seller seller = findSellerById(sellerId);
    seller.approve(approvedBy);
  }

  /**
   * 판매자를 거절한다.
   *
   * @param sellerId 판매자 ID
   * @param reason 거절 사유
   * @throws SellerException 판매자를 찾을 수 없거나 거절할 수 없는 상태인 경우
   */
  public void rejectSeller(UUID sellerId, String reason) {
    Seller seller = findSellerById(sellerId);
    seller.reject(reason);
  }

  /**
   * 판매자를 정지한다.
   *
   * @param sellerId 판매자 ID
   * @throws SellerException 판매자를 찾을 수 없거나 정지할 수 없는 상태인 경우
   */
  public void suspendSeller(UUID sellerId) {
    Seller seller = findSellerById(sellerId);
    seller.suspend();
  }

  /**
   * 판매자 정지를 해제한다.
   *
   * @param sellerId 판매자 ID
   * @throws SellerException 판매자를 찾을 수 없거나 활성화할 수 없는 상태인 경우
   */
  public void activateSeller(UUID sellerId) {
    Seller seller = findSellerById(sellerId);
    seller.activate();
  }

  /**
   * 판매자를 탈퇴 처리한다.
   *
   * @param sellerId 판매자 ID
   * @throws SellerException 판매자를 찾을 수 없거나 탈퇴할 수 없는 상태인 경우
   */
  public void withdrawSeller(UUID sellerId) {
    Seller seller = findSellerById(sellerId);
    seller.withdraw();
  }

  private Seller findSellerById(UUID sellerId) {
    return sellerRepository.findById(sellerId)
        .orElseThrow(() -> new SellerException(SellerErrorCode.SELLER_NOT_FOUND));
  }
}