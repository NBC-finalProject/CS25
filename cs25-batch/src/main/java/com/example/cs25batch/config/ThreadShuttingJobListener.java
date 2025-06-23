package com.example.cs25batch.config;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class ThreadShuttingJobListener implements JobExecutionListener {

    private final ThreadPoolTaskExecutor taskExecutor;

    public ThreadShuttingJobListener(@Qualifier("taskExecutor") ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        taskExecutor.shutdown();
    }
}
