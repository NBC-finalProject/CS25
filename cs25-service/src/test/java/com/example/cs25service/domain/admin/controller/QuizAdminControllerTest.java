package com.example.cs25service.domain.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25service.domain.admin.dto.request.QuizCreateRequestDto;
import com.example.cs25service.domain.admin.dto.request.QuizUpdateRequestDto;
import com.example.cs25service.domain.admin.dto.response.QuizDetailDto;
import com.example.cs25service.domain.admin.service.QuizAdminService;
import com.example.cs25service.domain.security.jwt.provider.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(QuizAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuizAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizAdminService quizAdminService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("POST /admin/quizzes/upload")
    @WithMockUser(roles = "ADMIN")
    class UploadQuizTest {

        @Test
        @DisplayName("JSON 파일 업로드 성공")
        void uploadQuiz_success() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                "file", "quiz.json", MediaType.APPLICATION_JSON_VALUE,
                "[{\"question\":\"test\"}]".getBytes()
            );

            mockMvc.perform(multipart("/admin/quizzes/upload")
                    .file(file)
                    .param("categoryType", "Backend")
                    .param("formatType", "MULTIPLE_CHOICE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("문제 등록 성공"));

            verify(quizAdminService).uploadQuizJson(any(), eq("Backend"),
                eq(QuizFormatType.MULTIPLE_CHOICE));
        }
    }

    @Nested
    @DisplayName("GET /admin/quizzes")
    @WithMockUser(roles = "ADMIN")
    class GetQuizDetailsTest {

        @Test
        @DisplayName("문제 목록 조회 성공")
        void getQuizList_success() throws Exception {
            Page<QuizDetailDto> quizPage = new PageImpl<>(List.of(
                QuizDetailDto.builder().quizId(1L).question("Q1").build()
            ));

            given(quizAdminService.getAdminQuizDetails(1, 30)).willReturn(quizPage);

            mockMvc.perform(get("/admin/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].quizId").value(1L));
        }
    }

    @Nested
    @DisplayName("GET /admin/quizzes/{quizId}")
    @WithMockUser(roles = "ADMIN")
    class GetQuizDetailTest {

        @Test
        @DisplayName("문제 상세 조회 성공")
        void getQuizDetail_success() throws Exception {
            QuizDetailDto dto = QuizDetailDto.builder()
                .quizId(1L)
                .question("Q1")
                .build();

            given(quizAdminService.getAdminQuizDetail(1L)).willReturn(dto);

            mockMvc.perform(get("/admin/quizzes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quizId").value(1L));
        }
    }

    @Nested
    @DisplayName("POST /admin/quizzes")
    @WithMockUser(roles = "ADMIN")
    class CreateQuizTest {

        @Test
        @DisplayName("문제 생성 성공")
        void createQuiz_success() throws Exception {
            QuizCreateRequestDto request = new QuizCreateRequestDto(
                "질문1", "Database", null
                , "답", "해설~~", QuizFormatType.SUBJECTIVE
            );

            given(quizAdminService.createQuiz(any())).willReturn(1L);

            mockMvc.perform(post("/admin/quizzes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(1L));
        }
    }

    @Nested
    @DisplayName("PATCH /admin/quizzes/{quizId}")
    @WithMockUser(roles = "ADMIN")
    class UpdateQuizTest {

        QuizCategory parentCategory = QuizCategory.builder()
            .categoryType("BACKEND")
            .build();

        QuizCategory databaseCategory = QuizCategory.builder()
            .categoryType("Database")
            .parent(parentCategory)
            .build();

        Quiz quiz = Quiz.builder()
            .answer("답1")
            .category(databaseCategory)
            .choice(null)
            .commentary("해설123~~")
            .level(QuizLevel.EASY)
            .question("질문11")
            .type(QuizFormatType.SUBJECTIVE)
            .build();

        @Test
        @DisplayName("문제 수정 성공")
        void updateQuiz_success() throws Exception {
            QuizUpdateRequestDto request = new QuizUpdateRequestDto(
                "질문11", "Database", null
                , "답1", "해설123~~", QuizFormatType.SUBJECTIVE
            );

            QuizDetailDto updatedDto = new QuizDetailDto(quiz, 5L);

            given(quizAdminService.updateQuiz(eq(1L), any())).willReturn(updatedDto);

            mockMvc.perform(patch("/admin/quizzes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.question").value("질문11"));
        }
    }

    @Nested
    @DisplayName("DELETE /admin/quizzes/{quizId}")
    @WithMockUser(roles = "ADMIN")
    class DeleteQuizTest {

        @Test
        @DisplayName("문제 삭제 성공")
        void deleteQuiz_success() throws Exception {
            mockMvc.perform(delete("/admin/quizzes/1"))
                .andExpect(status().isNoContent());

            verify(quizAdminService).deleteQuiz(1L);
        }
    }
}
