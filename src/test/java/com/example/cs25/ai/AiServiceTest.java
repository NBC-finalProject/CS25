package com.example.cs25.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cs25.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25.domain.ai.service.AiService;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizFormatType;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AiServiceTest {

    @Autowired
    private AiService aiService;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserQuizAnswerRepository userQuizAnswerRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private VectorStore vectorStore; // RAG 문서 저장소

    @PersistenceContext
    private EntityManager em;

    private Quiz quiz;
    private Subscription memberSubscription;
    private Subscription guestSubscription;
    private UserQuizAnswer answerWithMember;
    private UserQuizAnswer answerWithGuest;

    @BeforeEach
    void setUp() {
        // 카테고리 생성
        QuizCategory quizCategory = new QuizCategory(null, "BACKEND");
        em.persist(quizCategory);

        // 퀴즈 생성
        quiz = new Quiz(
                null,
                QuizFormatType.SUBJECTIVE,
                "HTTP와 HTTPS의 차이점을 설명하세요.",
                "HTTPS는 암호화, HTTP는 암호화X",
                "HTTPS는 SSL/TLS로 암호화되어 보안성이 높다.",
                null,
                quizCategory
        );
        quizRepository.save(quiz);

        // 구독 생성 (회원, 비회원)
        memberSubscription = Subscription.builder()
                .email("test@example.com")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .subscriptionType(Subscription.decodeDays(0b1111111))
                .build();
        subscriptionRepository.save(memberSubscription);

        guestSubscription = Subscription.builder()
                .email("guest@example.com")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .subscriptionType(Subscription.decodeDays(0b1111111))
                .build();
        subscriptionRepository.save(guestSubscription);

        // 사용자 답변 생성
        answerWithMember = UserQuizAnswer.builder()
                .userAnswer("HTTP는 암호화가 없고, HTTPS는 암호화로 보안성이 높아요.")
                .subscription(memberSubscription)
                .isCorrect(null)
                .quiz(quiz)
                .build();
        userQuizAnswerRepository.save(answerWithMember);

        answerWithGuest = UserQuizAnswer.builder()
                .userAnswer("HTTP는 암호화가 없고, HTTPS는 암호화로 보안성이 높아요.")
                .subscription(guestSubscription)
                .isCorrect(null)
                .quiz(quiz)
                .build();
        userQuizAnswerRepository.save(answerWithGuest);

    }

    @Test
    void testGetFeedbackForMember() {
        AiFeedbackResponse response = aiService.getFeedback(answerWithMember.getId());

        assertThat(response).isNotNull();
        assertThat(response.getQuizId()).isEqualTo(quiz.getId());
        assertThat(response.getQuizAnswerId()).isEqualTo(answerWithMember.getId());
        assertThat(response.getAiFeedback()).isNotBlank();

        var updated = userQuizAnswerRepository.findById(answerWithMember.getId()).orElseThrow();
        assertThat(updated.getAiFeedback()).isEqualTo(response.getAiFeedback());
        assertThat(updated.getIsCorrect()).isNotNull();

        System.out.println("[회원 구독] AI 피드백:\n" + response.getAiFeedback());
    }

    @Test
    void testGetFeedbackForGuest() {
        AiFeedbackResponse response = aiService.getFeedback(answerWithGuest.getId());

        assertThat(response).isNotNull();
        assertThat(response.getQuizId()).isEqualTo(quiz.getId());
        assertThat(response.getQuizAnswerId()).isEqualTo(answerWithGuest.getId());
        assertThat(response.getAiFeedback()).isNotBlank();

        var updated = userQuizAnswerRepository.findById(answerWithGuest.getId()).orElseThrow();
        assertThat(updated.getAiFeedback()).isEqualTo(response.getAiFeedback());
        assertThat(updated.getIsCorrect()).isNotNull();

        System.out.println("[비회원 구독] AI 피드백:\n" + response.getAiFeedback());
    }
}
