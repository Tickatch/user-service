package com.tickatch.user_service.customer.presentation.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.tickatch.user_service.common.domain.vo.UserStatus;
import com.tickatch.user_service.config.TestSecurityConfig;
import com.tickatch.user_service.customer.application.service.command.CustomerCommandService;
import com.tickatch.user_service.customer.application.service.query.CustomerQueryService;
import com.tickatch.user_service.customer.application.service.query.dto.CustomerResponse;
import com.tickatch.user_service.customer.domain.vo.CustomerGrade;
import com.tickatch.user_service.customer.presentation.api.dto.ChangeGradeRequest;
import com.tickatch.user_service.customer.presentation.api.dto.CreateCustomerRequest;
import com.tickatch.user_service.customer.presentation.api.dto.UpdateCustomerProfileRequest;
import io.github.tickatch.common.security.test.MockUser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 고객 API 컨트롤러 기능 테스트.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@WebMvcTest(controllers = CustomerApi.class)
@Import(TestSecurityConfig.class)
@DisplayName("CustomerApi 기능 테스트")
class CustomerApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CustomerCommandService customerCommandService;

  @MockitoBean
  private CustomerQueryService customerQueryService;

  private static final String BASE_URL = "/api/v1/user/customers";

  private CustomerResponse createResponse(UUID id, String email, String name) {
    return new CustomerResponse(
        id, email, name, "01012345678",
        LocalDate.of(1990, 1, 1), CustomerGrade.NORMAL,
        UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
    );
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("고객 목록을 조회한다")
  void getCustomers() throws Exception {
    UUID id = UUID.randomUUID();
    given(customerQueryService.searchCustomers(any(), any()))
        .willReturn(new PageImpl<>(List.of(createResponse(id, "test@example.com", "홍길동"))));

    mockMvc.perform(get(BASE_URL))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[0].email").value("test@example.com"));
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("고객 단건을 조회한다")
  void getCustomer() throws Exception {
    UUID id = UUID.randomUUID();
    given(customerQueryService.getCustomer(id)).willReturn(createResponse(id, "test@example.com", "홍길동"));

    mockMvc.perform(get(BASE_URL + "/{id}", id))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.email").value("test@example.com"));
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("내 정보를 조회한다")
  void getMe() throws Exception {
    UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    given(customerQueryService.getCustomer(userId)).willReturn(createResponse(userId, "test@example.com", "홍길동"));

    mockMvc.perform(get(BASE_URL + "/me"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(userId.toString()));
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("고객을 생성한다")
  void createCustomer() throws Exception {
    UUID id = UUID.randomUUID();
    CreateCustomerRequest request = new CreateCustomerRequest(
        "test@example.com", "홍길동", "010-1234-5678", LocalDate.of(1990, 1, 1)
    );
    given(customerCommandService.createCustomer(any())).willReturn(id);

    mockMvc.perform(post(BASE_URL)
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
    UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest(
        "김철수", "010-9999-8888", LocalDate.of(1995, 5, 5)
    );
    willDoNothing().given(customerCommandService).updateProfile(any());

    mockMvc.perform(put(BASE_URL + "/{id}/profile", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("등급을 변경한다")
  void changeGrade() throws Exception {
    UUID id = UUID.randomUUID();
    ChangeGradeRequest request = new ChangeGradeRequest(CustomerGrade.VIP);
    willDoNothing().given(customerCommandService).changeGrade(eq(id), eq(CustomerGrade.VIP));

    mockMvc.perform(put(BASE_URL + "/{id}/grade", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("고객을 정지한다")
  void suspendCustomer() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(customerCommandService).suspendCustomer(id);

    mockMvc.perform(post(BASE_URL + "/{id}/suspend", id))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("고객을 활성화한다")
  void activateCustomer() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(customerCommandService).activateCustomer(id);

    mockMvc.perform(post(BASE_URL + "/{id}/activate", id))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("고객을 탈퇴 처리한다")
  void withdrawCustomer() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(customerCommandService).withdrawCustomer(id);

    mockMvc.perform(delete(BASE_URL + "/{id}", id))
        .andDo(print())
        .andExpect(status().isOk());
  }
}