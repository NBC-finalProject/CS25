package com.example.cs25.batch.component.processor;

import com.example.cs25.domain.mail.dto.MailDto;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.exception.QuizException;
import com.example.cs25.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailMessageProcessor implements ItemProcessor<Map<String, String>, MailDto> {

    private final SubscriptionRepository subscriptionRepository;
    private final QuizRepository quizRepository;

    @Override
    public MailDto process(Map<String, String> message) throws Exception {
        Long subscriptionId = Long.valueOf(message.get("subscriptionId"));
        Long quizId = Long.valueOf(message.get("quizId"));

        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new QuizException(QuizExceptionCode.NOT_FOUND_ERROR));

        return new MailDto(subscription, quiz);
    }
}
