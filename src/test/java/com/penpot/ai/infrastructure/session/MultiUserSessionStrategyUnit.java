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
@DisplayName("MultiUserSessionStrategy — Unit")
class MultiUserSessionStrategyUnit {

    private final MultiUserSessionStrategy strategy = new MultiUserSessionStrategy();

    @Mock
    private WebSocketSession openSession;

    @Mock
    private WebSocketSession closedSession;

    private static final String SESSION_ID_1 = "session-001";
    private static final String SESSION_ID_2 = "session-002";
    private static final String TOKEN_A = "token-user-A";
    private static final String TOKEN_B = "token-user-B";

    @Nested
    @DisplayName("selectSession — no user token in criteria")
    class NoTokenTests {

        @Test
        @DisplayName("selectSession — returns empty when criteria has no userToken")
        void selectSession_returnsEmptyWhenNoTokenInCriteria() {
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
    }

    @Nested
    @DisplayName("selectSession — token matching")
    class TokenMatchingTests {

        @Test
        @DisplayName("selectSession — returns session when token matches")
        void selectSession_returnsSessionWhenTokenMatches() {
            // GIVEN
            when(openSession.isOpen()).thenReturn(true);

            Map<String, WebSocketSession> sessions = new LinkedHashMap<>();
            sessions.put(SESSION_ID_1, openSession);
            Map<String, String> tokens = Map.of(SESSION_ID_1, TOKEN_A);
            SessionCriteria criteria = SessionCriteria.forUser(TOKEN_A);

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isPresent().contains(openSession);
        }

        @Test
        @DisplayName("selectSession — returns empty when token does not match any session")
        void selectSession_returnsEmptyWhenTokenDoesNotMatch() {
            // GIVEN
            Map<String, WebSocketSession> sessions = new LinkedHashMap<>();
            sessions.put(SESSION_ID_1, openSession);
            Map<String, String> tokens = Map.of(SESSION_ID_1, TOKEN_A);
            SessionCriteria criteria = SessionCriteria.forUser(TOKEN_B);

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("selectSession — returns empty when session has no token mapping")
        void selectSession_returnsEmptyWhenSessionHasNoToken() {
            // GIVEN
            Map<String, WebSocketSession> sessions = new LinkedHashMap<>();
            sessions.put(SESSION_ID_1, openSession);
            Map<String, String> tokens = Map.of();
            SessionCriteria criteria = SessionCriteria.forUser(TOKEN_A);

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("selectSession — requireActive filtering with token")
    class RequireActiveWithTokenTests {

        @Test
        @DisplayName("selectSession — returns empty when token matches but session is closed and requireActive is true")
        void selectSession_returnsEmptyWhenTokenMatchesButSessionClosed() {
            // GIVEN
            when(closedSession.isOpen()).thenReturn(false);

            Map<String, WebSocketSession> sessions = new LinkedHashMap<>();
            sessions.put(SESSION_ID_1, closedSession);
            Map<String, String> tokens = Map.of(SESSION_ID_1, TOKEN_A);
            SessionCriteria criteria = SessionCriteria.forUser(TOKEN_A);

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("selectSession — returns closed session when token matches and requireActive is false")
        void selectSession_returnsClosedSessionWhenRequireActiveFalse() {
            // GIVEN
            Map<String, WebSocketSession> sessions = new LinkedHashMap<>();
            sessions.put(SESSION_ID_1, closedSession);
            Map<String, String> tokens = Map.of(SESSION_ID_1, TOKEN_A);
            SessionCriteria criteria = SessionCriteria.builder()
                    .userToken(Optional.of(TOKEN_A))
                    .requireActive(false)
                    .build();

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isPresent().contains(closedSession);
        }
    }

    @Nested
    @DisplayName("selectSession — multiple sessions with different tokens")
    class MultipleSessionsTests {

        @Test
        @DisplayName("selectSession — returns correct session among multiple users")
        void selectSession_returnsCorrectSessionAmongMultipleUsers() {
            // GIVEN
            when(openSession.isOpen()).thenReturn(true);

            Map<String, WebSocketSession> sessions = new LinkedHashMap<>();
            sessions.put(SESSION_ID_1, closedSession);
            sessions.put(SESSION_ID_2, openSession);
            Map<String, String> tokens = Map.of(
                    SESSION_ID_1, TOKEN_A,
                    SESSION_ID_2, TOKEN_B);
            SessionCriteria criteria = SessionCriteria.forUser(TOKEN_B);

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isPresent().contains(openSession);
        }
    }

    @Nested
    @DisplayName("selectSession — empty sessions map")
    class EmptySessionsTests {

        @Test
        @DisplayName("selectSession — returns empty when sessions map is empty even with valid token")
        void selectSession_returnsEmptyWhenNoSessions() {
            // GIVEN
            Map<String, WebSocketSession> sessions = Map.of();
            Map<String, String> tokens = Map.of();
            SessionCriteria criteria = SessionCriteria.forUser(TOKEN_A);

            // WHEN
            Optional<WebSocketSession> result = strategy.selectSession(sessions, tokens, criteria);

            // THEN
            assertThat(result).isEmpty();
        }
    }
}