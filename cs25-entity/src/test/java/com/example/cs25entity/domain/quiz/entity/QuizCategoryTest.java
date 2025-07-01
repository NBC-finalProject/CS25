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
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class}) // QueryDsl, Jpa 설정
@ActiveProfiles("test") // application-test.properties 사용 선언
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // DB 설정이 그대로 사용됨 (application-test.properties 기반)
class QuizCategoryTest {

	@Autowired QuizCategoryRepository quizCategoryRepository;

	private QuizCategory parentQuizCategory;

	@BeforeEach
	void setParent(){
		parentQuizCategory = QuizCategory.builder()
			.categoryType("BACKEND")
			.build();
		quizCategoryRepository.save(parentQuizCategory);
	}

	@Test
	@DisplayName("퀴즈 카테고리 빌더 생성자 테스트")
	void builder() {
		// given
		QuizCategory frontend = QuizCategory.builder()
			.categoryType("FRONTEND")
			.parent(null)
			.build();
		QuizCategory savedFrontend = quizCategoryRepository.save(frontend);

		// when
		QuizCategory child1 = QuizCategory.builder()
			.categoryType("REACT")
			.parent(savedFrontend)
			.build();
		QuizCategory savedChild1 = quizCategoryRepository.save(child1);

		QuizCategory child2 = QuizCategory.builder()
			.categoryType("NEXT")
			.parent(savedFrontend)
			.build();
		QuizCategory savedChild2 = quizCategoryRepository.save(child2);

		// then
		assertEquals("FRONTEND", savedFrontend.getCategoryType());
		assertNull(savedFrontend.getParent());
		assertFalse(savedFrontend.isChildCategory());
		assertNotNull(savedFrontend.getId());

		assertEquals("REACT", savedChild1.getCategoryType());
		assertEquals(savedFrontend, savedChild1.getParent());
		assertTrue(savedChild1.isChildCategory());
		assertNotNull(savedChild1.getId());
		
		assertEquals("NEXT", savedChild2.getCategoryType());
		assertEquals(savedFrontend, savedChild2.getParent());
		assertTrue(savedChild2.isChildCategory());
		assertNotNull(savedChild2.getId());
	}

	@Test
	@DisplayName("자식 카테고리인지 확인합니다.")
	void isChildCategory_test() {
	    // when
		QuizCategory child1 = QuizCategory.builder()
			.categoryType("SOFTWARE")
			.parent(parentQuizCategory)
			.build();
		quizCategoryRepository.save(child1);

		QuizCategory child2 = QuizCategory.builder()
			.categoryType("DATABASE")
			.parent(parentQuizCategory)
			.build();
		quizCategoryRepository.save(child2);

	    // then
		assertFalse(parentQuizCategory.isChildCategory());
		assertTrue(child1.isChildCategory());
		assertTrue(child2.isChildCategory());
	}
}