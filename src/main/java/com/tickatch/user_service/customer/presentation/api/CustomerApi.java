package com.tickatch.user_service.customer.presentation.api;

import com.tickatch.user_service.customer.application.service.command.CustomerCommandService;
import com.tickatch.user_service.customer.application.service.query.CustomerQueryService;
import com.tickatch.user_service.customer.application.service.query.dto.CustomerResponse;
import com.tickatch.user_service.customer.application.service.query.dto.CustomerSearchRequest;
import com.tickatch.user_service.customer.presentation.api.dto.ChangeGradeRequest;
import com.tickatch.user_service.customer.presentation.api.dto.CreateCustomerRequest;
import com.tickatch.user_service.customer.presentation.api.dto.UpdateCustomerProfileRequest;
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
 * 고객 API 컨트롤러.
 *
 * <p>고객의 CRUD 및 상태 관리를 위한 REST API를 제공한다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see CustomerCommandService
 * @see CustomerQueryService
 */
@Tag(name = "Customer", description = "고객 관리 API")
@RestController
@RequestMapping("/api/v1/user/customers")
@RequiredArgsConstructor
public class CustomerApi {

  private final CustomerCommandService customerCommandService;
  private final CustomerQueryService customerQueryService;

  // ========== 조회 ==========

  /**
   * 고객 목록을 조회한다.
   *
   * @param request 검색 조건 (이메일, 이름, 연락처, 상태, 등급)
   * @param pageable 페이징 정보 (기본값: size=10, sort=createdAt DESC)
   * @return 페이징된 고객 목록
   */
  @Operation(summary = "고객 목록 조회", description = "검색 조건과 페이징을 적용하여 고객 목록을 조회한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공")
  })
  @GetMapping
  public ApiResponse<PageResponse<CustomerResponse>> getCustomers(
      @ModelAttribute CustomerSearchRequest request,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
      Pageable pageable) {
    var customers = customerQueryService.searchCustomers(request, pageable);
    return ApiResponse.success(PageResponse.from(customers));
  }

  /**
   * 고객 단건을 조회한다.
   *
   * @param id 고객 ID
   * @return 고객 상세 정보
   */
  @Operation(summary = "고객 단건 조회", description = "고객 ID로 고객 상세 정보를 조회한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "고객을 찾을 수 없음")
  })
  @GetMapping("/{id}")
  public ApiResponse<CustomerResponse> getCustomer(
      @Parameter(description = "고객 ID", required = true) @PathVariable UUID id) {
    return ApiResponse.success(customerQueryService.getCustomer(id));
  }

  /**
   * 내 정보를 조회한다.
   *
   * @param user 인증된 사용자 정보
   * @return 내 고객 정보
   */
  @Operation(summary = "내 정보 조회", description = "인증된 사용자의 고객 정보를 조회한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "고객을 찾을 수 없음")
  })
  @GetMapping("/me")
  public ApiResponse<CustomerResponse> getMe(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    return ApiResponse.success(customerQueryService.getCustomer(UUID.fromString(user.getUserId())));
  }

  // ========== 생성 ==========

  /**
   * 고객을 생성한다.
   *
   * @param request 고객 생성 요청
   * @param user 인증된 사용자 정보
   * @return 생성된 고객 ID
   */
  @Operation(summary = "고객 생성", description = "새 고객을 생성한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "201",
          description = "생성 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "409",
          description = "이미 존재하는 이메일")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<UUID> createCustomer(
      @Valid @RequestBody CreateCustomerRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    UUID customerId = customerCommandService.createCustomer(
        request.toCommand(UUID.fromString(user.getUserId()))
    );
    return ApiResponse.success(customerId);
  }

  // ========== 수정 ==========

  /**
   * 고객 프로필을 수정한다.
   *
   * @param id 고객 ID
   * @param request 프로필 수정 요청
   * @return 빈 응답
   */
  @Operation(summary = "프로필 수정", description = "고객 프로필을 수정한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "수정 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "고객을 찾을 수 없음")
  })
  @PutMapping("/{id}/profile")
  public ApiResponse<Void> updateProfile(
      @Parameter(description = "고객 ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody UpdateCustomerProfileRequest request) {
    customerCommandService.updateProfile(request.toCommand(id));
    return ApiResponse.success();
  }

  /**
   * 고객 등급을 변경한다.
   *
   * @param id 고객 ID
   * @param request 등급 변경 요청
   * @return 빈 응답
   */
  @Operation(summary = "등급 변경", description = "고객 등급을 변경한다. 등급 하향은 불가능하다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "변경 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "등급 하향 불가"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "고객을 찾을 수 없음")
  })
  @PutMapping("/{id}/grade")
  public ApiResponse<Void> changeGrade(
      @Parameter(description = "고객 ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody ChangeGradeRequest request) {
    customerCommandService.changeGrade(id, request.grade());
    return ApiResponse.success();
  }

  // ========== 상태 관리 ==========

  /**
   * 고객을 정지한다.
   *
   * @param id 고객 ID
   * @return 빈 응답
   */
  @Operation(summary = "고객 정지", description = "고객을 정지 상태로 변경한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "정지 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "고객을 찾을 수 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "422",
          description = "이미 탈퇴한 고객")
  })
  @PostMapping("/{id}/suspend")
  public ApiResponse<Void> suspendCustomer(
      @Parameter(description = "고객 ID", required = true) @PathVariable UUID id) {
    customerCommandService.suspendCustomer(id);
    return ApiResponse.success();
  }

  /**
   * 정지된 고객을 활성화한다.
   *
   * @param id 고객 ID
   * @return 빈 응답
   */
  @Operation(summary = "고객 활성화", description = "정지된 고객을 활성화 상태로 변경한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "활성화 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "고객을 찾을 수 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "422",
          description = "이미 탈퇴한 고객")
  })
  @PostMapping("/{id}/activate")
  public ApiResponse<Void> activateCustomer(
      @Parameter(description = "고객 ID", required = true) @PathVariable UUID id) {
    customerCommandService.activateCustomer(id);
    return ApiResponse.success();
  }

  /**
   * 고객을 탈퇴 처리한다.
   *
   * @param id 고객 ID
   * @return 빈 응답
   */
  @Operation(summary = "고객 탈퇴", description = "고객을 탈퇴 처리한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "탈퇴 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "고객을 찾을 수 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "422",
          description = "이미 탈퇴한 고객")
  })
  @DeleteMapping("/{id}")
  public ApiResponse<Void> withdrawCustomer(
      @Parameter(description = "고객 ID", required = true) @PathVariable UUID id) {
    customerCommandService.withdrawCustomer(id);
    return ApiResponse.success();
  }
}