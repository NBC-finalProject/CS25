package com.example.cs25.batch.jobs;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import com.example.cs25.domain.mail.service.MailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StopWatch;

@SpringBootTest
@Import(TestMailConfig.class) //제거하면 실제 발송, 주석 처리 시 테스트만
class DailyMailSendJobTest {

    @Autowired
    private MailService mailService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("mailJob")
    private Job mailJob;

    @Autowired
    @Qualifier("mailProducerJob")
    private Job mailProducerJob;

    @Autowired
    @Qualifier("mailConsumerJob")
    private Job mailConsumerJob;

    @Autowired
    @Qualifier("mailConsumerWithAsyncJob")
    private Job mailConsumerWithAsyncJob;

    @AfterEach
    void cleanUp() {
        redisTemplate.delete("quiz-email-stream");
        redisTemplate.delete("quiz-email-retry-stream");
    }

//    @Test
//    void testMailJob_배치_테스트() throws Exception {
//        JobParameters params = new JobParametersBuilder()
//            .addLong("timestamp", System.currentTimeMillis())
//            .toJobParameters();
//
//        JobExecution result = jobLauncher.run(mailJob, params);
//
//        System.out.println("Batch Exit Status: " + result.getExitStatus());
//        verify(mailService, atLeast(0)).sendQuizEmail(any(), any());
//    }
//
//    @Test
//    void 메일발송_동기_성능측정() throws Exception {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start("mailJob");
//        //when
//        JobParameters params = new JobParametersBuilder()
//            .addLong("timestamp", System.currentTimeMillis())
//            .toJobParameters();
//
//        JobExecution execution = jobLauncher.run(mailJob, params);
//        stopWatch.stop();
//
//        // then
//        long totalMillis = stopWatch.getTotalTimeMillis();
//        long count = execution.getStepExecutions().stream()
//            .mapToLong(StepExecution::getWriteCount).sum();
//        long avgMillis = (count == 0) ? totalMillis : totalMillis / count;
//        System.out.println("배치 종료 상태: " + execution.getExitStatus());
//        System.out.println("총 발송 시간(ms): " + totalMillis);
//        System.out.println("총 발송 시도) " + count);
//        System.out.println("평균 시간(ms): " + avgMillis);
//
//    }
//
//    @Test
//    void 메일발송_MQ_동기_성능측정() throws Exception {
//
//        //when
//        StopWatch stopWatchProducer = new StopWatch();
//        stopWatchProducer.start("mailMQJob-producer");
//
//        JobParameters producerParams = new JobParametersBuilder()
//            .addLong("timestamp", System.currentTimeMillis())
//            .toJobParameters();
//
//        JobExecution producerExecution = jobLauncher.run(mailProducerJob, producerParams);
//        stopWatchProducer.stop();
//
//        Thread.sleep(2000);
//
//        StopWatch stopWatchConsumer = new StopWatch();
//        stopWatchConsumer.start("mailMQJob-consumer");
//        JobParameters consumerParams = new JobParametersBuilder()
//            .addLong("timestamp", System.currentTimeMillis())
//            .toJobParameters();
//
//        JobExecution consumerExecution = jobLauncher.run(mailConsumerJob, consumerParams);
//        stopWatchConsumer.stop();
//
//        // then
//        long totalMillis = stopWatchProducer.getTotalTimeMillis() + stopWatchConsumer.getTotalTimeMillis();
//        long count = consumerExecution.getStepExecutions().stream()
//            .mapToLong(StepExecution::getWriteCount).sum();
//        long avgMillis = (count == 0) ? totalMillis : totalMillis / count;
//        System.out.println("배치 종료 상태: " + consumerExecution.getExitStatus());
//        System.out.println("총 발송 시간(ms): " + totalMillis);
//        System.out.println("총 발송 시도) " + count);
//        System.out.println("평균 시간(ms): " + avgMillis);
//
//    }
//
//    @Test
//    void 메일발송_MQ_비동기_성능측정() throws Exception {
//
//        //when
//        StopWatch stopWatchProducer = new StopWatch();
//        stopWatchProducer.start("mailMQAsyncJob-producer");
//
//        JobParameters producerParams = new JobParametersBuilder()
//            .addLong("timestamp", System.currentTimeMillis())
//            .toJobParameters();
//
//        JobExecution producerExecution = jobLauncher.run(mailProducerJob, producerParams);
//        stopWatchProducer.stop();
//
//        Thread.sleep(2000); //어느 정도로 설정해놓는게 좋을까요? Job 2개 연속 실행 방지
//
//        StopWatch stopWatchConsumer = new StopWatch();
//        stopWatchConsumer.start("mailMQAsyncJob-consumer");
//        JobParameters consumerParams = new JobParametersBuilder()
//            .addLong("timestamp", System.currentTimeMillis())
//            .toJobParameters();
//
//        JobExecution consumerExecution = jobLauncher.run(mailConsumerWithAsyncJob, consumerParams);
//        stopWatchConsumer.stop();
//
//        // then
//        long totalMillis = stopWatchProducer.getTotalTimeMillis() + stopWatchConsumer.getTotalTimeMillis();
//        long count = consumerExecution.getStepExecutions().stream()
//            .mapToLong(StepExecution::getWriteCount).sum();
//        long avgMillis = (count == 0) ? totalMillis : totalMillis / count;
//        System.out.println("배치 종료 상태: " + consumerExecution.getExitStatus());
//        System.out.println("총 발송 시간(ms): " + totalMillis);
//        System.out.println("총 발송 시도 " + count);
//        System.out.println("평균 시간(ms): " + avgMillis);
//    }
}
