package com.penpot.ai.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static com.penpot.ai.infrastructure.config.EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE;

import java.util.List;

/**
 * Service d'optimisation dédié à la génération et à la mise en cache absolue des plongements lexicaux (embeddings).
 * <p>
 * Une requête identique produira systématiquement le même vecteur mathématique. Par conséquent, il déploie une stratégie 
 * de cache permanent (sans expiration) qui associe l'empreinte de la requête (hash) à son tableau de flottants résultant.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingCacheService {

    private final EmbeddingModel embeddingModel;

    /**
     * Génère le vecteur de représentation mathématique (embedding) d'une requête courte en s'appuyant sur une stratégie de cache absolu.
     *
     * @param query La phrase ou la requête textuelle formulée par l'utilisateur nécessitant une vectorisation.
     * @return      Un tableau unidimensionnel de nombres à virgule flottante représentant l'embedding de la requête 
     * (comportant typiquement 1024 dimensions pour un modèle tel que mxbai-embed-large).
     */
    @Cacheable(value = QUERY_EMBEDDINGS_CACHE, key = "#query")
    public float[] embedQuery(String query) {
        log.debug("Computing embedding for query: {} (CACHE MISS)", truncate(query, 50));

        long startTime = System.currentTimeMillis();
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(query));
        float[] embedding = response.getResult().getOutput();
        long duration = System.currentTimeMillis() - startTime;

        log.info("Embedding computed in {}ms (dimensions: {}) - CACHED for query: {}", 
            duration, 
            embedding.length, 
            truncate(query, 50)
        );

        return embedding;
    }

    /**
     * Calcule et met en cache l'empreinte vectorielle d'un document complet, une opération particulièrement onéreuse.
     *
     * @param content L'intégralité du contenu textuel du document ou du gabarit à analyser.
     * @return        Le vecteur d'embedding correspondant au contenu fourni.
     */
    @Cacheable(value = QUERY_EMBEDDINGS_CACHE, key = "#content.hashCode()")
    public float[] embedDocument(String content) {
        log.debug("Computing embedding for document (length: {} chars)", content.length());
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(content));
        float[] embedding = response.getResult().getOutput();
        log.debug("Document embedding computed (dimensions: {})", embedding.length);
        return embedding;
    }

    /**
     * Fonction utilitaire chargée de formater les chaînes de caractères destinées aux logs.
     *
     * @param str       La chaîne de caractères originelle à traiter.
     * @param maxLength La limite maximale de caractères autorisée avant troncature.
     * @return          La chaîne originale si sa taille est conforme, ou une version raccourcie complétée de points de suspension.
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return "null";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }
}