package com.example.cs25service.domain.ai.client;

import java.util.function.Consumer;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

public interface AiChatClient {

    String call(String systemPrompt, String userPrompt);

    ChatClient raw();

    Flux<String> stream(String systemPrompt,String userPrompt);
}
