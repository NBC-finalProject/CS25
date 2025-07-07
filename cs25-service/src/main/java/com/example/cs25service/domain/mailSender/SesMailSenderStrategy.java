package com.example.cs25service.domain.mailSender;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25service.domain.mail.service.SesMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@RequiredArgsConstructor
@Component("sesServiceMailSender")
public class SesMailSenderStrategy implements MailSenderServiceStrategy {

    private final SesMailService sesMailService;
    private final SesV2Client sesV2Client;

    @Override
    public void sendVerificationCodeMail(String toEmail, String code) {
        sesMailService.sendVerificationCodeEmail(toEmail, code);
    }

    @Override
    public void sendQuizMail(Subscription subscription, Quiz quiz) {
        sesMailService.sendQuizMail(subscription, quiz);
    }
}
