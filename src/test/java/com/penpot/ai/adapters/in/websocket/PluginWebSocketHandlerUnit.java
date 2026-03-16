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

        @BeforeEach
        void stubHandshake() throws Exception {
            when(session.getId()).thenReturn("session-1");
            when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"type\":\"session-id\",\"sessionId\":\"session-1\"}");
            when(sessionManager.getActiveSessionCount()).thenReturn(1);
        }

        @Test
        @DisplayName("afterConnectionEstablished — registers session with userToken")
        void afterConnectionEstablished_registersSessionWithUserToken() throws Exception {
            handler.afterConnectionEstablished(session);
            verify(sessionManager).registerSession(session);
        }

        @Test
        @DisplayName("afterConnectionEstablished — registers session without userToken (null query)")
        void afterConnectionEstablished_registersSessionWithoutUserToken() throws Exception {
            handler.afterConnectionEstablished(session);
            verify(sessionManager).registerSession(session);
        }

        @Test
        @DisplayName("afterConnectionEstablished — registers session with query but no userToken param")
        void afterConnectionEstablished_registersSessionWithQueryButNoUserToken() throws Exception {
            handler.afterConnectionEstablished(session);
            verify(sessionManager).registerSession(session);
        }

        @Test
        @DisplayName("afterConnectionEstablished — registers session with multiple query params")
        void afterConnectionEstablished_registersSessionWithMultipleQueryParams() throws Exception {
            handler.afterConnectionEstablished(session);
            verify(sessionManager).registerSession(session);
        }

        @Test
        @DisplayName("extractUserToken — returns token when userToken= is not the first param")
        void extractUserToken_returnsTokenWhenNotFirstParam() throws Exception {
            handler.afterConnectionEstablished(session);
            verify(sessionManager).registerSession(session);
        }

        @Test
        @DisplayName("extractUserToken — query contains userToken= but no param starts with userToken=")
        void extractUserToken_queryContainsUserTokenButNoParamStartsWithUserToken() throws Exception {
            handler.afterConnectionEstablished(session);
            verify(sessionManager).registerSession(session);
        }

        @Test
        @DisplayName("afterConnectionEstablished — registers session with null URI")
        void afterConnectionEstablished_registersSessionWithNullUri() throws Exception {
            handler.afterConnectionEstablished(session);
            verify(sessionManager).registerSession(session);
        }
    }

    @Nested
    @DisplayName("handleTextMessage — traitement des messages")
    class HandleTextMessageTests {

        @Test
        @DisplayName("handleTextMessage — id present but success missing (should return null)")
        void handleTextMessage_idPresentButSuccessMissing() throws Exception {
            String payload = "{\"id\":\"task-4\"}";
            TextMessage message = new TextMessage(payload);

            Map<String, Object> map = new HashMap<>();
            map.put("id", "task-4");

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(map);

            handler.handleTextMessage(session, message);

            verify(taskOrchestrator, never()).notifyResponse(any());
        }

        @Test
        @DisplayName("handleTextMessage — notifies taskOrchestrator on valid task-response envelope")
        void handleTextMessage_notifiesOrchestratorOnValidEnvelope() throws Exception {
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

            handler.handleTextMessage(session, message);

            verify(taskOrchestrator).notifyResponse(response);
        }

        @Test
        @DisplayName("handleTextMessage — task-response envelope with null response field")
        void handleTextMessage_taskResponseEnvelopeWithNullResponseField() throws Exception {
            String payload = "{\"type\":\"task-response\"}";
            TextMessage message = new TextMessage(payload);

            Map<String, Object> map = new HashMap<>();
            map.put("type", "task-response");
            map.put("response", null);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(map);

            handler.handleTextMessage(session, message);

            verify(taskOrchestrator, never()).notifyResponse(any());
        }

        @Test
        @DisplayName("handleTextMessage — notifies taskOrchestrator on direct format")
        void handleTextMessage_notifiesOrchestratorOnDirectFormat() throws Exception {
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

            handler.handleTextMessage(session, message);

            verify(taskOrchestrator).notifyResponse(response);
        }

        @Test
        @DisplayName("handleTextMessage — logs warning on unknown task response")
        void handleTextMessage_logsWarningOnUnknownTaskResponse() throws Exception {
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

            handler.handleTextMessage(session, message);

            verify(taskOrchestrator).notifyResponse(response);
        }

        @Test
        @DisplayName("handleTextMessage — ignores non-task-response message")
        void handleTextMessage_ignoresNonTaskResponseMessage() throws Exception {
            String payload = "{\"type\":\"ping\"}";
            TextMessage message = new TextMessage(payload);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenReturn(Map.of("type", "ping"));

            handler.handleTextMessage(session, message);

            verify(taskOrchestrator, never()).notifyResponse(any());
        }

        @Test
        @DisplayName("handleTextMessage — notifies error when parsing fails and taskId found")
        void handleTextMessage_notifiesErrorWhenParsingFailsAndTaskIdFound() throws Exception {
            String payload = "{\"id\":\"task-3\",\"broken\":true}";
            TextMessage message = new TextMessage(payload);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("parse error"))
                    .thenReturn(Map.of("id", "task-3"));

            handler.handleTextMessage(session, message);

            verify(taskOrchestrator).notifyError(eq("task-3"), any(Exception.class));
        }

        @Test
        @DisplayName("handleTextMessage — returns null when payload is invalid JSON")
        void handleTextMessage_returnsNullWhenPayloadIsInvalidJson() throws Exception {
            String payload = "not-a-json";
            TextMessage message = new TextMessage(payload);

            when(session.getId()).thenReturn("session-1");

            handler.handleTextMessage(session, message);

            verify(taskOrchestrator, never()).notifyResponse(any());
            verify(taskOrchestrator, never()).notifyError(anyString(), any());
        }

        @Test
        @DisplayName("handleTextMessage — skips notifyError when response field has no id")
        void handleTextMessage_skipsNotifyErrorWhenResponseFieldHasNoId() throws Exception {
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

            handler.handleTextMessage(session, message);

            verify(taskOrchestrator, never()).notifyError(anyString(), any());
        }

        @Test
        @DisplayName("handleTextMessage — skips notifyError when parsing fails and no taskId found")
        void handleTextMessage_skipsNotifyErrorWhenParsingFailsAndNoTaskId() throws Exception {
            String payload = "{\"broken\":true}";
            TextMessage message = new TextMessage(payload);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("parse error"))
                    .thenReturn(Map.of("broken", true));

            handler.handleTextMessage(session, message);

            verify(taskOrchestrator, never()).notifyError(anyString(), any());
        }

        @Test
        @DisplayName("handleTextMessage — skips notifyError when tryExtractTaskId also throws")
        void handleTextMessage_skipsNotifyErrorWhenTryExtractTaskIdAlsoThrows() throws Exception {
            String payload = "{\"broken\":true}";
            TextMessage message = new TextMessage(payload);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("parse error"))
                    .thenThrow(new RuntimeException("extract error"));

            handler.handleTextMessage(session, message);

            verify(taskOrchestrator, never()).notifyError(anyString(), any());
        }

        @Test
        @DisplayName("handleTextMessage — notifies error when taskId found in nested response field")
        void handleTextMessage_notifiesErrorWhenTaskIdFoundInNestedResponse() throws Exception {
            String payload = "{\"response\":{\"id\":\"task-nested\"}}";
            TextMessage message = new TextMessage(payload);

            when(session.getId()).thenReturn("session-1");
            when(objectMapper.readValue(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("parse error"))
                    .thenReturn(Map.of("response", Map.of("id", "task-nested")));

            handler.handleTextMessage(session, message);

            verify(taskOrchestrator).notifyError(eq("task-nested"), any(Exception.class));
        }
    }

    @Nested
    @DisplayName("afterConnectionClosed — fermeture session")
    class AfterConnectionClosedTests {

        @Test
        @DisplayName("afterConnectionClosed — unregisters session when still active sessions")
        void afterConnectionClosed_unregistersSession() {
            when(session.getId()).thenReturn("session-1");
            when(sessionManager.getActiveSessionCount()).thenReturn(1);
            when(sessionManager.hasActiveSessions()).thenReturn(true);

            handler.afterConnectionClosed(session, CloseStatus.NORMAL);

            verify(sessionManager).unregisterSession(session);
            verify(taskOrchestrator, never()).cancelAllPendingTasks(anyString());
        }

        @Test
        @DisplayName("afterConnectionClosed — cancels all tasks when no active sessions")
        void afterConnectionClosed_cancelsAllTasksWhenNoActiveSessions() {
            when(session.getId()).thenReturn("session-1");
            when(sessionManager.getActiveSessionCount()).thenReturn(0);
            when(sessionManager.hasActiveSessions()).thenReturn(false);

            handler.afterConnectionClosed(session, CloseStatus.NORMAL);

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
            when(session.getId()).thenReturn("session-1");
            when(session.isOpen()).thenReturn(true);

            handler.handleTransportError(session, new RuntimeException("transport error"));

            verify(session).close(CloseStatus.SERVER_ERROR);
            verify(sessionManager).unregisterSession(session);
        }

        @Test
        @DisplayName("handleTransportError — skips close if session already closed")
        void handleTransportError_skipsCloseIfSessionAlreadyClosed() throws IOException {
            when(session.getId()).thenReturn("session-1");
            when(session.isOpen()).thenReturn(false);

            handler.handleTransportError(session, new RuntimeException("transport error"));

            verify(session, never()).close(any());
            verify(sessionManager).unregisterSession(session);
        }

        @Test
        @DisplayName("handleTransportError — unregisters session even if close throws IOException")
        void handleTransportError_unregistersEvenIfCloseThrows() throws IOException {
            when(session.getId()).thenReturn("session-1");
            when(session.isOpen()).thenReturn(true);
            doThrow(new IOException("close failed")).when(session).close(any());

            handler.handleTransportError(session, new RuntimeException("transport error"));

            verify(sessionManager).unregisterSession(session);
        }
    }
}