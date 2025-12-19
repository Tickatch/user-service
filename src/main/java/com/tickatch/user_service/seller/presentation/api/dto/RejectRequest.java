package com.tickatch.user_service.seller.presentation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 판매자 거절 요청 DTO.
 *
 * <p>PENDING 상태의 판매자를 거절할 때 사용한다. 거절 사유는 필수이며, 판매자에게 전달된다.
 *
 * @param reason 거절 사유
 * @author Tickatch
 * @since 1.0.0
 */
public record RejectRequest(
    @NotBlank(message = "거절 사유는 필수입니다") @Size(max = 500, message = "거절 사유는 500자 이하여야 합니다")
        String reason) {}
