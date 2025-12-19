package com.tickatch.user_service.seller.presentation.api.dto;

import com.tickatch.user_service.seller.application.service.command.dto.UpdateSellerProfileCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * 판매자 프로필 수정 요청 DTO.
 *
 * <p>판매자 프로필을 수정할 때 필요한 정보를 담는다.
 *
 * @param name 이름
 * @param phone 연락처
 * @author Tickatch
 * @since 1.0.0
 */
public record UpdateSellerProfileRequest(
    @NotBlank(message = "이름은 필수입니다") @Size(max = 50, message = "이름은 50자 이하여야 합니다") String name,
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "연락처 형식이 올바르지 않습니다")
        String phone) {

  /**
   * Command 객체로 변환한다.
   *
   * @param sellerId 판매자 ID
   * @return UpdateSellerProfileCommand
   */
  public UpdateSellerProfileCommand toCommand(UUID sellerId) {
    return UpdateSellerProfileCommand.of(sellerId, name, phone);
  }
}
