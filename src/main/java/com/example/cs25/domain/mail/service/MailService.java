package com.example.cs25.domain.mail.service;

import com.example.cs25.domain.mail.exception.CustomMailException;
import com.example.cs25.domain.mail.exception.MailExceptionCode;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.subscription.entity.Subscription;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender; //config 없어도 properties 있으면 자동 생성되므로 autowired 사용도 가능
    private final SpringTemplateEngine templateEngine;

    protected String generateQuizLink(String apiUrl, Long subscriptionId, Long quizId) {
        String path = apiUrl.startsWith("/") ? apiUrl.substring(1) : apiUrl;
        String domain = "https://localhost:8080/" + path;
        return String.format("%s?subscriptionId=%d&quizId=%d", domain, subscriptionId, quizId);
    }

    public void sendVerificationCodeEmail(String toEmail, String code)
        throws MessagingException {
        Context context = new Context();
        context.setVariable("code", code);
        String htmlContent = templateEngine.process("verification-code", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("[CS25] 이메일 인증코드");
        helper.setText(htmlContent, true); // true = HTML

        mailSender.send(message);
    }

    public void sendQuizEmail(String apiUrl, Subscription subscription, Quiz quiz) {
        try {
            Context context = new Context();
            context.setVariable("question", quiz.getQuestion());
            context.setVariable("quizLink", generateQuizLink(apiUrl, subscription.getId(), quiz.getId()));
            context.setVariable("toEmail", subscription.getEmail());
            String htmlContent = templateEngine.process("today-quiz", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(subscription.getEmail());
            helper.setSubject("[CS25] 오늘의 문제 도착");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new CustomMailException(MailExceptionCode.EMAIL_SEND_FAILED_ERROR);
        }
    }

}
