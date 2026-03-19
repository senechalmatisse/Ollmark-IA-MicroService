package com.penpot.ai.infrastructure.provider;

import com.penpot.ai.core.domain.TaskComplexity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Stratégie Ollama (local).
 * Activée quand : penpot.ai.provider=ollama (défaut)
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "penpot.ai.provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaStrategy implements AiProviderStrategy {

    private final OllamaChatModel chatModel;
    private final MessageChatMemoryAdvisor memoryAdvisor;
    private final Map<TaskComplexity, OllamaChatOptions> optionsMap;

    public OllamaStrategy(
        OllamaChatModel chatModel,
        MessageChatMemoryAdvisor memoryAdvisor,
        @Value("${penpot.ai.executor.model:qwen3:8b}") String model,
        @Value("${penpot.ai.executor.temperature:0.7}") Double temperature,
        @Value("${penpot.ai.executor.max-tokens:4096}") Integer maxTokens
    ) {
        this.chatModel = chatModel;
        this.memoryAdvisor = memoryAdvisor;
        this.optionsMap = Map.of(
            TaskComplexity.SIMPLE, OllamaChatOptions.builder()
                .model(model).temperature(0.1).topK(3).build(),
            TaskComplexity.CREATIVE, OllamaChatOptions.builder()
                .model(model).temperature(0.8).topK(5).topP(0.9).build(),
            TaskComplexity.COMPLEX, OllamaChatOptions.builder()
                .model(model).enableThinking()
                .temperature(temperature).numPredict(maxTokens)
                .topP(0.95).topK(3).repeatPenalty(1.15)
                .presencePenalty(0.3).numCtx(32000).build()
        );
        log.info("[OllamaStrategy] Initialized with model={}", model);
    }

    @Override
    public String providerId() {
        return "ollama";
    }

    @Override
    public ChatClient buildForComplexity(TaskComplexity complexity) {
        OllamaChatOptions opts = optionsMap.getOrDefault(complexity, optionsMap.get(TaskComplexity.SIMPLE));
        return ChatClient.builder(chatModel)
            .defaultOptions(opts)
            .defaultAdvisors(memoryAdvisor)
            .build();
    }
}