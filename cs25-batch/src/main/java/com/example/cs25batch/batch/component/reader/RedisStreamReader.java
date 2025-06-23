package com.example.cs25batch.batch.component.reader;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("redisConsumeReader")
@RequiredArgsConstructor
public class RedisStreamReader implements ItemReader<Map<String, String>> {

    private static final String STREAM = "quiz-email-stream";

    private final StringRedisTemplate redisTemplate;

    @Override
    public Map<String, String> read() {
        //long start = System.currentTimeMillis();
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
            StreamOffset.create(STREAM, ReadOffset.latest()) // 최신 메시지만 소비
        );

        if (records == null || records.isEmpty()) {
            return null;
        }

        MapRecord<String, Object, Object> msg = records.get(0);

        Map<String, String> data = new HashMap<>();
        msg.getValue().forEach((k, v) -> data.put(k.toString(), v.toString()));

        //long end = System.currentTimeMillis();
        //log.info("[3. Queue에서 꺼내기] {}ms", end - start);

        return data;
    }
}
