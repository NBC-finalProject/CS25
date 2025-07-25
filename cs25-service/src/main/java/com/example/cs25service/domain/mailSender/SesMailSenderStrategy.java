package com.example.cs25service.domain.mailSender;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25service.domain.mail.service.SesMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component("sesServiceMailSender")
public class SesMailSenderStrategy implements MailSenderServiceStrategy {

    private final SesMailService sesMailService;

    @Override
    public void sendVerificationCodeMail(String toEmail, String code) {
        sesMailService.sendVerificationCodeEmail(toEmail, code);
    }

    @Override
    public void sendQuizMail(Subscription subscription, Quiz quiz) {
        sesMailService.sendQuizMail(subscription, quiz);
    }
}
