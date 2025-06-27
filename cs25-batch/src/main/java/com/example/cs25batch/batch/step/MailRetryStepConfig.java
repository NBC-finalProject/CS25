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
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MailRetryStepConfig {
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
