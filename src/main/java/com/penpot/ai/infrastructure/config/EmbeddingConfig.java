package com.penpot.ai.infrastructure.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmbeddingConfig {

    @Value("${penpot.ai.provider:ollama}")
    private String provider;

    @Bean
    @Primary
    public EmbeddingModel embeddingModel(
        @Qualifier("ollamaEmbeddingModel") EmbeddingModel ollama,
        @Qualifier("openAiEmbeddingModel") EmbeddingModel openai
    ) {
        return "openrouter".equalsIgnoreCase(provider) ? openai : ollama;
    }
}