package com.tickatch.user_service.admin.presentation.api;

import com.tickatch.user_service.admin.application.service.command.AdminCommandService;
import com.tickatch.user_service.admin.application.service.query.AdminQueryService;
import com.tickatch.user_service.admin.application.service.query.dto.AdminResponse;
import com.tickatch.user_service.admin.application.service.query.dto.AdminSearchRequest;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import com.tickatch.user_service.admin.presentation.api.dto.ChangeRoleRequest;
import com.tickatch.user_service.admin.presentation.api.dto.CreateAdminRequest;
import com.tickatch.user_service.admin.presentation.api.dto.UpdateAdminProfileRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 API 컨트롤러.
 *
 * <p>관리자의 CRUD 및 상태 관리를 위한 REST API를 제공한다.
 *
 * @author Tickatch
 * @since 1.0.0
 * @see AdminCommandService
 * @see AdminQueryService
 */
@Tag(name = "Admin", description = "관리자 관리 API")
@RestController
@RequestMapping("/api/v1/user/admins")
@RequiredArgsConstructor
public class AdminApi {

  private final AdminCommandService adminCommandService;
  private final AdminQueryService adminQueryService;

  // ========== 조회 ==========

  /**
   * 관리자 목록을 조회한다.
   *
   * @param request 검색 조건 (이메일, 이름, 상태, 역할, 부서)
   * @param pageable 페이징 정보 (기본값: size=10, sort=createdAt DESC)
   * @return 페이징된 관리자 목록
   */
  @Operation(summary = "관리자 목록 조회", description = "검색 조건과 페이징을 적용하여 관리자 목록을 조회한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공")
  })
  @GetMapping
  public ApiResponse<PageResponse<AdminResponse>> getAdmins(
      @ModelAttribute AdminSearchRequest request,
      @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
      Pageable pageable) {
    var admins = adminQueryService.searchAdmins(request, pageable);
    return ApiResponse.success(PageResponse.from(admins));
  }

  /**
   * 관리자 단건을 조회한다.
   *
   * @param id 관리자 ID
   * @return 관리자 상세 정보
   */
  @Operation(summary = "관리자 단건 조회", description = "관리자 ID로 관리자 상세 정보를 조회한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "관리자를 찾을 수 없음")
  })
  @GetMapping("/{id}")
  public ApiResponse<AdminResponse> getAdmin(
      @Parameter(description = "관리자 ID", required = true) @PathVariable UUID id) {
    return ApiResponse.success(adminQueryService.getAdmin(id));
  }

  /**
   * 내 정보를 조회한다.
   *
   * @param user 인증된 사용자 정보
   * @return 내 관리자 정보
   */
  @Operation(summary = "내 정보 조회", description = "인증된 사용자의 관리자 정보를 조회한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "관리자를 찾을 수 없음")
  })
  @GetMapping("/me")
  public ApiResponse<AdminResponse> getMe(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    return ApiResponse.success(adminQueryService.getAdmin(UUID.fromString(user.getUserId())));
  }

  /**
   * 역할별 활성 관리자 수를 조회한다.
   *
   * @param role 역할
   * @return 활성 관리자 수
   */
  @Operation(summary = "역할별 활성 관리자 수 조회", description = "특정 역할의 활성 관리자 수를 조회한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공")
  })
  @GetMapping("/count")
  public ApiResponse<Long> countActiveByRole(
      @Parameter(description = "역할", required = true) @RequestParam AdminRole role) {
    return ApiResponse.success(adminQueryService.countActiveByRole(role));
  }

  // ========== 생성 ==========

  /**
   * 관리자를 생성한다.
   *
   * @param request 관리자 생성 요청
   * @param user 인증된 사용자 정보
   * @return 생성된 관리자 ID
   */
  @Operation(summary = "관리자 생성", description = "새 관리자를 생성한다.")
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
  public ApiResponse<UUID> createAdmin(
      @Valid @RequestBody CreateAdminRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    UUID adminId = adminCommandService.createAdmin(
        request.toCommand(UUID.fromString(user.getUserId()))
    );
    return ApiResponse.success(adminId);
  }

  // ========== 수정 ==========

  /**
   * 관리자 프로필을 수정한다.
   *
   * @param id 관리자 ID
   * @param request 프로필 수정 요청
   * @return 빈 응답
   */
  @Operation(summary = "프로필 수정", description = "관리자 프로필을 수정한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "수정 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "관리자를 찾을 수 없음")
  })
  @PutMapping("/{id}/profile")
  public ApiResponse<Void> updateProfile(
      @Parameter(description = "관리자 ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody UpdateAdminProfileRequest request) {
    adminCommandService.updateProfile(request.toCommand(id));
    return ApiResponse.success();
  }

  /**
   * 관리자 역할을 변경한다.
   *
   * @param id 대상 관리자 ID
   * @param request 역할 변경 요청
   * @param user 인증된 사용자 정보 (변경자)
   * @return 빈 응답
   */
  @Operation(summary = "역할 변경", description = "관리자 역할을 변경한다. ADMIN 역할만 가능하며, 자신의 역할은 변경 불가하다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "변경 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "자기 자신 역할 변경 불가"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "ADMIN만 역할 변경 가능"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "관리자를 찾을 수 없음")
  })
  @PutMapping("/{id}/role")
  public ApiResponse<Void> changeRole(
      @Parameter(description = "대상 관리자 ID", required = true) @PathVariable UUID id,
      @Valid @RequestBody ChangeRoleRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    adminCommandService.changeRole(
        request.toCommand(id, UUID.fromString(user.getUserId()))
    );
    return ApiResponse.success();
  }

  // ========== 상태 관리 ==========

  /**
   * 관리자를 정지한다.
   *
   * @param id 관리자 ID
   * @return 빈 응답
   */
  @Operation(summary = "관리자 정지", description = "관리자를 정지 상태로 변경한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "정지 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "관리자를 찾을 수 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "422",
          description = "이미 탈퇴한 관리자")
  })
  @PostMapping("/{id}/suspend")
  public ApiResponse<Void> suspendAdmin(
      @Parameter(description = "관리자 ID", required = true) @PathVariable UUID id) {
    adminCommandService.suspendAdmin(id);
    return ApiResponse.success();
  }

  /**
   * 정지된 관리자를 활성화한다.
   *
   * @param id 관리자 ID
   * @return 빈 응답
   */
  @Operation(summary = "관리자 활성화", description = "정지된 관리자를 활성화 상태로 변경한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "활성화 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "관리자를 찾을 수 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "422",
          description = "이미 탈퇴한 관리자")
  })
  @PostMapping("/{id}/activate")
  public ApiResponse<Void> activateAdmin(
      @Parameter(description = "관리자 ID", required = true) @PathVariable UUID id) {
    adminCommandService.activateAdmin(id);
    return ApiResponse.success();
  }

  /**
   * 관리자를 탈퇴 처리한다.
   *
   * @param id 관리자 ID
   * @return 빈 응답
   */
  @Operation(summary = "관리자 탈퇴", description = "관리자를 탈퇴 처리한다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "탈퇴 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "관리자를 찾을 수 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "422",
          description = "이미 탈퇴한 관리자")
  })
  @DeleteMapping("/{id}")
  public ApiResponse<Void> withdrawAdmin(
      @Parameter(description = "관리자 ID", required = true) @PathVariable UUID id) {
    adminCommandService.withdrawAdmin(id);
    return ApiResponse.success();
  }
}