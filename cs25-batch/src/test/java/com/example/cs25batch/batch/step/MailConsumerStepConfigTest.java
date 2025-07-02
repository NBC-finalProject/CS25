package com.example.cs25batch.batch.step;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.cs25batch.batch.component.processor.MailConsumerProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
class MailConsumerStepConfigTest {
    @Mock
    private MailConsumerProcessor mailConsumerProcessor;

    private Tasklet mailConsumerTasklet;

    @BeforeEach
    void setUp() {
        MailConsumerStepConfig config = new MailConsumerStepConfig(mailConsumerProcessor);
        mailConsumerTasklet = config.mailConsumerTasklet();
    }

    @Test
    @DisplayName("메시지를_읽고_처리한다")
    void setMailConsumerTasklet_success() throws Exception {
        // when
        RepeatStatus status = mailConsumerTasklet.execute(null, null);

        // then
        verify(mailConsumerProcessor).process("quiz-email-stream");
        assertEquals(RepeatStatus.FINISHED, status);
    }
}