package com.penpot.ai.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du ChatClient dédié au routing d'intention.
 *
 * <p>Utilise OpenRouter pour la classification.
 * Séparé de l'exécuteur : pas de mémoire, température 0, tokens limités.</p>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "penpot.ai.provider", havingValue = "openrouter")
public class RouterConfig {

    @Value("${penpot.ai.router.model:qwen/qwen-2.5-7b-instruct}")
    private String routerModel;

    @Value("${spring.ai.openai.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${penpot.ai.router.max-tokens:256}")
    private Integer maxTokens;

    /**
     * ChatClient léger pour la classification d'intention.
     * Pas de mémoire, déterministe, tokens limités.
     */
    @Bean("routerChatClient")
    public ChatClient routerChatClient() {

        log.info("[RouterConfig] Configuring router ChatClient with model: {}", routerModel);

        OpenAiApi openAiApi = OpenAiApi.builder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .build();

        OpenAiChatOptions routerOptions = OpenAiChatOptions.builder()
            .model(routerModel)
            .temperature(0.0)
            .maxTokens(maxTokens)
            .build();

        OpenAiChatModel routerChatModel = OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(routerOptions)
            .build();

        log.info("[RouterConfig] Router ChatClient ready (model={}, temperature=0.0, maxTokens={})",
            routerModel, maxTokens);

        return ChatClient.builder(routerChatModel).build();
    }
}