package com.tickatch.user_service.seller.application.service.command.dto;

import java.util.UUID;

/**
 * 판매자 프로필 수정 커맨드.
 *
 * @param sellerId 판매자 ID
 * @param name 이름
 * @param phone 연락처
 */
public record UpdateSellerProfileCommand(
    UUID sellerId,
    String name,
    String phone
) {

  public static UpdateSellerProfileCommand of(UUID sellerId, String name, String phone) {
    return new UpdateSellerProfileCommand(sellerId, name, phone);
  }
}