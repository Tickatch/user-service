package com.tickatch.user_service.customer.domain.repository;

import com.tickatch.user_service.customer.domain.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Customer JPA 리포지토리.
 *
 * <p>Spring Data JPA 기본 CRUD 기능을 제공한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public interface CustomerJpaRepository extends JpaRepository<Customer, UUID> {

  /**
   * 이메일로 Customer를 조회한다.
   *
   * @param email 이메일
   * @return 조회된 Customer (없으면 empty)
   */
  Optional<Customer> findByEmail(String email);

  /**
   * 이메일로 Customer 존재 여부를 확인한다.
   *
   * @param email 이메일
   * @return 존재하면 true
   */
  boolean existsByEmail(String email);
}
