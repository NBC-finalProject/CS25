package com.example.cs25.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cs25.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25.domain.ai.service.AiService;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizCategoryType;
import com.example.cs25.domain.quiz.entity.QuizFormatType;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @PersistenceContext
    private EntityManager em;

    private Quiz quiz;
    private Subscription memberSubscription;
    private Subscription guestSubscription;
    private UserQuizAnswer answerWithMember; // 회원
    private UserQuizAnswer answerWithGuest;  // 비회원

    @BeforeEach
    void setUp() {
        QuizCategory quizCategory = new QuizCategory(null, "BACKEND");
        em.persist(quizCategory);

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

        // 회원 구독
        memberSubscription = Subscription.builder()
            .email("test@example.com")
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .isActive(true)
            .subscriptionType(0b1111111)
            .build();
        subscriptionRepository.save(memberSubscription);

        // 비회원 구독
        guestSubscription = Subscription.builder()
            .email("guest@example.com")
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(7))
            .isActive(true)
            .subscriptionType(0b1111111)
            .build();
        subscriptionRepository.save(guestSubscription);

        // 회원 답변
        answerWithMember = UserQuizAnswer.builder()
            .userAnswer("HTTP는 암호화가 없고, HTTPS는 암호화로 보안성이 높아요.")
            .subscription(memberSubscription)
            .isCorrect(null)
            .quiz(quiz)
            .build();
        userQuizAnswerRepository.save(answerWithMember);

        // 비회원 답변
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
        AiFeedbackResponse response = aiService.getFeedback(quiz.getId(),
            memberSubscription.getId());

        assertThat(response).isNotNull();
        assertThat(response.getQuizId()).isEqualTo(quiz.getId());
        assertThat(response.getQuizAnswerId()).isEqualTo(answerWithMember.getId());
        assertThat(response.getAiFeedback()).isNotEmpty();

        System.out.println("[회원 구독] AI 피드백: " + response.getAiFeedback());
    }

    @Test
    void testGetFeedbackForGuest() {
        AiFeedbackResponse response = aiService.getFeedback(quiz.getId(),
            guestSubscription.getId());

        assertThat(response).isNotNull();
        assertThat(response.getQuizId()).isEqualTo(quiz.getId());
        assertThat(response.getQuizAnswerId()).isEqualTo(answerWithGuest.getId());
        assertThat(response.getAiFeedback()).isNotEmpty();

        System.out.println("[비회원 구독] AI 피드백: " + response.getAiFeedback());
    }
}
