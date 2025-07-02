package com.example.cs25service.domain.quiz.service;

import static com.example.cs25entity.domain.quiz.enums.QuizFormatType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25service.domain.quiz.dto.TodayQuizResponseDto;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class QuizPageServiceTest {

	@Mock
	private QuizRepository quizRepository;

	@InjectMocks
	private QuizPageService quizPageService;

	@Test
	@DisplayName("객관식 오늘의 문제를 반환합니다.")
	void showTodayQuizPage_multiple_choice() {
	    // given
		Quiz quiz = mock(Quiz.class);
		when(quiz.getType()).thenReturn(MULTIPLE_CHOICE);
		when(quizRepository.findBySerialIdOrElseThrow(anyString())).thenReturn(quiz);
		when(quiz.getQuestion()).thenReturn("오늘 뭐먹지?");
		when(quiz.getChoice()).thenReturn("1/2/3/4");
		when(quiz.getAnswer()).thenReturn("4");
		when(quiz.getCommentary()).thenReturn("뭐 먹을지 고르기 어렵다");
		when(quiz.getLevel()).thenReturn(QuizLevel.HARD);
		when(quiz.getCategory()).thenReturn(mock(QuizCategory.class));

	    // when
		TodayQuizResponseDto result = quizPageService.showTodayQuizPage("quizId");

	    // then
		assertNotNull(result);
		assertEquals("오늘 뭐먹지?", result.getQuestion());
		assertEquals("4", result.getAnswerNumber());
		assertEquals("HARD", result.getQuizLevel());
	}

	@Test
	@DisplayName("주관식 단답형 오늘의 문제를 반환합니다.")
	void showTodayQuizPage_short_answer() {
		// given
		Quiz quiz = mock(Quiz.class);
		when(quiz.getType()).thenReturn(SHORT_ANSWER);
		when(quizRepository.findBySerialIdOrElseThrow(anyString())).thenReturn(quiz);
		when(quiz.getQuestion()).thenReturn("오늘 뭐먹지?");
		when(quiz.getAnswer()).thenReturn("밥");
		when(quiz.getCommentary()).thenReturn("뭐 먹을지 고르기 어렵다");
		when(quiz.getLevel()).thenReturn(QuizLevel.HARD);
		when(quiz.getCategory()).thenReturn(mock(QuizCategory.class));

		// when
		TodayQuizResponseDto result = quizPageService.showTodayQuizPage("quizId");

		// then
		assertNotNull(result);
		assertEquals("오늘 뭐먹지?", result.getQuestion());
		assertNull(result.getChoice1());
		assertEquals("밥", result.getAnswer());
		assertEquals("SHORT_ANSWER", result.getQuizType());
	}

	@Test
	@DisplayName("주관식 서술형 오늘의 문제를 반환합니다.")
	void showTodayQuizPage_subjective() {
		// given
		Quiz quiz = mock(Quiz.class);
		when(quiz.getType()).thenReturn(SUBJECTIVE);
		when(quizRepository.findBySerialIdOrElseThrow(anyString())).thenReturn(quiz);
		when(quiz.getQuestion()).thenReturn("오늘 뭐먹지?");
		when(quiz.getAnswer()).thenReturn("밥");
		when(quiz.getCommentary()).thenReturn("뭐 먹을지 고르기 어렵다");
		when(quiz.getLevel()).thenReturn(QuizLevel.HARD);
		when(quiz.getCategory()).thenReturn(mock(QuizCategory.class));

		// when
		TodayQuizResponseDto result = quizPageService.showTodayQuizPage("quizId");

		// then
		assertNotNull(result);
		assertEquals("오늘 뭐먹지?", result.getQuestion());
		assertNull(result.getChoice1());
		assertEquals("밥", result.getAnswer());
		assertEquals("SUBJECTIVE", result.getQuizType());
	}

	@Test
	@DisplayName("존재하지 않는 퀴즈타입은 예외처리 합니다.")
	void showTodayQuizPage_quizType_not_found() {
		// given
		String quizId = "quizSerialId";
		Quiz quiz = mock(Quiz.class);

		when(quizRepository.findBySerialIdOrElseThrow(anyString())).thenReturn(quiz);
		when(quiz.getType()).thenReturn(null);

		//when
		QuizException e = assertThrows(QuizException.class,
			() -> ReflectionTestUtils
				.invokeMethod(
					quizPageService,
					"showTodayQuizPage", quizId
				)
		);

		//then
		assertEquals(QuizExceptionCode.QUIZ_TYPE_NOT_FOUND_ERROR, e.getErrorCode());
	}

	@Test
	@DisplayName("오늘의문제 분야가 소분류까지 있을 경우")
	void getQuizCategory_childCategory() {
	    // given
		Quiz quiz = mock(Quiz.class);
		QuizCategory quizCategory = mock(QuizCategory.class);

		// when
		when(quiz.getCategory()).thenReturn(quizCategory);
		when(quiz.getCategory().getParent()).thenReturn(quizCategory);
		when(quiz.getCategory().isChildCategory()).thenReturn(true);
		when(quiz.getCategory().getCategoryType()).thenReturn("BACKEND");

	    // then
		assertDoesNotThrow(() ->
			ReflectionTestUtils
				.invokeMethod(quizPageService,
					"getQuizCategory", quiz
				)
		);
	}
}