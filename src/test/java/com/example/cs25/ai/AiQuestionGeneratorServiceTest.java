package com.example.cs25.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cs25.domain.ai.service.AiQuestionGeneratorService;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AiQuestionGeneratorServiceTest {

    @Autowired
    private AiQuestionGeneratorService aiQuestionGeneratorService;

    @Autowired
    private QuizCategoryRepository quizCategoryRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private VectorStore vectorStore;

    @PersistenceContext
    private EntityManager em;

    @BeforeEach
    void setUp() {
        // 벡터 검색에 사용되는 카테고리 목록 등록
        quizCategoryRepository.saveAll(List.of(
            new QuizCategory(null, "운영체제"),
            new QuizCategory(null, "컴퓨터구조"),
            new QuizCategory(null, "자료구조"),
            new QuizCategory(null, "네트워크"),
            new QuizCategory(null, "DB"),
            new QuizCategory(null, "보안")
        ));
    }

    @Test
    @DisplayName("RAG 문서를 기반으로 문제를 생성하고 DB에 저장한다")
    void generateQuestionFromContextTest() {
        // when
        Quiz quiz = aiQuestionGeneratorService.generateQuestionFromContext();

        // then
        assertThat(quiz).isNotNull();
        assertThat(quiz.getQuestion()).isNotBlank();
        assertThat(quiz.getAnswer()).isNotBlank();
        assertThat(quiz.getCommentary()).isNotBlank();
        assertThat(quiz.getCategory()).isNotNull();

        Quiz persistedQuiz = quizRepository.findById(quiz.getId()).orElseThrow();
        assertThat(persistedQuiz.getQuestion()).isEqualTo(quiz.getQuestion());

        // info
        System.out.println("""
                ✅ 생성된 문제 정보
                - 문제: %s
                - 정답: %s
                - 해설: %s
                - 카테고리: %s
            """.formatted(
            quiz.getQuestion(),
            quiz.getAnswer(),
            quiz.getCommentary(),
            quiz.getCategory().getCategoryType()
        ));
    }
}
