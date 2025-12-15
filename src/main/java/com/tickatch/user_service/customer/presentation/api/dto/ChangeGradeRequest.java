package com.tickatch.user_service.customer.presentation.api.dto;

import com.tickatch.user_service.customer.domain.vo.CustomerGrade;
import jakarta.validation.constraints.NotNull;

/**
 * 고객 등급 변경 요청 DTO.
 *
 * <p>고객의 등급을 변경할 때 사용한다. 등급 하향은 불가능하다.
 *
 * @param grade 새 등급
 * @author Tickatch
 * @since 1.0.0
 */
public record ChangeGradeRequest(
    @NotNull(message = "등급은 필수입니다")
    CustomerGrade grade
) {

}