package com.example.cs25service.domain.mailSender;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25service.domain.mail.service.SesMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

@RequiredArgsConstructor
@Component("sesServiceMailSender")
public class SesMailSenderStrategy implements MailSenderServiceStrategy{

    private final SesMailService sesMailService;
    private final SesV2Client sesV2Client;

    @Override
    public void sendVerificationCodeMail(String toEmail, String code) {
        sesMailService.sendVerificationCodeEmail(toEmail, code);
    }
}
