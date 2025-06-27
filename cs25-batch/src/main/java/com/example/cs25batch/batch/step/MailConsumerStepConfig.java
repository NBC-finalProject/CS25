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

@RequiredArgsConstructor
@Configuration
public class MailConsumerStepConfig {

    @Value("${mail.strategy:sesMailSender}")
    private String strategyKey;

    private final MailConsumerProcessor processor;

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
    ) {
        return (contribution, chunkContext) -> {
            processor.process("quiz-email-stream");
            return RepeatStatus.FINISHED;
        };
    }
}
