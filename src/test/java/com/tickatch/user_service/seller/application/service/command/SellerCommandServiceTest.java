package com.tickatch.user_service.seller.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.config.QueryDslTestConfig;
import com.tickatch.user_service.seller.application.messaging.SellerLogEventPublisher;
import com.tickatch.user_service.seller.application.service.command.dto.CreateSellerCommand;
import com.tickatch.user_service.seller.application.service.command.dto.UpdateSellerProfileCommand;
import com.tickatch.user_service.seller.application.service.command.dto.UpdateSettlementInfoCommand;
import com.tickatch.user_service.seller.domain.Seller;
import com.tickatch.user_service.seller.domain.SellerRepository;
import com.tickatch.user_service.seller.domain.exception.SellerErrorCode;
import com.tickatch.user_service.seller.domain.exception.SellerException;
import com.tickatch.user_service.seller.domain.repository.SellerRepositoryImpl;
import com.tickatch.user_service.seller.domain.vo.SellerStatus;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@Import({QueryDslTestConfig.class, SellerRepositoryImpl.class, SellerCommandService.class})
@DisplayName("SellerCommandService 테스트")
class SellerCommandServiceTest {

  @Autowired private SellerCommandService sellerCommandService;

  @Autowired private SellerRepository sellerRepository;

  @Autowired private EntityManager entityManager;

  @MockitoBean private SellerLogEventPublisher logEventPublisher;

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @Nested
  @DisplayName("createSeller 테스트")
  class CreateSellerTest {

    @Test
    @DisplayName("판매자를 생성한다")
    void createSeller_success() {
      // given
      UUID authId = UUID.randomUUID();
      CreateSellerCommand command =
          CreateSellerCommand.of(
              authId,
              "seller@example.com",
              "김판매",
              "010-1234-5678",
              "판매상점",
              "1234567890",
              "김대표",
              null);

      // when
      UUID sellerId = sellerCommandService.createSeller(command);
      flushAndClear();

      // then
      Seller saved = sellerRepository.findById(sellerId).orElseThrow();
      assertThat(saved.getEmail()).isEqualTo("seller@example.com");
      assertThat(saved.getProfile().getName()).isEqualTo("김판매");
      assertThat(saved.getBusinessInfo().getBusinessName()).isEqualTo("판매상점");
      assertThat(saved.getBusinessInfo().getBusinessNumber()).isEqualTo("1234567890");
      assertThat(saved.getSellerStatus()).isEqualTo(SellerStatus.PENDING);
    }

    @Test
    @DisplayName("이미 존재하는 이메일이면 예외가 발생한다")
    void createSeller_duplicateEmail_throwsException() {
      // given
      Seller existing =
          Seller.create(
              UUID.randomUUID(),
              "seller@example.com",
              "기존판매자",
              "010-0000-0000",
              "기존상점",
              "0000000000",
              "기존대표",
              null);
      sellerRepository.save(existing);
      flushAndClear();

      CreateSellerCommand command =
          CreateSellerCommand.of(
              UUID.randomUUID(),
              "seller@example.com",
              "김판매",
              "010-1234-5678",
              "판매상점",
              "1234567890",
              "김대표",
              null);

      // when & then
      assertThatThrownBy(() -> sellerCommandService.createSeller(command))
          .isInstanceOf(SellerException.class)
          .satisfies(
              ex -> {
                SellerException sellerException = (SellerException) ex;
                assertThat(sellerException.getErrorCode())
                    .isEqualTo(SellerErrorCode.SELLER_ALREADY_EXISTS);
              });
    }

    @Test
    @DisplayName("이미 존재하는 사업자등록번호면 예외가 발생한다")
    void createSeller_duplicateBusinessNumber_throwsException() {
      // given
      Seller existing =
          Seller.create(
              UUID.randomUUID(),
              "existing@example.com",
              "기존판매자",
              "010-0000-0000",
              "기존상점",
              "1234567890",
              "기존대표",
              null);
      sellerRepository.save(existing);
      flushAndClear();

      CreateSellerCommand command =
          CreateSellerCommand.of(
              UUID.randomUUID(),
              "new@example.com",
              "김판매",
              "010-1234-5678",
              "판매상점",
              "1234567890",
              "김대표",
              null);

      // when & then
      assertThatThrownBy(() -> sellerCommandService.createSeller(command))
          .isInstanceOf(SellerException.class)
          .satisfies(
              ex -> {
                SellerException sellerException = (SellerException) ex;
                assertThat(sellerException.getErrorCode())
                    .isEqualTo(SellerErrorCode.BUSINESS_NUMBER_ALREADY_EXISTS);
              });
    }
  }

