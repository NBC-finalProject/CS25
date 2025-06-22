package com.example.cs25batch.batch.service;

import com.example.cs25entity.domain.mail.exception.CustomMailException;
import com.example.cs25entity.domain.mail.exception.MailExceptionCode;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

@Service
@RequiredArgsConstructor
public class BatchMailService {

    private final SpringTemplateEngine templateEngine;
    private final StringRedisTemplate redisTemplate;

    //producer
    public void enqueueQuizEmail(Long subscriptionId) {
        redisTemplate.opsForStream()
            .add("quiz-email-stream", Map.of("subscriptionId", subscriptionId.toString()));
    }

    protected String generateQuizLink(Long subscriptionId, Long quizId) {
        String domain = "https://cs25.co.kr/todayQuiz";
        return String.format("%s?subscriptionId=%d&quizId=%d", domain, subscriptionId, quizId);
    }

    public void sendQuizEmail(Subscription subscription, Quiz quiz) {
        try {
            Context context = new Context();
            context.setVariable("toEmail", subscription.getEmail());
            context.setVariable("question", quiz.getQuestion());
            context.setVariable("quizLink", generateQuizLink(subscription.getId(), quiz.getId()));
            String htmlContent = templateEngine.process("mail-template", context);

            //수신인
            Destination destination = Destination.builder()
                .toAddresses(subscription.getEmail())
                .build();

            //이메일 제목
            Content subject = Content.builder()
                .data("[CS25] 오늘의 문제 도착")
                .charset("UTF-8")
                .build();

            //html 구성
            Content htmlBody = Content.builder()
                .data(htmlContent)
                .charset("UTF-8")
                .build();

            Body body = Body.builder()
                .html(htmlBody)
                .build();

            Message message = Message.builder()
                .subject(subject)
                .body(body)
                .build();

            EmailContent emailContent = EmailContent.builder()
                .simple(message)
                .build();

            String senderEmail = "no-reply@cs25.co.kr";
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress(senderEmail)
                .build();

        } catch (SesV2Exception e) {
            throw new CustomMailException(MailExceptionCode.EMAIL_SEND_FAILED_ERROR);
        }
    }
}
