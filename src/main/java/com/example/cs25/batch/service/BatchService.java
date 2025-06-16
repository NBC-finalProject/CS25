package com.example.cs25.batch.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BatchService {

    private final JobLauncher jobLauncher;

    private final Job mailJob;

    public BatchService(
        JobLauncher jobLauncher,
        @Qualifier("mailJob") Job mailJob
    ) {
        this.jobLauncher = jobLauncher;
        this.mailJob = mailJob;
    }


    public void activeBatch() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(mailJob, params);
        } catch (Exception e) {
            throw new RuntimeException("메일 배치 실행 실패", e);
        }
    }
}
