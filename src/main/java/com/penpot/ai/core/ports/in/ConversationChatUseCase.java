package com.penpot.ai.core.ports.in;

import java.util.*;

import reactor.core.publisher.Mono;

/**
 * Port d'entrée pour le chat avec gestion de conversation.
 */
public interface ConversationChatUseCase {

    /**
     * Engage une conversation avec l'assistant IA.
     *
     * @param projectId ID du projet Penpot
     * @param message   message de l'utilisateur
     * @return la réponse générée par l'IA
     */
    Mono<String> chat(String projectId, String message, String sessionId);

    /**
     * Démarre une nouvelle conversation pour un projet.
     * 
     * @param projectId ID du projet
     * @return ID du projet (confirmation)
     */
    String startNewConversation(String projectId);

    /**
     * Efface l'historique d'une conversation pour un projet.
     * 
     * @param projectId ID du projet
     */
    void clearConversation(String projectId);

    List<Map<String, Object>> getHistory(String projectId, int limit);
}