package com.example.cs25service.domain.mail.service;

import com.example.cs25entity.domain.mail.exception.CustomMailException;
import com.example.cs25entity.domain.mail.exception.MailExceptionCode;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

@Service
@RequiredArgsConstructor
public class SesMailService {

    private final SpringTemplateEngine templateEngine;
    private final SesV2Client sesV2Client;

    public void sendVerificationCodeEmail(String toEmail, String code) {
        try {
            Context context = new Context();
            context.setVariable("code", code);
            String htmlContent = templateEngine.process("verification-code", context);

            //수신인
            Destination destination = Destination.builder()
                .toAddresses(toEmail)
                .build();

            //이메일 제목
            Content subject = Content.builder()
                .data("[CS25] " + "이메일 인증코드")
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

            SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress("CS25 <noreply@cs25.co.kr>")
                .build();

            sesV2Client.sendEmail(emailRequest);
        } catch (SesV2Exception e) {
            throw new CustomMailException(MailExceptionCode.EMAIL_SEND_FAILED_ERROR);
        }
    }
}
