package com.penpot.ai.adapters.in.web;

import com.penpot.ai.core.ports.in.*;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * Point d’entrée REST principal du module.
 * <p>
 * Ce contrôleur expose les endpoints HTTP permettant :
 * <ul>
 *     <li>d’exécuter du code JavaScript dans le contexte du plugin Penpot</li>
 *     <li>d’interagir avec un assistant IA conversationnel</li>
 *     <li>de gérer des conversations persistées via ChatMemory</li>
 * </ul>
 *
 * <h2>Architecture</h2>
 * <p>
 * Le contrôleur agit uniquement comme adaptateur HTTP (layer "in"),
 * déléguant toute la logique métier aux {@code UseCase} du cœur applicatif.
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {
    
    private static final String SUCCESS_KEY = "success";
    private static final String ERROR_KEY = "error";
    private static final String CONVERSATION_ID_KEY = "conversationId";
    private static final String ANONYMOUS_USER = "anonymous";

    /** Use case gérant les conversations IA et la mémoire de chat. */
    private final ConversationChatUseCase conversationChatUseCase;

    /**
     * Envoie un message à l’assistant IA dans le cadre d’une conversation existante.
     * <p>
     * La mémoire conversationnelle est entièrement gérée par ChatMemory :
     * <ul>
     *     <li>chargement automatique de l’historique</li>
     *     <li>persistance des messages</li>
     *     <li>gestion de la fenêtre de contexte</li>
     * </ul>
     *
     * <h3>Outils IA</h3>
     * <p>
     * L’IA peut invoquer automatiquement des outils de recherche de templates
     * (function calling) lorsqu’une intention marketing est détectée.
     *
     * @param request requête contenant l’identifiant de conversation et le message utilisateur
     * @return réponse HTTP contenant la réponse générée par l’IA
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest request) {
        log.info("POST /ai/chat (conversationId: {}, message length: {} chars)",
            request.getConversationId(),
            request.getMessage() != null ? request.getMessage().length() : 0);

        return conversationChatUseCase
            .chat(
                request.getConversationId(),
                request.getMessage(),
                request.getUserToken()
            )
            .onErrorResume(e -> {
                log.error("Stream failed for conversation: {}",
                    request.getConversationId(), e);
                return Flux.just("[ERROR] " + e.getMessage());
            });
    }

    /**
     * Démarre une nouvelle conversation IA.
     * <p>
     * Une conversation peut être associée à un utilisateur identifié
     * ou rester anonyme.
     *
     * @param request requête optionnelle contenant l’identifiant utilisateur
     * @return un nouvel identifiant de conversation
     */
    @PostMapping("/chat/new")
    public ResponseEntity<Map<String, Object>> startNewConversation(
        @RequestBody(required = false) NewConversationRequest request
    ) {
        try {
            String userId = request != null ? request.getUserId() : null;
            log.info("POST /ai/chat/new (userId: {})", userId != null ? userId : ANONYMOUS_USER);

            String conversationId = conversationChatUseCase.startNewConversation(userId);
            return ResponseEntity.ok(Map.of(
                SUCCESS_KEY, true,
                CONVERSATION_ID_KEY, conversationId,
                "userId", userId != null ? userId : ANONYMOUS_USER,
                "info", "New conversation started. Use this conversationId for subsequent messages."
            ));
        } catch (Exception e) {
            log.error("Failed to start new conversation", e);
            return ResponseEntity.status(500).body(buildErrorResponse(e.getMessage()));
        }
    }

    /**
     * Supprime l’intégralité de l’historique d’une conversation.
     *
     * @param conversationId identifiant de la conversation à supprimer
     * @return confirmation de la suppression
     */
    @DeleteMapping("/chat/{conversationId}")
    public ResponseEntity<Map<String, Object>> clearConversation(
        @PathVariable String conversationId
    ) {
        try {
            log.info("DELETE /ai/chat/{}", conversationId);
            conversationChatUseCase.clearConversation(conversationId);
            return ResponseEntity.ok(Map.of(
                SUCCESS_KEY, true,
                CONVERSATION_ID_KEY, conversationId,
                "info", "Conversation history cleared successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid conversation ID: {}", e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to clear conversation", e);
            return ResponseEntity.status(500).body(buildErrorResponse(e.getMessage()));
        }
    }

    /**
     * Construit une réponse d’erreur standardisée.
     *
     * @param errorMessage message d’erreur
     * @return map d’erreur sérialisable en JSON
     */
    private Map<String, Object> buildErrorResponse(String errorMessage) {
        return Map.of(
            SUCCESS_KEY, false,
            ERROR_KEY, errorMessage != null ? errorMessage : "Unknown error"
        );
    }
}

/**
 * DTO représentant une requête de chat conversationnel.
 */
@Data
class ChatRequest {

    /**
     * Identifiant unique de la conversation.
     * Permet de récupérer et persister l’historique.
     */
    private String conversationId;

    /** Message envoyé par l’utilisateur. */
    private String message;

    private String userToken;
}

/**
 * DTO utilisé pour démarrer une nouvelle conversation.
 */
@Data
class NewConversationRequest {

    /**
     * Identifiant utilisateur optionnel.
     * Peut être {@code null} pour une conversation anonyme.
     */
    private String userId;
}