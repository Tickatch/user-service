package com.tickatch.user_service.seller.domain.repository.dto;

import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.seller.domain.vo.SellerStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Seller 검색 조건.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Getter
@Builder
public class SellerSearchCondition {

  private final String email;
  private final String name;
  private final UserStatus status;
  private final SellerStatus sellerStatus;
  private final String businessName;
  private final String businessNumber;
}