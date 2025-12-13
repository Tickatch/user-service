package com.tickatch.user_service.seller.domain;

import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.CANNOT_REGISTER_PERFORMANCE;
import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.CANNOT_UPDATE_SETTLEMENT_BEFORE_APPROVAL;
import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.INVALID_REJECTION_REASON;
import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.SELLER_ALREADY_APPROVED;
import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.SELLER_ALREADY_REJECTED;
import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.SELLER_NOT_PENDING;

import com.tickatch.user_service.common.domain.BaseUser;
import com.tickatch.user_service.common.domain.vo.Address;
import com.tickatch.user_service.common.domain.vo.UserProfile;
import com.tickatch.user_service.seller.domain.exception.SellerException;
import com.tickatch.user_service.seller.domain.vo.BusinessInfo;
import com.tickatch.user_service.seller.domain.vo.SellerStatus;
import com.tickatch.user_service.seller.domain.vo.SettlementInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 판매자 엔티티 (Aggregate Root).
 *
 * <p>공연 판매자를 나타낸다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Entity
@Table(name = "sellers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller extends BaseUser {

  /**
   * 사업자 정보.
   */
  @Embedded
  private BusinessInfo businessInfo;

  /**
   * 정산 정보.
   */
  @Embedded
  private SettlementInfo settlementInfo;

  /**
   * 판매자 승인 상태.
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "seller_status", nullable = false, length = 20)
  private SellerStatus sellerStatus;

  /**
   * 승인 일시.
   */
  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  /**
   * 승인자.
   */
  @Column(name = "approved_by", length = 100)
  private String approvedBy;

  /**
   * 거절 사유.
   */
  @Column(name = "rejected_reason", length = 500)
  private String rejectedReason;

  private Seller(UUID id, String email, UserProfile profile, BusinessInfo businessInfo) {
    super(id, email, profile);
    this.businessInfo = businessInfo;
    this.settlementInfo = SettlementInfo.empty();
    this.sellerStatus = SellerStatus.PENDING;
  }

  /**
   * 판매자 생성 (정적 팩토리).
   *
   * @param authId Auth Service의 인증 ID (= 사용자 ID)
   * @param email 이메일
   * @param name 이름
   * @param phone 연락처
   * @param businessName 상호명
   * @param businessNumber 사업자등록번호
   * @param representativeName 대표자명
   * @param businessAddress 사업장 주소
   * @return 생성된 Seller (PENDING 상태)
   */
  public static Seller create(UUID authId, String email, String name, String phone,
      String businessName, String businessNumber,
      String representativeName, Address businessAddress) {
    UserProfile profile = UserProfile.of(name, phone);
    BusinessInfo businessInfo = BusinessInfo.of(businessName, businessNumber, representativeName, businessAddress);
    return new Seller(authId, email, profile, businessInfo);
  }

  /**
   * 판매자 승인.
   *
   * @param approvedBy 승인자
   * @throws SellerException 승인 대기 상태가 아닌 경우
   */
  public void approve(String approvedBy) {
    validateNotWithdrawn();
    validateCanApprove();

    this.sellerStatus = SellerStatus.APPROVED;
    this.approvedAt = LocalDateTime.now();
    this.approvedBy = approvedBy;
    this.rejectedReason = null;
  }

  /**
   * 판매자 거절.
   *
   * @param reason 거절 사유
   * @throws SellerException 승인 대기 상태가 아니거나 사유가 없는 경우
   */
  public void reject(String reason) {
    validateNotWithdrawn();
    validateCanReject();
    validateRejectionReason(reason);

    this.sellerStatus = SellerStatus.REJECTED;
    this.rejectedReason = reason;
    this.approvedAt = null;
    this.approvedBy = null;
  }

  /**
   * 사업자 정보 수정.
   *
   * @param businessName 새 상호명
   * @param businessNumber 새 사업자등록번호
   * @param representativeName 새 대표자명
   * @param businessAddress 새 사업장 주소
   */
  public void updateBusinessInfo(String businessName, String businessNumber,
      String representativeName, Address businessAddress) {
    validateNotWithdrawn();
    this.businessInfo = this.businessInfo.update(businessName, businessNumber, representativeName, businessAddress);
  }

  /**
   * 정산 정보 수정 (승인된 판매자만 가능).
   *
   * @param bankCode 은행 코드
   * @param accountNumber 계좌번호
   * @param accountHolder 예금주명
   * @throws SellerException 승인 상태가 아닌 경우
   */
  public void updateSettlementInfo(String bankCode, String accountNumber, String accountHolder) {
    validateNotWithdrawn();
    if (!this.sellerStatus.canUpdateSettlement()) {
      throw new SellerException(CANNOT_UPDATE_SETTLEMENT_BEFORE_APPROVAL);
    }
    this.settlementInfo = this.settlementInfo.update(bankCode, accountNumber, accountHolder);
  }

  /**
   * 공연 등록 가능 여부 확인.
   *
   * @return 공연 등록 가능하면 true
   */
  public boolean canRegisterPerformance() {
    return isActive() && this.sellerStatus.canRegisterPerformance();
  }

  /**
   * 공연 등록 가능 여부 검증.
   *
   * @throws SellerException 공연 등록 불가능한 경우
   */
  public void validateCanRegisterPerformance() {
    if (!canRegisterPerformance()) {
      throw new SellerException(CANNOT_REGISTER_PERFORMANCE);
    }
  }

  /**
   * 승인된 판매자인지 확인.
   *
   * @return 승인됐으면 true
   */
  public boolean isApproved() {
    return this.sellerStatus.isApproved();
  }

  /**
   * 승인 대기 중인지 확인.
   *
   * @return 대기 중이면 true
   */
  public boolean isPending() {
    return this.sellerStatus.isPending();
  }

  /**
   * 거절됐는지 확인.
   *
   * @return 거절됐으면 true
   */
  public boolean isRejected() {
    return this.sellerStatus.isRejected();
  }

  private void validateCanApprove() {
    if (!this.sellerStatus.canApprove()) {
      if (this.sellerStatus.isApproved()) {
        throw new SellerException(SELLER_ALREADY_APPROVED);
      }
      if (this.sellerStatus.isRejected()) {
        throw new SellerException(SELLER_ALREADY_REJECTED);
      }
      throw new SellerException(SELLER_NOT_PENDING);
    }
  }

  private void validateCanReject() {
    if (!this.sellerStatus.canReject()) {
      if (this.sellerStatus.isApproved()) {
        throw new SellerException(SELLER_ALREADY_APPROVED);
      }
      if (this.sellerStatus.isRejected()) {
        throw new SellerException(SELLER_ALREADY_REJECTED);
      }
      throw new SellerException(SELLER_NOT_PENDING);
    }
  }

  private void validateRejectionReason(String reason) {
    if (reason == null || reason.isBlank()) {
      throw new SellerException(INVALID_REJECTION_REASON);
    }
  }

  /**
   * JPA @Embedded 객체 초기화.
   *
   * JPA는 @Embedded 객체의 모든 컬럼이 null이면 객체 자체를 null로 로드한다.
   * 이 콜백에서 null인 @Embedded 객체를 빈 객체로 초기화한다.
   */
  @PostLoad
  private void initEmbeddedObjects() {
    if (this.settlementInfo == null) {
      this.settlementInfo = SettlementInfo.empty();
    }
  }
}