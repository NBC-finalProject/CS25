package com.example.cs25.batch.jobs;

import com.example.cs25.domain.mail.service.MailService;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;

@TestConfiguration
public class TestMailConfig {

    @Bean
    public JavaMailSender mailSender() {

        JavaMailSender mockSender = Mockito.mock(JavaMailSender.class);
        Mockito.when(mockSender.createMimeMessage())
            .thenReturn(new MimeMessage((Session) null));
        return mockSender;
    }

    @Bean
    public MailService mailService(JavaMailSender mailSender,
        SpringTemplateEngine templateEngine,
        StringRedisTemplate redisTemplate) {
        // 진짜 객체로 생성 후 spy 래핑
        MailService target = new MailService(mailSender, templateEngine, redisTemplate);
        return Mockito.spy(target);
    }
}