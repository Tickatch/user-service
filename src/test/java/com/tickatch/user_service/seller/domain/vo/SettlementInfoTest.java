package com.tickatch.user_service.seller.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.seller.domain.exception.SellerErrorCode;
import com.tickatch.user_service.seller.domain.exception.SellerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("SettlementInfo 테스트")
class SettlementInfoTest {

  private static final String VALID_BANK_CODE = "004"; // KB국민
  private static final String VALID_ACCOUNT_NUMBER = "12345678901234";
  private static final String VALID_ACCOUNT_HOLDER = "홍길동";

  @Nested
  class 생성_테스트 {

    @Test
    void 유효한_정보로_생성한다() {
      SettlementInfo settlementInfo = SettlementInfo.of(VALID_BANK_CODE, VALID_ACCOUNT_NUMBER, VALID_ACCOUNT_HOLDER);
      assertThat(settlementInfo.getBankCode()).isEqualTo(VALID_BANK_CODE);
      assertThat(settlementInfo.getAccountNumber()).isEqualTo(VALID_ACCOUNT_NUMBER);
      assertThat(settlementInfo.getAccountHolder()).isEqualTo(VALID_ACCOUNT_HOLDER);
    }

    @Test
    void 계좌번호_하이픈을_제거한다() {
      SettlementInfo settlementInfo = SettlementInfo.of(VALID_BANK_CODE, "1234-5678-901234", VALID_ACCOUNT_HOLDER);
      assertThat(settlementInfo.getAccountNumber()).isEqualTo("12345678901234");
    }

    @Test
    void 빈_정산_정보를_생성한다() {
      SettlementInfo settlementInfo = SettlementInfo.empty();
      assertThat(settlementInfo.isEmpty()).isTrue();
      assertThat(settlementInfo.isComplete()).isFalse();
    }
  }

  @Nested
  class 은행_코드_검증_테스트 {

    @ParameterizedTest
    @NullAndEmptySource
    void 은행_코드가_null이거나_빈_값이면_예외가_발생한다(String invalidCode) {
      assertThatThrownBy(() -> SettlementInfo.of(invalidCode, VALID_ACCOUNT_NUMBER, VALID_ACCOUNT_HOLDER))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_BANK_CODE);
          });
    }

    @ParameterizedTest
    @ValueSource(strings = {"001", "999", "abc", "00"})
    void 유효하지_않은_은행_코드는_예외가_발생한다(String invalidCode) {
      assertThatThrownBy(() -> SettlementInfo.of(invalidCode, VALID_ACCOUNT_NUMBER, VALID_ACCOUNT_HOLDER))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_BANK_CODE);
          });
    }

    @ParameterizedTest
    @ValueSource(strings = {"004", "088", "020", "081", "090", "092"})
    void 유효한_은행_코드인_지_확인한다(String validCode) {
      SettlementInfo settlementInfo = SettlementInfo.of(validCode, VALID_ACCOUNT_NUMBER, VALID_ACCOUNT_HOLDER);
      assertThat(settlementInfo.getBankCode()).isEqualTo(validCode);
    }
  }

  @Nested
  class 계좌번호_검증_테스트 {

    @ParameterizedTest
    @NullAndEmptySource
    void 계좌번호가_null이거나_빈_값이면_예외가_발생한다(String invalidNumber) {
      assertThatThrownBy(() -> SettlementInfo.of(VALID_BANK_CODE, invalidNumber, VALID_ACCOUNT_HOLDER))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_ACCOUNT_NUMBER);
          });
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "123456789012345", "abcdefghij", "12-34-56"})
    void 유효하지_않은_계좌번호는_예외가_발생한다(String invalidNumber) {
      assertThatThrownBy(() -> SettlementInfo.of(VALID_BANK_CODE, invalidNumber, VALID_ACCOUNT_HOLDER))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_ACCOUNT_NUMBER);
          });
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567890", "12345678901234", "1234-5678-9012"})
    void 유효한_계좌번호를_검증한다(String validNumber) {
      SettlementInfo settlementInfo = SettlementInfo.of(VALID_BANK_CODE, validNumber, VALID_ACCOUNT_HOLDER);
      assertThat(settlementInfo.getAccountNumber()).matches("^[0-9]{10,14}$");
    }
  }

  @Nested
  class 예금주명_검증_테스트 {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void 예금주명이_null이거나_빈_값이면_예외가_발생한다(String invalidHolder) {
      assertThatThrownBy(() -> SettlementInfo.of(VALID_BANK_CODE, VALID_ACCOUNT_NUMBER, invalidHolder))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_ACCOUNT_HOLDER);
          });
    }

    @Test
    void 예금주명이_100자를_초과하면_예외가_발생한다() {
      String longHolder = "가".repeat(101);
      assertThatThrownBy(() -> SettlementInfo.of(VALID_BANK_CODE, VALID_ACCOUNT_NUMBER, longHolder))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_ACCOUNT_HOLDER);
          });
    }
  }

  @Nested
  class 상태_확인_테스트 {

    @Test
    void 모든_필드_입력_시_isComplete에서_true가_반환된다() {
      SettlementInfo settlementInfo = SettlementInfo.of(VALID_BANK_CODE, VALID_ACCOUNT_NUMBER, VALID_ACCOUNT_HOLDER);
      assertThat(settlementInfo.isComplete()).isTrue();
      assertThat(settlementInfo.isEmpty()).isFalse();
    }

    @Test
    void 빈_정산_정보는_isEmpty_true를_반환_isComplete는_false를_반환한다() {
      SettlementInfo settlementInfo = SettlementInfo.empty();
      assertThat(settlementInfo.isEmpty()).isTrue();
      assertThat(settlementInfo.isComplete()).isFalse();
    }
  }

  @Nested
  class 마스킹_테스트 {

    @Test
    void 계좌번호_마스킹을_한다() {
      SettlementInfo settlementInfo = SettlementInfo.of(VALID_BANK_CODE, "12345678901234", VALID_ACCOUNT_HOLDER);
      String masked = settlementInfo.getMaskedAccountNumber();
      assertThat(masked).isEqualTo("1234**********");
    }
  }

  @Nested
  class 수정_테스트 {

    @Test
    void 정산_정보를_수정한다() {
      SettlementInfo original = SettlementInfo.of(VALID_BANK_CODE, VALID_ACCOUNT_NUMBER, VALID_ACCOUNT_HOLDER);
      SettlementInfo updated = original.update("088", "09876543210987", "김철수");
      assertThat(updated.getBankCode()).isEqualTo("088");
      assertThat(updated.getAccountHolder()).isEqualTo("김철수");
      // 원본 불변 확인
      assertThat(original.getBankCode()).isEqualTo(VALID_BANK_CODE);
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_값이면_동등하다() {
      SettlementInfo info1 = SettlementInfo.of(VALID_BANK_CODE, VALID_ACCOUNT_NUMBER, VALID_ACCOUNT_HOLDER);
      SettlementInfo info2 = SettlementInfo.of(VALID_BANK_CODE, VALID_ACCOUNT_NUMBER, VALID_ACCOUNT_HOLDER);
      assertThat(info1).isEqualTo(info2);
      assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
    }
  }
}