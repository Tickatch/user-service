package com.tickatch.user_service.seller.presentation.api.dto;

import com.tickatch.user_service.seller.application.service.command.dto.UpdateSettlementInfoCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * 정산 정보 수정 요청 DTO.
 *
 * <p>판매자의 정산 정보를 수정할 때 필요한 정보를 담는다. 승인된 판매자만 정산 정보를 수정할 수 있다.
 *
 * @param bankCode 은행 코드 (3자리)
 * @param accountNumber 계좌번호
 * @param accountHolder 예금주명
 * @author Tickatch
 * @since 1.0.0
 */
public record UpdateSettlementInfoRequest(
    @NotBlank(message = "은행 코드는 필수입니다")
        @Pattern(regexp = "^[0-9]{3}$", message = "은행 코드는 3자리 숫자여야 합니다")
        String bankCode,
    @NotBlank(message = "계좌번호는 필수입니다")
        @Pattern(regexp = "^[0-9-]{10,20}$", message = "계좌번호 형식이 올바르지 않습니다")
        String accountNumber,
    @NotBlank(message = "예금주명은 필수입니다") @Size(max = 100, message = "예금주명은 100자 이하여야 합니다")
        String accountHolder) {

  /**
   * Command 객체로 변환한다.
   *
   * @param sellerId 판매자 ID
   * @return UpdateSettlementInfoCommand
   */
  public UpdateSettlementInfoCommand toCommand(UUID sellerId) {
    return UpdateSettlementInfoCommand.of(sellerId, bankCode, accountNumber, accountHolder);
  }
}
