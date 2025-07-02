package com.example.cs25batch.batch.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

@ExtendWith(MockitoExtension.class)
class SesMailServiceTest {
    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private SesV2Client sesV2Client;

    @InjectMocks
    private SesMailService sesMailService;

    @Mock
    private Subscription subscription;

    @Mock
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        when(subscription.getEmail()).thenReturn("test@test.com");
        when(subscription.getSerialId()).thenReturn("test-123");
        when(quiz.getQuestion()).thenReturn("질문입니다.");
        when(quiz.getSerialId()).thenReturn("quiz-123");
    }

    @Test
    @DisplayName("sendQuizEmail이 정상적으로 호출된다.")
    void sendQuizEmail_success() {
        // given
        when(templateEngine.process(eq("mail-template"), any(IContext.class)))
            .thenReturn("<html>메일 내용</html>");
        // when
        sesMailService.sendQuizEmail(subscription, quiz);

        // then
        verify(templateEngine).process(eq("mail-template"), any(IContext.class));
        verify(sesV2Client).sendEmail(any(SendEmailRequest.class));
    }

}