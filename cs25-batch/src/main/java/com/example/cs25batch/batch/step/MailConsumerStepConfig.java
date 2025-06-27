package com.example.cs25batch.batch.step;

import com.example.cs25batch.batch.component.logger.MailStepLogger;
import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.batch.service.TodayQuizService;
import com.example.cs25batch.context.MailSenderContext;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import java.util.List;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MailConsumerStepConfig {

    @Value("${mail.strategy:sesMailSender}")
    private String strategyKey;

    @Bean
    public Step mailConsumerStep(JobRepository jobRepository,
        @Qualifier("mailConsumerTasklet") Tasklet mailTasklet,
        MailStepLogger mailStepLogger,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("mailConsumerStep", jobRepository)
            .tasklet(mailTasklet, transactionManager)
            .listener(mailStepLogger)
            .build();
    }

    // TODO: Chunk 방식 고려
    @Bean
    public Tasklet mailConsumerTasklet(
        SubscriptionRepository subscriptionRepository,
        TodayQuizService todayQuizService,
        MailSenderContext mailSenderContext,
        StringRedisTemplate redisTemplate
    ) {
        return (contribution, chunkContext) -> {
            while (true) {
                //MQ에서 메시지 꺼내기
                List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                    StreamReadOptions.empty().count(1),
                    StreamOffset.create("quiz-email-stream", ReadOffset.lastConsumed())
                );

                if (records == null || records.isEmpty()) {
                    break;
                }

                //실제 객체 조회
                //long getStart = System.currentTimeMillis();
                MapRecord<String, Object, Object> record = records.get(0);

                String subscriptionIdStr = (String) record.getValue().get("subscriptionId");
                Long subscriptionId = Long.valueOf(subscriptionIdStr);
                String recordId = record.getId().getValue();

                Subscription subscription = subscriptionRepository.findByIdOrElseThrow(
                    subscriptionId);
                //long getEnd = System.currentTimeMillis();
                //log.info("[2. 구독 정보 조회] Id : {}, eamil : {}, {}ms", subscriptionId, subscription
                //    .getEmail(), getEnd - getStart);

                if (subscription.isActive() && subscription.isTodaySubscribed()) {
                    //long quizStart = System.currentTimeMillis();
                    Quiz quiz = todayQuizService.getTodayQuizBySubscription(subscription);
                    //long quizEnd = System.currentTimeMillis();
                    //log.info("[3. 문제 출제] QuizId : {} {}ms", quiz.getId(), quizEnd - quizStart);

                    MailDto mailDto = MailDto.builder()
                        .subscription(subscription)
                        .quiz(quiz)
                        .build();

                    //long mailStart = System.currentTimeMillis();
                    mailSenderContext.send(mailDto, strategyKey);
                    //long mailEnd = System.currentTimeMillis();
                    //log.info("[4. 메일 발송] {}ms", mailEnd - mailStart);
                }

                //다 쓴 메시지는 삭제
                redisTemplate.opsForStream().delete("quiz-email-stream", record.getId());
            }
            return RepeatStatus.FINISHED;
        };
    }
}
