package com.penpot.ai.infrastructure.provider;

import org.springframework.ai.chat.client.ChatClient;
import com.penpot.ai.core.domain.TaskComplexity;

/**
 * Strategy Pattern : abstraction d'un fournisseur AI.
 *
 * Pour ajouter un nouveau fournisseur (ex: Anthropic, Mistral) :
 * 1. Implémenter cette interface
 * 2. L'annoter @Component + @ConditionalOnProperty(name="penpot.ai.provider", havingValue="monFournisseur")
 * 3. Ajouter la valeur dans application.yml : penpot.ai.provider: monFournisseur
 * C'est tout.
 */
public interface AiProviderStrategy {

    /**
     * Identifiant du fournisseur (ex: "ollama", "openrouter").
     * Doit correspondre à la valeur de penpot.ai.provider dans application.yml.
     */
    String providerId();

    /**
     * Construit un ChatClient adapté à la complexité de la tâche.
     * Chaque fournisseur gère ses propres options (température, modèle, etc.)
     */
    ChatClient buildForComplexity(TaskComplexity complexity);
}