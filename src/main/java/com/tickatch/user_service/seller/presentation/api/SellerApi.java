package com.tickatch.user_service.seller.presentation.api;

import com.tickatch.user_service.seller.application.service.command.SellerCommandService;
import com.tickatch.user_service.seller.application.service.query.SellerQueryService;
import com.tickatch.user_service.seller.application.service.query.dto.SellerResponse;
import com.tickatch.user_service.seller.application.service.query.dto.SellerSearchRequest;
import com.tickatch.user_service.seller.presentation.api.dto.CreateSellerRequest;
import com.tickatch.user_service.seller.presentation.api.dto.RejectRequest;
import com.tickatch.user_service.seller.presentation.api.dto.UpdateSellerProfileRequest;
import com.tickatch.user_service.seller.presentation.api.dto.UpdateSettlementInfoRequest;
import io.github.tickatch.common.api.ApiResponse;
import io.github.tickatch.common.api.PageResponse;
import io.github.tickatch.common.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 판매자 API 컨트롤러.
 *
 * <p>판매자의 CRUD, 승인/거절 및 상태 관리를 위한 REST API를 제공한다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see SellerCommandService
 * @see SellerQueryService
 */
@Tag(name = "Seller", description = "판매자 관리 API")
@RestController
@RequestMapping("/api/v1/user/sellers")
@RequiredArgsConstructor
public class SellerApi {

  private final SellerCommandService sellerCommandService;
  private final SellerQueryService sellerQueryService;

  // ========== 조회 ==========

