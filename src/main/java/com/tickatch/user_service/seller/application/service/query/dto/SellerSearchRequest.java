package com.tickatch.user_service.seller.application.service.query.dto;

import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.seller.domain.repository.dto.SellerSearchCondition;
import com.tickatch.user_service.seller.domain.vo.SellerStatus;

/**
 * 판매자 검색 요청 DTO.
 */
public record SellerSearchRequest(
    String email,
    String name,
    UserStatus status,
    SellerStatus sellerStatus,
    String businessName,
    String businessNumber
) {

  public SellerSearchCondition toCondition() {
    return SellerSearchCondition.builder()
        .email(email)
        .name(name)
        .status(status)
        .sellerStatus(sellerStatus)
        .businessName(businessName)
        .businessNumber(businessNumber)
        .build();
  }
}