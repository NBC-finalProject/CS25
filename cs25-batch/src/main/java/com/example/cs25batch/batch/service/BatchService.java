package com.example.cs25batch.batch.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BatchService {

    private final JobLauncher jobLauncher;
    private final Job producerJob;
    private final Job consumerJob;

    @Autowired
    public BatchService(JobLauncher jobLauncher,
        @Qualifier("mailProducerJob") Job producerJob,
        @Qualifier("mailConsumerWithAsyncJob") Job consumerJob
    ) {
        this.jobLauncher = jobLauncher;
        this.producerJob = producerJob;
        this.consumerJob = consumerJob;
    }

    public void activeProducerJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(producerJob, params);
        } catch (Exception e) {
            throw new RuntimeException("메일 배치 실행 실패", e);
        }
    }

    public void activeConsumerJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(consumerJob, params);
        } catch (Exception e) {
            throw new RuntimeException("메일 배치 실행 실패", e);
        }
    }
}
