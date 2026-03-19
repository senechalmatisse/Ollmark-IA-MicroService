package com.penpot.ai.infrastructure.config;

import com.penpot.ai.application.tools.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
@ConditionalOnProperty(
    name = "penpot.ai.provider",
    havingValue = "openrouter"
)
public class OpenRouterConfig {

    /**
     * Builder principal utilisé par tout le système (RAG, Adapter, etc.)
     * 🔥 AVEC TOOLS PENPOT INJECTÉS
     */
    @Bean("executorChatClientBuilder")
    @Primary
    public ChatClient.Builder executorChatClientBuilder(
            ChatModel chatModel,
            MessageChatMemoryAdvisor memoryAdvisor,
            PenpotShapeTools shapeTools,
            PenpotContentTools contentTools,
            PenpotLayoutTools layoutTools,
            PenpotTransformTools transformTools,
            PenpotInspectorTools inspectorTools,
            PenpotDeleteTools deleteTools
    ) {
        log.info("[OpenRouterConfig] Creating executorChatClientBuilder WITH TOOLS");

        return ChatClient.builder(chatModel)
                .defaultAdvisors(memoryAdvisor)
                .defaultTools(   // 🔥 MAGIC ICI
                        shapeTools,
                        contentTools,
                        layoutTools,
                        transformTools,
                        inspectorTools,
                        deleteTools
                );
    }

    /**
     * ChatClient principal
     */
    @Bean("executorChatClient")
    public ChatClient executorChatClient(
            @Qualifier("executorChatClientBuilder") ChatClient.Builder builder
    ) {
        log.info("[OpenRouterConfig] Building executorChatClient");
        return builder.build();
    }
}