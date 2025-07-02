package com.example.cs25service.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.admin.dto.request.CreateQuizDto;
import com.example.cs25service.domain.admin.dto.request.QuizCreateRequestDto;
import com.example.cs25service.domain.admin.dto.request.QuizUpdateRequestDto;
import com.example.cs25service.domain.admin.dto.response.QuizDetailDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QuizAdminServiceTest {

    @InjectMocks
    private QuizAdminService quizAdminService;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private UserQuizAnswerRepository quizAnswerRepository;

    @Mock
    private QuizCategoryRepository quizCategoryRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Validator validator;

    QuizCategory parentCategory;
    QuizCategory subCategory1;

    @BeforeEach
    void setUp() {
        // 상위 카테고리와 하위 카테고리 mock
        parentCategory = QuizCategory.builder()
            .categoryType("Backend")
            .build();

        subCategory1 = QuizCategory.builder()
            .categoryType("InformationSystemManagement")
            .parent(parentCategory)
            .build();

        ReflectionTestUtils.setField(parentCategory, "children", List.of(subCategory1));
    }


    @Nested
    @DisplayName("uploadQuizJson 함수는")
    class inUploadQuizJson {

        @Test
        @DisplayName("정상작동_시_퀴즈가저장된다")
        void uploadQuizJson_success() throws Exception {
            // given
            String categoryType = "Backend";
            QuizFormatType formatType = QuizFormatType.MULTIPLE_CHOICE;

            // JSON을 담은 가짜 파일 생성
            String json = """
                [
                  {
                    "question": "HTTP는 상태를 유지한다.",
                    "choice": "1.예/2.아니오",
                    "answer": "2",
                    "commentary": "HTTP는 무상태 프로토콜입니다.",
                    "category": "InformationSystemManagement",
                    "level": "EASY"
                  }
                ]
                """;

            MockMultipartFile file = new MockMultipartFile("file", "quiz.json", "application/json",
                json.getBytes());

            // CreateQuizDto mock
            CreateQuizDto quizDto = CreateQuizDto.builder()
                .question("HTTP는 상태를 유지한다.")
                .choice("1.예/2.아니오")
                .answer("2")
                .commentary("HTTP는 무상태 프로토콜입니다.")
                .category("InformationSystemManagement")
                .level("EASY")
                .build();

            CreateQuizDto[] quizDtos = {quizDto};

            given(quizCategoryRepository.findByCategoryTypeOrElseThrow("Backend"))
                .willReturn(parentCategory);

            given(objectMapper.readValue(any(InputStream.class), eq(CreateQuizDto[].class)))
                .willReturn(quizDtos);

            given(validator.validate(any(CreateQuizDto.class)))
                .willReturn(Collections.emptySet());

            // when
            quizAdminService.uploadQuizJson(file, categoryType, formatType);

            // then
            then(quizRepository).should(times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("정상작동_시_퀴즈가저장된다")
        void uploadQuizJson_JSON_PARSING_FAILED_ERROR() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "quiz.json", "application/json",
                "invalid".getBytes());

            given(quizCategoryRepository.findByCategoryTypeOrElseThrow("Backend"))
                .willReturn(parentCategory);

            given(objectMapper.readValue(any(InputStream.class), eq(CreateQuizDto[].class)))
                .willThrow(new IOException("파싱 오류"));

            // when & then
            assertThatThrownBy(() ->
                quizAdminService.uploadQuizJson(file, "Backend", QuizFormatType.MULTIPLE_CHOICE)
            ).isInstanceOf(QuizException.class)
                .hasMessageContaining("JSON 파싱 실패");
        }

        @Test
        @DisplayName("유효성 검증 실패 시 예외발생 한다")
        void uploadQuizJson_QUIZ_VALIDATION_FAILED_ERROR() throws Exception {
            // given
            CreateQuizDto quizDto = CreateQuizDto.builder()
                .question(null)  // 필수값 빠짐
                .choice("1.예/2.아니오")
                .answer("2")
                .category("Infra")
                .level("EASY")
                .build();

            CreateQuizDto[] quizDtos = {quizDto};

            MockMultipartFile file = new MockMultipartFile("file", "quiz.json", "application/json",
                "any".getBytes());

            given(quizCategoryRepository.findByCategoryTypeOrElseThrow("Backend"))
                .willReturn(parentCategory);
            given(objectMapper.readValue(any(InputStream.class), eq(CreateQuizDto[].class)))
                .willReturn(quizDtos);

            // 검증 실패 set
            Set<ConstraintViolation<CreateQuizDto>> violations = Set.of(
                mock(ConstraintViolation.class));
            given(validator.validate(any(CreateQuizDto.class)))
                .willReturn(violations);

            // when & then
            assertThatThrownBy(() ->
                quizAdminService.uploadQuizJson(file, "Backend", QuizFormatType.MULTIPLE_CHOICE)
            ).isInstanceOf(QuizException.class)
                .hasMessageContaining("Quiz 유효성 검증 실패");
        }
    }

    @Nested
    @DisplayName("getAdminQuizDetails 함수는")
    class inGetAdminQuizDetails {

        @Test
        @DisplayName("정상 작동 시 퀴즈리스트를 반환한다")
        void getAdminQuizDetails_success() {
            // given
            Quiz quiz = Quiz.builder()
                .question("Spring이란?")
                .answer("프레임워크")
                .commentary("스프링은 프레임워크입니다.")
                .choice(null)
                .type(QuizFormatType.MULTIPLE_CHOICE)
                .category(QuizCategory.builder().categoryType("SoftwareDevelopment")
                    .parent(parentCategory).build())
                .build();
            ReflectionTestUtils.setField(quiz, "id", 1L);

            Page<Quiz> quizPage = new PageImpl<>(List.of(quiz));

            given(quizRepository.findAllOrderByCreatedAtDesc(any(Pageable.class)))
                .willReturn(quizPage);
            given(quizAnswerRepository.countByQuizId(1L))
                .willReturn(3L);

            // when
            Page<QuizDetailDto> result = quizAdminService.getAdminQuizDetails(1, 10);

            // then
            assertThat(result).hasSize(1);
            QuizDetailDto dto = result.getContent().get(0);
            assertThat(dto.getQuestion()).isEqualTo("Spring이란?");
            assertThat(dto.getAnswer()).isEqualTo("프레임워크");
            assertThat(dto.getSolvedCnt()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("getAdminQuizDetail 함수는")
    class inGetAdminQuizDetail {

        @Test
        @DisplayName("정상 작동 시 퀴즈리스트를 반환한다")
        void getAdminQuizDetail_success() {
            // given
            Long quizId = 1L;

            Quiz quiz = Quiz.builder()
                .question("REST란?")
                .answer("자원 기반 아키텍처")
                .commentary("HTTP URI를 통해 자원을 명확히 구분합니다.")
                .choice(null)
                .type(QuizFormatType.MULTIPLE_CHOICE)
                .category(QuizCategory.builder().categoryType("SoftwareDevelopment")
                    .parent(parentCategory).build())
                .build();
            ReflectionTestUtils.setField(quiz, "id", 1L);

            given(quizRepository.findByIdOrElseThrow(quizId)).willReturn(quiz);
            given(quizAnswerRepository.countByQuizId(quizId)).willReturn(5L);

            // when
            QuizDetailDto result = quizAdminService.getAdminQuizDetail(quizId);

            // then
            assertThat(result.getQuizId()).isEqualTo(quizId);
            assertThat(result.getQuestion()).isEqualTo("REST란?");
            assertThat(result.getAnswer()).isEqualTo("자원 기반 아키텍처");
            assertThat(result.getSolvedCnt()).isEqualTo(5L);
        }

        @Test
        @DisplayName("없는_id면_예외가 발생한다.")
        void getAdminQuizDetail_NOT_FOUND_ERROR() {
            // given
            Long quizId = 999L;

            given(quizRepository.findByIdOrElseThrow(quizId))
                .willThrow(new QuizException(QuizExceptionCode.NOT_FOUND_ERROR));

            // when & then
            assertThatThrownBy(() -> quizAdminService.getAdminQuizDetail(quizId))
                .isInstanceOf(QuizException.class)
                .hasMessageContaining("해당 퀴즈를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("createQuiz 함수는")
    class inCreateQuiz {

        QuizCreateRequestDto requestDto = new QuizCreateRequestDto();

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(requestDto, "question", "REST란?");
            ReflectionTestUtils.setField(requestDto, "category", subCategory1.getCategoryType());
            ReflectionTestUtils.setField(requestDto, "choice", null);
            ReflectionTestUtils.setField(requestDto, "answer", "자원 기반 아키텍처");
            ReflectionTestUtils.setField(requestDto, "commentary", "HTTP URI를 통해 자원을 명확히 구분합니다.");
            ReflectionTestUtils.setField(requestDto, "quizType", QuizFormatType.SUBJECTIVE);
        }

        @Test
        @DisplayName("정상 작동 시 퀴즈ID를 반환 한다")
        void createQuiz_success() {
            // given

            Quiz savedQuiz = Quiz.builder()
                .category(subCategory1)
                .question(requestDto.getQuestion())
                .answer(requestDto.getAnswer())
                .choice(requestDto.getChoice())
                .commentary(requestDto.getCommentary())
                .build();
            ReflectionTestUtils.setField(savedQuiz, "id", 1L);

            given(
                quizCategoryRepository.findByCategoryTypeOrElseThrow("InformationSystemManagement"))
                .willReturn(subCategory1);

            given(quizRepository.save(any(Quiz.class)))
                .willReturn(savedQuiz);

            // when
            Long resultId = quizAdminService.createQuiz(requestDto);

            // then
            assertThat(resultId).isEqualTo(1L);
        }

        @Test
        @DisplayName("카테고리가 없으면 예외가 발생한다")
        void createQuiz_QUIZ_CATEGORY_NOT_FOUND_ERROR() {
            // given
            ReflectionTestUtils.setField(requestDto, "category", "NonExist");

            given(quizCategoryRepository.findByCategoryTypeOrElseThrow("NonExist"))
                .willThrow(new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR));

            // when & then
            assertThatThrownBy(() -> quizAdminService.createQuiz(requestDto))
                .isInstanceOf(QuizException.class)
                .hasMessageContaining("QuizCategory 를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("updateQuiz 함수는")
    class inUpdateQuiz {

        QuizUpdateRequestDto requestDto = new QuizUpdateRequestDto();

        @Test
        @DisplayName("모든 필드를 정상적으로 업데이트하면 DTO를 반환한다")
        void updateQuiz_success() {
            // given
            Long quizId = 1L;
            Quiz quiz = createSampleQuiz();
            ReflectionTestUtils.setField(quiz, "id", quizId);

            ReflectionTestUtils.setField(requestDto, "question", "기존 문제");
            ReflectionTestUtils.setField(requestDto, "category", subCategory1.getCategoryType());
            ReflectionTestUtils.setField(requestDto, "choice", null);
            ReflectionTestUtils.setField(requestDto, "answer", "1");
            ReflectionTestUtils.setField(requestDto, "commentary", "기존 해설");
            ReflectionTestUtils.setField(requestDto, "quizType", QuizFormatType.SUBJECTIVE);

            given(quizRepository.findByIdOrElseThrow(quizId)).willReturn(quiz);
            given(quizCategoryRepository.findByCategoryTypeOrElseThrow(
                "InformationSystemManagement")).willReturn(subCategory1);
            given(quizAnswerRepository.countByQuizId(quizId)).willReturn(5L);

            // when
            QuizDetailDto result = quizAdminService.updateQuiz(quizId, requestDto);

            // then
            assertThat(result.getQuestion()).isEqualTo("기존 문제");
            assertThat(result.getCommentary()).isEqualTo("기존 해설");
            assertThat(result.getCategory()).isEqualTo("InformationSystemManagement");
            assertThat(result.getChoice()).isEqualTo(null);
            assertThat(result.getType()).isEqualTo("SUBJECTIVE");
            assertThat(result.getSolvedCnt()).isEqualTo(5L);
        }

        @Test
        @DisplayName("카테고리만 변경되면 category 만 업데이트된다")
        void updateQuiz_category_success() {
            // given
            Long quizId = 1L;
            Quiz quiz = createSampleQuiz();
            ReflectionTestUtils.setField(quiz, "id", quizId);
            ReflectionTestUtils.setField(requestDto, "category", "Programming");

            QuizCategory newCategory = QuizCategory.builder()
                .categoryType("Programming")
                .parent(parentCategory)
                .build();

            ReflectionTestUtils.setField(parentCategory, "children",
                List.of(subCategory1, newCategory));

            given(quizRepository.findByIdOrElseThrow(quizId)).willReturn(quiz);
            given(quizCategoryRepository.findByCategoryTypeOrElseThrow("Programming")).willReturn(
                newCategory);
            given(quizAnswerRepository.countByQuizId(quizId)).willReturn(0L);

            // when
            QuizDetailDto result = quizAdminService.updateQuiz(quizId, requestDto);

            // then
            assertThat(result.getCategory()).isEqualTo("Programming");
        }

        @Test
        @DisplayName("존재하지 않는 퀴즈 ID면 예외가 발생한다")
        void updateQuiz_NOT_FOUND_ERROR() {
            // given
            Long quizId = 999L;

            ReflectionTestUtils.setField(requestDto, "question", "변경된 질문121");

            given(quizRepository.findByIdOrElseThrow(quizId))
                .willThrow(new QuizException(QuizExceptionCode.NOT_FOUND_ERROR));

            // when & then
            assertThatThrownBy(() -> quizAdminService.updateQuiz(quizId, requestDto))
                .isInstanceOf(QuizException.class)
                .hasMessageContaining("해당 퀴즈를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 예외가 발생한다")
        void updateQuiz_QUIZ_CATEGORY_NOT_FOUND_ERROR() {
            // given
            Long quizId = 1L;
            Quiz quiz = createSampleQuiz();
            ReflectionTestUtils.setField(quiz, "id", quizId);
            ReflectionTestUtils.setField(requestDto, "category", "NonExist");

            given(quizRepository.findByIdOrElseThrow(quizId)).willReturn(quiz);
            given(quizCategoryRepository.findByCategoryTypeOrElseThrow("NonExist"))
                .willThrow(new QuizException(QuizExceptionCode.QUIZ_CATEGORY_NOT_FOUND_ERROR));

            // when & then
            assertThatThrownBy(() -> quizAdminService.updateQuiz(quizId, requestDto))
                .isInstanceOf(QuizException.class)
                .hasMessageContaining("QuizCategory 를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("퀴즈 타입을 MULTIPLE_CHOICE로 변경하려는데 choice가 없으면 예외 발생")
        void updateQuiz_MULTIPLE_CHOICE_REQUIRE_ERROR() {
            // given
            Long quizId = 1L;
            Quiz quiz = createSampleQuiz();
            ReflectionTestUtils.setField(quiz, "id", quizId);
            ReflectionTestUtils.setField(requestDto, "quizType", QuizFormatType.MULTIPLE_CHOICE);

            given(quizRepository.findByIdOrElseThrow(quizId)).willReturn(quiz);

            // when & then
            assertThatThrownBy(() -> quizAdminService.updateQuiz(quizId, requestDto))
                .isInstanceOf(QuizException.class)
                .hasMessageContaining("객관식 문제에는 선택지가 필요합니다.");
        }

        // 헬퍼 메서드
        private Quiz createSampleQuiz() {
            return Quiz.builder()
                .question("기존 문제")
                .answer("1")
                .commentary("기존 해설")
                .choice(null)
                .type(QuizFormatType.SUBJECTIVE)
                .category(subCategory1)
                .build();
        }
    }

}