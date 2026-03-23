package com.penpot.ai.infrastructure.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.List;
import java.util.Map;

/**
 * Décorateur de {@link ToolCallingManager} garantissant que le contexte de session
 * (sessionId et userToken) est correctement propagé dans le ThreadLocal du thread
 * d'exécution des outils, même si ce thread diffère du thread HTTP initial.
 *
 * <h2>Problème résolu</h2>
 * <p>
 * Spring AI 1.1.x peut exécuter les outils {@code @Tool} sur un thread différent
 * (pool interne, threads virtuels Java 21…). Le {@link SessionContextHolder} utilisant
 * un {@link ThreadLocal}, les valeurs définies sur le thread HTTP disparaissent
 * lors du changement de thread, causant une sélection de session erronée dans
 * {@link com.penpot.ai.adapters.out.plugin.PluginBridgeAdapter}.
 * </p>
 *
 * <h2>Solution</h2>
 * <p>
 * Le {@code sessionId} et le {@code userToken} sont déjà présents dans le
 * {@code toolContext} du {@link Prompt} (injecté par {@code OllamaAiAdapter}).
 * Ce contexte voyage avec le Prompt indépendamment des changements de thread.
 * Avant chaque exécution d'outil, ce décorateur relit ces valeurs depuis le
 * toolContext et les restitue dans le ThreadLocal du bon thread.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class SessionAwareToolCallingManager implements ToolCallingManager {

    private final ToolCallingManager delegate;

    @Override
    public List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions options) {
        return delegate.resolveToolDefinitions(options);
    }

    /**
     * Exécute les appels d'outils en restaurant d'abord le contexte de session
     * dans le ThreadLocal du thread courant, puis délègue au manager sous-jacent.
     */
    @Override
    public ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse) {
        Map<String, Object> toolContext = extractToolContext(prompt);

        String sessionId = (String) toolContext.getOrDefault("sessionId", "");
        String userToken = (String) toolContext.getOrDefault("userToken", "");

        if (sessionId != null && !sessionId.isBlank()) {
            SessionContextHolder.setSessionId(sessionId);
            log.debug("Restored sessionId={} in ThreadLocal on tool thread {}",
                sessionId, Thread.currentThread().getId());
        }
        if (userToken != null && !userToken.isBlank()) {
            SessionContextHolder.setUserToken(userToken);
            log.debug("Restored userToken in ThreadLocal on tool thread {}",
                Thread.currentThread().getId());
        }

        try {
            return delegate.executeToolCalls(prompt, chatResponse);
        } finally {
            SessionContextHolder.clearAll();
        }
    }

    /**
     * Extrait le toolContext depuis les options du Prompt.
     * Retourne une Map vide si les options ne sont pas de type {@link ToolCallingChatOptions}.
     */
    private Map<String, Object> extractToolContext(Prompt prompt) {
        if (prompt.getOptions() instanceof ToolCallingChatOptions opts) {
            Map<String, Object> ctx = opts.getToolContext();
            if (ctx != null) {
                return ctx;
            }
        }
        return Map.of();
    }
}
