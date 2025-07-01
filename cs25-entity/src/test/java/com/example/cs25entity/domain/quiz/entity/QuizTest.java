package com.example.cs25entity.domain.quiz.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.example.cs25common.global.config.JpaAuditingConfig;
import com.example.cs25entity.config.QuerydslConfig;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class}) // QueryDsl, Jpa 설정
@ActiveProfiles("test") // application-test.properties 사용 선언
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // DB 설정이 그대로 사용됨 (application-test.properties 기반)
class QuizTest {

	@Autowired QuizRepository quizRepository;
	@Autowired QuizCategoryRepository quizCategoryRepository;

	private Quiz subjectiveQuiz; // 서술형 문제
	private Quiz multipleChoiceQuiz; // 객관식 문제
	private QuizCategory quizCategory;

	@BeforeEach
	void setQuiz(){
		// 퀴즈 카테고리 생성
		quizCategory = QuizCategory.builder()
			.categoryType("BACKEND")
			.build();
		quizCategoryRepository.save(quizCategory);

		// 퀴즈 엔티티 생성
		subjectiveQuiz = Quiz.builder()
			.type(QuizFormatType.SUBJECTIVE)
			.question("HTTP와 HTTPS의 차이점을 설명하세요.")
			.answer("HTTPS는 암호화가 되어있고, HTTP는 암호화가 되어있지 않다.")
			.commentary("HTTPS는 SSL/TLS로 암호화되어 보안성이 높다.")
			.choice(null) // 객관식일 경우에만 값이 들어감
			.category(quizCategory)
			.level(QuizLevel.EASY)
			.build();
		multipleChoiceQuiz = Quiz.builder()
			.type(QuizFormatType.MULTIPLE_CHOICE)
			.question("UML 다이어그램 중 순차 다이어그램에 대한 설명으로 틀린 것은?")
			.answer("2.주로 시스템의 정적 측면을 모델링하기 위해 사용한다.")
			.commentary("정답은 \\\"주로 시스템의 정적 측면을 모델링하기 위해 사용한다.\\\" 이다. 순차 다이어그램은 객체 간의 동적 상호작용을 모델링하는 것이 주된 목적이며, 일반적으로 다이어그램의 수직 방향이 시간의 흐름을 나타낸다. 회귀 메시지(Self-Message), 제어블록(Statement block) 등으로 구성된다. 따라서, 주로 시스템의 정적 측면을 모델링하기 위해 사용하는 것은 아니다.")
			.choice("1.객체 간의 동적 상호작용을 시간 개념을 중심으로 모델링 하는 것이다./2.주로 시스템의 정적 측면을 모델링하기 위해 사용한다./3.일반적으로 다이어그램의 수직 방향이 시간의 흐름을 나타낸다./4.회귀 메시지(Self-Message), 제어블록(Statement block) 등으로 구성된다./")
			.category(quizCategory)
			.level(QuizLevel.NORMAL)
			.build();
		quizRepository.save(subjectiveQuiz);
		quizRepository.save(multipleChoiceQuiz);
	}

	@Test
	@DisplayName("퀴즈 빌더 생성자 동작 테스트")
	void builder() {
	    // given & when
		Quiz quiz = Quiz.builder()
			.type(QuizFormatType.SUBJECTIVE)
			.question("HTTP와 HTTPS의 차이점을 설명하세요.")
			.answer("HTTPS는 암호화가 되어있고, HTTP는 암호화가 되어있지 않다.")
			.commentary("HTTPS는 SSL/TLS로 암호화되어 보안성이 높다.")
			.choice(null) // 객관식일 경우에만 값이 들어감
			.category(quizCategory)
			.level(QuizLevel.EASY)
			.build();
		quizRepository.save(quiz);

	    // then
		assertEquals(QuizFormatType.SUBJECTIVE, quiz.getType());
		assertEquals("HTTP와 HTTPS의 차이점을 설명하세요.", quiz.getQuestion());
		assertEquals("HTTPS는 암호화가 되어있고, HTTP는 암호화가 되어있지 않다.", quiz.getAnswer());
		assertEquals("HTTPS는 SSL/TLS로 암호화되어 보안성이 높다.", quiz.getCommentary());
		assertFalse(quiz.isDeleted());
		assertEquals(QuizLevel.EASY, quiz.getLevel());
		assertNotNull(quiz.getSerialId());
	}

	@Test
	@DisplayName("퀴즈 카테고리를 업데이트합니다.")
	void updateQuizCategory() {
	    // given
		QuizCategory newQuizCategory = QuizCategory.builder()
			.categoryType("FRONTEND")
			.build();

	    // when
		subjectiveQuiz.updateCategory(newQuizCategory);

	    // then
		assertEquals(newQuizCategory, subjectiveQuiz.getCategory());
	}

	@Test
	@DisplayName("객관식 보기를 업데이트합니다.")
	void updateChoice() {
	    // given
		String newChoice = "1.객체./2.시스템./3.다이어그램./4.회귀 메시지./";

	    // when
		subjectiveQuiz.updateChoice(newChoice);
		multipleChoiceQuiz.updateChoice(newChoice);

	    // then
		assertEquals(newChoice, multipleChoiceQuiz.getChoice());
		assertNull(subjectiveQuiz.getChoice());
	}

	@Test
	@DisplayName("문제를 업데이트합니다.")
	void updateQuestion() {
	    // given
		String newQuestion = "오늘 뭐먹지?";

	    // when
		subjectiveQuiz.updateQuestion(newQuestion);
		multipleChoiceQuiz.updateQuestion(newQuestion);

	    // then
		assertEquals(newQuestion, subjectiveQuiz.getQuestion());
		assertEquals(newQuestion, multipleChoiceQuiz.getQuestion());
	}

	@Test
	@DisplayName("")
	void updateCommentary() {
	    // given
		String newCommentary = "코멘타리 업데이트";

		// when
		subjectiveQuiz.updateQuestion(newCommentary);
		multipleChoiceQuiz.updateQuestion(newCommentary);

		// then
		assertEquals(newCommentary, subjectiveQuiz.getQuestion());
		assertEquals(newCommentary, multipleChoiceQuiz.getQuestion());
	}

	@Test
	@DisplayName("")
	void updateType() {
	    // given
		QuizFormatType subjectiveQuizType = QuizFormatType.SUBJECTIVE;
		QuizFormatType multipleQuizType = QuizFormatType.MULTIPLE_CHOICE;

	    // when
		subjectiveQuiz.updateType(multipleQuizType);
		multipleChoiceQuiz.updateType(subjectiveQuizType);

	    // then
		assertNull(multipleChoiceQuiz.getChoice());
		assertEquals(subjectiveQuizType, multipleChoiceQuiz.getType());
		assertEquals(multipleQuizType, subjectiveQuiz.getType());
	}

	@Test
	@DisplayName("퀴즈를 활성화시킵니다.")
	void enableQuiz_test() {
	    // given
		subjectiveQuiz.disableQuiz();

	    // when
		subjectiveQuiz.enableQuiz();

	    // then
		assertFalse(subjectiveQuiz.isDeleted());
	}

	@Test
	@DisplayName("퀴즈를 비활성화시킵니다.")
	void disableQuiz_test() {
	    // given
		subjectiveQuiz.enableQuiz();

	    // when
		subjectiveQuiz.disableQuiz();

	    // then
		assertTrue(subjectiveQuiz.isDeleted());
	}
}