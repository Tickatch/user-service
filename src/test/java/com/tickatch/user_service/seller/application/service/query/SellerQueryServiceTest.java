package com.tickatch.user_service.seller.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.config.QueryDslTestConfig;
import com.tickatch.user_service.seller.application.service.query.dto.SellerResponse;
import com.tickatch.user_service.seller.application.service.query.dto.SellerSearchRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import({QueryDslTestConfig.class, SellerRepositoryImpl.class, SellerQueryService.class})
@DisplayName("SellerQueryService 테스트")
class SellerQueryServiceTest {

  @Autowired
  private SellerQueryService sellerQueryService;

  @Autowired
  private SellerRepository sellerRepository;

  @Autowired
  private EntityManager entityManager;

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @Nested
  @DisplayName("getSeller 테스트")
  class GetSellerTest {

    @Test
    @DisplayName("ID로 판매자를 조회한다")
    void getSeller_success() {
      // given
      Seller seller = Seller.create(
          UUID.randomUUID(), "seller@example.com", "김판매", "010-1234-5678",
          "판매상점", "1234567890", "김대표", null
      );
      sellerRepository.save(seller);
      flushAndClear();

      UUID sellerId = seller.getId();

      // when
      SellerResponse response = sellerQueryService.getSeller(sellerId);

      // then
      assertThat(response.id()).isEqualTo(sellerId);
      assertThat(response.email()).isEqualTo("seller@example.com");
      assertThat(response.name()).isEqualTo("김판매");
      assertThat(response.businessName()).isEqualTo("판매상점");
      assertThat(response.formattedBusinessNumber()).isEqualTo("123-45-67890");
      assertThat(response.sellerStatus()).isEqualTo(SellerStatus.PENDING);
      assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 ID면 예외가 발생한다")
    void getSeller_notFound_throwsException() {
      // given
      UUID sellerId = UUID.randomUUID();

      // when & then
      assertThatThrownBy(() -> sellerQueryService.getSeller(sellerId))
          .isInstanceOf(SellerException.class)
          .satisfies(ex -> {
            SellerException sellerException = (SellerException) ex;
            assertThat(sellerException.getErrorCode()).isEqualTo(SellerErrorCode.SELLER_NOT_FOUND);
          });
    }
  }

  @Nested
  @DisplayName("getSellerByEmail 테스트")
  class GetSellerByEmailTest {

    @Test
    @DisplayName("이메일로 판매자를 조회한다")
    void getSellerByEmail_success() {
      // given
      String email = "seller@example.com";
      Seller seller = Seller.create(
          UUID.randomUUID(), email, "김판매", "010-1234-5678",
          "판매상점", "1234567890", "김대표", null
      );
      sellerRepository.save(seller);
      flushAndClear();

      // when
      SellerResponse response = sellerQueryService.getSellerByEmail(email);

      // then
      assertThat(response.email()).isEqualTo(email);
      assertThat(response.name()).isEqualTo("김판매");
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 예외가 발생한다")
    void getSellerByEmail_notFound_throwsException() {
      // given
      String email = "notfound@example.com";

      // when & then
      assertThatThrownBy(() -> sellerQueryService.getSellerByEmail(email))
          .isInstanceOf(SellerException.class)
          .satisfies(ex -> {
            SellerException sellerException = (SellerException) ex;
            assertThat(sellerException.getErrorCode()).isEqualTo(SellerErrorCode.SELLER_NOT_FOUND);
          });
    }
  }

  @Nested
  @DisplayName("searchSellers 테스트")
  class SearchSellersTest {

    @Test
    @DisplayName("조건으로 판매자를 검색한다")
    void searchSellers_success() {
      // given
      Seller seller1 = Seller.create(UUID.randomUUID(), "seller1@example.com", "김판매", "010-1111-1111",
          "판매상점1", "1234567890", "김대표", null);
      Seller seller2 = Seller.create(UUID.randomUUID(), "seller2@example.com", "이판매", "010-2222-2222",
          "판매상점2", "0987654321", "이대표", null);
      sellerRepository.save(seller1);
      sellerRepository.save(seller2);
      flushAndClear();

      SellerSearchRequest request = new SellerSearchRequest(
          null, null, UserStatus.ACTIVE, SellerStatus.PENDING, null, null
      );
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<SellerResponse> result = sellerQueryService.searchSellers(request, pageable);

      // then
      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getContent()).extracting(SellerResponse::businessName)
          .containsExactlyInAnyOrder("판매상점1", "판매상점2");
    }

    @Test
    @DisplayName("승인된 판매자만 검색한다")
    void searchSellers_approvedOnly() {
      // given
      Seller pending = Seller.create(UUID.randomUUID(), "pending@example.com", "대기판매자", "010-1111-1111",
          "대기상점", "1111111111", "대기대표", null);
      Seller approved = Seller.create(UUID.randomUUID(), "approved@example.com", "승인판매자", "010-2222-2222",
          "승인상점", "2222222222", "승인대표", null);
      approved.approve("admin");
      sellerRepository.save(pending);
      sellerRepository.save(approved);
      flushAndClear();

      SellerSearchRequest request = new SellerSearchRequest(
          null, null, null, SellerStatus.APPROVED, null, null
      );
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<SellerResponse> result = sellerQueryService.searchSellers(request, pageable);

      // then
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).sellerStatus()).isEqualTo(SellerStatus.APPROVED);
    }
  }

  @Nested
  @DisplayName("existsByEmail 테스트")
  class ExistsByEmailTest {

    @Test
    @DisplayName("이메일이 존재하면 true를 반환한다")
    void existsByEmail_true() {
      // given
      String email = "exists@example.com";
      Seller seller = Seller.create(UUID.randomUUID(), email, "김판매", "010-1234-5678",
          "판매상점", "1234567890", "김대표", null);
      sellerRepository.save(seller);
      flushAndClear();

      // when
      boolean result = sellerQueryService.existsByEmail(email);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 false를 반환한다")
    void existsByEmail_false() {
      // given
      String email = "notexists@example.com";

      // when
      boolean result = sellerQueryService.existsByEmail(email);

      // then
      assertThat(result).isFalse();
    }
  }

  @Nested
  @DisplayName("existsByBusinessNumber 테스트")
  class ExistsByBusinessNumberTest {

    @Test
    @DisplayName("사업자등록번호가 존재하면 true를 반환한다")
    void existsByBusinessNumber_true() {
      // given
      String businessNumber = "1234567890";
      Seller seller = Seller.create(UUID.randomUUID(), "seller@example.com", "김판매", "010-1234-5678",
          "판매상점", businessNumber, "김대표", null);
      sellerRepository.save(seller);
      flushAndClear();

      // when
      boolean result = sellerQueryService.existsByBusinessNumber(businessNumber);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사업자등록번호가 존재하지 않으면 false를 반환한다")
    void existsByBusinessNumber_false() {
      // given
      String businessNumber = "0000000000";

      // when
      boolean result = sellerQueryService.existsByBusinessNumber(businessNumber);

      // then
      assertThat(result).isFalse();
    }
  }
}