package com.example.cs25batch.batch.component.reader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.bucket4j.Bucket;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class RedisStreamReaderTest {
    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOps;

    @Mock
    private Bucket bucket;

    private RedisStreamReader reader;

    @BeforeEach
    void setUp() {
        reader = new RedisStreamReader(redisTemplate, bucket);
    }

    @Test
    @DisplayName("record가_있으면_subscriptionId와_recordId를_반환한다.")
    void record_isExist_thenReturn_subscriptionId_recordId() {
        // given
        when(bucket.tryConsume(1)).thenReturn(true);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);

        Map<Object, Object> value = Map.of("subscriptionId", "123");
        MapRecord<String, Object, Object> record =
            StreamRecords.newRecord()
            .in("quiz-email-stream")
            .withId(RecordId.of("test-123"))
            .ofMap(value);

        when(streamOps.read(
            any(Consumer.class),
            any(StreamReadOptions.class),
            any(StreamOffset.class))
        ).thenReturn(List.of(record));

        // when
        Map<String, String> result = assertDoesNotThrow(() -> reader.read());

        // then
        assertThat(result)
            .containsEntry("subscriptionId", "123")
            .containsEntry("recordId", "test-123");

        verify(streamOps).acknowledge("quiz-email-stream", "mail-consumer-group", record.getId());
    }
}