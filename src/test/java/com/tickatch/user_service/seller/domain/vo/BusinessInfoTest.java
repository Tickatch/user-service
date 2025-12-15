package com.tickatch.user_service.seller.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.common.domain.vo.Address;
import com.tickatch.user_service.seller.domain.exception.SellerErrorCode;
import com.tickatch.user_service.seller.domain.exception.SellerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("BusinessInfo 테스트")
class BusinessInfoTest {

  @Nested
  class 생성_테스트 {
    @Test
    void 유효한_정보로_생성한다() {
      Address address = Address.of("12345", "서울시 강남구", "테헤란로 123");
      BusinessInfo businessInfo = BusinessInfo.of("테스트 상점", "1234567890", "홍길동", address);
      assertThat(businessInfo.getBusinessName()).isEqualTo("테스트 상점");
      assertThat(businessInfo.getBusinessNumber()).isEqualTo("1234567890");
      assertThat(businessInfo.getRepresentativeName()).isEqualTo("홍길동");
      assertThat(businessInfo.getBusinessAddress()).isNotNull();
    }

    @Test
    void 사업자등록번호_하이픈을_제거한다() {
      BusinessInfo businessInfo = BusinessInfo.of("테스트 상점", "123-45-67890", "홍길동", null);
      assertThat(businessInfo.getBusinessNumber()).isEqualTo("1234567890");
    }

    @Test
    void 사업장_주소_없이_생성한다() {
      BusinessInfo businessInfo = BusinessInfo.of("테스트 상점", "1234567890", "홍길동", null);
      assertThat(businessInfo.getBusinessAddress()).isNotNull();
      assertThat(businessInfo.getBusinessAddress().isEmpty()).isTrue();
    }
  }

  @Nested
  class 상호명_검증_테스트 {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void 상호명이_null이거나_빈_값이면_예외를_발생시킨다(String invalidName) {
      assertThatThrownBy(() -> BusinessInfo.of(invalidName, "1234567890", "홍길동", null))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_BUSINESS_NAME);
          });
    }

    @Test
    void 상호명이_200자를_초과하면_예외가_발생한다() {
      String longName = "가".repeat(201);
      assertThatThrownBy(() -> BusinessInfo.of(longName, "1234567890", "홍길동", null))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_BUSINESS_NAME);
          });
    }
  }

  @Nested
  class 사업자등록번호_검증_테스트 {

    @ParameterizedTest
    @NullAndEmptySource
    void 사업자등록번호가_null이거나_빈_값이면_예외가_발생한다(String invalidNumber) {
      assertThatThrownBy(() -> BusinessInfo.of("테스트 상점", invalidNumber, "홍길동", null))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_BUSINESS_NUMBER);
          });
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "12345678901", "abcdefghij", "123-456-789"})
    void 사업자등록번호가_10자리_숫자가_아니면_예외가_발생한다(String invalidNumber) {
      assertThatThrownBy(() -> BusinessInfo.of("테스트 상점", invalidNumber, "홍길동", null))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_BUSINESS_NUMBER);
          });
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567890", "123-45-67890", "1234-56-7890"})
    void 유효한_사업자등록번호_형식이다(String validNumber) {
      BusinessInfo businessInfo = BusinessInfo.of("테스트 상점", validNumber, "홍길동", null);
      assertThat(businessInfo.getBusinessNumber()).hasSize(10);
    }
  }

  @Nested
  class 대표자명_검증_테스트 {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void 대표자명이_null이거나_빈_값이면_예외가_발생한다(String invalidName) {
      assertThatThrownBy(() -> BusinessInfo.of("테스트 상점", "1234567890", invalidName, null))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_REPRESENTATIVE_NAME);
          });
    }

    @Test
    void 대표자명이_100자를_초과하면_예외가_발생한다() {
      String longName = "가".repeat(101);
      assertThatThrownBy(() -> BusinessInfo.of("테스트 상점", "1234567890", longName, null))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_REPRESENTATIVE_NAME);
          });
    }
  }

  @Nested
  class 포맷팅_테스트 {

    @Test
    void 사업자등록번호_포맷팅이_가능하다() {
      BusinessInfo businessInfo = BusinessInfo.of("테스트 상점", "1234567890", "홍길동", null);
      String formatted = businessInfo.getFormattedBusinessNumber();
      assertThat(formatted).isEqualTo("123-45-67890");
    }
  }

  @Nested
  class 수정_테스트 {

    @Test
    void 사업자_정보를_수정할_수_있다() {
      BusinessInfo original = BusinessInfo.of("원래 상점", "1234567890", "홍길동", null);
      BusinessInfo updated = original.update("새 상점", "0987654321", "김철수", null);
      assertThat(updated.getBusinessName()).isEqualTo("새 상점");
      assertThat(updated.getBusinessNumber()).isEqualTo("0987654321");
      // 원본 불변 확인
      assertThat(original.getBusinessName()).isEqualTo("원래 상점");
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_값이면_동등하다() {
      BusinessInfo info1 = BusinessInfo.of("테스트 상점", "1234567890", "홍길동", null);
      BusinessInfo info2 = BusinessInfo.of("테스트 상점", "123-45-67890", "홍길동", null);
      assertThat(info1).isEqualTo(info2);
      assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
    }
  }
}