package com.tickatch.user_service.seller.domain.vo;

import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.INVALID_ACCOUNT_HOLDER;
import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.INVALID_ACCOUNT_NUMBER;
import static com.tickatch.user_service.seller.domain.exception.SellerErrorCode.INVALID_BANK_CODE;

import com.tickatch.user_service.seller.domain.exception.SellerException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 정산 정보 Value Object.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementInfo {

  private static final int MAX_ACCOUNT_HOLDER_LENGTH = 100;
  private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^[0-9]{10,14}$");

  /**
   * 유효한 은행 코드 목록.
   */
  private static final Set<String> VALID_BANK_CODES = Set.of(
      "004", // KB국민
      "088", // 신한
      "020", // 우리
      "081", // 하나
      "003", // IBK기업
      "011", // NH농협
      "023", // SC제일
      "027", // 한국씨티
      "039", // 경남
      "034", // 광주
      "031", // 대구
      "032", // 부산
      "037", // 전북
      "035", // 제주
      "007", // 수협
      "045", // 새마을금고
      "048", // 신협
      "064", // 산림조합
      "071", // 우체국
      "089", // K뱅크
      "090", // 카카오뱅크
      "092"  // 토스뱅크
  );

  /**
   * 은행 코드.
   */
  @Column(name = "bank_code", length = 10)
  private String bankCode;

  /**
   * 계좌번호.
   */
  @Column(name = "account_number", length = 50)
  private String accountNumber;

  /**
   * 예금주명.
   */
  @Column(name = "account_holder", length = 100)
  private String accountHolder;

  private SettlementInfo(String bankCode, String accountNumber, String accountHolder) {
    this.bankCode = bankCode;
    this.accountNumber = accountNumber;
    this.accountHolder = accountHolder;
  }

  /**
   * SettlementInfo 생성.
   *
   * @param bankCode 은행 코드
   * @param accountNumber 계좌번호
   * @param accountHolder 예금주명
   * @return SettlementInfo 인스턴스
   * @throws SellerException 유효성 검증 실패 시
   */
  public static SettlementInfo of(String bankCode, String accountNumber, String accountHolder) {
    validateBankCode(bankCode);
    validateAccountNumber(accountNumber);
    validateAccountHolder(accountHolder);

    String normalizedNumber = normalizeAccountNumber(accountNumber);
    return new SettlementInfo(bankCode, normalizedNumber, accountHolder.trim());
  }

  /**
   * 빈 정산 정보 생성.
   *
   * @return 빈 SettlementInfo 인스턴스
   */
  public static SettlementInfo empty() {
    return new SettlementInfo(null, null, null);
  }

  /**
   * 정산 정보 수정.
   *
   * @param bankCode 새 은행 코드
   * @param accountNumber 새 계좌번호
   * @param accountHolder 새 예금주명
   * @return 수정된 SettlementInfo
   */
  public SettlementInfo update(String bankCode, String accountNumber, String accountHolder) {
    return SettlementInfo.of(bankCode, accountNumber, accountHolder);
  }

  /**
   * 정산 정보가 비어있는지 확인.
   *
   * @return 비어있으면 true
   */
  public boolean isEmpty() {
    return (bankCode == null || bankCode.isBlank())
        && (accountNumber == null || accountNumber.isBlank())
        && (accountHolder == null || accountHolder.isBlank());
  }

  /**
   * 정산 정보가 완전한지 확인 (모든 필드 입력).
   *
   * @return 완전하면 true
   */
  public boolean isComplete() {
    return bankCode != null && !bankCode.isBlank()
        && accountNumber != null && !accountNumber.isBlank()
        && accountHolder != null && !accountHolder.isBlank();
  }

  private static void validateBankCode(String bankCode) {
    if (bankCode == null || bankCode.isBlank()) {
      throw new SellerException(INVALID_BANK_CODE);
    }
    if (!VALID_BANK_CODES.contains(bankCode)) {
      throw new SellerException(INVALID_BANK_CODE);
    }
  }

  private static void validateAccountNumber(String accountNumber) {
    if (accountNumber == null || accountNumber.isBlank()) {
      throw new SellerException(INVALID_ACCOUNT_NUMBER);
    }
    String normalized = accountNumber.replaceAll("-", "");
    if (!ACCOUNT_NUMBER_PATTERN.matcher(normalized).matches()) {
      throw new SellerException(INVALID_ACCOUNT_NUMBER);
    }
  }

  private static void validateAccountHolder(String accountHolder) {
    if (accountHolder == null || accountHolder.isBlank()) {
      throw new SellerException(INVALID_ACCOUNT_HOLDER);
    }
    if (accountHolder.length() > MAX_ACCOUNT_HOLDER_LENGTH) {
      throw new SellerException(INVALID_ACCOUNT_HOLDER);
    }
  }

  private static String normalizeAccountNumber(String accountNumber) {
    return accountNumber.replaceAll("-", "");
  }

  /**
   * 마스킹된 계좌번호 반환.
   *
   * @return 앞 4자리만 표시 (예: 1234******)
   */
  public String getMaskedAccountNumber() {
    if (accountNumber == null || accountNumber.length() < 4) {
      return accountNumber;
    }
    return accountNumber.substring(0, 4) + "*".repeat(accountNumber.length() - 4);
  }
}