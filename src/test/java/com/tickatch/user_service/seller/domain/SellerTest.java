package com.tickatch.user_service.seller.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.common.domain.exception.UserErrorCode;
import com.tickatch.user_service.common.domain.exception.UserException;
import com.tickatch.user_service.common.domain.vo.Address;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.seller.domain.exception.SellerErrorCode;
import com.tickatch.user_service.seller.domain.exception.SellerException;
import com.tickatch.user_service.seller.domain.vo.SellerStatus;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Seller 테스트")
class SellerTest {

  private UUID authId;
  private String email;
  private String name;
  private String phone;
  private String businessName;
  private String businessNumber;
  private String representativeName;
  private Address businessAddress;

  @BeforeEach
  void setUp() {
    authId = UUID.randomUUID();
    email = "seller@test.com";
    name = "판매자";
    phone = "01012345678";
    businessName = "테스트 상점";
    businessNumber = "1234567890";
    representativeName = "홍길동";
    businessAddress = Address.of("12345", "서울시 강남구", "테헤란로 123");
  }

  private Seller createSeller() {
    return Seller.create(authId, email, name, phone, businessName, businessNumber, representativeName, businessAddress);
  }

  @Nested
  class 생성_테스트 {

    @Test
    void 유효한_정보로_판매자를_생성한다() {
      Seller seller = createSeller();
      assertThat(seller.getId()).isEqualTo(authId);
      assertThat(seller.getEmail()).isEqualTo(email);
      assertThat(seller.getProfile().getName()).isEqualTo(name);
      assertThat(seller.getBusinessInfo().getBusinessName()).isEqualTo(businessName);
      assertThat(seller.getBusinessInfo().getBusinessNumber()).isEqualTo(businessNumber);
      assertThat(seller.getSellerStatus()).isEqualTo(SellerStatus.PENDING);
      assertThat(seller.getStatus()).isEqualTo(UserStatus.ACTIVE);
      assertThat(seller.getSettlementInfo().isEmpty()).isTrue();
    }

    @Test
    void 사업장_주소_없이_생성한다() {
      Seller seller = Seller.create(authId, email, name, phone, businessName, businessNumber, representativeName, null);
      assertThat(seller.getBusinessInfo().getBusinessAddress().isEmpty()).isTrue();
    }
  }

  @Nested
  class 승인_테스트 {

    @Test
    void PENDING_상태에서_승인한다() {
      Seller seller = createSeller();
      String approvedBy = "admin@test.com";
      seller.approve(approvedBy);
      assertThat(seller.isApproved()).isTrue();
      assertThat(seller.getSellerStatus()).isEqualTo(SellerStatus.APPROVED);
      assertThat(seller.getApprovedAt()).isNotNull();
      assertThat(seller.getApprovedBy()).isEqualTo(approvedBy);
      assertThat(seller.getRejectedReason()).isNull();
    }

