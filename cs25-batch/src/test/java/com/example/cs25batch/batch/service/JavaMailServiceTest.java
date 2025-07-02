package com.example.cs25batch.batch.service;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
class JavaMailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private JavaMailService javaMailService;

    @Mock
    private MimeMessage mimeMessage;

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
    void sendQuizEmail_success() throws Exception {
        // given
        when(templateEngine.process(eq("mail-template"), any(IContext.class)))
            .thenReturn("<html>메일 내용</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        javaMailService.sendQuizEmail(subscription, quiz);

        // then
        verify(templateEngine).process(eq("mail-template"), any(IContext.class));
        verify(mailSender).send(mimeMessage);
    }

}