package com.example.cs25batch.batch.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailProducerJobConfig {

    @Bean
    public Job mailProducerJob(JobRepository jobRepository,
        @Qualifier("mailProducerStep") Step mailStep) {
        return new JobBuilder("mailProducerJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(mailStep)
            .build();
    }

}
