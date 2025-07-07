package com.example.cs25service.domain.mailSender;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;

public interface MailSenderServiceStrategy {
    void sendVerificationCodeMail(String email, String code);

    void sendQuizMail(Subscription subscription, Quiz quiz);
}
