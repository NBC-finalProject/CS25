package com.example.cs25batch.batch.component.reader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component("redisRetryReader")
@RequiredArgsConstructor
public class RedisStreamRetryReader implements ItemReader<Map<String, String>> {

    private final StringRedisTemplate redisTemplate;

    @Override
    public Map<String, String> read() {
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
            .read(StreamOffset.fromStart("quiz-email-retry-stream"));

        if (records == null || records.isEmpty()) {
            return null;
        }

        MapRecord<String, Object, Object> msg = records.get(0);
        redisTemplate.opsForStream().delete("quiz-email-retry-stream", msg.getId());

        Map<String, String> data = new HashMap<>();
        msg.getValue().forEach((k, v) -> data.put(k.toString(), v.toString()));
        return data;
    }
}
