package com.example.cs25.batch.component.reader;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component("redisConsumeReader")
@RequiredArgsConstructor
public class RedisStreamReader implements ItemReader<Map<String, String>> {

    private static final String STREAM = "quiz-email-stream";
    private static final String GROUP = "mail-consumer-group";
    private static final String CONSUMER = "mail-worker";

    private final StringRedisTemplate redisTemplate;

    @Override
    public Map<String, String> read() {
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
            Consumer.from(GROUP, CONSUMER),
            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
            StreamOffset.create(STREAM, ReadOffset.lastConsumed())
        );

        if (records == null || records.isEmpty()) {
            return null;
        }

        MapRecord<String, Object, Object> msg = records.get(0);
        redisTemplate.opsForStream().acknowledge(STREAM, GROUP, msg.getId());

        Map<String, String> data = new HashMap<>();
        msg.getValue().forEach((k, v) -> data.put(k.toString(), v.toString()));
        return data;
    }
}
