package com.tickatch.user_service.seller.application.service.command.dto;

import java.util.UUID;

/**
 * 정산 정보 수정 커맨드.
 *
 * @param sellerId 판매자 ID
 * @param bankCode 은행 코드
 * @param accountNumber 계좌번호
 * @param accountHolder 예금주명
 */
public record UpdateSettlementInfoCommand(
    UUID sellerId,
    String bankCode,
    String accountNumber,
    String accountHolder
) {

  public static UpdateSettlementInfoCommand of(
      UUID sellerId,
      String bankCode,
      String accountNumber,
      String accountHolder
  ) {
    return new UpdateSettlementInfoCommand(sellerId, bankCode, accountNumber, accountHolder);
  }
}