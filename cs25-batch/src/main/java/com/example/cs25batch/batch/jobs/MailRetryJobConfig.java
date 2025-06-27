package com.example.cs25batch.batch.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailRetryJobConfig {
    @Bean
    public Job mailRetryJob(JobRepository jobRepository,
        @Qualifier("mailRetryStep") Step mailRetryStep) {
        return new JobBuilder("mailRetryJob", jobRepository)
            .start(mailRetryStep)
            .build();
    }
}
