package com.tickatch.user_service.customer.domain;

import com.tickatch.user_service.customer.domain.repository.dto.CustomerSearchCondition;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Customer 리포지토리 인터페이스.
 *
 * <p>도메인 레이어에서 정의하고, 인프라스트럭처 레이어에서 구현한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
public interface CustomerRepository {

  /**
   * Customer를 저장한다.
   *
   * @param customer 저장할 Customer 엔티티
   * @return 저장된 Customer 엔티티
   */
  Customer save(Customer customer);

  /**
   * ID로 Customer를 조회한다.
   *
   * @param id Customer ID (= Auth ID)
   * @return 조회된 Customer (없으면 empty)
   */
  Optional<Customer> findById(UUID id);

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

  /**
   * 검색 조건에 맞는 Customer 목록을 페이징하여 조회한다.
   *
   * @param condition 검색 조건
   * @param pageable 페이징 정보
   * @return 페이징된 Customer 목록
   */
  Page<Customer> findAllByCondition(CustomerSearchCondition condition, Pageable pageable);
}
