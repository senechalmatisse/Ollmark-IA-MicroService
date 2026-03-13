package com.penpot.ai.adapters.in.websocket;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.application.service.TaskOrchestrator;
import com.penpot.ai.infrastructure.session.SessionManager;
import com.penpot.ai.model.PluginTaskResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PluginWebSocketHandler — Unit")
public class PluginWebSocketHandlerUnit {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private TaskOrchestrator taskOrchestrator;

    @Mock
    private WebSocketSession session;

    @InjectMocks
    private PluginWebSocketHandler handler;

    @Nested
    @DisplayName("afterConnectionEstablished — enregistrement session")
    class AfterConnectionEstablishedTests {

        @Test
        @DisplayName("afterConnectionEstablished — registers session with userToken")
        void afterConnectionEstablished_registersSessionWithUserToken() throws Exception {
            // GIVEN
            when(session.getId()).thenReturn("session-1");
            when(session.getUri()).thenReturn(new URI("ws://localhost?userToken=abc123"));
            when(sessionManager.getActiveSessionCount()).thenReturn(1);

            // WHEN
            handler.afterConnectionEstablished(session);

            // THEN
            verify(sessionManager).registerSession(session);
        }

        @Test
        @DisplayName("afterConnectionEstablished — registers session without userToken (null query)")
        void afterConnectionEstablished_registersSessionWithoutUserToken() throws Exception {
            // GIVEN
            when(session.getId()).thenReturn("session-1");
            when(session.getUri()).thenReturn(new URI("ws://localhost"));
            when(sessionManager.getActiveSessionCount()).thenReturn(1);

            // WHEN
            handler.afterConnectionEstablished(session);

            // THEN
            verify(sessionManager).registerSession(session);
        }

        @Test
        @DisplayName("afterConnectionEstablished — registers session with query but no userToken param")
        void afterConnectionEstablished_registersSessionWithQueryButNoUserToken() throws Exception {
            // GIVEN
            when(session.getId()).thenReturn("session-1");
            when(session.getUri()).thenReturn(new URI("ws://localhost?foo=bar&baz=qux"));
            when(sessionManager.getActiveSessionCount()).thenReturn(1);

            // WHEN
            handler.afterConnectionEstablished(session);

            // THEN
            verify(sessionManager).registerSession(session);
        }

        @Test
        @DisplayName("afterConnectionEstablished — registers session with multiple query params")
        void afterConnectionEstablished_registersSessionWithMultipleQueryParams() throws Exception {
            // GIVEN
            when(session.getId()).thenReturn("session-1");
            when(session.getUri()).thenReturn(new URI("ws://localhost?foo=bar&userToken=token42&baz=qux"));
            when(sessionManager.getActiveSessionCount()).thenReturn(1);

            // WHEN
            handler.afterConnectionEstablished(session);

            // THEN
            verify(sessionManager).registerSession(session);
        }

        @Test
        @DisplayName("extractUserToken — returns token when userToken= is not the first param")
        void extractUserToken_returnsTokenWhenNotFirstParam() throws Exception {
            // GIVEN
            when(session.getId()).thenReturn("session-1");
            when(session.getUri()).thenReturn(new URI("ws://localhost?aaa=111&userToken=mytoken"));
            when(sessionManager.getActiveSessionCount()).thenReturn(1);

            // WHEN
            handler.afterConnectionEstablished(session);

            // THEN
            verify(sessionManager).registerSession(session);
        }

        @Test
        @DisplayName("extractUserToken — query contains userToken= but no param starts with userToken=")
        void extractUserToken_queryContainsUserTokenButNoParamStartsWithUserToken() throws Exception {
            // GIVEN
            when(session.getId()).thenReturn("session-1");
            when(session.getUri()).thenReturn(new URI("ws://localhost?foo=bar&baz=userToken=tokenX"));
            when(sessionManager.getActiveSessionCount()).thenReturn(1);

            // WHEN
            handler.afterConnectionEstablished(session);

            // THEN
            verify(sessionManager).registerSession(session);
        }

        @Test
        @DisplayName("afterConnectionEstablished — registers session with null URI")
        void afterConnectionEstablished_registersSessionWithNullUri() throws Exception {
            // GIVEN
            when(session.getId()).thenReturn("session-1");
            when(session.getUri()).thenReturn(null);
            when(sessionManager.getActiveSessionCount()).thenReturn(1);

            // WHEN
            handler.afterConnectionEstablished(session);

            // THEN
            verify(sessionManager).registerSession(session);
        }
    }

    @Nested
    @DisplayName("handleTextMessage — traitement des messages")
    class HandleTextMessageTests {

        @Test
        @DisplayName("handleTextMessage — id present but success missing (should return null)")
        void handleTextMessage_idPresentButSuccessMissing() throws Exception {
            // GIVEN
            String payload = "{\"id\":\"task-4\"}";
            TextMessage message = new TextMessage(payload);

            Map<String, Object> map = new HashMap<>();
            map.put("id", "task-4");

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(map);

            // WHEN
            handler.handleTextMessage(session, message);

            // THEN
            verify(taskOrchestrator, never()).notifyResponse(any());
        }

