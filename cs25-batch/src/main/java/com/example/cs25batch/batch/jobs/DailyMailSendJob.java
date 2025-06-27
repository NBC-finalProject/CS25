package com.example.cs25batch.batch.jobs;

import com.example.cs25batch.batch.component.logger.MailStepLogger;
import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.batch.service.BatchProducerService;
import com.example.cs25batch.batch.service.SesMailService;
import com.example.cs25batch.batch.service.BatchSubscriptionService;
import com.example.cs25batch.batch.service.TodayQuizService;
import com.example.cs25batch.config.ThreadShuttingJobListener;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.dto.SubscriptionMailTargetDto;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DailyMailSendJob {

    private final BatchSubscriptionService subscriptionService;
    private final TodayQuizService todayQuizService;
    private final BatchProducerService batchProducerService;

    //Message Queue 적용 후
    @Bean
    public Job mailJob(JobRepository jobRepository,
        @Qualifier("mailStep") Step mailStep) {
        return new JobBuilder("mailJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(mailStep)
            .build();
    }

    @Bean
    public Step mailStep(JobRepository jobRepository,
        @Qualifier("mailTasklet") Tasklet mailTasklet,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("mailStep", jobRepository)
            .tasklet(mailTasklet, transactionManager)
            .build();
    }

    // TODO: Chunk 방식 고려
    @Bean
    public Tasklet mailTasklet(SubscriptionRepository subscriptionRepository) {
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
                    mailService.sendQuizEmail(subscription, quiz);
                    //long mailEnd = System.currentTimeMillis();
                    //log.info("[4. 메일 발송] {}ms", mailEnd - mailStart);
                }
            }

            log.info("[배치 종료] 메일 발송 완료");
            return RepeatStatus.FINISHED;
        };
    }

    //Message Queue 적용 후
    @Bean
    public Job mailProducerJob(JobRepository jobRepository,
        @Qualifier("mailProducerStep") Step mailStep) {
        return new JobBuilder("mailProducerJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(mailStep)
            .build();
    }

    @Bean
    public Step mailProducerStep(JobRepository jobRepository,
        @Qualifier("mailProducerTasklet") Tasklet mailTasklet,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("mailProducerStep", jobRepository)
            .tasklet(mailTasklet, transactionManager)
            .build();
    }

    // TODO: Chunk 방식 고려
    @Bean
    public Tasklet mailProducerTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[배치 시작] 메일 발송 대상 구독자 선별");

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

            log.info("[배치 종료] MessageQueue push 완료");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mail-step-thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Job mailConsumerWithAsyncJob(JobRepository jobRepository,
        @Qualifier("mailConsumerWithAsyncStep") Step mailConsumeStep,
        ThreadShuttingJobListener threadShuttingJobListener
    ) {
        return new JobBuilder("mailConsumerWithAsyncJob", jobRepository)
            .start(mailConsumeStep)
            .listener(threadShuttingJobListener)
            .build();
    }

    @Bean
    public Step mailConsumerWithAsyncStep(
        JobRepository jobRepository,
        @Qualifier("redisConsumeReader") ItemReader<Map<String, String>> reader,
        @Qualifier("mailMessageProcessor") ItemProcessor<Map<String, String>, MailDto> processor,
        @Qualifier("mailWriter") ItemWriter<MailDto> writer,
        PlatformTransactionManager transactionManager,
        MailStepLogger mailStepLogger,
        @Qualifier("taskExecutor") ThreadPoolTaskExecutor taskExecutor
    ) {
        return new StepBuilder("mailConsumerWithAsyncStep", jobRepository)
            .<Map<String, String>, MailDto>chunk(5, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .taskExecutor(taskExecutor)
            .listener(mailStepLogger)
            .build();
    }

    @Bean
    public Job mailRetryJob(JobRepository jobRepository,
        @Qualifier("mailRetryStep") Step mailRetryStep) {
        return new JobBuilder("mailRetryJob", jobRepository)
            .start(mailRetryStep)
            .build();
    }

    //실패한 요청 처리
    @Bean
    public Step mailRetryStep(
        JobRepository jobRepository,
        @Qualifier("redisRetryReader") ItemReader<Map<String, String>> reader,
        @Qualifier("mailMessageProcessor") ItemProcessor<Map<String, String>, MailDto> processor,
        @Qualifier("mailWriter") ItemWriter<MailDto> writer,
        PlatformTransactionManager transactionManager,
        MailStepLogger mailStepLogger
    ) {
        return new StepBuilder("mailRetryStep", jobRepository)
            .<Map<String, String>, MailDto>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .listener(mailStepLogger)
            .build();
    }

}
