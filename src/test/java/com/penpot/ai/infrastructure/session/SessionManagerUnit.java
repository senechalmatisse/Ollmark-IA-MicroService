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
            sessionManager.registerSession(session1);

            // THEN
            assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);
            assertThat(sessionManager.hasActiveSessions()).isTrue();
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
            sessionManager.registerSession(session1);

            // WHEN
            sessionManager.unregisterSession(session1);

            // THEN
            assertThat(sessionManager.getActiveSessionCount()).isEqualTo(0);
            assertThat(sessionManager.hasActiveSessions()).isFalse();
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
}