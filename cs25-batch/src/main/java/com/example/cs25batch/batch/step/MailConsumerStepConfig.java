package com.example.cs25batch.batch.step;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.dto.SubscriptionMailTargetDto;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class MailConsumerStepConfig {
    @Bean
    public Step mailConsumerStep(JobRepository jobRepository,
        @Qualifier("mailConsumerTasklet") Tasklet mailTasklet,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("mailConsumerStep", jobRepository)
            .tasklet(mailTasklet, transactionManager)
            .build();
    }

    // TODO: Chunk 방식 고려
    @Bean
    public Tasklet mailConsumerTasklet(SubscriptionRepository subscriptionRepository) {
        return (contribution, chunkContext) -> {
            log.info("[배치 시작] 메일 발송 대상 구독자 선별");

            //long searchStart = System.currentTimeMillis();
            List<SubscriptionMailTargetDto> subscriptions = subscriptionService.getTodaySubscriptions();
            //long searchEnd = System.currentTimeMillis();

            //log.info("[1. 발송 리스트 조회] {}개, {}ms", subscriptions.size(), searchEnd - searchStart);

            for (SubscriptionMailTargetDto sub : subscriptions) {
                Long subscriptionId = sub.getSubscriptionId();

                //long getStart = System.currentTimeMillis();
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

                    //long mailStart = System.currentTimeMillis();
                    //mailService.sendQuizEmail(subscription, quiz);
                    //long mailEnd = System.currentTimeMillis();
                    //log.info("[4. 메일 발송] {}ms", mailEnd - mailStart);
                }
            }

            log.info("[배치 종료] 메일 발송 완료");
            return RepeatStatus.FINISHED;
        };
    }
}
