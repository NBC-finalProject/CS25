package com.example.cs25batch.sender.context;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.sender.MailSenderStrategy;
import java.util.Map;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailSenderContext {
    private final Map<String, MailSenderStrategy> strategyMap;

    public void send(MailDto dto, String strategyKey) {
        MailSenderStrategy strategy = getValidStrategy(strategyKey);
        strategy.sendQuizMail(dto);
    }

    private MailSenderStrategy getValidStrategy(String strategyKey) {
        MailSenderStrategy strategy = strategyMap.get(strategyKey);
        if (strategy == null) {
            throw new IllegalArgumentException("메일 전략이 존재하지 않습니다: " + strategyKey);
        }
        return strategy;
    }

}
