package com.tickatch.user_service.seller.presentation.api;

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
import com.tickatch.user_service.seller.application.service.command.SellerCommandService;
import com.tickatch.user_service.seller.application.service.query.SellerQueryService;
import com.tickatch.user_service.seller.application.service.query.dto.SellerResponse;
import com.tickatch.user_service.seller.domain.vo.SellerStatus;
import com.tickatch.user_service.seller.presentation.api.dto.CreateSellerRequest;
import com.tickatch.user_service.seller.presentation.api.dto.CreateSellerRequest.AddressRequest;
import com.tickatch.user_service.seller.presentation.api.dto.RejectRequest;
import com.tickatch.user_service.seller.presentation.api.dto.UpdateSellerProfileRequest;
import com.tickatch.user_service.seller.presentation.api.dto.UpdateSettlementInfoRequest;
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
 * 판매자 API 컨트롤러 기능 테스트.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@WebMvcTest(controllers = SellerApi.class)
@Import(TestSecurityConfig.class)
@DisplayName("SellerApi 기능 테스트")
class SellerApiTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private SellerCommandService sellerCommandService;

  @MockitoBean
  private SellerQueryService sellerQueryService;

  private static final String BASE_URL = "/api/v1/user/sellers";

  private SellerResponse createResponse(UUID id, String email, String name) {
    return new SellerResponse(
        id, email, name, "01012345678",
        "테스트상점", "123-45-67890", "홍길동",
        SellerStatus.APPROVED, UserStatus.ACTIVE, true,
        LocalDateTime.now(), null, null,
        LocalDateTime.now(), LocalDateTime.now()
    );
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("판매자 목록을 조회한다")
  void getSellers() throws Exception {
    UUID id = UUID.randomUUID();
    given(sellerQueryService.searchSellers(any(), any()))
        .willReturn(new PageImpl<>(List.of(createResponse(id, "seller@example.com", "홍길동"))));

    mockMvc.perform(get(BASE_URL))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].email").value("seller@example.com"));
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("판매자 단건을 조회한다")
  void getSeller() throws Exception {
    UUID id = UUID.randomUUID();
    given(sellerQueryService.getSeller(id)).willReturn(createResponse(id, "seller@example.com", "홍길동"));

    mockMvc.perform(get(BASE_URL + "/{id}", id))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.email").value("seller@example.com"));
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("내 정보를 조회한다")
  void getMe() throws Exception {
    UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    given(sellerQueryService.getSeller(userId)).willReturn(createResponse(userId, "seller@example.com", "홍길동"));

    mockMvc.perform(get(BASE_URL + "/me"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(userId.toString()));
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("판매자를 생성한다")
  void createSeller() throws Exception {
    UUID id = UUID.randomUUID();
    CreateSellerRequest request = new CreateSellerRequest(
        "seller@example.com", "홍길동", "010-1234-5678",
        "테스트상점", "1234567890", "홍길동",
        new AddressRequest("12345", "서울시 강남구", "123호")
    );
    given(sellerCommandService.createSeller(any())).willReturn(id);

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
    UpdateSellerProfileRequest request = new UpdateSellerProfileRequest("김철수", "010-9999-8888");
    willDoNothing().given(sellerCommandService).updateProfile(any());

    mockMvc.perform(put(BASE_URL + "/{id}/profile", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("정산 정보를 수정한다")
  void updateSettlementInfo() throws Exception {
    UUID id = UUID.randomUUID();
    UpdateSettlementInfoRequest request = new UpdateSettlementInfoRequest("088", "12345678901234", "홍길동");
    willDoNothing().given(sellerCommandService).updateSettlementInfo(any());

    mockMvc.perform(put(BASE_URL + "/{id}/settlement", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("판매자를 승인한다")
  void approveSeller() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(sellerCommandService).approveSeller(eq(id), any());

    mockMvc.perform(post(BASE_URL + "/{id}/approve", id))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("판매자를 거절한다")
  void rejectSeller() throws Exception {
    UUID id = UUID.randomUUID();
    RejectRequest request = new RejectRequest("서류 미비");
    willDoNothing().given(sellerCommandService).rejectSeller(eq(id), eq("서류 미비"));

    mockMvc.perform(post(BASE_URL + "/{id}/reject", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("판매자를 정지한다")
  void suspendSeller() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(sellerCommandService).suspendSeller(id);

    mockMvc.perform(post(BASE_URL + "/{id}/suspend", id))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("판매자를 활성화한다")
  void activateSeller() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(sellerCommandService).activateSeller(id);

    mockMvc.perform(post(BASE_URL + "/{id}/activate", id))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @MockUser(userId = "550e8400-e29b-41d4-a716-446655440000")
  @DisplayName("판매자를 탈퇴 처리한다")
  void withdrawSeller() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(sellerCommandService).withdrawSeller(id);

    mockMvc.perform(delete(BASE_URL + "/{id}", id))
        .andDo(print())
        .andExpect(status().isOk());
  }
}