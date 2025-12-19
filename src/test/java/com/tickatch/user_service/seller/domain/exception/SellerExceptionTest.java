package com.tickatch.user_service.seller.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;

@DisplayName("SellerException 단위 테스트")
class SellerExceptionTest {

  @ParameterizedTest
  @EnumSource(SellerErrorCode.class)
  void 모든_에러코드는_status와_code를_가진다(SellerErrorCode errorCode) {
    assertThat(errorCode.getStatus()).isPositive();
    assertThat(errorCode.getCode()).isNotBlank();
  }

  @Test
  void 조회_에러코드는_404_상태코드를_가진다() {
    assertThat(SellerErrorCode.SELLER_NOT_FOUND.getStatus())
        .isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void 사업자_정보_검증_에러코드는_400_상태코드를_가진다() {
    assertThat(SellerErrorCode.INVALID_BUSINESS_NAME.getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(SellerErrorCode.INVALID_BUSINESS_NUMBER.getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(SellerErrorCode.INVALID_REPRESENTATIVE_NAME.getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(SellerErrorCode.INVALID_BUSINESS_ADDRESS.getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(SellerErrorCode.DUPLICATE_BUSINESS_NUMBER.getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void 정산_정보_검증_에러코드는_400_상태코드를_가진다() {
    assertThat(SellerErrorCode.INVALID_BANK_CODE.getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(SellerErrorCode.INVALID_ACCOUNT_NUMBER.getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(SellerErrorCode.INVALID_ACCOUNT_HOLDER.getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void 권한_에러코드는_403_상태코드를_가진다() {
    assertThat(SellerErrorCode.SELLER_NOT_APPROVED.getStatus())
        .isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(SellerErrorCode.SELLER_SUSPENDED.getStatus())
        .isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(SellerErrorCode.SELLER_WITHDRAWN.getStatus())
        .isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @Test
  void 비즈니스_규칙_에러코드는_422_상태코드를_가진다() {
    assertThat(SellerErrorCode.SELLER_NOT_PENDING.getStatus())
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    assertThat(SellerErrorCode.SELLER_ALREADY_APPROVED.getStatus())
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    assertThat(SellerErrorCode.SELLER_ALREADY_REJECTED.getStatus())
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    assertThat(SellerErrorCode.CANNOT_UPDATE_SETTLEMENT_BEFORE_APPROVAL.getStatus())
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
  }

  @Test
  void 에러코드와_인자로_예외를_생성한다() {
    String sellerId = "seller-123";
    SellerException exception = new SellerException(SellerErrorCode.SELLER_NOT_FOUND, sellerId);
    assertThat(exception.getErrorCode()).isEqualTo(SellerErrorCode.SELLER_NOT_FOUND);
    assertThat(exception.getErrorArgs()).containsExactly(sellerId);
  }
}
