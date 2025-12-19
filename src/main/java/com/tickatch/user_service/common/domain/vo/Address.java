package com.tickatch.user_service.common.domain.vo;

import static com.tickatch.user_service.common.domain.exception.UserErrorCode.INVALID_ADDRESS;

import com.tickatch.user_service.common.domain.exception.UserException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주소 Value Object.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

  private static final int MAX_ZIP_CODE_LENGTH = 10;
  private static final int MAX_ADDRESS_LENGTH = 200;

  @Column(name = "zip_code", length = 10)
  private String zipCode;

  @Column(name = "address1", length = 200)
  private String address1;

  @Column(name = "address2", length = 200)
  private String address2;

  private Address(String zipCode, String address1, String address2) {
    this.zipCode = zipCode;
    this.address1 = address1;
    this.address2 = address2;
  }

  /**
   * Address 생성.
   *
   * @param zipCode 우편번호
   * @param address1 기본 주소
   * @param address2 상세 주소
   * @return Address 인스턴스
   * @throws UserException 유효성 검증 실패 시
   */
  public static Address of(String zipCode, String address1, String address2) {
    validate(zipCode, address1, address2);
    return new Address(zipCode, address1, address2);
  }

  /**
   * 빈 주소 생성.
   *
   * @return 빈 Address 인스턴스
   */
  public static Address empty() {
    return new Address(null, null, null);
  }

  /**
   * 주소 수정.
   *
   * @param zipCode 새 우편번호
   * @param address1 새 기본 주소
   * @param address2 새 상세 주소
   * @return 수정된 Address
   */
  public Address update(String zipCode, String address1, String address2) {
    return Address.of(zipCode, address1, address2);
  }

  /**
   * 주소가 비어있는지 확인.
   *
   * @return 비어있으면 true
   */
  public boolean isEmpty() {
    return (zipCode == null || zipCode.isBlank())
        && (address1 == null || address1.isBlank())
        && (address2 == null || address2.isBlank());
  }

  /**
   * 전체 주소 문자열 반환.
   *
   * @return 전체 주소
   */
  public String getFullAddress() {
    if (isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    if (zipCode != null && !zipCode.isBlank()) {
      sb.append("(").append(zipCode).append(") ");
    }
    if (address1 != null && !address1.isBlank()) {
      sb.append(address1);
    }
    if (address2 != null && !address2.isBlank()) {
      sb.append(" ").append(address2);
    }
    return sb.toString().trim();
  }

  private static void validate(String zipCode, String address1, String address2) {
    // 모두 비어있으면 유효 (선택 필드)
    boolean allEmpty =
        (zipCode == null || zipCode.isBlank())
            && (address1 == null || address1.isBlank())
            && (address2 == null || address2.isBlank());
    if (allEmpty) {
      return;
    }

    // 하나라도 입력했으면 기본 주소는 필수
    if (address1 == null || address1.isBlank()) {
      throw new UserException(INVALID_ADDRESS);
    }

    // 길이 검증
    if (zipCode != null && zipCode.length() > MAX_ZIP_CODE_LENGTH) {
      throw new UserException(INVALID_ADDRESS);
    }
    if (address1.length() > MAX_ADDRESS_LENGTH) {
      throw new UserException(INVALID_ADDRESS);
    }
    if (address2 != null && address2.length() > MAX_ADDRESS_LENGTH) {
      throw new UserException(INVALID_ADDRESS);
    }
  }
}
