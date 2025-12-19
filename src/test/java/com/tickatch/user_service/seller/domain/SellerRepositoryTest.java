package com.tickatch.user_service.seller.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickatch.user_service.common.domain.vo.Address;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.config.QueryDslTestConfig;
import com.tickatch.user_service.seller.domain.repository.SellerRepositoryImpl;
import com.tickatch.user_service.seller.domain.repository.dto.SellerSearchCondition;
import com.tickatch.user_service.seller.domain.vo.SellerStatus;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest
@Import({QueryDslTestConfig.class, SellerRepositoryImpl.class})
@DisplayName("SellerRepository 테스트")
class SellerRepositoryTest {

  @Autowired private SellerRepository sellerRepository;

  private Seller seller1;
  private Seller seller2;
  private Seller seller3;

  @BeforeEach
  void setUp() {
    Address address = Address.of("12345", "서울시 강남구", "테헤란로 123");

    seller1 =
        Seller.create(
            UUID.randomUUID(),
            "seller1@test.com",
            "판매자1",
            "01012345678",
            "테스트상점1",
            "1234567890",
            "홍길동",
            address);

    seller2 =
        Seller.create(
            UUID.randomUUID(),
            "seller2@test.com",
            "판매자2",
            "01087654321",
            "테스트상점2",
            "0987654321",
            "김철수",
            address);

    seller3 =
        Seller.create(
            UUID.randomUUID(),
            "seller3@test.com",
            "판매자3",
            "01011112222",
            "우수상점",
            "1111111111",
            "이영희",
            address);
  }

  @Nested
  class save_테스트 {

    @Test
    void Seller정보를_저장한다() {
      Seller saved = sellerRepository.save(seller1);

      assertThat(saved.getId()).isEqualTo(seller1.getId());
      assertThat(saved.getEmail()).isEqualTo("seller1@test.com");
      assertThat(saved.getProfile().getName()).isEqualTo("판매자1");
      assertThat(saved.getBusinessInfo().getBusinessName()).isEqualTo("테스트상점1");
      assertThat(saved.getBusinessInfo().getBusinessNumber()).isEqualTo("1234567890");
      assertThat(saved.getSellerStatus()).isEqualTo(SellerStatus.PENDING);
      assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void Seller정보를_수정한다() {
      Seller saved = sellerRepository.save(seller1);
      saved.approve("admin@test.com");

      Optional<Seller> found = sellerRepository.findById(saved.getId());

      assertThat(found).isPresent();
      assertThat(found.get().getSellerStatus()).isEqualTo(SellerStatus.APPROVED);
      assertThat(found.get().getApprovedBy()).isEqualTo("admin@test.com");
      assertThat(found.get().getApprovedAt()).isNotNull();
    }
  }

  @Nested
  class FindById_테스트 {

    @Test
    void ID로_seller_조회에_성공한다() {
      sellerRepository.save(seller1);

      Optional<Seller> found = sellerRepository.findById(seller1.getId());

      assertThat(found).isPresent();
      assertThat(found.get().getEmail()).isEqualTo("seller1@test.com");
    }

    @Test
    void 존재하지_않는_ID_조회_시_empty를_반환한다() {
      Optional<Seller> found = sellerRepository.findById(UUID.randomUUID());

      assertThat(found).isEmpty();
    }
  }

  @Nested
  class FindByEmail_테스트 {

    @Test
    void 이메일로_Seller_조회를_성공한다() {
      sellerRepository.save(seller1);

      boolean exists = sellerRepository.existsByEmail("seller1@test.com");

      assertThat(exists).isTrue();
    }

    @Test
    void 존재하지_않는_이메일_조회_시_empty를_반환한다() {
      boolean exists = sellerRepository.existsByEmail("notfound@test.com");

      assertThat(exists).isFalse();
    }
  }

  @Nested
  class ExistsByEmail_테스트 {

    @Test
    void 사업자등록번호로_존재_여부를_확인한다() {
      sellerRepository.save(seller1);

      boolean exists = sellerRepository.existsByBusinessNumber("1234567890");

      assertThat(exists).isTrue();
    }

    @Test
    void 사업자등록번호로_존재_여부를_확인할_때_존재하지_않는다() {
      boolean exists = sellerRepository.existsByBusinessNumber("9999999999");

      assertThat(exists).isFalse();
    }
  }

  @Nested
  class FindAllByCondition_테스트 {

    @BeforeEach
    void 데이터초기화() {
      sellerRepository.save(seller1);
      seller2.approve("admin@test.com");
      sellerRepository.save(seller2);
      seller3.reject("서류 미비");
      sellerRepository.save(seller3);
    }

    @Test
    void 조건_없이_전체를_조회한다() {
      SellerSearchCondition condition = SellerSearchCondition.builder().build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Seller> result = sellerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void 이메일로_검색한다() {
      SellerSearchCondition condition = SellerSearchCondition.builder().email("seller1").build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Seller> result = sellerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getEmail()).isEqualTo("seller1@test.com");
    }

    @Test
    void 판매자의_PENDING_상태로_검색한다() {
      SellerSearchCondition condition =
          SellerSearchCondition.builder().sellerStatus(SellerStatus.PENDING).build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Seller> result = sellerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getProfile().getName()).isEqualTo("판매자1");
    }

    @Test
    void 판매자의_APPROVED_상태로_검색한다() {
      SellerSearchCondition condition =
          SellerSearchCondition.builder().sellerStatus(SellerStatus.APPROVED).build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Seller> result = sellerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getProfile().getName()).isEqualTo("판매자2");
    }

    @Test
    void 판매자의_REJECTED_상태로_검색한다() {
      SellerSearchCondition condition =
          SellerSearchCondition.builder().sellerStatus(SellerStatus.REJECTED).build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Seller> result = sellerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getProfile().getName()).isEqualTo("판매자3");
    }

    @Test
    void 상호명으로_검색한다() {
      SellerSearchCondition condition = SellerSearchCondition.builder().businessName("우수").build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Seller> result = sellerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getBusinessInfo().getBusinessName()).isEqualTo("우수상점");
    }

    @Test
    void 사업자등록번호로_검색한다() {
      SellerSearchCondition condition =
          SellerSearchCondition.builder().businessNumber("1234567890").build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Seller> result = sellerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getProfile().getName()).isEqualTo("판매자1");
    }

    @Test
    void 복합_조건으로_검색한다() {
      SellerSearchCondition condition =
          SellerSearchCondition.builder()
              .status(UserStatus.ACTIVE)
              .sellerStatus(SellerStatus.APPROVED)
              .build();
      PageRequest pageable = PageRequest.of(0, 10);

      Page<Seller> result = sellerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getProfile().getName()).isEqualTo("판매자2");
    }

    @Test
    void 페이지_동작을_확인한다() {
      SellerSearchCondition condition = SellerSearchCondition.builder().build();
      PageRequest pageable = PageRequest.of(0, 2);

      Page<Seller> result = sellerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getTotalPages()).isEqualTo(2);
      assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void 상호명_오름차순으로_정렬_동작을_확인한다() {
      SellerSearchCondition condition = SellerSearchCondition.builder().build();
      PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "businessName"));

      Page<Seller> result = sellerRepository.findAllByCondition(condition, pageable);

      assertThat(result.getContent().get(0).getBusinessInfo().getBusinessName()).isEqualTo("우수상점");
      assertThat(result.getContent().get(1).getBusinessInfo().getBusinessName())
          .isEqualTo("테스트상점1");
      assertThat(result.getContent().get(2).getBusinessInfo().getBusinessName())
          .isEqualTo("테스트상점2");
    }
  }
}
