package com.example.cs25batch.batch.jobs;

import com.example.cs25batch.batch.service.BatchProducerService;
import com.example.cs25batch.batch.service.BatchSubscriptionService;
import com.example.cs25batch.batch.service.TodayQuizService;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.dto.SubscriptionMailTargetDto;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MailConsumerJobConfig {
    @Bean
    public Job mailConsumerJob(JobRepository jobRepository,
        @Qualifier("mailConsumerStep") Step mailStep) {
        return new JobBuilder("mailConsumerJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(mailStep)
            .build();
    }
}
