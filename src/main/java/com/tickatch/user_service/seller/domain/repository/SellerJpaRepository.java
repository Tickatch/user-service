package com.tickatch.user_service.seller.domain.repository;

import com.tickatch.user_service.seller.domain.Seller;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Seller JPA 리포지토리.
 *
 * <p>Spring Data JPA 기본 CRUD 기능을 제공한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public interface SellerJpaRepository extends JpaRepository<Seller, UUID> {

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
  @Query(
      "SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Seller s "
          + "WHERE s.businessInfo.businessNumber = :businessNumber")
  boolean existsByBusinessNumber(@Param("businessNumber") String businessNumber);
}
