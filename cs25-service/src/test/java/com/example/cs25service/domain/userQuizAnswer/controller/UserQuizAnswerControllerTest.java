package com.example.cs25service.domain.userQuizAnswer.controller;

import com.example.cs25service.domain.userQuizAnswer.dto.SelectionRateResponseDto;
import com.example.cs25service.domain.userQuizAnswer.dto.UserQuizAnswerRequestDto;
import com.example.cs25service.domain.userQuizAnswer.dto.UserQuizAnswerResponseDto;
import com.example.cs25service.domain.userQuizAnswer.service.UserQuizAnswerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@WebMvcTest(UserQuizAnswerController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserQuizAnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserQuizAnswerService userQuizAnswerService;

    private UserQuizAnswerResponseDto userQuizAnswerResponseDto;

    @BeforeEach
    void setup(){
        userQuizAnswerResponseDto = UserQuizAnswerResponseDto.builder()
                .userQuizAnswerId(1L)
                .question("문제")
                .answer("답안")
                .commentary("해설")
                .isCorrect(true)
                .userAnswer("사용자 답안")
                .aiFeedback(null)
                .duplicated(false)
                .build();
    }

    @Test
    @DisplayName("정답 제출하기")
    @WithMockUser(username = "testUser")
    void submitAnswer() throws Exception {
        //given
        String quizSerialId = "uuid_quiz";

        given(userQuizAnswerService.submitAnswer(eq(quizSerialId), any(UserQuizAnswerRequestDto.class)))
                .willReturn(userQuizAnswerResponseDto);

        //when & then
        mockMvc.perform(MockMvcRequestBuilders
                .post("/quizzes/{quizSerialId}", "uuid_quiz")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                          "answer":"정답",
                          "subscriptionId": "uuid_subscription"
                    }
                """)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpCode").value(200));
    }

    @Test
    @DisplayName("객관식 or 주관식 채점")
    @WithMockUser(username = "testUser")
    void evaluateAnswer() throws Exception {
        //given
        Long userQuizAnswerId = 1L;

        given(userQuizAnswerService.evaluateAnswer(eq(userQuizAnswerId))).willReturn(userQuizAnswerResponseDto);

        //when & then
        mockMvc.perform(MockMvcRequestBuilders
                .post("/quizzes/evaluate/{userQuizAnswerId}", 1L)
                    .with(csrf()))
                    .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpCode").value(200));
    }

    @Test
    @DisplayName("특정 퀴즈의 선택률 계산")
    @WithMockUser(username = "testUser")
    void calculateSelectionRateByOption() throws Exception {
        //given
        String quizSerialId = "uuid_quiz";
        given(userQuizAnswerService.calculateSelectionRateByOption(eq(quizSerialId))).willReturn(any(SelectionRateResponseDto.class));

        //when & then
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/quizzes/{quizSerialId}/select-rate", "uuid_quiz")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

}