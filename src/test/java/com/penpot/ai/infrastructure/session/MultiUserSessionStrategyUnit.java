package com.penpot.ai.infrastructure.session;

import static org.assertj.core.api.Assertions.assertThat;

import com.penpot.ai.core.domain.SessionCriteria;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MultiUserSessionStrategy — Unit")
class MultiUserSessionStrategyUnit {

    private final MultiUserSessionStrategy strategy = new MultiUserSessionStrategy();

    @Mock
    private WebSocketSession openSession;

    private static final String SESSION_ID_1 = "session-001";
    private static final String TOKEN_A = "token-user-A";

    @Nested
    @DisplayName("selectSession")
    class NoTokenTests {

        @Test
        @DisplayName("always returns empty regardless of sessions")
        void selectSession_alwaysReturnsEmpty() {
            // GIVEN
            Map<String, WebSocketSession> sessions = new LinkedHashMap<>();
            sessions.put(SESSION_ID_1, openSession);
            Map<String, String> tokens = Map.of(SESSION_ID_1, TOKEN_A);
            SessionCriteria criteria = SessionCriteria.any();

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty even if sessions exist")
        void selectSession_returnsEmptyEvenWithSessions() {
            // GIVEN
            Map<String, WebSocketSession> sessions = new HashMap<>();
            sessions.put("s1", null);
            Map<String, String> tokens = Map.of("s1", "token-A");
            SessionCriteria criteria = SessionCriteria.any();

            // WHEN
            Optional<WebSocketSession> result =
                    strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isEmpty();
        }
    }
}