package com.example.cs25batch.batch.component.processor;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.batch.service.TodayQuizService;
import com.example.cs25batch.context.MailSenderContext;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailConsumerProcessor {
    private final SubscriptionRepository subscriptionRepository;
    private final TodayQuizService todayQuizService;
    private final MailSenderContext mailSenderContext;
    private final StringRedisTemplate redisTemplate;

    @Value("${mail.strategy:javaBatchMailSender}")
    private String strategyKey;

    public void process(String streamKey) {
        while (true) {
            List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                StreamReadOptions.empty().count(1),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed())
            );

            if (records == null || records.isEmpty()) break;

            MapRecord<String, Object, Object> record = records.get(0);
            try {
                Long subscriptionId = Long.valueOf((String) record.getValue().get("subscriptionId"));
                Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);

                if (subscription.isActive() && subscription.isTodaySubscribed()) {
                    Quiz quiz = todayQuizService.getTodayQuizBySubscription(subscription);
                    MailDto mailDto = MailDto.builder()
                        .subscription(subscription)
                        .quiz(quiz)
                        .build();

                    mailSenderContext.send(mailDto, strategyKey);
                }

                // 메일 발송 성공 시 삭제
                redisTemplate.opsForStream().delete(streamKey, record.getId());

            } catch (Exception e) {
                // 실패해도 다음 record로 넘어가기
            }
        }
    }
}