  /**
   * 판매자 목록을 조회한다.
   *
   * @param request 검색 조건 (이메일, 이름, 상태, 승인상태, 상호명, 사업자번호)
   * @param pageable 페이징 정보 (기본값: size=10, sort=createdAt DESC)
   * @return 페이징된 판매자 목록
   */
  @Operation(summary = "판매자 목록 조회", description = "검색 조건과 페이징을 적용하여 판매자 목록을 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공")
  })
  @GetMapping
  public ApiResponse<PageResponse<SellerResponse>> getSellers(
      @ModelAttribute SellerSearchRequest request,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    var sellers = sellerQueryService.searchSellers(request, pageable);
    return ApiResponse.success(PageResponse.from(sellers));
  }

  /**
   * 판매자 단건을 조회한다.
   *
   * @param id 판매자 ID
   * @return 판매자 상세 정보
   */
  @Operation(summary = "판매자 단건 조회", description = "판매자 ID로 판매자 상세 정보를 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "판매자를 찾을 수 없음")
  })
  @GetMapping("/{id}")
  public ApiResponse<SellerResponse> getSeller(
      @Parameter(description = "판매자 ID", required = true) @PathVariable UUID id) {
    return ApiResponse.success(sellerQueryService.getSeller(id));
  }

  /**
   * 내 정보를 조회한다.
   *
   * @param user 인증된 사용자 정보
   * @return 내 판매자 정보
   */
  @Operation(summary = "내 정보 조회", description = "인증된 사용자의 판매자 정보를 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "판매자를 찾을 수 없음")
  })
  @GetMapping("/me")
  public ApiResponse<SellerResponse> getMe(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    return ApiResponse.success(sellerQueryService.getSeller(UUID.fromString(user.getUserId())));
  }

  // ========== 생성 ==========

  /**
   * 판매자를 생성한다.
   *
   * @param request 판매자 생성 요청
   * @param user 인증된 사용자 정보
   * @return 생성된 판매자 ID
   */
  @Operation(summary = "판매자 생성", description = "새 판매자를 PENDING 상태로 생성한다. 관리자 승인 후 활동 가능하다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "생성 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "잘못된 요청"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "이미 존재하는 이메일 또는 사업자등록번호")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<UUID> createSeller(
      @Valid @RequestBody CreateSellerRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    UUID sellerId =
        sellerCommandService.createSeller(request.toCommand(UUID.fromString(user.getUserId())));
    return ApiResponse.success(sellerId);
  }

  // ========== 수정 ==========

  /**
   * 판매자 프로필을 수정한다.
   *
   * @param id 판매자 ID
   * @param request 프로필 수정 요청
   * @return 빈 응답
   */
  @Operation(summary = "프로필 수정", description = "판매자 프로필을 수정한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "수정 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "잘못된 요청"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "판매자를 찾을 수 없음")
  })
  @PutMapping("/{id}/profile")
  public ApiResponse<Void> updateProfile(
      @Parameter(description = "판매자 ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody UpdateSellerProfileRequest request) {
    sellerCommandService.updateProfile(request.toCommand(id));
    return ApiResponse.success();
  }

  /**
   * 정산 정보를 수정한다.
   *
   * @param id 판매자 ID
   * @param request 정산 정보 수정 요청
   * @return 빈 응답
   */
  @Operation(summary = "정산 정보 수정", description = "판매자의 정산 정보를 수정한다. 승인된 판매자만 가능하다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "수정 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "잘못된 요청"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "판매자를 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "승인 전에는 수정 불가")
  })
  @PutMapping("/{id}/settlement")
  public ApiResponse<Void> updateSettlementInfo(
      @Parameter(description = "판매자 ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody UpdateSettlementInfoRequest request) {
    sellerCommandService.updateSettlementInfo(request.toCommand(id));
    return ApiResponse.success();
  }

  // ========== 심사 ==========

  /**
   * 판매자를 승인한다.
   *
   * @param id 판매자 ID
   * @param user 인증된 관리자 정보
   * @return 빈 응답
   */
  @Operation(summary = "판매자 승인", description = "PENDING 상태의 판매자를 승인한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "승인 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "판매자를 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "PENDING 상태가 아님")
  })
  @PostMapping("/{id}/approve")
  public ApiResponse<Void> approveSeller(
      @Parameter(description = "판매자 ID", required = true) @PathVariable UUID id,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    sellerCommandService.approveSeller(id, user.getUserId());
    return ApiResponse.success();
  }

  /**
   * 판매자를 거절한다.
   *
   * @param id 판매자 ID
   * @param request 거절 요청 (사유 포함)
   * @return 빈 응답
   */
  @Operation(summary = "판매자 거절", description = "PENDING 상태의 판매자를 거절한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "거절 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "거절 사유 누락"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "판매자를 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "PENDING 상태가 아님")
  })
  @PostMapping("/{id}/reject")
  public ApiResponse<Void> rejectSeller(
      @Parameter(description = "판매자 ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody RejectRequest request) {
    sellerCommandService.rejectSeller(id, request.reason());
    return ApiResponse.success();
  }

  // ========== 상태 관리 ==========

  /**
   * 판매자를 정지한다.
   *
   * @param id 판매자 ID
   * @return 빈 응답
   */
  @Operation(summary = "판매자 정지", description = "판매자를 정지 상태로 변경한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "정지 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "판매자를 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "이미 탈퇴한 판매자")
  })
  @PostMapping("/{id}/suspend")
  public ApiResponse<Void> suspendSeller(
      @Parameter(description = "판매자 ID", required = true) @PathVariable UUID id) {
    sellerCommandService.suspendSeller(id);
    return ApiResponse.success();
  }

  /**
   * 정지된 판매자를 활성화한다.
   *
   * @param id 판매자 ID
   * @return 빈 응답
   */
  @Operation(summary = "판매자 활성화", description = "정지된 판매자를 활성화 상태로 변경한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "활성화 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "판매자를 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "이미 탈퇴한 판매자")
  })
  @PostMapping("/{id}/activate")
  public ApiResponse<Void> activateSeller(
      @Parameter(description = "판매자 ID", required = true) @PathVariable UUID id) {
    sellerCommandService.activateSeller(id);
    return ApiResponse.success();
  }

  /**
   * 판매자를 탈퇴 처리한다.
   *
   * @param id 판매자 ID
   * @return 빈 응답
   */
  @Operation(summary = "판매자 탈퇴", description = "판매자를 탈퇴 처리한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "탈퇴 성공"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "판매자를 찾을 수 없음"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "422",
        description = "이미 탈퇴한 판매자")
  })
  @DeleteMapping("/{id}")
  public ApiResponse<Void> withdrawSeller(
      @Parameter(description = "판매자 ID", required = true) @PathVariable UUID id) {
    sellerCommandService.withdrawSeller(id);
    return ApiResponse.success();
  }
}
