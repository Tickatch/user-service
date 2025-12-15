package com.tickatch.user_service.seller.application.service.command.dto;

import com.tickatch.user_service.common.domain.vo.Address;
import java.util.UUID;

/**
 * 판매자 생성 커맨드.
 *
 * @param authId Auth Service에서 발급된 ID
 * @param email 이메일
 * @param name 대표자명
 * @param phone 연락처
 * @param businessName 상호명
 * @param businessNumber 사업자등록번호
 * @param representativeName 대표자명
 * @param businessAddress 사업장 주소 (선택)
 */
public record CreateSellerCommand(
    UUID authId,
    String email,
    String name,
    String phone,
    String businessName,
    String businessNumber,
    String representativeName,
    Address businessAddress
) {

  public static CreateSellerCommand of(
      UUID authId,
      String email,
      String name,
      String phone,
      String businessName,
      String businessNumber,
      String representativeName,
      Address businessAddress
  ) {
    return new CreateSellerCommand(
        authId, email, name, phone, businessName, businessNumber, representativeName, businessAddress
    );
  }
}