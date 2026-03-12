package com.penpot.ai.adapters.in.web;

import com.penpot.ai.core.ports.in.*;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

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
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    
    /** Use case gérant les conversations IA et la mémoire de chat. */
    private final ConversationChatUseCase conversationChatUseCase;

    /** Use case gérant la configuration de l'IA. */
    private final AiConfigUseCase aiConfigUseCase;

    @PostMapping("/chat")
    public reactor.core.publisher.Mono<ResponseEntity<Map<String, Object>>> chat(@RequestBody(required = false) ChatRequest request) {
        if (request == null) {
            return reactor.core.publisher.Mono.just(ResponseEntity.badRequest().body(buildErrorResponse("Request body is missing")));
        }
        log.info("POST /api/ai/chat (projectId: {}, message length: {} chars)",
            request.getProjectId(),
            request.getMessage() != null ? request.getMessage().length() : 0);

        return conversationChatUseCase
            .chat(
                request.getProjectId(),
                request.getMessage()
            )
            .map(response -> ResponseEntity.ok(buildResponse(true, request.getProjectId(), response)))
            .onErrorResume(e -> {
                log.error("Chat failed for project: {}", request.getProjectId(), e);
                return reactor.core.publisher.Mono.just(
                    ResponseEntity.status(500).body(buildErrorResponse(e.getMessage()))
                );
            });
    }

    /**
     * Démarre une nouvelle conversation IA pour un projet.
     */
    @PostMapping("/chat/new")
    public ResponseEntity<Map<String, Object>> startNewConversation(
        @RequestBody(required = false) NewConversationRequest request
    ) {
        if (request == null) {
            return ResponseEntity.badRequest().body(buildErrorResponse("Request body is missing"));
        }
        try {
            log.info("POST /api/ai/chat/new (projectId: {})", request.getProjectId());
            String projectId = conversationChatUseCase.startNewConversation(request.getProjectId());
            return ResponseEntity.ok(buildResponse(true, projectId, null));
        } catch (Exception e) {
            log.error("Failed to start new conversation", e);
            return ResponseEntity.status(500).body(buildErrorResponse(e.getMessage()));
        }
    }

    /**
     * Supprime l’historique d’une conversation pour un projet.
     */
    @DeleteMapping("/chat/{projectId}")
    public ResponseEntity<Map<String, Object>> clearConversation(
        @PathVariable String projectId
    ) {
        try {
            log.info("DELETE /api/ai/chat/{} (projectId: {})", projectId, projectId);
            conversationChatUseCase.clearConversation(projectId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "projectId", projectId
            ));
        } catch (Exception e) {
            log.error("Failed to clear conversation", e);
            return ResponseEntity.status(500).body(buildErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig(@RequestParam String projectId) {
        return ResponseEntity.ok(aiConfigUseCase.getConfig(projectId));
    }

    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> updateConfig(
        @RequestParam String projectId,
        @RequestBody Map<String, Object> config
    ) {
        aiConfigUseCase.updateConfig(projectId, config);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/config/prompt")
    public ResponseEntity<Map<String, Object>> getPrompt(@RequestParam String projectId) {
        return ResponseEntity.ok(Map.of("prompt", aiConfigUseCase.getPrompt(projectId)));
    }

    @PostMapping("/config/prompt")
    public ResponseEntity<Map<String, Object>> updatePrompt(
        @RequestParam String projectId,
        @RequestBody Map<String, String> body
    ) {
        String prompt = body.get("prompt");
        aiConfigUseCase.updatePrompt(projectId, prompt);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "prompt", prompt
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Unhandled exception in AiController: {}", e.getMessage(), e);
        return ResponseEntity.status(500).body(buildErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonError(org.springframework.http.converter.HttpMessageNotReadableException e) {
        log.error("JSON parsing error: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(buildErrorResponse("Invalid JSON or encoding: " + e.getMessage()));
    }

    private Map<String, Object> buildResponse(boolean success, String projectId, String response) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", success);
        res.put("projectId", projectId);
        res.put("response", response);
        return res;
    }

    private Map<String, Object> buildErrorResponse(String errorMessage) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("error", errorMessage != null ? errorMessage : "Unknown error");
        return res;
    }
}

@Data
class ChatRequest {
    private String projectId;
    private String message;
}

@Data
class NewConversationRequest {
    private String projectId;
}