        @Test
        @DisplayName("handleTextMessage — notifies taskOrchestrator on valid task-response envelope")
        void handleTextMessage_notifiesOrchestratorOnValidEnvelope() throws Exception {
            // GIVEN
            String payload = "{\"type\":\"task-response\",\"response\":{\"id\":\"task-1\",\"success\":true}}";
            TextMessage message = new TextMessage(payload);

            PluginTaskResponse<?> response = new PluginTaskResponse<>();
            response.setId("task-1");
            response.setSuccess(true);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenReturn(Map.of("type", "task-response",
                            "response", Map.of("id", "task-1", "success", true)));
            when(objectMapper.convertValue(any(), eq(PluginTaskResponse.class))).thenReturn(response);
            when(taskOrchestrator.notifyResponse(response)).thenReturn(true);

            // WHEN
            handler.handleTextMessage(session, message);

            // THEN
            verify(taskOrchestrator).notifyResponse(response);
        }

        @Test
        @DisplayName("handleTextMessage — task-response envelope with null response field")
        void handleTextMessage_taskResponseEnvelopeWithNullResponseField() throws Exception {
            // GIVEN
            String payload = "{\"type\":\"task-response\"}";
            TextMessage message = new TextMessage(payload);

            Map<String, Object> map = new HashMap<>();
            map.put("type", "task-response");
            map.put("response", null);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(map);

            // WHEN
            handler.handleTextMessage(session, message);

            // THEN
            verify(taskOrchestrator, never()).notifyResponse(any());
        }

        @Test
        @DisplayName("handleTextMessage — notifies taskOrchestrator on direct format")
        void handleTextMessage_notifiesOrchestratorOnDirectFormat() throws Exception {
            // GIVEN
            String payload = "{\"id\":\"task-2\",\"success\":true}";
            TextMessage message = new TextMessage(payload);

            PluginTaskResponse<?> response = new PluginTaskResponse<>();
            response.setId("task-2");
            response.setSuccess(true);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenReturn(Map.of("id", "task-2", "success", true));
            when(objectMapper.convertValue(any(), eq(PluginTaskResponse.class))).thenReturn(response);
            when(taskOrchestrator.notifyResponse(response)).thenReturn(true);

            // WHEN
            handler.handleTextMessage(session, message);

            // THEN
            verify(taskOrchestrator).notifyResponse(response);
        }

        @Test
        @DisplayName("handleTextMessage — logs warning on unknown task response")
        void handleTextMessage_logsWarningOnUnknownTaskResponse() throws Exception {
            // GIVEN
            String payload = "{\"id\":\"task-unknown\",\"success\":true}";
            TextMessage message = new TextMessage(payload);

            PluginTaskResponse<?> response = new PluginTaskResponse<>();
            response.setId("task-unknown");
            response.setSuccess(true);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenReturn(Map.of("id", "task-unknown", "success", true));
            when(objectMapper.convertValue(any(), eq(PluginTaskResponse.class))).thenReturn(response);
            when(taskOrchestrator.notifyResponse(response)).thenReturn(false);

            // WHEN
            handler.handleTextMessage(session, message);

            // THEN
            verify(taskOrchestrator).notifyResponse(response);
        }

        @Test
        @DisplayName("handleTextMessage — ignores non-task-response message")
        void handleTextMessage_ignoresNonTaskResponseMessage() throws Exception {
            // GIVEN
            String payload = "{\"type\":\"ping\"}";
            TextMessage message = new TextMessage(payload);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenReturn(Map.of("type", "ping"));

            // WHEN
            handler.handleTextMessage(session, message);

            // THEN
            verify(taskOrchestrator, never()).notifyResponse(any());
        }

        @Test
        @DisplayName("handleTextMessage — notifies error when parsing fails and taskId found")
        void handleTextMessage_notifiesErrorWhenParsingFailsAndTaskIdFound() throws Exception {
            // GIVEN
            String payload = "{\"id\":\"task-3\",\"broken\":true}";
            TextMessage message = new TextMessage(payload);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("parse error"))
                    .thenReturn(Map.of("id", "task-3"));

            // WHEN
            handler.handleTextMessage(session, message);

            // THEN
            verify(taskOrchestrator).notifyError(eq("task-3"), any(Exception.class));
        }

        @Test
        @DisplayName("handleTextMessage — returns null when payload is invalid JSON")
        void handleTextMessage_returnsNullWhenPayloadIsInvalidJson() throws Exception {
            // GIVEN
            String payload = "not-a-json";
            TextMessage message = new TextMessage(payload);

            when(session.getId()).thenReturn("session-1");

            // WHEN
            handler.handleTextMessage(session, message);

            // THEN
            verify(taskOrchestrator, never()).notifyResponse(any());
            verify(taskOrchestrator, never()).notifyError(anyString(), any());
        }

        @Test
        @DisplayName("handleTextMessage — skips notifyError when response field has no id")
        void handleTextMessage_skipsNotifyErrorWhenResponseFieldHasNoId() throws Exception {
            // GIVEN
            String payload = "{\"response\":{\"data\":\"something\"}}";
            TextMessage message = new TextMessage(payload);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("data", "something");

            Map<String, Object> map = new HashMap<>();
            map.put("response", responseMap);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("parse error"))
                    .thenReturn(map);

