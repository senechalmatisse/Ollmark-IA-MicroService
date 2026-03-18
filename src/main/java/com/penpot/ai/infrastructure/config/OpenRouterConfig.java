package com.penpot.ai.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class OpenRouterConfig {

    /**
     * Builder principal utilisé par tout le système (RAG, Adapter, etc.)
     */
    @Bean("executorChatClientBuilder")
    @Primary
    public ChatClient.Builder executorChatClientBuilder(
        ChatModel chatModel,
        MessageChatMemoryAdvisor memoryAdvisor
    ) {
        log.info("[OpenRouterConfig] Creating executorChatClientBuilder");

        return ChatClient.builder(chatModel)
            .defaultAdvisors(memoryAdvisor);
    }

    /**
     * ChatClient principal (optionnel mais recommandé)
     */
    @Bean("executorChatClient")
    public ChatClient executorChatClient(
        @Qualifier("executorChatClientBuilder") ChatClient.Builder builder
    ) {
        log.info("[OpenRouterConfig] Building executorChatClient");
        return builder.build();
    }
}