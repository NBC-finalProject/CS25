package com.example.cs25batch.batch.component.writer;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.sender.context.MailSenderContext;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MailWriterTest {
    @Mock
    private MailSenderContext mailSenderContext;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOps;

    private MailWriter mailWriter;

    @BeforeEach
    void setUp() {
        mailWriter = new MailWriter(mailSenderContext, redisTemplate);
        // strategyKey 수동 주입
        ReflectionTestUtils.setField(mailWriter, "strategyKey", "javaBatchMailSender");

        when(redisTemplate.opsForStream()).thenReturn(streamOps);
    }

    @Test
    @DisplayName("정상적으로_메일을_보내고_stream을_삭제한다")
    void send_mail_success_and_delete_stream() throws Exception {
        // given
        MailDto mail = MailDto.builder()
            .recordId("test-123")
            .subscription(mock(Subscription.class))
            .quiz(mock(Quiz.class))
            .build();

        // when
        Chunk<MailDto> chunk = new Chunk<>(List.of(mail));
        mailWriter.write(chunk);

        // then
        verify(mailSenderContext).send(eq(mail), eq("javaBatchMailSender"));
        verify(streamOps).delete(eq("quiz-email-stream"), eq(RecordId.of("test-123")));
    }

    @Test
    @DisplayName("예외가_발생해도_stream_삭제는_수행된다")
    void though_occur_exception_delete_stream() throws Exception {
        // given
        MailDto mail = MailDto.builder()
            .recordId("test-123")
            .subscription(mock(Subscription.class))
            .quiz(mock(Quiz.class))
            .build();

        doThrow(new RuntimeException("메일 에러")).when(mailSenderContext).send(any(), any());

        // when
        Chunk<MailDto> chunk = new Chunk<>(List.of(mail));
        mailWriter.write(chunk);

        // then
        verify(mailSenderContext).send(any(), any());
        verify(streamOps).delete(eq("quiz-email-stream"), eq(RecordId.of("test-123")));
    }
}