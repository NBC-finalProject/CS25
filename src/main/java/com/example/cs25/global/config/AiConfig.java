package com.example.cs25.global.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAiKey;

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

//    @Bean
//    public EmbeddingModel embeddingModel() {
//        OpenAiApi openAiApi = OpenAiApi.builder()
//                .apiKey(openAiKey)
//                .build();
//        return new OpenAiEmbeddingModel(openAiApi);
//    }

    @Bean
    public EmbeddingModel embeddingModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
            .apiKey(openAiKey)
            .build();
        return new OpenAiEmbeddingModel(openAiApi);
    }
}
