package com.example.cs25service.domain.profile.controller;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25service.domain.profile.dto.ProfileResponseDto;
import com.example.cs25service.domain.profile.dto.ProfileWrongQuizResponseDto;
import com.example.cs25service.domain.profile.dto.UserSubscriptionResponseDto;
import com.example.cs25service.domain.profile.dto.WrongQuizDto;
import com.example.cs25service.domain.profile.service.ProfileService;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.subscription.dto.SubscriptionHistoryDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25service.domain.userQuizAnswer.dto.CategoryUserAnswerRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@WebMvcTest(ProfileController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    private Subscription subscription;

    @BeforeEach
    void setUp(){
        QuizCategory quizCategory = QuizCategory.builder()
                .categoryType("BACKEND")
                .build();

        subscription = Subscription.builder()
                .category(quizCategory)
                .email("test@naver.com")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .subscriptionType(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
                .build();
        ReflectionTestUtils.setField(subscription, "id", 1L);
    }

    @Test
    @DisplayName("사용자 정보 조회")
    @WithMockUser(username = "testUser")
    void getProfile() throws Exception {
        //given

        ProfileResponseDto profileResponseDto = ProfileResponseDto.builder()
                .name("test")
                .rank(1)
                .score(1.0)
                .subscriptionId("uuid_subscription")
                .build();

        given(profileService.getProfile(any(AuthUser.class))).willReturn(profileResponseDto);

        //when & then
        mockMvc.perform(MockMvcRequestBuilders
                .get("/profile")
                    .with(csrf()))
                    .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.httpCode").value(200));

    }

    @Test
    @DisplayName("사용자 구독정보 조회")
    @WithMockUser(username = "testUser")
    void getUserSubscription() throws Exception {
        //given
        SubscriptionInfoDto subscriptionInfoDto = SubscriptionInfoDto.builder()
                .category(subscription.getCategory().getCategoryType())
                .email(subscription.getEmail())
                .active(true)
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .build();

        Set<DayOfWeek> subscriptionType = EnumSet.of(
                DayOfWeek.SATURDAY,
                DayOfWeek.MONDAY,
                DayOfWeek.FRIDAY
        );

        SubscriptionHistoryDto subscriptionHistoryDto = SubscriptionHistoryDto.builder()
                .categoryType("BACKEND")
                .subscriptionId(1L)
                .subscriptionType(subscriptionType)
                .startDate(LocalDate.now())
                .updateDate(LocalDate.now())
                .build();

        List<SubscriptionHistoryDto> subscriptionLogPage = List.of(
                subscriptionHistoryDto
        );

        UserSubscriptionResponseDto userSubscriptionResponseDto = UserSubscriptionResponseDto.builder()
                .email("test@naver.com")
                .name("test")
                .subscriptionInfoDto(subscriptionInfoDto)
                .subscriptionLogPage(subscriptionLogPage)
                .userId("uuid_user")
                .build();

        given(profileService.getUserSubscription(any(AuthUser.class))).willReturn(userSubscriptionResponseDto);

        //when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/profile/subscription")
                        .with(csrf()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.httpCode").value(200));
    }

    @Test
    @DisplayName("틀린 문제 다시보기 API 테스트")
    @WithMockUser(username = "testUser")
    void getWrongQuiz() throws Exception {
        // given
        List<WrongQuizDto> wrongQuizList = List.of(
                new WrongQuizDto("문제1", "사용자답1", "정답1", "해설1"),
                new WrongQuizDto("문제2", "사용자답2", "정답2", "해설2")
        );

        ProfileWrongQuizResponseDto responseDto = new ProfileWrongQuizResponseDto(
                "uuid_user", wrongQuizList, new PageImpl<>(wrongQuizList)
        );

        given(profileService.getWrongQuiz(any(AuthUser.class), any(Pageable.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/profile/wrong-quiz")
                        .param("page", "0")
                        .param("size", "5")
                        .with(csrf()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.httpCode").value(200));
    }


    @Test
    @DisplayName("문제 선택률")
    @WithMockUser(username = "testUser")
    void getCorrectRateByCategory() throws Exception{

        Map<String, Double> correctRates = Map.of(
                "보기 1", 0.2,
                "보기 2", 0.3,
                "보기 3", 0.3,
                "보기 4", 0.2
        );

        CategoryUserAnswerRateResponse categoryUserAnswerRateResponse = CategoryUserAnswerRateResponse.builder()
                .correctRates(correctRates)
                .build();

        given(profileService.getUserQuizAnswerCorrectRate(any(AuthUser.class))).willReturn(categoryUserAnswerRateResponse);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/profile/correct-rate")
                .with(csrf()))
        .andDo(print())
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(jsonPath("$.httpCode").value(200));
    }
}