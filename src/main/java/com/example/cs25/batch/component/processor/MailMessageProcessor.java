package com.example.cs25.batch.component.processor;

import com.example.cs25.domain.mail.dto.MailDto;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.service.TodayQuizService;
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
    private final TodayQuizService todayQuizService;

    @Override
    public MailDto process(Map<String, String> message) throws Exception {
        Long subscriptionId = Long.valueOf(message.get("subscriptionId"));

        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);

        //MessageQueue에 들어간 후 실제 메일 발송 전에 구독 정보가 변경된 경우에 대한 유효성 검증
        //구독 해지 또는 구독 요일 변경
        if(!subscription.isActive() || !subscription.isTodaySubscribed()){
            return null;
        }

        //Quiz 출제
        Quiz quiz = todayQuizService.getTodayQuizBySubscription(subscription);

        return new MailDto(subscription, quiz);
    }
}
