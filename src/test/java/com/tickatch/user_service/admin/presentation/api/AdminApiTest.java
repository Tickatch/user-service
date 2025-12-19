package com.tickatch.user_service.admin.presentation.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickatch.user_service.admin.application.service.command.AdminCommandService;
import com.tickatch.user_service.admin.application.service.query.AdminQueryService;
import com.tickatch.user_service.admin.application.service.query.dto.AdminResponse;
import com.tickatch.user_service.admin.domain.vo.AdminRole;
import com.tickatch.user_service.admin.presentation.api.dto.ChangeRoleRequest;
import com.tickatch.user_service.admin.presentation.api.dto.CreateAdminRequest;
import com.tickatch.user_service.admin.presentation.api.dto.UpdateAdminProfileRequest;
import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.config.TestSecurityConfig;
import io.github.tickatch.common.security.test.MockUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 관리자 API 컨트롤러 기능 테스트.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@WebMvcTest(controllers = AdminApi.class)
@Import(TestSecurityConfig.class)
@DisplayName("AdminApi 기능 테스트")
class AdminApiTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AdminCommandService adminCommandService;

  @MockitoBean private AdminQueryService adminQueryService;

  private static final String BASE_URL = "/api/v1/user/admins";

  private AdminResponse createResponse(UUID id, String email, String name, AdminRole role) {
    return new AdminResponse(
        id,
        email,
        name,
        "01012345678",
        "개발팀",
        role,
        UserStatus.ACTIVE,
        LocalDateTime.now(),
        LocalDateTime.now());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("관리자 목록을 조회한다")
  void getAdmins() throws Exception {
    UUID id = UUID.randomUUID();
    given(adminQueryService.searchAdmins(any(), any()))
        .willReturn(
            new PageImpl<>(
                List.of(createResponse(id, "admin@example.com", "관리자", AdminRole.MANAGER))));

    mockMvc
        .perform(get(BASE_URL))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].email").value("admin@example.com"));
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("관리자 단건을 조회한다")
  void getAdmin() throws Exception {
    UUID id = UUID.randomUUID();
    given(adminQueryService.getAdmin(id))
        .willReturn(createResponse(id, "admin@example.com", "관리자", AdminRole.MANAGER));

    mockMvc
        .perform(get(BASE_URL + "/{id}", id))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.email").value("admin@example.com"));
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("내 정보를 조회한다")
  void getMe() throws Exception {
    UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    given(adminQueryService.getAdmin(userId))
        .willReturn(createResponse(userId, "admin@example.com", "관리자", AdminRole.ADMIN));

    mockMvc
        .perform(get(BASE_URL + "/me"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(userId.toString()));
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("역할별 활성 관리자 수를 조회한다")
  void countActiveByRole() throws Exception {
    given(adminQueryService.countActiveByRole(AdminRole.ADMIN)).willReturn(5L);

    mockMvc
        .perform(get(BASE_URL + "/count").param("role", "ADMIN"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").value(5));
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("관리자를 생성한다")
  void createAdmin() throws Exception {
    UUID id = UUID.randomUUID();
    CreateAdminRequest request =
        new CreateAdminRequest(
            "admin@example.com", "관리자", "010-1234-5678", "개발팀", AdminRole.MANAGER);
    given(adminCommandService.createAdmin(any())).willReturn(id);

    mockMvc
        .perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data").value(id.toString()));
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("프로필을 수정한다")
  void updateProfile() throws Exception {
    UUID id = UUID.randomUUID();
    UpdateAdminProfileRequest request =
        new UpdateAdminProfileRequest("김관리", "010-9999-8888", "운영팀");
    willDoNothing().given(adminCommandService).updateProfile(any());

    mockMvc
        .perform(
            put(BASE_URL + "/{id}/profile", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("역할을 변경한다")
  void changeRole() throws Exception {
    UUID id = UUID.randomUUID();
    ChangeRoleRequest request = new ChangeRoleRequest(AdminRole.ADMIN);
    willDoNothing().given(adminCommandService).changeRole(any());

    mockMvc
        .perform(
            put(BASE_URL + "/{id}/role", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("관리자를 정지한다")
  void suspendAdmin() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(adminCommandService).suspendAdmin(id);

    mockMvc.perform(post(BASE_URL + "/{id}/suspend", id)).andDo(print()).andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("관리자를 활성화한다")
  void activateAdmin() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(adminCommandService).activateAdmin(id);

    mockMvc
        .perform(post(BASE_URL + "/{id}/activate", id))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("관리자를 탈퇴 처리한다")
  void withdrawAdmin() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(adminCommandService).withdrawAdmin(id);

    mockMvc.perform(delete(BASE_URL + "/{id}", id)).andDo(print()).andExpect(status().isOk());
  }
}
