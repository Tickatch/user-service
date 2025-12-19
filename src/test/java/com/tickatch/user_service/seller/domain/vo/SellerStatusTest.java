package com.tickatch.user_service.seller.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("SellerStatus 테스트")
class SellerStatusTest {

  @ParameterizedTest
  @EnumSource(SellerStatus.class)
  void 모든_상태는_description을_가진다(SellerStatus status) {
    assertThat(status.getDescription()).isNotBlank();
  }

  @Nested
  class 상태_확인_테스트 {

    @Test
    void PENDING_상태를_확인한다() {
      SellerStatus status = SellerStatus.PENDING;

      assertThat(status.isPending()).isTrue();
      assertThat(status.isApproved()).isFalse();
      assertThat(status.isRejected()).isFalse();
      assertThat(status.isTerminal()).isFalse();
    }

    @Test
    void APPROVED_상태를_확인한다() {
      SellerStatus status = SellerStatus.APPROVED;

      assertThat(status.isPending()).isFalse();
      assertThat(status.isApproved()).isTrue();
      assertThat(status.isRejected()).isFalse();
      assertThat(status.isTerminal()).isTrue();
    }

    @Test
    void REJECTED_상태를_확인한다() {
      SellerStatus status = SellerStatus.REJECTED;

      assertThat(status.isPending()).isFalse();
      assertThat(status.isApproved()).isFalse();
      assertThat(status.isRejected()).isTrue();
      assertThat(status.isTerminal()).isTrue();
    }
  }

  @Nested
  class 상태_전이_가능_여부_테스트 {

    @Test
    void PENDING에서_승인이_가능하다() {
      assertThat(SellerStatus.PENDING.canApprove()).isTrue();
      assertThat(SellerStatus.APPROVED.canApprove()).isFalse();
      assertThat(SellerStatus.REJECTED.canApprove()).isFalse();
    }

    @Test
    void PENDING에서_거절이_가능하다() {
      assertThat(SellerStatus.PENDING.canReject()).isTrue();
      assertThat(SellerStatus.APPROVED.canReject()).isFalse();
      assertThat(SellerStatus.REJECTED.canReject()).isFalse();
    }
  }

  @Nested
  class 비즈니스_권한_확인_테스트 {

    @Test
    void APPROVED에서만_공연_등록이_가능하다() {
      assertThat(SellerStatus.PENDING.canRegisterPerformance()).isFalse();
      assertThat(SellerStatus.APPROVED.canRegisterPerformance()).isTrue();
      assertThat(SellerStatus.REJECTED.canRegisterPerformance()).isFalse();
    }

    @Test
    void APROVED에서만_정산_정보_수정이_가능하다() {
      assertThat(SellerStatus.PENDING.canUpdateSettlement()).isFalse();
      assertThat(SellerStatus.APPROVED.canUpdateSettlement()).isTrue();
      assertThat(SellerStatus.REJECTED.canUpdateSettlement()).isFalse();
    }
  }
}
