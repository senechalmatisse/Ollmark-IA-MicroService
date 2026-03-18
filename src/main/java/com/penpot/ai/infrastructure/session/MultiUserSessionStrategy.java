package com.penpot.ai.infrastructure.session;

import com.penpot.ai.core.domain.SessionCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import java.util.*;

/**
 * Stratégie de sélection pour le mode multi-utilisateur.
 * Sélectionne la session correspondant au token utilisateur.
 */
@Slf4j
@Component
@ConditionalOnProperty(
    name = "penpot.multi-user",
    havingValue = "true"
)
public class MultiUserSessionStrategy implements SessionSelectionStrategy {

    @Override
    public Optional<WebSocketSession> selectSession(
        Map<String, WebSocketSession> sessions,
        Map<String, String> sessionTokens,
        SessionCriteria criteria
    ) {
        log.debug("Using multi-user session selection strategy");
        log.warn("Multi-user mode: no sessionId provided, cannot route to specific session");
        return Optional.empty();
    }
}