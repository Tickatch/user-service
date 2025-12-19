package com.tickatch.user_service.customer.domain.repository.dto;

import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.customer.domain.vo.CustomerGrade;
import lombok.Builder;
import lombok.Getter;

/**
 * Customer 검색 조건.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Builder
public class CustomerSearchCondition {

  private final String email;
  private final String name;
  private final String phone;
  private final UserStatus status;
  private final CustomerGrade grade;
}
