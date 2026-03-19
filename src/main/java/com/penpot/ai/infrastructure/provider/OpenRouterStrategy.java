package com.penpot.ai.infrastructure.provider;

import com.penpot.ai.core.domain.TaskComplexity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Stratégie OpenRouter (cloud).
 * Activée quand : penpot.ai.provider=openrouter
 *
 * Expose aussi openRouterChatModel comme @Primary ChatModel
 * pour que RagConfig puisse l'injecter sans qualifier.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "penpot.ai.provider", havingValue = "openrouter")
public class OpenRouterStrategy implements AiProviderStrategy {

    private final OpenAiChatModel chatModel;
    private final MessageChatMemoryAdvisor memoryAdvisor;
    private final Map<TaskComplexity, OpenAiChatOptions> optionsMap;

    public OpenRouterStrategy(
        MessageChatMemoryAdvisor memoryAdvisor,
        @Value("${spring.ai.openai.base-url:https://openrouter.ai/api/v1}") String baseUrl,
        @Value("${spring.ai.openai.api-key}") String apiKey,
        @Value("${penpot.ai.executor.model:qwen/qwen3.5-35b-a3b}") String model,
        @Value("${penpot.ai.executor.temperature:0.7}") Double temperature,
        @Value("${penpot.ai.executor.max-tokens:4096}") Integer maxTokens
    ) {
        this.memoryAdvisor = memoryAdvisor;
        this.chatModel = OpenAiChatModel.builder()
            .openAiApi(OpenAiApi.builder().baseUrl(baseUrl).apiKey(apiKey).build())
            .defaultOptions(OpenAiChatOptions.builder().model(model).build())
            .build();

        this.optionsMap = Map.of(
            TaskComplexity.SIMPLE, OpenAiChatOptions.builder()
                .model(model).temperature(0.1).maxTokens(512).build(),
            TaskComplexity.CREATIVE, OpenAiChatOptions.builder()
                .model(model).temperature(0.8).topP(0.9).maxTokens(maxTokens).build(),
            TaskComplexity.COMPLEX, OpenAiChatOptions.builder()
                .model(model).temperature(temperature).maxTokens(maxTokens).build()
        );
        log.info("[OpenRouterStrategy] Initialized with model={}, baseUrl={}", model, baseUrl);
    }

    @Override
    public String providerId() {
        return "openrouter";
    }

    @Override
    public ChatClient buildForComplexity(TaskComplexity complexity) {
        OpenAiChatOptions opts = optionsMap.getOrDefault(
            complexity, optionsMap.get(TaskComplexity.SIMPLE));
        return ChatClient.builder(chatModel)
            .defaultOptions(opts)
            .defaultAdvisors(memoryAdvisor)
            .build();
    }

    /**
     * Expose le ChatModel comme bean @Primary pour l'injection dans RagConfig.
     * Nécessaire car RagConfig injecte ChatModel sans qualifier.
     */
    @Bean
    @Primary
    public ChatModel openRouterPrimaryChatModel() {
        return chatModel;
    }
}