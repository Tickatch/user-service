package com.tickatch.user_service.admin.domain.repository.dto;

import com.tickatch.user_service.admin.domain.vo.AdminRole;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Admin 검색 조건.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Builder
public class AdminSearchCondition {

  private final String email;
  private final String name;
  private final UserStatus status;
  private final AdminRole adminRole;
  private final String department;
}
