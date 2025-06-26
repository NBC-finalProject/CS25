package com.example.cs25service.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25service.domain.ai.service.AiFeedbackStreamWorker;
import com.example.cs25service.domain.ai.service.AiQuestionGeneratorService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)  // 스프링 컨텍스트 리프레시
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

    @Autowired
    private AiFeedbackStreamWorker aiFeedbackStreamWorker;

    @BeforeEach
    void setUp() {
        List<String> requiredCategories = List.of(
            "SoftwareDevelopment",
            "SoftwareDesign",
            "Programming",
            "Database",
            "InformationSystemManagement"
        );

        for (String categoryType : requiredCategories) {
            boolean exists = quizCategoryRepository.existsByCategoryType(categoryType);
            if (!exists) {
                quizCategoryRepository.save(new QuizCategory(categoryType, null));
            }
        }

        em.flush();
        em.clear();
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

    @AfterEach
    void tearDown() {
        aiFeedbackStreamWorker.stop();
    }
}
