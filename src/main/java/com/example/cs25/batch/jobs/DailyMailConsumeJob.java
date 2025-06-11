package com.example.cs25.batch.jobs;

package com.example.cs25.batch.jobs;

import com.example.cs25.domain.mail.dto.MailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DailyMailConsumeJob {

    private final ItemReader<Map<String, String>> redisStreamReader;
    private final ItemProcessor<Map<String, String>, MailDto> mailMessageProcessor;
    private final ItemWriter<MailDto> mailWriter;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job quizEmailJob() {
        return new JobBuilder("quizEmailJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(quizEmailStep())
            .build();
    }

    @Bean
    public Step quizEmailStep() {
        return new StepBuilder("quizEmailStep", jobRepository)
            .<Map<String, String>, MailDto>chunk(10, transactionManager)
            .reader(redisStreamReader)
            .processor(mailMessageProcessor)
            .writer(mailWriter)
            .build();
    }
}
