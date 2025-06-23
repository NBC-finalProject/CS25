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

@SpringBootApplication(
        scanBasePackages = {
                "com.example"
        }
)
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
                                   @Value("${spring.batch.job.name:}") String jobNames) {
/*        return args -> {
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
        };*/
        return args -> {
            if (jobNames == null || jobNames.isBlank()) {
                System.out.println("No job specified. Use --spring.batch.job.names=job1,job2 to run jobs.");
                return;
            }

            String[] jobNameArray = jobNames.split(",");
            for (String jobName : jobNameArray) {
                jobName = jobName.trim();
                Job job = context.getBean(jobName, Job.class);

                JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // 중복 방지
                    .toJobParameters();

                System.out.println("Running job: " + jobName);
                jobLauncher.run(job, params);

                // 시간 차를 두고 실행 (예: 5초)
                Thread.sleep(10000);
            }
        };
    }
}
