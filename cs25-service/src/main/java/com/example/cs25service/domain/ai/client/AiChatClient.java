package com.example.cs25service.domain.ai.client;

import org.springframework.ai.chat.client.ChatClient;

public interface AiChatClient {

    String call(String systemPrompt, String userPrompt);

    ChatClient raw();
}
