package com.example.cs25batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Cs25BatchApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Cs25BatchApplication.class);
        System.setProperty("spring.batch.job.enabled", "false"); // 배치 자동실행 비활성화
        app.setWebApplicationType(WebApplicationType.NONE);  // Web 서버 비활성화
        ConfigurableApplicationContext context = app.run(args);
    }

    @Bean
    public CommandLineRunner runJob(JobLauncher jobLauncher, 
                                   ApplicationContext context,
                                   @Value("${spring.batch.job.name:}") String jobName) {
        return args -> {
            // 외부에서 Job 이름이 지정된 경우에만 실행
            if (jobName != null && !jobName.isEmpty()) {
                Job job = context.getBean(jobName, Job.class);
                
                JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // 중복 실행 방지
                    .toJobParameters();

                jobLauncher.run(job, params);
            } else {
                System.out.println("No job specified. Use --spring.batch.job.name=jobName to run a specific job.");
            }
        };
    }

}
