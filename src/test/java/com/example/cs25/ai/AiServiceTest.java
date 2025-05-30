package com.example.cs25.ai;

import com.example.cs25.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25.domain.ai.service.AiService;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizCategoryType;
import com.example.cs25.domain.quiz.entity.QuizFormatType;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25.domain.users.entity.SocialType;
import com.example.cs25.domain.users.entity.User;
import com.example.cs25.domain.users.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class AiServiceTest {

    @Autowired
    private AiService aiService;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private UserQuizAnswerRepository userQuizAnswerRepository;
    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    private Quiz quiz;
    private User user;
    private UserQuizAnswer answerWithUser; //로그인
    private UserQuizAnswer answerWithoutUser; //비로그인

    @BeforeEach
    public void setUp() {
        // QuizCategory 생성 및 저장
        QuizCategory quizCategory = new QuizCategory(
                null,
                QuizCategoryType.BACKEND
        );
        em.persist(quizCategory);

        // Quiz 임의 생성 및 저장
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

        // User 임의 생성 및 저장(로그인 사용자)
        user = User.builder()
                .email("test@example.com")
                .name("테스트유저")
                .socialType(SocialType.KAKAO)
                .build();
        userRepository.save(user);

        // UserQuizAnswer 임의 생성 및 저장(로그인 사용자)
        answerWithUser = UserQuizAnswer.builder()
                .userAnswer("HTTP는 암호화가 없고, HTTPS는 암호화로 보안성이 높아요.")
                .aiFeedback(null)
                .isCorrect(null)
                .user(user)
                .quiz(quiz)
                .build();
        userQuizAnswerRepository.save(answerWithUser);

        // UserQuizAnswer (비로그인 사용자)
        answerWithoutUser = UserQuizAnswer.builder()
                .userAnswer("HTTP는 암호화가 없고, HTTPS는 암호화로 보안성이 높아요.")
                .aiFeedback(null)
                .isCorrect(null)
                .user(null)
                .quiz(quiz)
                .build();
        userQuizAnswerRepository.save(answerWithoutUser);
    }

    @Test
    void testGetFeedbackWithLogin() {
        // 로그인 사용자의 답변이 가장 최근이면 이 답변이 평가됨
        AiFeedbackResponse response = aiService.getFeedback(quiz.getId());

        assertThat(response).isNotNull();
        assertThat(response.getQuizId()).isEqualTo(quiz.getId());
        assertThat(response.getQuizAnswerId()).isEqualTo(answerWithoutUser.getId()); // 가장 최근 저장된 답변
        assertThat(response.getAiFeedback()).isNotEmpty();

        System.out.println("[로그인 상태] AI 피드백: " + response.getAiFeedback());
    }

    @Test
    void testGetFeedbackWithoutLogin() {
        // 비로그인 답변이 가장 최근이면 이 답변이 평가됨
        // 또는, 테스트 목적에 따라 user=null인 답변만 따로 조회하도록 서비스 로직을 바꿀 수도 있음
        AiFeedbackResponse response = aiService.getFeedback(quiz.getId());

        assertThat(response).isNotNull();
        assertThat(response.getQuizId()).isEqualTo(quiz.getId());
        assertThat(response.getQuizAnswerId()).isEqualTo(answerWithoutUser.getId());
        assertThat(response.getAiFeedback()).isNotEmpty();

        System.out.println("[비로그인 상태] AI 피드백: " + response.getAiFeedback());
    }
}
