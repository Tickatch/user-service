package com.tickatch.user_service.customer.application.service.query;

import com.tickatch.user_service.customer.application.service.query.dto.CustomerResponse;
import com.tickatch.user_service.customer.application.service.query.dto.CustomerSearchRequest;
import com.tickatch.user_service.customer.domain.Customer;
import com.tickatch.user_service.customer.domain.CustomerRepository;
import com.tickatch.user_service.customer.domain.exception.CustomerErrorCode;
import com.tickatch.user_service.customer.domain.exception.CustomerException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고객 조회 서비스.
 *
 * <p>고객 조회 작업을 처리한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerQueryService {

  private final CustomerRepository customerRepository;

  /**
   * ID로 고객을 조회한다.
   *
   * @param customerId 고객 ID
   * @return 고객 응답
   * @throws CustomerException 고객을 찾을 수 없는 경우
   */
  public CustomerResponse getCustomer(UUID customerId) {
    Customer customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(() -> new CustomerException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
    return CustomerResponse.from(customer);
  }

  /**
   * 이메일로 고객을 조회한다.
   *
   * @param email 이메일
   * @return 고객 응답
   * @throws CustomerException 고객을 찾을 수 없는 경우
   */
  public CustomerResponse getCustomerByEmail(String email) {
    Customer customer =
        customerRepository
            .findByEmail(email)
            .orElseThrow(() -> new CustomerException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
    return CustomerResponse.from(customer);
  }

  /**
   * 조건에 맞는 고객 목록을 페이징하여 조회한다.
   *
   * @param request 검색 요청
   * @param pageable 페이징 정보
   * @return 페이징된 고객 응답 목록
   */
  public Page<CustomerResponse> searchCustomers(CustomerSearchRequest request, Pageable pageable) {
    return customerRepository
        .findAllByCondition(request.toCondition(), pageable)
        .map(CustomerResponse::from);
  }

  /**
   * 이메일 존재 여부를 확인한다.
   *
   * @param email 이메일
   * @return 존재하면 true
   */
  public boolean existsByEmail(String email) {
    return customerRepository.existsByEmail(email);
  }
}
