package com.example.cs25service.domain.ai.config;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.api.AnthropicApi;
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
    public ChatClient AichatClient(OpenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    public OpenAiChatModel openAiChatModel() {
        OpenAiApi api = OpenAiApi.builder()
            .apiKey(openAiKey)
            .build();

        return OpenAiChatModel.builder()
            .openAiApi(api)
            .build();
    }

    @Bean
    public AnthropicChatModel anthropicChatModel(@Value("${spring.ai.anthropic.api-key}") String claudeKey) {
        AnthropicApi api = AnthropicApi.builder()
            .apiKey(claudeKey)
            .build();

        return AnthropicChatModel.builder()
            .anthropicApi(api)
            .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
            .apiKey(openAiKey)
            .build();
        return new OpenAiEmbeddingModel(openAiApi);
    }
}