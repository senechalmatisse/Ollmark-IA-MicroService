package com.penpot.ai.application.usecases;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import com.penpot.ai.application.service.MessagePersistenceService;
import com.penpot.ai.core.ports.in.ConversationChatUseCase;
import com.penpot.ai.core.ports.out.AiServicePort;
import com.penpot.ai.shared.exception.ToolExecutionException;
import com.penpot.ai.shared.util.ValidationUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Implémentation du use case de chat conversationnel.
 * 
 * <h2>Responsabilité unique</h2>
 * Ce use case orchestre les conversations entre l'utilisateur et l'assistant IA
 * pour la génération de contenu marketing graphique sur Penpot.
 * 
 * <h2>Architecture</h2>
 * <ul>
 *     <li>Délègue toute l'intelligence artificielle à {@link AiServicePort}</li>
 *     <li>Ne contient aucune logique IA directement</li>
 *     <li>Gère uniquement la validation et la génération d'IDs de conversation</li>
 * </ul>
 * 
 * <h2>Gestion de la mémoire</h2>
 * La mémoire conversationnelle (ChatMemory) est entièrement gérée par
 * {@link com.penpot.ai.adapters.out.ai.OllamaAiAdapter} :
 * <ul>
 *     <li>Chargement automatique de l'historique</li>
 *     <li>Sauvegarde automatique des messages</li>
 *     <li>Gestion de la fenêtre de messages</li>
 * </ul>
 * 
 * <h2>Workflow utilisateur</h2>
 * <pre>
 * 1. Utilisateur : "Crée un post Instagram pour ma boulangerie"
 *    ↓
 * 2. Use Case : Valide et transmet à AiServicePort
 *    ↓
 * 3. OllamaAiAdapter : 
 *    - Charge l'historique de la conversation
 *    - Invoque les tools RAG (searchTemplates)
 *    - Génère la réponse avec le template approprié
 *    - Sauvegarde dans l'historique
 *    ↓
 * 4. Retour utilisateur : "J'ai trouvé 3 templates..."
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationChatUseCaseImpl implements ConversationChatUseCase {

    /** Port vers le service d'IA. */
    private final AiServicePort aiService;

    private final ChatMemory chatMemory;

    private final MessagePersistenceService messagePersistenceService;

    @Override
    public Mono<String> chat(String projectId, String message, String sessionId) {
        validateChatInput(projectId, message, sessionId);
        String rawConversationKey = (sessionId != null && !sessionId.isBlank())
            ? projectId + ":" + sessionId
            : projectId;

        //  transformation en UUID compatible DB
        String conversationKey = UUID.nameUUIDFromBytes(
            rawConversationKey.getBytes(StandardCharsets.UTF_8)
        ).toString();

        log.info(
            "Processing chat for project: {} (conversationKey: {}, sessionId: {})", 
            projectId, conversationKey, sessionId
        );

        return aiService.chat(conversationKey, message, sessionId)
            .collectList()
            .map(list -> String.join("", list))
            .doOnSuccess(aiResponse -> {
                log.info("[Persistence] Triggering persist for project={} sessionId={} responseLength={}",
                    projectId, sessionId, aiResponse != null ? aiResponse.length() : 0);
                messagePersistenceService.persist(projectId, sessionId, message, aiResponse);
            });
    }

    @Override
    public String startNewConversation(String projectId) {
        ValidationUtils.requireNonBlank(projectId, "Project ID");
        log.info("Started new conversation for project: {}", projectId);
        return projectId;
    }

    @Override
    public void clearConversation(String projectId) {
        ValidationUtils.requireNonBlank(projectId, "Project ID");
        log.info("Clearing conversation history for project: {}", projectId);

        try {
            aiService.clearConversation(projectId);
            log.info("Conversation for project {} cleared successfully", projectId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid project ID: {}", projectId, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to clear conversation for project: {}", projectId, e);
            throw new ToolExecutionException(
                "Failed to clear conversation for project " + projectId + ": " + e.getMessage(), 
                e
            );
        }
    }

    @Override
    public List<Map<String, Object>> getHistory(String projectId, int limit) {
        ValidationUtils.requireNonBlank(projectId, "Project ID");
        try {
            List<Message> messages = chatMemory.get(projectId);
            if (messages == null || messages.isEmpty()) return List.of();

            return messages.stream()
                .map(m -> {
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("role", m.getMessageType().getValue());
                    msg.put("content", m.getText());
                    return msg;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not retrieve history for project {}: {}", projectId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Valide les entrées du chat.
     * 
     * @param conversationId l'ID de conversation
     * @param message le message de l'utilisateur
     * @param sessionID l'ID de la session
     * @throws ValidationException si les paramètres sont invalides
     */
    private void validateChatInput(String conversationId, String message, String sessionId) {
        ValidationUtils.requireNonBlank(conversationId, "Conversation ID");
        ValidationUtils.validateString(message, "Message", 10000);
        ValidationUtils.requireNonBlank(sessionId, "Session ID");
    }
}