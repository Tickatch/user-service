package com.tickatch.user_service.seller.application.service.query.dto;

import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.seller.domain.Seller;
import com.tickatch.user_service.seller.domain.vo.SellerStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/** 판매자 조회 응답 DTO. */
public record SellerResponse(
    UUID id,
    String email,
    String name,
    String phone,
    String businessName,
    String formattedBusinessNumber,
    String representativeName,
    SellerStatus sellerStatus,
    UserStatus status,
    boolean hasSettlementInfo,
    LocalDateTime approvedAt,
    String approvedBy,
    String rejectedReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static SellerResponse from(Seller seller) {
    return new SellerResponse(
        seller.getId(),
        seller.getEmail(),
        seller.getProfile().getName(),
        seller.getProfile().getPhone(),
        seller.getBusinessInfo().getBusinessName(),
        seller.getBusinessInfo().getFormattedBusinessNumber(),
        seller.getBusinessInfo().getRepresentativeName(),
        seller.getSellerStatus(),
        seller.getStatus(),
        seller.getSettlementInfo() != null && seller.getSettlementInfo().isComplete(),
        seller.getApprovedAt(),
        seller.getApprovedBy(),
        seller.getRejectedReason(),
        seller.getCreatedAt(),
        seller.getUpdatedAt());
  }
}
