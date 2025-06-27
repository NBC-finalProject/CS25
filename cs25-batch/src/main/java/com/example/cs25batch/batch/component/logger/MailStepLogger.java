package com.example.cs25batch.batch.component.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class MailStepLogger implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(MailStepLogger.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("[{}] Step 시작", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("[{}] Step 종료 - 상태: {}", stepExecution.getStepName(),
            stepExecution.getExitStatus());
        return stepExecution.getExitStatus();
    }
}
