package com.example.cs25batch.batch.service;

import com.example.cs25batch.util.MailLinkGenerator;
import com.example.cs25entity.domain.mail.exception.CustomMailException;
import com.example.cs25entity.domain.mail.exception.MailExceptionCode;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class JavaMailService {

    private final JavaMailSender mailSender; //config 없어도 properties 있으면 자동 생성되므로 autowired 사용도 가능
    private final SpringTemplateEngine templateEngine;
    private final StringRedisTemplate redisTemplate;

    public void sendQuizEmail(Subscription subscription, Quiz quiz) {
        try {
            Context context = new Context();
            context.setVariable("toEmail", subscription.getEmail());
            context.setVariable("question", quiz.getQuestion());
            context.setVariable("quizLink", MailLinkGenerator.generateQuizLink(subscription.getSerialId(), quiz.getSerialId()));
            context.setVariable("subscriptionSettings", MailLinkGenerator.generateSubscriptionSettings(subscription.getSerialId()));
            String htmlContent = templateEngine.process("mail-template", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(subscription.getEmail());
            helper.setSubject("[CS25] " + quiz.getQuestion());
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new CustomMailException(MailExceptionCode.EMAIL_SEND_FAILED_ERROR);
        }
    }
}
