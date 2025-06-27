package com.example.cs25batch.batch.step;

import com.example.cs25batch.batch.component.logger.MailStepLogger;
import com.example.cs25batch.batch.component.processor.MailConsumerProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class MailRetryStepConfig {

    @Value("${mail.strategy:sesMailSender}")
    private String strategyKey;

    private final MailConsumerProcessor processor;

    @Bean
    public Step mailRetryStep(
        JobRepository jobRepository,
        @Qualifier("mailRetryTasklet") Tasklet retryTasklet,
        MailStepLogger mailStepLogger,
        PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("mailRetryStep", jobRepository)
            .tasklet(retryTasklet, transactionManager)
            .listener(mailStepLogger)
            .build();
    }

    @Bean
    public Tasklet mailRetryTasklet(
    ) {
        return (contribution, chunkContext) -> {
            processor.process("quiz-email-retry-stream");
            return RepeatStatus.FINISHED;
        };
    }
}
