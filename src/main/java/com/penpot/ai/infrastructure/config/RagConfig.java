package com.penpot.ai.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du système RAG modulaire (Retrieval-Augmented Generation).
 *
 * <p>Provider-agnostique : fonctionne avec Ollama et OpenRouter.
 * Le bean EmbeddingModel injecté est celui marqué @Primary par la strategy active.</p>
 *
 * <p>Pour OpenRouter : déclarer openAiEmbeddingModel en @Primary dans OpenRouterStrategy
 * ou via spring.autoconfigure.exclude pour désactiver l'auto-config Ollama.</p>
 */
@Slf4j
@Configuration
public class RagConfig {

    @Value("${penpot.ai.rag.similarity-threshold:0.5}")
    private double similarityThreshold;

    @Value("${penpot.ai.rag.top-k:3}")
    private int topK;

    @Value("${penpot.ai.rag.query-variants:2}")
    private int queryVariants;

    @Value("${penpot.ai.embedding.provider:openai}")
    private String embeddingProvider;

    /**
     * VectorStore en mémoire.
     * Le bean EmbeddingModel @Primary est résolu automatiquement selon le provider actif.
     */
    @Bean
    public VectorStore vectorStore(
        @Qualifier("ollamaEmbeddingModel") EmbeddingModel ollama,
        @Qualifier("openAiEmbeddingModel") EmbeddingModel openai
    ) {
        EmbeddingModel selected = embeddingProvider.equals("ollama") ? ollama : openai;

        log.info("Using EmbeddingModel provider: {}", embeddingProvider);

        return SimpleVectorStore.builder(selected).build();
    }

    /**
     * Pipeline RAG complet, provider-agnostique.
     * Utilise ChatModel directement pour éviter la dépendance à OllamaChatOptions.
     */
    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(
        @Qualifier("executorChatClientBuilder") ChatClient.Builder chatClientBuilder,
        VectorStore vectorStore,
        ChatModel chatModel
    ) {
        log.info("Configuring RetrievalAugmentationAdvisor:");
        log.info("  - similarityThreshold : {}", similarityThreshold);
        log.info("  - topK                : {}", topK);
        log.info("  - queryVariants       : {}", queryVariants);

        // Builder dédié aux transformations RAG (température 0, sans advisors)
        ChatClient.Builder transformerBuilder = ChatClient.builder(chatModel);

        RewriteQueryTransformer rewriteTransformer = RewriteQueryTransformer.builder()
            .chatClientBuilder(transformerBuilder)
            .build();

        MultiQueryExpander multiQueryExpander = MultiQueryExpander.builder()
            .chatClientBuilder(transformerBuilder)
            .numberOfQueries(queryVariants)
            .includeOriginal(true)
            .build();

        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(similarityThreshold)
            .topK(topK)
            .build();

        ContextualQueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
            .allowEmptyContext(true)
            .build();

        return RetrievalAugmentationAdvisor.builder()
            .queryTransformers(rewriteTransformer)
            .queryExpander(multiQueryExpander)
            .documentRetriever(documentRetriever)
            .queryAugmenter(queryAugmenter)
            .build();
    }
}