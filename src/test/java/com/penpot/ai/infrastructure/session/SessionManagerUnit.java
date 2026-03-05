package com.penpot.ai.infrastructure.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.penpot.ai.core.domain.SessionCriteria;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionManager — Unit")
class SessionManagerUnit {

    @Mock
    private SessionSelectionStrategy selectionStrategy;

    @Mock
    private WebSocketSession session1;

    @Mock
    private WebSocketSession session2;

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager(selectionStrategy);
    }

    @Nested
    @DisplayName("registerSession")
    class RegisterSessionTests {

        @Test
        @DisplayName("registerSession — increments active session count")
        void registerSession_incrementsActiveSessionCount() {
            // GIVEN
            when(session1.getId()).thenReturn("s1");

            // WHEN
            sessionManager.registerSession(session1, null);

            // THEN
            assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);
            assertThat(sessionManager.hasActiveSessions()).isTrue();
        }

        @Test
        @DisplayName("registerSession — stores user token when provided")
        void registerSession_storesUserTokenWhenProvided() {
            // GIVEN
            when(session1.getId()).thenReturn("s1");

            // WHEN
            sessionManager.registerSession(session1, "my-user-token");

            // THEN
            assertThat(sessionManager.getUserToken("s1")).isEqualTo("my-user-token");
        }

        @Test
        @DisplayName("registerSession — does not store token when null")
        void registerSession_doesNotStoreTokenWhenNull() {
            // GIVEN
            when(session1.getId()).thenReturn("s1");

            // WHEN
            sessionManager.registerSession(session1, null);

            // THEN
            assertThat(sessionManager.getUserToken("s1")).isNull();
        }

        @Test
        @DisplayName("registerSession — does not store token when blank")
        void registerSession_doesNotStoreTokenWhenBlank() {
            // GIVEN
            when(session1.getId()).thenReturn("s1");

            // WHEN
            sessionManager.registerSession(session1, "   ");

            // THEN
            assertThat(sessionManager.getUserToken("s1")).isNull();
        }

        @Test
        @DisplayName("registerSession — allows registering multiple sessions")
        void registerSession_allowsMultipleSessions() {
            // GIVEN
            when(session1.getId()).thenReturn("s1");
            when(session2.getId()).thenReturn("s2");

            // WHEN
            sessionManager.registerSession(session1, "token-A");
            sessionManager.registerSession(session2, "token-B");

            // THEN
            assertThat(sessionManager.getActiveSessionCount()).isEqualTo(2);
            assertThat(sessionManager.getUserToken("s1")).isEqualTo("token-A");
            assertThat(sessionManager.getUserToken("s2")).isEqualTo("token-B");
        }
    }

    @Nested
    @DisplayName("unregisterSession")
    class UnregisterSessionTests {

        @Test
        @DisplayName("unregisterSession — decrements active session count")
        void unregisterSession_decrementsActiveSessionCount() {
            // GIVEN
            when(session1.getId()).thenReturn("s1");
            sessionManager.registerSession(session1, "token");

            // WHEN
            sessionManager.unregisterSession(session1);

            // THEN
            assertThat(sessionManager.getActiveSessionCount()).isEqualTo(0);
            assertThat(sessionManager.hasActiveSessions()).isFalse();
        }

        @Test
        @DisplayName("unregisterSession — removes user token")
        void unregisterSession_removesUserToken() {
            // GIVEN
            when(session1.getId()).thenReturn("s1");
            sessionManager.registerSession(session1, "token");

            // WHEN
            sessionManager.unregisterSession(session1);

            // THEN
            assertThat(sessionManager.getUserToken("s1")).isNull();
        }

        @Test
        @DisplayName("unregisterSession — does not affect other sessions")
        void unregisterSession_doesNotAffectOtherSessions() {
            // GIVEN
            when(session1.getId()).thenReturn("s1");
            when(session2.getId()).thenReturn("s2");
            sessionManager.registerSession(session1, "token-A");
            sessionManager.registerSession(session2, "token-B");

            // WHEN
            sessionManager.unregisterSession(session1);

            // THEN
            assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);
            assertThat(sessionManager.getUserToken("s2")).isEqualTo("token-B");
        }
    }

    @Nested
    @DisplayName("findSession")
    class FindSessionTests {

        @Test
        @DisplayName("findSession — delegates to selectionStrategy and returns its result")
        void findSession_delegatesToStrategy() {
            // GIVEN
            SessionCriteria criteria = SessionCriteria.any();
            when(selectionStrategy.selectSession(anyMap(), anyMap(), any(SessionCriteria.class)))
                    .thenReturn(Optional.of(session1));

            // WHEN
            Optional<WebSocketSession> result = sessionManager.findSession(criteria);

            // THEN
            assertThat(result).isPresent().contains(session1);
            verify(selectionStrategy, times(1)).selectSession(anyMap(), anyMap(), eq(criteria));
        }

        @Test
        @DisplayName("findSession — returns empty when strategy returns empty")
        void findSession_returnsEmptyWhenStrategyReturnsEmpty() {
            // GIVEN
            SessionCriteria criteria = SessionCriteria.forUser("unknown-token");
            when(selectionStrategy.selectSession(anyMap(), anyMap(), any(SessionCriteria.class)))
                    .thenReturn(Optional.empty());

            // WHEN
            Optional<WebSocketSession> result = sessionManager.findSession(criteria);

            // THEN
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasActiveSessions")
    class HasActiveSessionsTests {

        @Test
        @DisplayName("hasActiveSessions — returns false when no sessions registered")
        void hasActiveSessions_returnsFalseWhenEmpty() {
            // GIVEN / WHEN / THEN
            assertThat(sessionManager.hasActiveSessions()).isFalse();
        }
    }

    @Nested
    @DisplayName("getActiveSessionCount")
    class GetActiveSessionCountTests {

        @Test
        @DisplayName("getActiveSessionCount — returns 0 when no sessions registered")
        void getActiveSessionCount_returnsZeroWhenEmpty() {
            // GIVEN / WHEN / THEN
            assertThat(sessionManager.getActiveSessionCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getUserToken")
    class GetUserTokenTests {

        @Test
        @DisplayName("getUserToken — returns null for unknown session id")
        void getUserToken_returnsNullForUnknownSession() {
            // GIVEN / WHEN / THEN
            assertThat(sessionManager.getUserToken("nonexistent")).isNull();
        }
    }

    @Nested
    @DisplayName("unregisterSession — additional tests")
    class UnregisterSessionAdditionalTests {

        @Test
        @DisplayName("unregisterSession — removes session with user token")
        void unregisterSession_removesSessionWithUserToken() {
            // GIVEN
            when(session1.getId()).thenReturn("s1");
            sessionManager.registerSession(session1, "token-A");

            // WHEN
            sessionManager.unregisterSession(session1);

            // THEN
            assertThat(sessionManager.getActiveSessionCount()).isEqualTo(0);
            assertThat(sessionManager.getUserToken("s1")).isNull();
            verify(selectionStrategy, never()).selectSession(anyMap(), anyMap(), any());
        }

        @Test
        @DisplayName("unregisterSession — removes session without user token")
        void unregisterSession_removesSessionWithoutUserToken() {
            // GIVEN
            when(session1.getId()).thenReturn("s1");
            sessionManager.registerSession(session1, null);

            // WHEN
            sessionManager.unregisterSession(session1);

            // THEN
            assertThat(sessionManager.getActiveSessionCount()).isEqualTo(0);
            assertThat(sessionManager.getUserToken("s1")).isNull();
            verify(selectionStrategy, never()).selectSession(anyMap(), anyMap(), any());
        }

        @Test
        @DisplayName("unregisterSession — does nothing for non-existent session")
        void unregisterSession_doesNothingForNonExistentSession() {
            // GIVEN
            when(session1.getId()).thenReturn("s1");

            // WHEN
            sessionManager.unregisterSession(session1);

            // THEN
            assertThat(sessionManager.getActiveSessionCount()).isEqualTo(0);
            assertThat(sessionManager.getUserToken("s1")).isNull();
            verify(selectionStrategy, never()).selectSession(anyMap(), anyMap(), any());
        }
    }
}