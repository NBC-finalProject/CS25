package com.example.cs25batch.batch.step;

import com.example.cs25batch.batch.component.logger.MailStepLogger;
import com.example.cs25batch.batch.service.BatchProducerService;
import com.example.cs25batch.batch.service.BatchSubscriptionService;
import com.example.cs25entity.domain.subscription.dto.SubscriptionMailTargetDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Configuration
public class MailProducerStepConfig {
    private final BatchSubscriptionService subscriptionService;
    private final BatchProducerService batchProducerService;

    @Bean
    public Step mailProducerStep(JobRepository jobRepository,
        @Qualifier("mailProducerTasklet") Tasklet mailTasklet,
        MailStepLogger mailStepLogger,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("mailProducerStep", jobRepository)
            .tasklet(mailTasklet, transactionManager)
            .listener(mailStepLogger)
            .build();
    }

    // TODO: Chunk 방식 고려
    @Bean
    public Tasklet mailProducerTasklet() {
        return (contribution, chunkContext) -> {
            //long searchStart = System.currentTimeMillis();
            List<SubscriptionMailTargetDto> subscriptions = subscriptionService.getTodaySubscriptions();
            //long searchEnd = System.currentTimeMillis();
            //log.info("[1. 발송 리스트 조회] {}개, {}ms", subscriptions.size(), searchEnd - searchStart);

            for (SubscriptionMailTargetDto sub : subscriptions) {
                Long subscriptionId = sub.getSubscriptionId();
                //메일을 발송해야 할 구독자 정보를 MessageQueue 에 넣음
                //long queueStart = System.currentTimeMillis();
                batchProducerService.enqueueQuizEmail(subscriptionId);
                //long queueEnd = System.currentTimeMillis();
                //log.info("[2. Queue에 넣기] {}ms", queueEnd-queueStart);
            }

            return RepeatStatus.FINISHED;
        };
    }
}
