package com.penpot.ai.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

/**
 * Configuration du ChatClient dédié au routing d'intention.
 *
 * <h2>Séparation des responsabilités</h2>
 * {@code OllamaConfig} configure l'exécuteur (qwen3:8b avec mémoire),
 * {@code RouterConfig} configure le classifieur (llama3.1 sans mémoire).
 *
 * <h2>Paramètres intentionnels</h2>
 * <ul>
 *   <li>{@code temperature=0.0} : classification déterministe, pas de créativité</li>
 *   <li>{@code numPredict=256} : une réponse JSON courte suffit, limite les tokens gaspillés</li>
 *   <li>Pas de {@code MessageChatMemoryAdvisor} : le router ne doit pas avoir de mémoire
 *       (chaque requête est indépendante)</li>
 * </ul>
 */
@Slf4j
@Configuration
public class RouterConfig {

    /**
     * Modèle utilisé pour le routing.
     * Configurable via {@code penpot.ai.router.model} dans application.properties.
     * Défaut : {@code llama3.1}.
     */
    @Value("${penpot.ai.router.model:llama3.1}")
    private String routerModel;

    @Value("${penpot.ai.router.temperature:0.0}")
    private Double routerTemperature;

    @Value("${penpot.ai.router.max-tokens:256}")
    private Integer routerMaxTokens;

    /**
     * Crée une instance dédiée de {@link OllamaChatModel} pour llama3.1.
     *
     * @param ollamaApi l'API Ollama partagée, injectée automatiquement par Spring AI
     * @return un ChatClient léger dédié à la classification
     */
    @Bean("routerChatClient")
    public ChatClient routerChatClient(OllamaApi ollamaApi) {
        log.info("[RouterConfig] Configuring router ChatClient with model: {}", routerModel);

        OllamaChatOptions routerOptions = OllamaChatOptions.builder()
            .model(routerModel)
            .temperature(routerTemperature)
            .numPredict(routerMaxTokens)
            .build();

        OllamaChatModel chatModel = OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(routerOptions)
            .build();

        log.info("[RouterConfig] Router ChatClient ready (model={}, temperature={}, numPredict={})",
            routerModel, routerTemperature, routerMaxTokens);

        return ChatClient.builder(chatModel).build();
    }
}