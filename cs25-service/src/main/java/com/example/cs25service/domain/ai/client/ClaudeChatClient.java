package com.example.cs25service.domain.ai.client;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

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
}
