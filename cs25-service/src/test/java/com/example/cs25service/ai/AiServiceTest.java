package com.example.cs25service.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.ai.client.AiChatClient;
import com.example.cs25service.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25service.domain.ai.prompt.AiPromptProvider;
import com.example.cs25service.domain.ai.service.AiFeedbackQueueService;
import com.example.cs25service.domain.ai.service.AiFeedbackStreamWorker;
import com.example.cs25service.domain.ai.service.AiService;
import com.example.cs25service.domain.ai.service.RagService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;

import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)  // 스프링 컨텍스트 리프레시
@Disabled
public class AiServiceTest {
    @Autowired
    private AiService aiService;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserQuizAnswerRepository userQuizAnswerRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private AiFeedbackStreamWorker aiFeedbackStreamWorker;

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
        QuizCategory quizCategory = new QuizCategory("BACKEND", null);
        em.persist(quizCategory);

        // 퀴즈 생성
        quiz = Quiz.builder()
            .type(QuizFormatType.SUBJECTIVE)
            .question("HTTP와 HTTPS의 차이점을 설명하세요.")
            .answer("HTTPS는 암호화, HTTP는 암호화X")
            .commentary("HTTPS는 SSL/TLS로 암호화되어 보안성이 높다.")
            .choice(null)
            .category(quizCategory)
            .level(QuizLevel.EASY)
            .build();
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

    @Test
    @DisplayName("6글자 이내에 정답이 포함된 경우 true 반환")
    void testIfAiFeedbackIsCorrectThenReturnTrue(){
        assertThat(aiService.isCorrect("- 정답 : 당신의 답은 완벽합니다.")).isTrue();
        assertThat(aiService.isCorrect("정답 : 당신의 답은 완벽합니다.")).isTrue();
        assertThat(aiService.isCorrect("정답입니다. 당신의 답은 완벽합니다.")).isTrue();
    }

    @Test
    @DisplayName("오답인 경우 false 반환")
    void testIfAiFeedbackIsWrongThenReturnfalse(){
        assertThat(aiService.isCorrect("- 오답 : 당신의 답은 완벽합니다.")).isFalse();
        assertThat(aiService.isCorrect("오답 : 당신의 답은 완벽합니다.")).isFalse();
        assertThat(aiService.isCorrect("오답입니다. 당신의 답은 완벽합니다.")).isFalse();
        assertThat(aiService.isCorrect("오답: 정답이라고 하기에는 부족합니다.")).isFalse();
    }

    @AfterEach
    void tearDown() {
        aiFeedbackStreamWorker.stop();
    }
}
