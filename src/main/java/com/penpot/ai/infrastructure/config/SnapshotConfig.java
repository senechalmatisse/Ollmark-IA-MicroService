
/**
 * Configuration class for automatic ThreadLocal propagation in reactive streams.
 * 
 * <p>This configuration enables the automatic propagation of the {@code conversationId} ThreadLocal
 * from the HTTP thread to Reactor's {@code boundedElastic} threads, ensuring that conversation
 * context is maintained across asynchronous operations.
 * 
 * <h2>Propagation Mechanism</h2>
 * <ol>
 *   <li>{@link Hooks#enableAutomaticContextPropagation()} — Activates automatic context propagation</li>
 *   <li>{@link ContextRegistry} registers accessor methods for the {@code conversationId} ThreadLocal</li>
 *   <li>{@code contextWrite()} in the reactive flux stores the value in the Reactor Context</li>
 *   <li>Reactor automatically restores the ThreadLocal before each operator execution</li>
 * </ol>
 * 
 * <h2>Usage</h2>
 * <p>This configuration is automatically instantiated by Spring during application startup.
 * The {@link #enableContextPropagation()} method is invoked via {@code @PostConstruct}
 * to configure the Reactor context propagation at initialization time.
 * 
 * <h2>Related Classes</h2>
 * <ul>
 *   <li>{@link SnapshotAspect} — Manages conversationId ThreadLocal storage and retrieval</li>
 * </ul>
 * 
 * @see SnapshotAspect
 * @see io.micrometer.context.ContextRegistry
 * @see reactor.core.publisher.Hooks
 */
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