  @Nested
  @DisplayName("updateProfile 테스트")
  class UpdateProfileTest {

    @Test
    @DisplayName("판매자 프로필을 수정한다")
    void updateProfile_success() {
      // given
      Seller seller =
          Seller.create(
              UUID.randomUUID(),
              "seller@example.com",
              "김판매",
              "010-1234-5678",
              "판매상점",
              "1234567890",
              "김대표",
              null);
      sellerRepository.save(seller);
      flushAndClear();

      UUID sellerId = seller.getId();
      UpdateSellerProfileCommand command =
          UpdateSellerProfileCommand.of(sellerId, "이판매", "010-9999-8888");

      // when
      sellerCommandService.updateProfile(command);
      flushAndClear();

      // then
      Seller updated = sellerRepository.findById(sellerId).orElseThrow();
      assertThat(updated.getProfile().getName()).isEqualTo("이판매");
      assertThat(updated.getProfile().getPhone()).isEqualTo("01099998888");
    }

    @Test
    @DisplayName("존재하지 않는 판매자면 예외가 발생한다")
    void updateProfile_notFound_throwsException() {
      // given
      UUID sellerId = UUID.randomUUID();
      UpdateSellerProfileCommand command =
          UpdateSellerProfileCommand.of(sellerId, "이판매", "010-9999-8888");

      // when & then
      assertThatThrownBy(() -> sellerCommandService.updateProfile(command))
          .isInstanceOf(SellerException.class)
          .satisfies(
              ex -> {
                SellerException sellerException = (SellerException) ex;
                assertThat(sellerException.getErrorCode())
                    .isEqualTo(SellerErrorCode.SELLER_NOT_FOUND);
              });
    }
  }

  @Nested
  @DisplayName("updateSettlementInfo 테스트")
  class UpdateSettlementInfoTest {

    @Test
    @DisplayName("정산 정보를 수정한다")
    void updateSettlementInfo_success() {
      // given
      Seller seller =
          Seller.create(
              UUID.randomUUID(),
              "seller@example.com",
              "김판매",
              "010-1234-5678",
              "판매상점",
              "1234567890",
              "김대표",
              null);
      seller.approve("admin");
      sellerRepository.save(seller);
      flushAndClear();

      UUID sellerId = seller.getId();
      UpdateSettlementInfoCommand command =
          UpdateSettlementInfoCommand.of(sellerId, "088", "110123456789", "김판매");

      // when
      sellerCommandService.updateSettlementInfo(command);
      flushAndClear();

      // then
      Seller updated = sellerRepository.findById(sellerId).orElseThrow();
      assertThat(updated.getSettlementInfo().isComplete()).isTrue();
    }

    @Test
    @DisplayName("승인 전에는 정산 정보를 수정할 수 없다")
    void updateSettlementInfo_notApproved_throwsException() {
      // given
      Seller seller =
          Seller.create(
              UUID.randomUUID(),
              "seller@example.com",
              "김판매",
              "010-1234-5678",
              "판매상점",
              "1234567890",
              "김대표",
              null);
      sellerRepository.save(seller);
      flushAndClear();

      UUID sellerId = seller.getId();
      UpdateSettlementInfoCommand command =
          UpdateSettlementInfoCommand.of(sellerId, "088", "110123456789", "김판매");

      // when & then
      assertThatThrownBy(() -> sellerCommandService.updateSettlementInfo(command))
          .isInstanceOf(SellerException.class)
          .satisfies(
              ex -> {
                SellerException sellerException = (SellerException) ex;
                assertThat(sellerException.getErrorCode())
                    .isEqualTo(SellerErrorCode.CANNOT_UPDATE_SETTLEMENT_BEFORE_APPROVAL);
              });
    }
  }

  @Nested
  @DisplayName("approveSeller 테스트")
  class ApproveSellerTest {

