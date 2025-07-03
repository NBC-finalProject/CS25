package com.example.cs25service.domain.admin.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25service.domain.admin.dto.response.SubscriptionPageResponseDto;
import com.example.cs25service.domain.admin.service.SubscriptionAdminService;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(SubscriptionAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubscriptionAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionAdminService subscriptionAdminService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("GET /admin/subscriptions")
    @WithMockUser(roles = "ADMIN")
    class GetSubscriptionLists {

        @Test
        @DisplayName("구독 목록 조회 성공")
        void getSubscriptionList_success() throws Exception {
            // given
            Page<SubscriptionPageResponseDto> mockPage = new PageImpl<>(List.of(
                SubscriptionPageResponseDto.builder()
                    .id(1L)
                    .email("test@email.com")
                    .category("BACKEND")
                    .serialId("subscription-SerialId-001")
                    .isActive(true)
                    .subscriptionType(
                        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY))
                    .build()
            ));

            given(subscriptionAdminService.getAdminSubscriptions(1, 30)).willReturn(mockPage);

            // when & then
            mockMvc.perform(get("/admin/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("test@email.com"))
                .andExpect(jsonPath("$.data.content[0].active").value(true));
        }
    }

    @Nested
    @DisplayName("GET /admin/subscriptions/{subscriptionId}")
    @WithMockUser(roles = "ADMIN")
    class GetSubscription {

        @Test
        @DisplayName("단일 구독 정보 조회 성공")
        void getSubscription_success() throws Exception {
            // given
            SubscriptionPageResponseDto responseDto = SubscriptionPageResponseDto.builder()
                .id(1L)
                .email("test@email.com")
                .category("BACKEND")
                .serialId("subscription-SerialId-001")
                .isActive(true)
                .subscriptionType(
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY))
                .build();

            given(subscriptionAdminService.getSubscription(1L)).willReturn(responseDto);

            // when & then
            mockMvc.perform(get("/admin/subscriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@email.com"))
                .andExpect(jsonPath("$.data.active").value(true));
        }
    }

    @Nested
    @DisplayName("PATCH /admin/subscriptions/{subscriptionId}")
    @WithMockUser(roles = "ADMIN")
    class DeleteSubscription {

        @Test
        @DisplayName("구독 삭제 성공")
        void deleteSubscription_success() throws Exception {
            // when & then
            mockMvc.perform(patch("/admin/subscriptions/1"))
                .andExpect(status().isOk());

            verify(subscriptionAdminService).deleteSubscription(1L);
        }
    }
}
