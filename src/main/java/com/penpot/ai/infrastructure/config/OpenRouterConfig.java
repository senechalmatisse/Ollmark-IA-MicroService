package com.penpot.ai.infrastructure.config;

import com.penpot.ai.infrastructure.session.SessionAwareToolCallingManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

@Slf4j
@Configuration
@ConditionalOnProperty(
    name = "penpot.ai.provider",
    havingValue = "openrouter"
)
public class OpenRouterConfig {

    /**
     * ACTIVE LE TOOL CALLING
     */
    @Bean
    public ToolCallAdvisor toolCallAdvisor(ToolCallingManager toolCallingManager) {
        return ToolCallAdvisor.builder()
                .toolCallingManager(new SessionAwareToolCallingManager(toolCallingManager))
                .build();
    }

    /**
     * BUILDER AVEC TOOLS + TOOL CALLING
     */
    @Bean("executorChatClientBuilder")
    @Primary
    public ChatClient.Builder executorChatClientBuilder(
        ChatModel chatModel,
        MessageChatMemoryAdvisor memoryAdvisor
    ) {
        log.info("OpenRouter ChatClient.Builder — toolCallAdvisor injecté via UnifiedAiAdapter");
        return ChatClient.builder(chatModel)
            .defaultAdvisors(memoryAdvisor);
    }

    @Bean("executorChatClient")
    public ChatClient executorChatClient(
            @Qualifier("executorChatClientBuilder") ChatClient.Builder builder
    ) {
        return builder.build();
    }
}