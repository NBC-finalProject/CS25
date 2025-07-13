package com.example.cs25service.domain.mail.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25service.domain.mailSender.context.MailSenderServiceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodayQuizService {
    private final SubscriptionRepository subscriptionRepository;
    private final QuizRepository quizRepository;
    private final MailSenderServiceContext mailSenderServiceContext;

    public void sendQuizMail(Long subscriptionId){
        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);
        Quiz quiz = quizRepository.findByIdOrElseThrow(1048600L); //일단 1L로 고정
        mailSenderServiceContext.sendQuizMail(subscription, quiz);
    }
}
