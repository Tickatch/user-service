package com.tickatch.user_service.seller.domain;

import com.tickatch.user_service.seller.domain.repository.dto.SellerSearchCondition;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Seller 리포지토리 인터페이스.
 *
 * <p>도메인 레이어에서 정의하고, 인프라스트럭처 레이어에서 구현한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public interface SellerRepository {

  /**
   * Seller를 저장한다.
   *
   * @param seller 저장할 Seller 엔티티
   * @return 저장된 Seller 엔티티
   */
  Seller save(Seller seller);

  /**
   * ID로 Seller를 조회한다.
   *
   * @param id Seller ID (= Auth ID)
   * @return 조회된 Seller (없으면 empty)
   */
  Optional<Seller> findById(UUID id);

  /**
   * 이메일로 Seller를 조회한다.
   *
   * @param email 이메일
   * @return 조회된 Seller (없으면 empty)
   */
  Optional<Seller> findByEmail(String email);

  /**
   * 이메일로 Seller 존재 여부를 확인한다.
   *
   * @param email 이메일
   * @return 존재하면 true
   */
  boolean existsByEmail(String email);

  /**
   * 사업자등록번호로 Seller 존재 여부를 확인한다.
   *
   * @param businessNumber 사업자등록번호
   * @return 존재하면 true
   */
  boolean existsByBusinessNumber(String businessNumber);

  /**
   * 검색 조건에 맞는 Seller 목록을 페이징하여 조회한다.
   *
   * @param condition 검색 조건
   * @param pageable 페이징 정보
   * @return 페이징된 Seller 목록
   */
  Page<Seller> findAllByCondition(SellerSearchCondition condition, Pageable pageable);
}
