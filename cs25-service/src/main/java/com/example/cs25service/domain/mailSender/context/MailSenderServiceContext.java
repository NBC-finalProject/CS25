package com.example.cs25service.domain.mailSender.context;

import com.example.cs25service.domain.mailSender.MailSenderServiceStrategy;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailSenderServiceContext {
    private final Map<String, MailSenderServiceStrategy> strategyMap;

    public void send(String toEmail, String code, String strategyKey) {
        MailSenderServiceStrategy strategy = strategyMap.get(strategyKey);
        if (strategy == null) {
            throw new IllegalArgumentException("메일 전략이 존재하지 않습니다: " + strategyKey);
        }
        strategy.sendVerificationCodeMail(toEmail, code);
    }
}
