package com.example.cs25.domain.mail.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.example.cs25.domain.mail.exception.CustomMailException;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizFormatType;
import com.example.cs25.domain.subscription.entity.Subscription;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MailServiceTest {

    @InjectMocks
    private MailService mailService;
    //서비스 내에 선언된 객체
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private SpringTemplateEngine templateEngine;
    //메서드 실행 시, 필요한 객체
    @Mock
    private MimeMessage mimeMessage;
    private final Long subscriptionId = 1L;
    private final Long quizId = 1L;
    private Subscription subscription;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        subscription = Subscription.builder()
            .subscriptionType(Subscription.decodeDays(1))
            .email("test@test.com")
            .startDate(LocalDate.of(2025, 5, 1))
            .endDate(LocalDate.of(2025, 5, 31))
            .category(new QuizCategory(1L, "BACKEND"))
            .build();

        ReflectionTestUtils.setField(subscription, "id", subscriptionId);

        quiz = Quiz.builder()
            .type(QuizFormatType.MULTIPLE_CHOICE)
            .question("테스트용 문제입니다. 무슨 용이라구요?")
            .answer("1.테스트/2.용용 죽겠지~/3.용용선생 꿔바로우 댕맛있음/4.용중의 용은 권지용")
            .commentary("문제에 답이 있다.")
            .choice("1.테스트")
            .category(new QuizCategory(1L, "BACKEND"))
            .build();

        ReflectionTestUtils.setField(quiz, "id", subscriptionId);

        given(templateEngine.process(anyString(), any(Context.class)))
            .willReturn("<html>stubbed</html>");

        given(mailSender.createMimeMessage())
            .willReturn(mimeMessage);

        //메일 send 요청을 보내지만 실제로는 발송하지 않는다
        willDoNothing().given(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void generateQuizLink_올바른_문제풀이링크를_반환한다() {
        //given
        String expectLink = "https://localhost:8080/example?subscriptionId=1&quizId=1";
        //when
        String link = mailService.generateQuizLink(subscriptionId, quizId);
        //then
        assertThat(link).isEqualTo(expectLink);
    }

    @Test
    void sendQuizEmail_문제풀이링크_발송에_성공하면_Template를_생성하고_send요청을_보낸다() throws Exception {
        //given
        //when
        mailService.sendQuizEmail(subscription, quiz);
        //then
        verify(templateEngine)
            .process(eq("today-quiz"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendQuizEmail_문제풀이링크_발송에_실패하면_CustomMailException를_던진다() throws Exception {
        // given
        doThrow(new MailSendException("발송 실패"))
            .when(mailSender).send(any(MimeMessage.class));
        // when & then
        assertThrows(CustomMailException.class, () ->
            mailService.sendQuizEmail(subscription, quiz)
        );
    }
}