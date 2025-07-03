package com.example.cs25service.domain.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.SubscriptionPeriod;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25service.domain.admin.dto.request.UserRoleUpdateRequestDto;
import com.example.cs25service.domain.admin.dto.response.UserDetailResponseDto;
import com.example.cs25service.domain.admin.dto.response.UserPageResponseDto;
import com.example.cs25service.domain.admin.service.UserAdminService;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import com.example.cs25service.domain.subscription.dto.SubscriptionRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ResponseStatus;

@ActiveProfiles("test")
@WebMvcTest(UserAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserAdminService userAdminService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("GET /admin/users")
    @WithMockUser(roles = "ADMIN")
    class GetUserListsTest {

        @Test
        @DisplayName("회원 목록 조회 성공")
        void getUserList_success() throws Exception {
            Page<UserPageResponseDto> mockPage = new PageImpl<>(List.of(
                UserPageResponseDto.builder().userId(1L).email("test@email.com").build()
            ));

            given(userAdminService.getAdminUsers(1, 30)).willReturn(mockPage);

            mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("test@email.com"));
        }
    }

    @Nested
    @DisplayName("GET /admin/users/{userId}")
    @WithMockUser(roles = "ADMIN")
    class GetUserDetailTest {

        @Test
        @DisplayName("회원 상세 조회 성공")
        void getUserDetail_success() throws Exception {
            UserDetailResponseDto dto = UserDetailResponseDto.builder()
                .userInfo(UserPageResponseDto.builder()
                    .userId(1L)
                    .email("test@email.com")
                    .build())
                .build();

            given(userAdminService.getAdminUserDetail(1L)).willReturn(dto);

            mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userInfo.email").value("test@email.com"));
        }
    }

    @Nested
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DisplayName("DELETE /admin/users/{userId}")
    @WithMockUser(roles = "ADMIN")
    class DisableUserTest {

        @Test
        @DisplayName("회원 탈퇴 성공")
        void disableUser_success() throws Exception {
            mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

            verify(userAdminService).disableUser(1L);
        }
    }

    @Nested
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DisplayName("PATCH /admin/users/{userId}/subscriptions")
    @WithMockUser(roles = "ADMIN")
    class UpdateSubscriptionTest {

        @Test
        @DisplayName("구독 수정 성공")
        void updateSubscription_success() throws Exception {
            SubscriptionRequestDto request = SubscriptionRequestDto.builder()
                .active(true)
                .category("Backend")
                .email("test@example.com")
                .period(SubscriptionPeriod.ONE_MONTH)
                .days(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
                .build();

            mockMvc.perform(patch("/admin/users/1/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("구독 정보 수정 성공"));

            verify(userAdminService).updateSubscription(eq(1L), any(SubscriptionRequestDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /admin/users/{userId}/subscriptions")
    @WithMockUser(roles = "ADMIN")
    class CancelSubscriptionTest {

        @Test
        @DisplayName("구독 취소 성공")
        void cancelSubscription_success() throws Exception {
            mockMvc.perform(delete("/admin/users/1/subscriptions"))
                .andExpect(status().isNoContent());

            verify(userAdminService).cancelSubscription(1L);
        }
    }

    @Nested
    @DisplayName("PATCH /admin/users/{userId}/role")
    @WithMockUser(roles = "ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    class PatchUserRoleTest {

        @Test
        @DisplayName("관리자 권한 수정 성공")
        void patchUserRole_success() throws Exception {
            UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto();
            ReflectionTestUtils.setField(request, "role", Role.ADMIN);

            mockMvc.perform(patch("/admin/users/1/role")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

            verify(userAdminService).patchUserRole(eq(1L), any(UserRoleUpdateRequestDto.class));
        }
    }
}
