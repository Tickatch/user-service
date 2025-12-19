package com.tickatch.user_service.seller.domain.vo;

import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.INVALID_BUSINESS_ADDRESS;
import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.INVALID_BUSINESS_NAME;
import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.INVALID_BUSINESS_NUMBER;
import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.INVALID_REPRESENTATIVE_NAME;

import com.tickatch.user_service.common.domain.vo.Address;
import com.tickatch.user_service.seller.domain.exception.SellerException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사업자 정보 Value Object.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusinessInfo {

  private static final int MAX_BUSINESS_NAME_LENGTH = 200;
  private static final int MAX_REPRESENTATIVE_NAME_LENGTH = 100;
  private static final Pattern BUSINESS_NUMBER_PATTERN = Pattern.compile("^[0-9]{10}$");

  /** 상호명. */
  @Column(name = "business_name", nullable = false, length = 200)
  private String businessName;

  /** 사업자등록번호 (10자리 숫자). */
  @Column(name = "business_number", nullable = false, length = 20)
  private String businessNumber;

  /** 대표자명. */
  @Column(name = "representative_name", nullable = false, length = 100)
  private String representativeName;

  /** 사업장 주소. */
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "zipCode", column = @Column(name = "business_zip_code", length = 10)),
    @AttributeOverride(
        name = "address1",
        column = @Column(name = "business_address1", length = 200)),
    @AttributeOverride(
        name = "address2",
        column = @Column(name = "business_address2", length = 200))
  })
  private Address businessAddress;

  private BusinessInfo(
      String businessName,
      String businessNumber,
      String representativeName,
      Address businessAddress) {
    this.businessName = businessName;
    this.businessNumber = businessNumber;
    this.representativeName = representativeName;
    this.businessAddress = businessAddress;
  }

  /**
   * BusinessInfo 생성.
   *
   * @param businessName 상호명 (필수)
   * @param businessNumber 사업자등록번호 (10자리 숫자, 필수)
   * @param representativeName 대표자명 (필수)
   * @param businessAddress 사업장 주소 (선택)
   * @return BusinessInfo 인스턴스
   * @throws SellerException 유효성 검증 실패 시
   */
  public static BusinessInfo of(
      String businessName,
      String businessNumber,
      String representativeName,
      Address businessAddress) {
    validateBusinessName(businessName);
    validateBusinessNumber(businessNumber);
    validateRepresentativeName(representativeName);
    validateBusinessAddress(businessAddress);

    String normalizedNumber = normalizeBusinessNumber(businessNumber);
    Address address = businessAddress != null ? businessAddress : Address.empty();

    return new BusinessInfo(
        businessName.trim(), normalizedNumber, representativeName.trim(), address);
  }

  /**
   * 사업자 정보 수정.
   *
   * @param businessName 새 상호명
   * @param businessNumber 새 사업자등록번호
   * @param representativeName 새 대표자명
   * @param businessAddress 새 사업장 주소
   * @return 수정된 BusinessInfo
   */
  public BusinessInfo update(
      String businessName,
      String businessNumber,
      String representativeName,
      Address businessAddress) {
    return BusinessInfo.of(businessName, businessNumber, representativeName, businessAddress);
  }

  private static void validateBusinessName(String businessName) {
    if (businessName == null || businessName.isBlank()) {
      throw new SellerException(INVALID_BUSINESS_NAME);
    }
    if (businessName.length() > MAX_BUSINESS_NAME_LENGTH) {
      throw new SellerException(INVALID_BUSINESS_NAME);
    }
  }

  private static void validateBusinessNumber(String businessNumber) {
    if (businessNumber == null || businessNumber.isBlank()) {
      throw new SellerException(INVALID_BUSINESS_NUMBER);
    }
    String normalized = businessNumber.replaceAll("-", "");
    if (!BUSINESS_NUMBER_PATTERN.matcher(normalized).matches()) {
      throw new SellerException(INVALID_BUSINESS_NUMBER);
    }
  }

  private static void validateRepresentativeName(String representativeName) {
    if (representativeName == null || representativeName.isBlank()) {
      throw new SellerException(INVALID_REPRESENTATIVE_NAME);
    }
    if (representativeName.length() > MAX_REPRESENTATIVE_NAME_LENGTH) {
      throw new SellerException(INVALID_REPRESENTATIVE_NAME);
    }
  }

  private static void validateBusinessAddress(Address businessAddress) {
    // 사업장 주소는 선택이지만, 입력했다면 기본 주소 필수
    if (businessAddress != null && !businessAddress.isEmpty()) {
      if (businessAddress.getAddress1() == null || businessAddress.getAddress1().isBlank()) {
        throw new SellerException(INVALID_BUSINESS_ADDRESS);
      }
    }
  }

  private static String normalizeBusinessNumber(String businessNumber) {
    return businessNumber.replaceAll("-", "");
  }

  /**
   * 포맷팅된 사업자등록번호 반환.
   *
   * @return XXX-XX-XXXXX 형식
   */
  public String getFormattedBusinessNumber() {
    if (businessNumber == null || businessNumber.length() != 10) {
      return businessNumber;
    }
    return businessNumber.substring(0, 3)
        + "-"
        + businessNumber.substring(3, 5)
        + "-"
        + businessNumber.substring(5);
  }
}
