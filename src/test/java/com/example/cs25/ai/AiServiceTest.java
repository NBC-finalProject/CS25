package com.example.cs25.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import com.example.cs25.domain.ai.config.AiPromptProperties;
import com.example.cs25.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25.domain.ai.prompt.AiPromptProvider;
import com.example.cs25.domain.ai.service.AiService;
import com.example.cs25.domain.ai.service.RagService;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizFormatType;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AiServiceTest {

    @Autowired
    private AiService aiService;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private UserQuizAnswerRepository userQuizAnswerRepository;

    @MockBean
    private ChatClient chatClient;
    @MockBean
    private RagService ragService;

    @Autowired
    private AiPromptProvider promptProvider;
    @Autowired
    private AiPromptProperties aiPromptProperties;

    private Quiz quiz;
    private Subscription subscription;
    private UserQuizAnswer answer;

    @BeforeEach
    void setUp() {
        // 퀴즈 및 카테고리 생성
        QuizCategory category = new QuizCategory(null, "NETWORK");
        quiz = new Quiz(
            null,
            QuizFormatType.SUBJECTIVE,
            "HTTP와 HTTPS의 차이점을 설명하세요.",
            "HTTPS는 암호화, HTTP는 암호화X",
            "HTTPS는 SSL/TLS로 암호화되어 보안성이 높다.",
            null,
            category
        );
        quizRepository.save(quiz);

        // 구독
        subscription = Subscription.builder()
            .email("member@example.com")
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .subscriptionType(Subscription.decodeDays(0b1111111))
            .build();
        subscriptionRepository.save(subscription);

        // 사용자 답변
        answer = UserQuizAnswer.builder()
            .quiz(quiz)
            .subscription(subscription)
            .userAnswer("HTTP는 암호화가 없고, HTTPS는 암호화로 보안성이 높다.")
            .isCorrect(null)
            .aiFeedback(null)
            .build();
        userQuizAnswerRepository.save(answer);

        // Mock - RAG 문서 반환
        List<Document> docs = List.of(new Document("HTTPS는 SSL/TLS로 보안을 강화합니다."));
        when(ragService.searchRelevant(anyString(), anyInt(), anyDouble())).thenReturn(docs);

        // Mock - ChatClient 응답 설정
        when(chatClient.prompt()
            .system(anyString())
            .user(anyString())
            .call()
            .content()
        ).thenReturn("정답: HTTPS는 암호화로 보안을 강화합니다.\n피드백: 정확히 설명하셨습니다.");
    }

    @Test
    void testGetFeedback() {
        AiFeedbackResponse response = aiService.getFeedback(answer.getId());

        assertThat(response).isNotNull();
        assertThat(response.getQuizId()).isEqualTo(quiz.getId());
        assertThat(response.getQuizAnswerId()).isEqualTo(answer.getId());
        assertThat(response.getAiFeedback()).startsWith("정답");

        var updated = userQuizAnswerRepository.findById(answer.getId()).orElseThrow();
        assertThat(updated.getAiFeedback()).isEqualTo(response.getAiFeedback());
        assertThat(updated.getIsCorrect()).isTrue();
    }
}
