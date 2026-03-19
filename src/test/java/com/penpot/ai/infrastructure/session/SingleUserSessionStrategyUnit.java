package com.penpot.ai.infrastructure.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.penpot.ai.core.domain.SessionCriteria;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SingleUserSessionStrategy — Unit")
class SingleUserSessionStrategyUnit {

    private final SingleUserSessionStrategy strategy = new SingleUserSessionStrategy();

    @Mock
    private WebSocketSession openSession;

    @Mock
    private WebSocketSession closedSession;

    private static final String SESSION_ID_1 = "session-001";
    private static final String SESSION_ID_2 = "session-002";

    @Nested
    @DisplayName("selectSession — empty sessions map")
    class EmptySessionsTests {

        @Test
        @DisplayName("selectSession — returns empty Optional when sessions map is empty")
        void selectSession_returnsEmptyWhenNoSessions() {
            // GIVEN
            Map<String, WebSocketSession> sessions = Map.of();
            Map<String, String> tokens = Map.of();
            SessionCriteria criteria = SessionCriteria.any();

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("selectSession — requireActive filtering")
    class RequireActiveTests {

        @Test
        @DisplayName("selectSession — returns first open session when requireActive is true")
        void selectSession_returnsFirstOpenSessionWhenRequireActiveTrue() {
            // GIVEN
            when(openSession.isOpen()).thenReturn(true);

            Map<String, WebSocketSession> sessions = new LinkedHashMap<>();
            sessions.put(SESSION_ID_1, openSession);
            Map<String, String> tokens = Map.of();
            SessionCriteria criteria = SessionCriteria.builder()
                    .requireActive(true)
                    .build();

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isPresent().contains(openSession);
        }

        @Test
        @DisplayName("selectSession — returns empty when requireActive is true and all sessions are closed")
        void selectSession_returnsEmptyWhenRequireActiveAndAllClosed() {
            // GIVEN
            when(closedSession.isOpen()).thenReturn(false);

            Map<String, WebSocketSession> sessions = new LinkedHashMap<>();
            sessions.put(SESSION_ID_1, closedSession);
            Map<String, String> tokens = Map.of();
            SessionCriteria criteria = SessionCriteria.builder()
                    .requireActive(true)
                    .build();

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("selectSession — returns closed session when requireActive is false")
        void selectSession_returnsClosedSessionWhenRequireActiveFalse() {
            // GIVEN
            Map<String, WebSocketSession> sessions = new LinkedHashMap<>();
            sessions.put(SESSION_ID_1, closedSession);
            Map<String, String> tokens = Map.of();
            SessionCriteria criteria = SessionCriteria.builder()
                    .requireActive(false)
                    .build();

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isPresent().contains(closedSession);
        }
    }

    @Nested
    @DisplayName("selectSession — multiple sessions")
    class MultipleSessionsTests {

        @Test
        @DisplayName("selectSession — skips closed session and returns next open one when requireActive is true")
        void selectSession_skipsClosedAndReturnsNextOpen() {
            // GIVEN
            when(closedSession.isOpen()).thenReturn(false);
            when(openSession.isOpen()).thenReturn(true);

            Map<String, WebSocketSession> sessions = new LinkedHashMap<>();
            sessions.put(SESSION_ID_1, closedSession);
            sessions.put(SESSION_ID_2, openSession);
            Map<String, String> tokens = Map.of();
            SessionCriteria criteria = SessionCriteria.builder()
                    .requireActive(true)
                    .build();

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isPresent().contains(openSession);
        }
    }
}