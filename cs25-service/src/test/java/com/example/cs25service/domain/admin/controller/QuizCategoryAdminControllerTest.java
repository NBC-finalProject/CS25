package com.example.cs25service.domain.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cs25service.domain.admin.service.QuizCategoryAdminService;
import com.example.cs25service.domain.quiz.dto.QuizCategoryRequestDto;
import com.example.cs25service.domain.quiz.dto.QuizCategoryResponseDto;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


@ActiveProfiles("test")
@WebMvcTest(QuizCategoryAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuizCategoryAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizCategoryAdminService quizCategoryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("POST /admin/quiz-categories")
    @WithMockUser(roles = "ADMIN")
    class CreateQuizCategoryTest {

        @Test
        @DisplayName("카테고리 생성 성공")
        void createQuizCategory_success() throws Exception {
            // given
            QuizCategoryRequestDto request = QuizCategoryRequestDto.builder()
                .category("Backend")
                .parentId(null).build();

            // when & then
            mockMvc.perform(post("/admin/quiz-categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("카테고리 등록 성공"));

            verify(quizCategoryService).createQuizCategory(any(QuizCategoryRequestDto.class));
        }
    }

    @Nested
    @DisplayName("PUT /admin/quiz-categories/{quizCategoryId}")
    @WithMockUser(roles = "ADMIN")
    class UpdateQuizCategoryTest {

        @Test
        @DisplayName("카테고리 수정 성공")
        void updateQuizCategory_success() throws Exception {
            // given
            QuizCategoryRequestDto request = QuizCategoryRequestDto.builder()
                .category("Backend")
                .parentId(null)
                .build();
            QuizCategoryResponseDto response = QuizCategoryResponseDto.builder()
                .main("cs")
                .sub(null).build();

            given(quizCategoryService.updateQuizCategory(eq(1L), any(QuizCategoryRequestDto.class)))
                .willReturn(response);

            // when & then
            mockMvc.perform(put("/admin/quiz-categories/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.main").value("cs"));
        }
    }

    @Nested
    @DisplayName("DELETE /admin/quiz-categories/{quizCategoryId}")
    @WithMockUser(roles = "ADMIN")
    class DeleteQuizCategoryTest {

        @Test
        @DisplayName("카테고리 삭제 성공")
        void deleteQuizCategory_success() throws Exception {
            // when & then
            mockMvc.perform(delete("/admin/quiz-categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("카테고리가 삭제되었습니다."));

            verify(quizCategoryService).deleteQuizCategory(1L);
        }
    }
}
