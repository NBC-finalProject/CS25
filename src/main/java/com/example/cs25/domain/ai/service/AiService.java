package com.example.cs25.domain.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;

    public String getFeedback(String userQuestion) {
        return chatClient.prompt()
                .user(userQuestion)
                .call()
                .content();
    }
}
