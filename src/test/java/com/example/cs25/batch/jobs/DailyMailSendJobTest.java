package com.example.cs25.batch.jobs;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.example.cs25.domain.mail.service.MailService;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
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
    private JobLauncher jobLauncher;

    @Autowired
    private Job mailJob;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private Job mailConsumeJob;

    @AfterEach
    void cleanUp() {
        redisTemplate.delete("quiz-email-stream");
        redisTemplate.delete("quiz-email-retry-stream");
    }

    @Test
    void testMailJob_배치_테스트() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        JobExecution result = jobLauncher.run(mailJob, params);

        System.out.println("Batch Exit Status: " + result.getExitStatus());
        verify(mailService, atLeast(0)).sendQuizEmail(any(), any());
    }

    @Test
    void testMailJob_발송_실패시_retry큐에서_재전송() throws Exception {
        doThrow(new RuntimeException("테스트용 메일 실패"))
            .doNothing() // 두 번째는 성공하도록
            .when(mailService).sendQuizEmail(any(), any());

        // 2. Job 실행
        JobParameters params = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

        jobLauncher.run(mailJob, params);

        // 3. retry-stream 큐가 비어있어야 정상 (재시도 후 성공했기 때문)
        Long retryCount = redisTemplate.opsForStream()
            .size("quiz-email-retry-stream");

        assertThat(retryCount).isEqualTo(0);
    }

    @Test
    void 대량메일발송_MQ비동기_성능측정() throws Exception {
        //given
        for (int i = 0; i < 1000; i++) {
            Map<String, String> data = Map.of(
                "email", "test@test.com",  // 실제 수신 가능한 테스트 이메일 권장
                "subscriptionId", "1",                  // 유효한 subscriptionId 필요
                "quizId", "1"                           // 유효한 quizId 필요
            );
            redisTemplate.opsForStream().add("quiz-email-stream", data);
        }

        //when
        JobParameters params = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("mailJob");

        JobExecution execution = jobLauncher.run(mailJob, params);
        stopWatch.stop();

        // then
        long totalMillis = stopWatch.getTotalTimeMillis();
        long count = execution.getStepExecutions().stream()
            .mapToLong(StepExecution::getWriteCount).sum();
        System.out.println("배치 종료 상태: " + execution.getExitStatus());
        System.out.println("총 발송 시간(ms): " + totalMillis);
        System.out.println("총 발송 시도) " + count);
//        System.out.println("평균 시간(ms): " + totalMillis/count);
    }
}