    @Test
    void 이미_승인된_판매자_승인_시_예외가_발생한다() {
      Seller seller = createSeller();
      seller.approve("admin@test.com");
      assertThatThrownBy(() -> seller.approve("admin@test.com"))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.SELLER_ALREADY_APPROVED);
          });
    }

    @Test
    void 거절된_판매자_승인_시_예외가_발생한다() {
      Seller seller = createSeller();
      seller.reject("서류 미비");
      assertThatThrownBy(() -> seller.approve("admin@test.com"))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.SELLER_ALREADY_REJECTED);
          });
    }

    @Test
    void 탈퇴한_판매자_승인_시_예외가_발생한다() {
      Seller seller = createSeller();
      seller.withdraw();
      assertThatThrownBy(() -> seller.approve("admin@test.com"))
          .isInstanceOf(UserException.class)
          .satisfies(e -> {
            UserException ue = (UserException) e;
            assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.USER_ALREADY_WITHDRAWN);
          });
    }
  }

  @Nested
  class 거절_테스트 {

    @Test
    void PENDING_상태에서_거절한다() {
      Seller seller = createSeller();
      String reason = "서류 미비";
      seller.reject(reason);
      assertThat(seller.isRejected()).isTrue();
      assertThat(seller.getSellerStatus()).isEqualTo(SellerStatus.REJECTED);
      assertThat(seller.getRejectedReason()).isEqualTo(reason);
      assertThat(seller.getApprovedAt()).isNull();
      assertThat(seller.getApprovedBy()).isNull();
    }

    @Test
    void 거절_사유_없이_거절_시_예외가_발생한다() {
      Seller seller = createSeller();
      assertThatThrownBy(() -> seller.reject(null))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_REJECTION_REASON);
          });
    }

    @Test
    void 빈_거절_사유로_거절_시_예외가_발생한다() {
      Seller seller = createSeller();
      assertThatThrownBy(() -> seller.reject("   "))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.INVALID_REJECTION_REASON);
          });
    }
  }

  @Nested
  class 사업자_정보_수정_테스트 {

    @Test
    void 사업자_정보를_수정한다() {
      Seller seller = createSeller();
      String newBusinessName = "새 상점";
      String newBusinessNumber = "0987654321";
      seller.updateBusinessInfo(newBusinessName, newBusinessNumber, representativeName, businessAddress);
      assertThat(seller.getBusinessInfo().getBusinessName()).isEqualTo(newBusinessName);
      assertThat(seller.getBusinessInfo().getBusinessNumber()).isEqualTo(newBusinessNumber);
    }

    @Test
    void 탈퇴한_판매자는_사업자_정보_수정이_불가능하다() {
      Seller seller = createSeller();
      seller.withdraw();
      assertThatThrownBy(() -> seller.updateBusinessInfo("새 상점", "0987654321", "김철수", null))
          .isInstanceOf(UserException.class)
          .satisfies(e -> {
            UserException ue = (UserException) e;
            assertThat(ue.getErrorCode()).isEqualTo(UserErrorCode.USER_ALREADY_WITHDRAWN);
          });
    }
  }

  @Nested
  class 정산_정보_수정_테스트 {

    @Test
    void 승인된_판매자는_정산_정보_수정이_가능하다() {
      Seller seller = createSeller();
      seller.approve("admin@test.com");
      seller.updateSettlementInfo("004", "12345678901234", "홍길동");
      assertThat(seller.getSettlementInfo().getBankCode()).isEqualTo("004");
      assertThat(seller.getSettlementInfo().getAccountNumber()).isEqualTo("12345678901234");
      assertThat(seller.getSettlementInfo().getAccountHolder()).isEqualTo("홍길동");
    }

    @Test
    void PENDING_상태에서_정산_정보_수정_시_예외가_발생한다() {
      Seller seller = createSeller();
      assertThatThrownBy(() -> seller.updateSettlementInfo("004", "12345678901234", "홍길동"))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.CANNOT_UPDATE_SETTLEMENT_BEFORE_APPROVAL);
          });
    }

    @Test
    void 거절된_판매자는_정산_정보_수정이_불가능하다() {
      Seller seller = createSeller();
      seller.reject("서류 미비");
      assertThatThrownBy(() -> seller.updateSettlementInfo("004", "12345678901234", "홍길동"))
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.CANNOT_UPDATE_SETTLEMENT_BEFORE_APPROVAL);
          });
    }
  }

  @Nested
  class 공연_등록_가능_여부_테스트 {

    @Test
    void 승인된_활성_판매자는_공연_등록이_가능하다() {
      Seller seller = createSeller();
      seller.approve("admin@test.com");
      assertThat(seller.canRegisterPerformance()).isTrue();
    }

    @Test
    void PENDING_상태에서는_공연_등록이_불가능하다() {
      Seller seller = createSeller();
      assertThat(seller.canRegisterPerformance()).isFalse();
    }

    @Test
    void 정지된_판매자는_공연_등록이_불가능하다() {
      Seller seller = createSeller();
      seller.approve("admin@test.com");
      seller.suspend();
      assertThat(seller.canRegisterPerformance()).isFalse();
    }

    @Test
    void 공연_등록_불가_시_검증_예외가_발생한다() {
      Seller seller = createSeller();
      assertThatThrownBy(seller::validateCanRegisterPerformance)
          .isInstanceOf(SellerException.class)
          .satisfies(e -> {
            SellerException se = (SellerException) e;
            assertThat(se.getErrorCode()).isEqualTo(SellerErrorCode.CANNOT_REGISTER_PERFORMANCE);
          });
    }
  }

  @Nested
  class 상태_변경_테스트 {

    @Test
    void 정지_처리가_가능하다() {
      Seller seller = createSeller();
      seller.suspend();
      assertThat(seller.isSuspended()).isTrue();
    }

    @Test
    void 정지_해제가_가능하다() {
      Seller seller = createSeller();
      seller.suspend();
      seller.activate();
      assertThat(seller.isActive()).isTrue();
    }

    @Test
    void 탈퇴_처리가_가능하다() {
      Seller seller = createSeller();
      seller.withdraw();
      assertThat(seller.isWithdrawn()).isTrue();
    }
  }
}