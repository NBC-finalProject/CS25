package com.example.cs25service.domain.ai.client;

import java.util.function.Consumer;
import org.springframework.ai.chat.client.ChatClient;

public interface AiChatClient {

    String call(String systemPrompt, String userPrompt);

    ChatClient raw();

    void  stream(String systemPrompt,String userPrompt, Consumer<String> onToken);
}
