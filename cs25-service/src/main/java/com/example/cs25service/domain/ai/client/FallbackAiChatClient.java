package com.example.cs25service.domain.ai.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("fallbackAiChatClient")
@RequiredArgsConstructor
@Slf4j
@Primary
public class FallbackAiChatClient implements AiChatClient {

    private final OpenAiChatClient openAiClient;
    private final ClaudeChatClient claudeClient;

    @Override
    public String call(String systemPrompt, String userPrompt) {
        if ("true".equals(System.getenv("MOCK_AI"))) {
            return "정답입니다. 이 피드백은 테스트용입니다.";
        }
        try {
            return openAiClient.call(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.warn("OpenAI 호출 실패. Claude로 폴백합니다.", e);
            return claudeClient.call(systemPrompt, userPrompt);
        }
    }

    @Override
    public org.springframework.ai.chat.client.ChatClient raw() {
        return openAiClient.raw(); // 기본은 OpenAI 기준
    }
}

