package com.example.cs25.batch.jobs;

import com.example.cs25.domain.mail.service.MailService;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.repository.QuizCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestMailConfig.class)
class DailyMailSendJobTest {

    @Autowired
    private MailService mailService;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job mailJob;

    @Test
    void testMailJob_배치_테스트() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        JobExecution result = jobLauncher.run(mailJob, params);

        System.out.println("Batch Exit Status: " + result.getExitStatus());
        verify(mailService, atLeast(0)).sendQuizEmail(any(), any());
    }
}
