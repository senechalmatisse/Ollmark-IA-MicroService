package com.penpot.ai.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.Map;

/**
 * Service de configuration des prompts système.
 * Charge et gère la configuration des prompts depuis prompts.yml,
 * fournissant un accès type-safe aux instructions système et autres
 * configurations avec gestion des valeurs par défaut.
 */
@Slf4j
@Service
public class PromptsConfigService {

    private static final String INITIAL_INSTRUCTIONS_KEY = "initial_instructions";
    private static final String DEFAULT_INSTRUCTIONS = """
        Tu es un **agent expert Penpot orienté création de contenu marketing graphique automatisé**.
        Tes responsabilités sont :
        1. Orchestrer les appels aux tools Penpot
        2. Exploiter les résultats fournis par le RAG (templates, intentions, patterns)
        3. Traduire une intention marketing en design graphique structuré
        4. Respecter strictement les contraintes de design existantes
    """;

    private Map<String, Object> promptsConfig;

    @PostConstruct
    public void init() {
        loadPromptsConfig();
    }

    private void loadPromptsConfig() {
        try {
            ClassPathResource resource = new ClassPathResource("data/prompts.yml");
            if (!resource.exists()) {
                log.warn("prompts.yml not found, using default configuration");
                promptsConfig = Map.of(INITIAL_INSTRUCTIONS_KEY, DEFAULT_INSTRUCTIONS);
                return;
            }

            Yaml yaml = new Yaml();
            try (InputStream inputStream = resource.getInputStream()) {
                Map<String, Object> config = yaml.load(inputStream);
                if (config == null) {
                    log.warn("prompts.yml is empty, using default configuration");
                    promptsConfig = Map.of(INITIAL_INSTRUCTIONS_KEY, DEFAULT_INSTRUCTIONS);
                    return;
                }
                promptsConfig = config;
                log.info("Loaded prompts configuration from prompts.yml");
            }
        } catch (IOException e) {
            log.error("Failed to load prompts.yml, using default configuration", e);
            promptsConfig = Map.of(INITIAL_INSTRUCTIONS_KEY, DEFAULT_INSTRUCTIONS);
        }
    }

    /**
     * Obtient les instructions initiales pour le système AI.
     * 
     * @return les instructions système
     */
    public String getInitialInstructions() {
        return (String) promptsConfig.getOrDefault(
            INITIAL_INSTRUCTIONS_KEY, 
            DEFAULT_INSTRUCTIONS
        );
    }
}