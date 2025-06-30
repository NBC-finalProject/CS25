package com.example.cs25service.domain.mail.service;

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
public class MailService {

    private final JavaMailSender mailSender; //config 없어도 properties 있으면 자동 생성되므로 autowired 사용도 가능
    private final SpringTemplateEngine templateEngine;
    private final StringRedisTemplate redisTemplate;

    public void sendVerificationCodeEmail(String toEmail, String code) throws MessagingException {
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
}