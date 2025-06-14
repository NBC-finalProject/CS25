package com.example.cs25.batch.jobs;

import com.example.cs25.domain.mail.dto.MailDto;
import com.example.cs25.domain.mail.service.MailService;
import com.example.cs25.batch.component.logger.MailStepLogger;
import com.example.cs25.domain.quiz.service.TodayQuizService;
import com.example.cs25.domain.subscription.dto.SubscriptionMailTargetDto;
import com.example.cs25.domain.subscription.service.SubscriptionService;
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
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DailyMailSendJob {

    private final SubscriptionService subscriptionService;
    private final TodayQuizService todayQuizService;
    private final MailService mailService;

    @Bean
    public Job mailProducerJob(JobRepository jobRepository,
        @Qualifier("mailProduceStep") Step mailStep) {
        return new JobBuilder("mailProducerJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(mailStep)
            .build();
    }

    @Bean
    public Step mailProduceStep(JobRepository jobRepository,
        @Qualifier("mailTasklet") Tasklet mailTasklet,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("mailProduceStep", jobRepository)
            .tasklet(mailTasklet, transactionManager)
            .build();
    }

    // TODO: Chunk 방식 고려
    @Bean
    public Tasklet mailTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[배치 시작] 메일 발송 대상 구독자 선별");
            List<SubscriptionMailTargetDto> subscriptions = subscriptionService.getTodaySubscriptions();

            for (SubscriptionMailTargetDto sub : subscriptions) {
                Long subscriptionId = sub.getSubscriptionId();
                //메일을 발송해야 할 구독자 정보를 MessageQueue 에 넣음
                mailService.enqueueQuizEmail(subscriptionId);
            }

            log.info("[배치 종료] MessageQueue push 완료");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mail-step-thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Job mailConsumeJob(JobRepository jobRepository,
        @Qualifier("mailConsumeStep") Step mailConsumeStep) {
        return new JobBuilder("mailConsumeJob", jobRepository)
            .start(mailConsumeStep)
            .build();
    }

    @Bean
    public Step mailConsumeStep(
        JobRepository jobRepository,
        @Qualifier("redisConsumeReader") ItemReader<Map<String, String>> reader,
        @Qualifier("mailMessageProcessor") ItemProcessor<Map<String, String>, MailDto> processor,
        @Qualifier("mailWriter") ItemWriter<MailDto> writer,
        PlatformTransactionManager transactionManager,
        MailStepLogger mailStepLogger,
        @Qualifier("taskExecutor") TaskExecutor taskExecutor
    ) {
        return new StepBuilder("mailConsumeStep", jobRepository)
            .<Map<String, String>, MailDto>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .taskExecutor(taskExecutor)
            .listener(mailStepLogger)
            .build();
    }

    @Bean
    public Job mailRetryJob(JobRepository jobRepository, Step mailRetryStep) {
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
