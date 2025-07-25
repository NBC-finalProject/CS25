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
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAiKey;

    @Value("${spring.ai.anthropic.api-key}")
    private String claudeKey;

    @Bean(name = "openAiChatModelClient")
    @Primary
    public ChatClient openAiChatClient() {
        OpenAiApi api = OpenAiApi.builder()
            .apiKey(openAiKey)
            .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
            .openAiApi(api)
            .build();

        return ChatClient.builder(chatModel).build();
    }

    @Bean(name = "anthropicChatClient")
    public ChatClient anthropicChatClient() {
        AnthropicApi api = AnthropicApi.builder()
            .apiKey(claudeKey)
            .build();

        AnthropicChatModel chatModel = AnthropicChatModel.builder()
            .anthropicApi(api)
            .build();

        return ChatClient.builder(chatModel).build();
    }

    /**
     * EmbeddingModel for OpenAI
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
            .apiKey(openAiKey)
            .build();

        return new OpenAiEmbeddingModel(openAiApi);
    }
}
