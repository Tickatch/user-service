package com.tickatch.user_service.seller.presentation.api.dto;

import com.tickatch.user_service.common.domain.vo.Address;
import com.tickatch.user_service.seller.application.service.command.dto.CreateSellerCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * 판매자 생성 요청 DTO.
 *
 * <p>새 판매자를 생성할 때 필요한 정보를 담는다. 생성된 판매자는 PENDING 상태로 시작하며, 관리자 승인 후 활동 가능하다.
 *
 * @param email 이메일
 * @param name 이름
 * @param phone 연락처
 * @param businessName 상호명
 * @param businessNumber 사업자등록번호 (10자리 숫자)
 * @param representativeName 대표자명
 * @param businessAddress 사업장 주소 (선택)
 * @author Tickatch
 * @since 1.0.0
 */
public record CreateSellerRequest(
    @NotBlank(message = "이메일은 필수입니다") @Email(message = "이메일 형식이 올바르지 않습니다") String email,
    @NotBlank(message = "이름은 필수입니다") @Size(max = 50, message = "이름은 50자 이하여야 합니다") String name,
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "연락처 형식이 올바르지 않습니다")
        String phone,
    @NotBlank(message = "상호명은 필수입니다") @Size(max = 100, message = "상호명은 100자 이하여야 합니다")
        String businessName,
    @NotBlank(message = "사업자등록번호는 필수입니다")
        @Pattern(regexp = "^[0-9]{10}$", message = "사업자등록번호는 10자리 숫자여야 합니다")
        String businessNumber,
    @NotBlank(message = "대표자명은 필수입니다") @Size(max = 50, message = "대표자명은 50자 이하여야 합니다")
        String representativeName,
    AddressRequest businessAddress) {

  /**
   * Command 객체로 변환한다.
   *
   * @param authId 인증 ID (Auth Service에서 발급)
   * @return CreateSellerCommand
   */
  public CreateSellerCommand toCommand(UUID authId) {
    Address address = businessAddress != null ? businessAddress.toAddress() : null;
    return CreateSellerCommand.of(
        authId, email, name, phone, businessName, businessNumber, representativeName, address);
  }

  /**
   * 주소 요청 DTO.
   *
   * @param zipCode 우편번호
   * @param address1 기본 주소
   * @param address2 상세 주소
   */
  public record AddressRequest(String zipCode, String address1, String address2) {

    /**
     * Address 도메인 객체로 변환한다.
     *
     * @return Address
     */
    public Address toAddress() {
      return Address.of(zipCode, address1, address2);
    }
  }
}
