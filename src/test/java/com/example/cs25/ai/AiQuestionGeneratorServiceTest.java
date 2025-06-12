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
    void generateQuestionFromContextTest() {
        Quiz quiz = aiQuestionGeneratorService.generateQuestionFromContext();

        assertThat(quiz).isNotNull();
        assertThat(quiz.getQuestion()).isNotBlank();
        assertThat(quiz.getAnswer()).isNotBlank();
        assertThat(quiz.getCommentary()).isNotBlank();
        assertThat(quiz.getCategory()).isNotNull();

        System.out.println("생성된 문제: " + quiz.getQuestion());
        System.out.println("생성된 정답: " + quiz.getAnswer());
        System.out.println("생성된 해설: " + quiz.getCommentary());
        System.out.println("선택된 카테고리: " + quiz.getCategory().getCategoryType());

        Quiz persistedQuiz = quizRepository.findById(quiz.getId()).orElseThrow();
        assertThat(persistedQuiz.getQuestion()).isEqualTo(quiz.getQuestion());
    }
}
