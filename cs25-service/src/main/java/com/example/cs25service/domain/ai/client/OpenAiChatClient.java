package com.example.cs25service.domain.ai.client;

import com.example.cs25service.domain.ai.exception.AiException;
import com.example.cs25service.domain.ai.exception.AiExceptionCode;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenAiChatClient implements AiChatClient {

    private final ChatClient openAiChatClient;

    @Override
    public String call(String systemPrompt, String userPrompt) {
        try {
            return openAiChatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content()
                .trim();
        } catch (Exception e) {
            throw new AiException(AiExceptionCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ChatClient raw() {
        return openAiChatClient;
    }

    @Override
    public void stream(String systemPrompt, String userPrompt, Consumer<String> onToken) {
        try {
            openAiChatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .stream()
                .content()
                .doOnNext(onToken)
                .subscribe();
        } catch (Exception e) {
            throw new AiException(AiExceptionCode.INTERNAL_SERVER_ERROR);
        }
    }
}

