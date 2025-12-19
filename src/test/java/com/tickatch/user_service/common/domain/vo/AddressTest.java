package com.tickatch.user_service.common.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.common.domain.exception.UserErrorCode;
import com.tickatch.user_service.common.domain.exception.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Address 단위 테스트")
class AddressTest {

  @Nested
  class 생성_테스트 {

    @Test
    void 유효한_주소_정보로_생성한다() {
      Address address = Address.of("12345", "서울시 강남구", "테헤란로 123");
      assertThat(address.getZipCode()).isEqualTo("12345");
      assertThat(address.getAddress1()).isEqualTo("서울시 강남구");
      assertThat(address.getAddress2()).isEqualTo("테헤란로 123");
    }

    @Test
    void 빈_주소를_생성한다() {
      Address address = Address.empty();
      assertThat(address.isEmpty()).isTrue();
      assertThat(address.getZipCode()).isNull();
      assertThat(address.getAddress1()).isNull();
      assertThat(address.getAddress2()).isNull();
    }

    @Test
    void 모두_null로_생성하면_빈_주소를_생성한다() {
      Address address = Address.of(null, null, null);
      assertThat(address.isEmpty()).isTrue();
    }

    @Test
    void 상세_주소_없이_생성한다() {
      Address address = Address.of("12345", "서울시 강남구", null);
      assertThat(address.getAddress1()).isEqualTo("서울시 강남구");
      assertThat(address.getAddress2()).isNull();
      assertThat(address.isEmpty()).isFalse();
    }
  }

  @Nested
  class 검증_테스트 {

    @Test
    void 우편번호만_있고_기본_주소가_없으면_예외가_발생한다() {
      assertThatThrownBy(() -> Address.of("12345", null, null))
          .isInstanceOf(UserException.class)
          .satisfies(
              e -> {
                UserException ue = (UserException) e;
                assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.INVALID_ADDRESS);
              });
    }

    @Test
    void 상세_주소만_있고_기본_주소가_없으면_예외가_발생한다() {
      assertThatThrownBy(() -> Address.of(null, null, "상세주소"))
          .isInstanceOf(UserException.class)
          .satisfies(
              e -> {
                UserException ue = (UserException) e;
                assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.INVALID_ADDRESS);
              });
    }

    @Test
    void 우편번호가_10자리를_초과하면_예외가_발생한다() {
      assertThatThrownBy(() -> Address.of("12345678901", "서울시 강남구", null))
          .isInstanceOf(UserException.class)
          .satisfies(
              e -> {
                UserException ue = (UserException) e;
                assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.INVALID_ADDRESS);
              });
    }

    @Test
    void 기본_주소가_200자를_초과하면_예외가_발생한다() {
      String longAddress = "가".repeat(201);
      assertThatThrownBy(() -> Address.of("12345", longAddress, null))
          .isInstanceOf(UserException.class)
          .satisfies(
              e -> {
                UserException ue = (UserException) e;
                assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.INVALID_ADDRESS);
              });
    }

    @Test
    void 상세_주소가_200자를_초과하면_예외가_발생한다() {
      String longAddress = "가".repeat(201);
      assertThatThrownBy(() -> Address.of("12345", "서울시 강남구", longAddress))
          .isInstanceOf(UserException.class)
          .satisfies(
              e -> {
                UserException ue = (UserException) e;
                assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.INVALID_ADDRESS);
              });
    }
  }

  @Nested
  class 전체_주소_반환_테스트 {

    @Test
    void 전체_주소를_문자열로_반환한다() {
      Address address = Address.of("12345", "서울시 강남구", "테헤란로 123");
      String fullAddress = address.getFullAddress();
      assertThat(fullAddress).isEqualTo("(12345) 서울시 강남구 테헤란로 123");
    }

    @Test
    void 우편번호_없이_전체_주소를_반환한다() {
      Address address = Address.of(null, "서울시 강남구", "테헤란로 123");
      String fullAddress = address.getFullAddress();
      assertThat(fullAddress).isEqualTo("서울시 강남구 테헤란로 123");
    }

    @Test
    void 빈_주소는_빈_문자열로_반환한다() {
      Address address = Address.empty();
      String fullAddress = address.getFullAddress();
      assertThat(fullAddress).isEmpty();
    }
  }

  @Nested
  class 수정_테스트 {

    @Test
    void 주소를_수정할_수_있다() {
      Address original = Address.of("12345", "서울시 강남구", "테헤란로 123");
      Address updated = original.update("54321", "부산시 해운대구", "해운대로 456");
      assertThat(updated.getZipCode()).isEqualTo("54321");
      assertThat(updated.getAddress1()).isEqualTo("부산시 해운대구");
      // 원본 불변 확인
      assertThat(original.getZipCode()).isEqualTo("12345");
    }
  }

  @Nested
  class 동등성_테스트 {

    @Test
    void 같은_값이면_동등하다() {
      Address address1 = Address.of("12345", "서울시 강남구", "테헤란로 123");
      Address address2 = Address.of("12345", "서울시 강남구", "테헤란로 123");
      assertThat(address1).isEqualTo(address2);
      assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
    }

    @Test
    void 다른_값이면_동등하지_않다() {
      Address address1 = Address.of("12345", "서울시 강남구", "테헤란로 123");
      Address address2 = Address.of("12345", "서울시 강남구", "테헤란로 456");
      assertThat(address1).isNotEqualTo(address2);
    }
  }
}
