
package com.penpot.ai.infrastructure.config;
import com.penpot.ai.application.tools.support.SnapshotAspect;
import org.springframework.context.annotation.Configuration;

import com.penpot.ai.application.tools.support.SnapshotAspect;

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Hooks;

/**
 * Configure la propagation automatique du ThreadLocal conversationId
 * depuis le thread HTTP vers les threads Reactor (boundedElastic).
 *
 * Mécanisme :
 *   1. Hooks.enableAutomaticContextPropagation() — active la propagation
 *   2. ContextRegistry enregistre un accesseur pour le ThreadLocal conversationId
 *   3. contextWrite() dans le Flux stocke la valeur dans le Reactor Context
 *   4. Reactor restaure automatiquement le ThreadLocal avant chaque opérateur
 */
@Slf4j
@Configuration
public class SnapshotConfig {

    @PostConstruct
    public void enableContextPropagation() {
        // Active la propagation automatique des ThreadLocal via Reactor Context
        Hooks.enableAutomaticContextPropagation();

        // Enregistre l'accesseur pour le conversationId
        ContextRegistry.getInstance().registerThreadLocalAccessor(
            "snapshotConversationId",
            SnapshotAspect::getConversationId,
            SnapshotAspect::setConversationId,
            SnapshotAspect::clearConversationId
        );

        log.info("[Snapshot] Reactor context propagation enabled for conversationId");
    }
}

