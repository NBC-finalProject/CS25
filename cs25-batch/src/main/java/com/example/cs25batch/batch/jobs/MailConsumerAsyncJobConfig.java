package com.example.cs25batch.batch.jobs;

import com.example.cs25batch.config.ThreadShuttingJobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConsumerAsyncJobConfig {
    @Bean
    public Job mailConsumerAsyncJob(JobRepository jobRepository,
        @Qualifier("mailConsumerAsyncStep") Step mailConsumeStep,
        ThreadShuttingJobListener threadShuttingJobListener
    ) {
        return new JobBuilder("mailConsumerAsyncJob", jobRepository)
            .start(mailConsumeStep)
            .listener(threadShuttingJobListener)
            .build();
    }
}
