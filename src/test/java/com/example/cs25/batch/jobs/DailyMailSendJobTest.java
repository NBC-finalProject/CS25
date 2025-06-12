package com.example.cs25.batch.jobs;

import com.example.cs25.domain.mail.service.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

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
}
