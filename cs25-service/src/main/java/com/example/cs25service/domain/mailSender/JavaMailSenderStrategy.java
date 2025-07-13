package com.example.cs25service.domain.mailSender;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25service.domain.mail.service.JavaMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("javaServiceMailSender")
@RequiredArgsConstructor
public class JavaMailSenderStrategy implements MailSenderServiceStrategy{
    private final JavaMailService javaMailService;

    @Override
    public void sendVerificationCodeMail(String email, String code) {
        javaMailService.sendVerificationCodeEmail(email, code);
    }

    @Override
    public void sendQuizMail(Subscription subscription, Quiz quiz){

    }
}