    @Test
    @DisplayName("판매자를 승인한다")
    void approveSeller_success() {
      // given
      Seller seller =
          Seller.create(
              UUID.randomUUID(),
              "seller@example.com",
              "김판매",
              "010-1234-5678",
              "판매상점",
              "1234567890",
              "김대표",
              null);
      sellerRepository.save(seller);
      flushAndClear();

      UUID sellerId = seller.getId();

      // when
      sellerCommandService.approveSeller(sellerId, "admin");
      flushAndClear();

      // then
      Seller updated = sellerRepository.findById(sellerId).orElseThrow();
      assertThat(updated.getSellerStatus()).isEqualTo(SellerStatus.APPROVED);
      assertThat(updated.getApprovedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("이미 승인된 판매자면 예외가 발생한다")
    void approveSeller_alreadyApproved_throwsException() {
      // given
      Seller seller =
          Seller.create(
              UUID.randomUUID(),
              "seller@example.com",
              "김판매",
              "010-1234-5678",
              "판매상점",
              "1234567890",
              "김대표",
              null);
      seller.approve("admin");
      sellerRepository.save(seller);
      flushAndClear();

      UUID sellerId = seller.getId();

      // when & then
      assertThatThrownBy(() -> sellerCommandService.approveSeller(sellerId, "admin2"))
          .isInstanceOf(SellerException.class)
          .satisfies(
              ex -> {
                SellerException sellerException = (SellerException) ex;
                assertThat(sellerException.getErrorCode())
                    .isEqualTo(SellerErrorCode.SELLER_ALREADY_APPROVED);
              });
    }
  }

  @Nested
  @DisplayName("rejectSeller 테스트")
  class RejectSellerTest {

    @Test
    @DisplayName("판매자를 거절한다")
    void rejectSeller_success() {
      // given
      Seller seller =
          Seller.create(
              UUID.randomUUID(),
              "seller@example.com",
              "김판매",
              "010-1234-5678",
              "판매상점",
              "1234567890",
              "김대표",
              null);
      sellerRepository.save(seller);
      flushAndClear();

      UUID sellerId = seller.getId();

      // when
      sellerCommandService.rejectSeller(sellerId, "서류 미비");
      flushAndClear();

      // then
      Seller updated = sellerRepository.findById(sellerId).orElseThrow();
      assertThat(updated.getSellerStatus()).isEqualTo(SellerStatus.REJECTED);
      assertThat(updated.getRejectedReason()).isEqualTo("서류 미비");
    }
  }

  @Nested
  @DisplayName("suspendSeller 테스트")
  class SuspendSellerTest {

    @Test
    @DisplayName("판매자를 정지한다")
    void suspendSeller_success() {
      // given
      Seller seller =
          Seller.create(
              UUID.randomUUID(),
              "seller@example.com",
              "김판매",
              "010-1234-5678",
              "판매상점",
              "1234567890",
              "김대표",
              null);
      sellerRepository.save(seller);
      flushAndClear();

      UUID sellerId = seller.getId();

      // when
      sellerCommandService.suspendSeller(sellerId);
      flushAndClear();

      // then
      Seller updated = sellerRepository.findById(sellerId).orElseThrow();
      assertThat(updated.isSuspended()).isTrue();
    }
  }

  @Nested
  @DisplayName("activateSeller 테스트")
  class ActivateSellerTest {

    @Test
    @DisplayName("정지된 판매자를 활성화한다")
    void activateSeller_success() {
      // given
      Seller seller =
          Seller.create(
              UUID.randomUUID(),
              "seller@example.com",
              "김판매",
              "010-1234-5678",
              "판매상점",
              "1234567890",
              "김대표",
              null);
      seller.suspend();
      sellerRepository.save(seller);
      flushAndClear();

      UUID sellerId = seller.getId();

      // when
      sellerCommandService.activateSeller(sellerId);
      flushAndClear();

      // then
      Seller updated = sellerRepository.findById(sellerId).orElseThrow();
      assertThat(updated.isActive()).isTrue();
    }
  }

  @Nested
  @DisplayName("withdrawSeller 테스트")
  class WithdrawSellerTest {

    @Test
    @DisplayName("판매자를 탈퇴 처리한다")
    void withdrawSeller_success() {
      // given
      Seller seller =
          Seller.create(
              UUID.randomUUID(),
              "seller@example.com",
              "김판매",
              "010-1234-5678",
              "판매상점",
              "1234567890",
              "김대표",
              null);
      sellerRepository.save(seller);
      flushAndClear();

      UUID sellerId = seller.getId();

      // when
      sellerCommandService.withdrawSeller(sellerId);
      flushAndClear();

      // then
      Seller updated = sellerRepository.findById(sellerId).orElseThrow();
      assertThat(updated.isWithdrawn()).isTrue();
    }
  }
}
