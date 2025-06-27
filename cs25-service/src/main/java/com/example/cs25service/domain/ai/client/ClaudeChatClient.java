package com.example.cs25service.domain.ai.client;

import com.example.cs25service.domain.ai.exception.AiException;
import com.example.cs25service.domain.ai.exception.AiExceptionCode;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class ClaudeChatClient implements AiChatClient {

    private final ChatClient anthropicChatClient;

    @Override
    public String call(String systemPrompt, String userPrompt) {
        return anthropicChatClient.prompt()
            .system(systemPrompt)
            .user(userPrompt)
            .call()
            .content();
    }

    @Override
    public ChatClient raw() {
        return anthropicChatClient;
    }

    @Override
    public Flux<String> stream(String systemPrompt, String userPrompt) {
        return anthropicChatClient.prompt()
            .system(systemPrompt)
            .user(userPrompt)
            .stream()
            .content()
            .onErrorResume(error -> {
                throw new AiException(AiExceptionCode.INTERNAL_SERVER_ERROR);
            });

    }
}
