package com.example.cs25.batch.jobs;

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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class HelloBatchJob {
	@Bean
	public Job helloJob(JobRepository jobRepository, @Qualifier("helloStep") Step helloStep) {
		return new JobBuilder("helloJob", jobRepository)
			.incrementer(new RunIdIncrementer())
			.start(helloStep)
			.build();
	}

	@Bean
	public Step helloStep(
		JobRepository jobRepository,
		@Qualifier("helloTasklet") Tasklet helloTasklet,
		PlatformTransactionManager transactionManager) {
		return new StepBuilder("helloStep", jobRepository)
			.tasklet(helloTasklet, transactionManager)
			.build();
	}

	@Bean
	public Tasklet helloTasklet() {
		return (contribution, chunkContext) -> {
			log.info("Hello, Batch!");
			System.out.println("Hello, Batch!");
			return RepeatStatus.FINISHED;
		};
	}
}
