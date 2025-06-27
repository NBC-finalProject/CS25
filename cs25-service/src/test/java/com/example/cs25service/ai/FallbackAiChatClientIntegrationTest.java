package com.example.cs25service.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.entity.SocialType;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.ai.service.AiFeedbackStreamWorker;
import com.example.cs25service.domain.ai.service.AiService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FallbackAiChatClientIntegrationTest {

    @Autowired
    private AiService aiService;

    @Autowired
    private UserQuizAnswerRepository userQuizAnswerRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private AiFeedbackStreamWorker aiFeedbackStreamWorker;

    @Test
    @DisplayName("OpenAI 호출 실패 시 Claude로 폴백하여 피드백 생성한다")
    void openAiFail_thenUseClaudeFeedback() {
        // given - 기본 퀴즈, 사용자, 정답 생성
        QuizCategory category = QuizCategory.builder()
            .categoryType("네트워크")
            .parent(null)
            .build();
        em.persist(category);

        Quiz quiz = Quiz.builder()
            .type(QuizFormatType.SUBJECTIVE)
            .question("HTTP와 HTTPS의 차이를 설명하시오.")
            .answer("HTTPS는 보안이 강화된 프로토콜이다.")
            .commentary("HTTPS는 SSL/TLS를 통해 데이터 암호화를 제공한다.")
            .category(category)
            .level(QuizLevel.NORMAL)
            .build();
        em.persist(quiz);

        Subscription subscription = Subscription.builder()
            .category(category)
            .email("fallback@test.com")
            .startDate(LocalDate.now().minusDays(1))
            .endDate(LocalDate.now().plusDays(30))
            .subscriptionType(Set.of())
            .build();
        em.persist(subscription);

        User user = User.builder()
            .email("fallback@test.com")
            .name("fallback_user")
            .socialType(SocialType.KAKAO)
            .role(Role.USER)
            .subscription(subscription)
            .build();
        em.persist(user);

        UserQuizAnswer answer = UserQuizAnswer.builder()
            .user(user)
            .quiz(quiz)
            .userAnswer("HTTPS는 HTTP보다 빠르다.")
            .aiFeedback(null)
            .isCorrect(null)
            .subscription(subscription)
            .build();
        em.persist(answer);

        // when - AI 피드백 호출
        var response = aiService.getFeedback(answer.getId());

        // then - Claude로부터 받은 피드백이 저장됨
        UserQuizAnswer updated = userQuizAnswerRepository.findById(answer.getId()).orElseThrow();

        assertThat(updated.getAiFeedback()).isNotBlank();
        assertThat(updated.getIsCorrect()).isNotNull();
        System.out.println("📢 Claude 기반 피드백: " + updated.getAiFeedback());
    }

    @AfterEach
    void tearDown() {
        aiFeedbackStreamWorker.stop();
    }
}