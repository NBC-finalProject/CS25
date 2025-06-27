package com.example.cs25batch.batch.step;

import com.example.cs25batch.batch.component.logger.MailStepLogger;
import com.example.cs25batch.batch.dto.MailDto;
import java.util.Map;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MailConsumerAsyncStepConfig {
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
}