            // WHEN
            handler.handleTextMessage(session, message);

            // THEN
            verify(taskOrchestrator, never()).notifyError(anyString(), any());
        }

        @Test
        @DisplayName("handleTextMessage — skips notifyError when parsing fails and no taskId found")
        void handleTextMessage_skipsNotifyErrorWhenParsingFailsAndNoTaskId() throws Exception {
            // GIVEN
            String payload = "{\"broken\":true}";
            TextMessage message = new TextMessage(payload);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("parse error"))
                    .thenReturn(Map.of("broken", true));

            // WHEN
            handler.handleTextMessage(session, message);

            // THEN
            verify(taskOrchestrator, never()).notifyError(anyString(), any());
        }

        @Test
        @DisplayName("handleTextMessage — skips notifyError when tryExtractTaskId also throws")
        void handleTextMessage_skipsNotifyErrorWhenTryExtractTaskIdAlsoThrows() throws Exception {
            // GIVEN
            String payload = "{\"broken\":true}";
            TextMessage message = new TextMessage(payload);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("parse error"))
                    .thenThrow(new RuntimeException("extract error"));

            // WHEN
            handler.handleTextMessage(session, message);

            // THEN
            verify(taskOrchestrator, never()).notifyError(anyString(), any());
        }

        @Test
        @DisplayName("handleTextMessage — notifies error when taskId found in nested response field")
        void handleTextMessage_notifiesErrorWhenTaskIdFoundInNestedResponse() throws Exception {
            // GIVEN
            String payload = "{\"response\":{\"id\":\"task-nested\"}}";
            TextMessage message = new TextMessage(payload);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("parse error"))
                    .thenReturn(Map.of("response", Map.of("id", "task-nested")));

            // WHEN
            handler.handleTextMessage(session, message);

            // THEN
            verify(taskOrchestrator).notifyError(eq("task-nested"), any(Exception.class));
        }
    }

    @Nested
    @DisplayName("afterConnectionClosed — fermeture session")
    class AfterConnectionClosedTests {

        @Test
        @DisplayName("afterConnectionClosed — unregisters session when still active sessions")
        void afterConnectionClosed_unregistersSession() {
            // GIVEN
            when(session.getId()).thenReturn("session-1");
            when(sessionManager.getActiveSessionCount()).thenReturn(1);
            when(sessionManager.hasActiveSessions()).thenReturn(true);

            // WHEN
            handler.afterConnectionClosed(session, CloseStatus.NORMAL);

            // THEN
            verify(sessionManager).unregisterSession(session);
            verify(taskOrchestrator, never()).cancelAllPendingTasks(anyString());
        }

        @Test
        @DisplayName("afterConnectionClosed — cancels all tasks when no active sessions")
        void afterConnectionClosed_cancelsAllTasksWhenNoActiveSessions() {
            // GIVEN
            when(session.getId()).thenReturn("session-1");
            when(sessionManager.getActiveSessionCount()).thenReturn(0);
            when(sessionManager.hasActiveSessions()).thenReturn(false);

            // WHEN
            handler.afterConnectionClosed(session, CloseStatus.NORMAL);

            // THEN
            verify(sessionManager).unregisterSession(session);
            verify(taskOrchestrator).cancelAllPendingTasks(anyString());
        }
    }

    @Nested
    @DisplayName("handleTransportError — erreurs de transport")
    class HandleTransportErrorTests {

        @Test
        @DisplayName("handleTransportError — closes open session and unregisters")
        void handleTransportError_closesOpenSessionAndUnregisters() throws IOException {
            // GIVEN
            when(session.getId()).thenReturn("session-1");
            when(session.isOpen()).thenReturn(true);

            // WHEN
            handler.handleTransportError(session, new RuntimeException("transport error"));

            // THEN
            verify(session).close(CloseStatus.SERVER_ERROR);
            verify(sessionManager).unregisterSession(session);
        }

        @Test
        @DisplayName("handleTransportError — skips close if session already closed")
        void handleTransportError_skipsCloseIfSessionAlreadyClosed() throws IOException {
            // GIVEN
            when(session.getId()).thenReturn("session-1");
            when(session.isOpen()).thenReturn(false);

            // WHEN
            handler.handleTransportError(session, new RuntimeException("transport error"));

            // THEN
            verify(session, never()).close(any());
            verify(sessionManager).unregisterSession(session);
        }

        @Test
        @DisplayName("handleTransportError — unregisters session even if close throws IOException")
        void handleTransportError_unregistersEvenIfCloseThrows() throws IOException {
            // GIVEN
            when(session.getId()).thenReturn("session-1");
            when(session.isOpen()).thenReturn(true);
            doThrow(new IOException("close failed")).when(session).close(any());

            // WHEN
            handler.handleTransportError(session, new RuntimeException("transport error"));

            // THEN
            verify(sessionManager).unregisterSession(session);
        }
    }
}