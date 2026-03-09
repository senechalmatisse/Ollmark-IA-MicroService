package com.penpot.ai.adapters.out.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.application.service.TaskOrchestrator;
import com.penpot.ai.core.domain.*;
import com.penpot.ai.infrastructure.session.SessionManager;
import com.penpot.ai.model.*;
import com.penpot.ai.shared.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PluginBridgeAdapter}.
 *
 * All collaborators (SessionManager, TaskOrchestrator, ObjectMapper, WebSocketSession)
 * are mocked. Tests validate orchestration logic, error handling, and thread-safety
 * guarantees without hitting real WebSocket or network resources.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PluginBridgeAdapter — Unit Tests")
public class PluginBridgeAdapterUnit {

    @Mock private SessionManager sessionManager;
    @Mock private ObjectMapper objectMapper;
    @Mock private TaskOrchestrator responseOrchestrator;
    @Mock private WebSocketSession webSocketSession;

    @InjectMocks private PluginBridgeAdapter adapter;

    private static final String TASK_ID = "task-001";
    private static final String SESSION_ID = "session-001";
    private static final String USER_TOKEN = "token-abc";
    private static final long   TIMEOUT_MS = 5_000L;
    private static final String SERIALIZED = "{\"id\":\"task-001\"}";

    private Task task;
    private PluginTaskRequest request;
    private CompletableFuture<PluginTaskResponse<?>> future;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(adapter, "pluginTimeoutMs", TIMEOUT_MS);

        task = mock(Task.class);
        when(task.getId()).thenReturn(TASK_ID);
        when(task.getType()).thenReturn(TaskType.EXECUTE_CODE);
        when(task.getParameters()).thenReturn(java.util.Map.of());
        when(task.getUserToken()).thenReturn(Optional.of(USER_TOKEN));

        request = PluginTaskRequest.builder()
            .id(TASK_ID).task(TaskType.EXECUTE_CODE.getTaskName()).params(java.util.Map.of()).build();

        future = new CompletableFuture<>();

        when(sessionManager.findSession(any(SessionCriteria.class)))
            .thenReturn(Optional.of(webSocketSession));
        when(webSocketSession.getId()).thenReturn(SESSION_ID);
        when(webSocketSession.isOpen()).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn(SERIALIZED);
        when(responseOrchestrator.registerTask(TASK_ID)).thenReturn(future);
    }

    // =========================================================================
    // sendTask() — happy path
    // =========================================================================

    @Nested
    @DisplayName("sendTask() — happy path")
    class SendTaskHappyPath {

        @Test
        @DisplayName("shouldReturnResponse_givenValidTaskAndOpenSession_whenSendTaskIsCalled")
        void shouldReturnResponse_givenValidTaskAndOpenSession_whenSendTaskIsCalled()
            throws Exception {
            // GIVEN
            PluginTaskResponse<String> pluginResponse = successResponse("ok");
            future.complete(pluginResponse);

            // WHEN
            PluginTaskResponse<String> result = adapter.sendTask(task);

            // THEN
            assertThat(result).isSameAs(pluginResponse);
            assertThat(result.getSuccess()).isTrue();
        }

        @Test
        @DisplayName("shouldSendSerializedJsonOverWebSocket_givenValidTask_whenSendTaskIsCalled")
        void shouldSendSerializedJsonOverWebSocket_givenValidTask_whenSendTaskIsCalled()
            throws Exception {
            // GIVEN
            future.complete(successResponse("done"));

            // WHEN
            adapter.sendTask(task);

            // THEN
            ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
            verify(webSocketSession).sendMessage(messageCaptor.capture());
            assertThat(messageCaptor.getValue().getPayload()).isEqualTo(SERIALIZED);
        }

        @Test
        @DisplayName("shouldRegisterAndUnregisterTask_givenSuccessfulExecution_whenSendTaskIsCalled")
        void shouldRegisterAndUnregisterTask_givenSuccessfulExecution_whenSendTaskIsCalled()
            throws Exception {
            // GIVEN
            future.complete(successResponse("done"));

            // WHEN
            adapter.sendTask(task);

            // THEN
            InOrder order = inOrder(responseOrchestrator);
            order.verify(responseOrchestrator).registerTask(TASK_ID);
            order.verify(responseOrchestrator).unregisterTask(TASK_ID);
        }

        @Test
        @DisplayName("shouldBuildCriteriaFromUserToken_givenTaskWithUserToken_whenSendTaskIsCalled")
        void shouldBuildCriteriaFromUserToken_givenTaskWithUserToken_whenSendTaskIsCalled()
            throws Exception {
            // GIVEN
            future.complete(successResponse("ok"));

            // WHEN
            adapter.sendTask(task);

            // THEN
            verify(sessionManager).findSession(argThat(c ->
                c.equals(SessionCriteria.forUser(USER_TOKEN))));
        }

        @Test
        @DisplayName("shouldBuildCriteriaWithAny_givenTaskWithoutUserToken_whenSendTaskIsCalled")
        void shouldBuildCriteriaWithAny_givenTaskWithoutUserToken_whenSendTaskIsCalled()
            throws Exception {
            // GIVEN
            when(task.getUserToken()).thenReturn(Optional.empty());
            future.complete(successResponse("ok"));

            // WHEN
            adapter.sendTask(task);

            // THEN
            verify(sessionManager).findSession(argThat(c ->
                c.equals(SessionCriteria.any())));
        }

        @Test
        @DisplayName("shouldCastResponseToExpectedType_givenTypedResponse_whenSendTaskIsCalled")
        void shouldCastResponseToExpectedType_givenTypedResponse_whenSendTaskIsCalled()
            throws Exception {
            // GIVEN
            PluginTaskResponse<Integer> typedResponse = successResponse(42);
            future.complete(typedResponse);

            // WHEN
            PluginTaskResponse<Integer> result = adapter.<Integer>sendTask(task);

            // THEN
            assertThat(result.getData()).isEqualTo(42);
        }
    }

    // =========================================================================
    // sendTask() — session errors
    // =========================================================================

    @Nested
    @DisplayName("sendTask() — session errors")
    class SendTaskSessionErrors {

        @Test
        @DisplayName("shouldThrowPluginConnectionException_givenNoActiveSession_whenSendTaskIsCalled")
        void shouldThrowPluginConnectionException_givenNoActiveSession_whenSendTaskIsCalled() {
            // GIVEN
            when(sessionManager.findSession(any())).thenReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> adapter.sendTask(task))
                .isInstanceOf(PluginConnectionException.class)
                .hasMessageContaining("No active plugin session found");
        }

        @Test
        @DisplayName("shouldNotRegisterTask_givenNoActiveSession_whenSendTaskIsCalled")
        void shouldNotRegisterTask_givenNoActiveSession_whenSendTaskIsCalled() {
            // GIVEN
            when(sessionManager.findSession(any())).thenReturn(Optional.empty());

            // WHEN
            catchThrowable(() -> adapter.sendTask(task));

            // THEN
            verifyNoInteractions(responseOrchestrator);
        }

        @Test
        @DisplayName("shouldThrowPluginConnectionException_givenSessionIsClosedBeforeSend_whenSendTaskIsCalled")
        void shouldThrowPluginConnectionException_givenSessionIsClosedBeforeSend_whenSendTaskIsCalled() {
            // GIVEN
            when(webSocketSession.isOpen()).thenReturn(false);

            // WHEN / THEN
            assertThatThrownBy(() -> adapter.sendTask(task))
                .isInstanceOf(TaskExecutionException.class)
                .hasMessageContaining("is not open");
        }

        @Test
        @DisplayName("shouldUnregisterTask_givenSessionIsClosedBeforeSend_whenSendTaskIsCalled")
        void shouldUnregisterTask_givenSessionIsClosedBeforeSend_whenSendTaskIsCalled() {
            // GIVEN
            when(webSocketSession.isOpen()).thenReturn(false);

            // WHEN
            catchThrowable(() -> adapter.sendTask(task));

            // THEN
            verify(responseOrchestrator).unregisterTask(TASK_ID);
        }
    }

    // =========================================================================
    // sendTask() — WebSocket send errors
    // =========================================================================

    @Nested
    @DisplayName("sendTask() — WebSocket send errors")
    class SendTaskWebSocketErrors {

        @Test
        @DisplayName("shouldThrowTaskExecutionException_givenSerializationFails_whenSendTaskIsCalled")
        void shouldThrowTaskExecutionException_givenSerializationFails_whenSendTaskIsCalled()
            throws Exception {
            // GIVEN
            when(objectMapper.writeValueAsString(any()))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("bad json") {});

            // WHEN / THEN
            assertThatThrownBy(() -> adapter.sendTask(task))
                .isInstanceOf(TaskExecutionException.class)
                .hasMessageContaining("Failed to send WebSocket message");
        }

        @Test
        @DisplayName("shouldThrowTaskExecutionException_givenWebSocketSendFails_whenSendTaskIsCalled")
        void shouldThrowTaskExecutionException_givenWebSocketSendFails_whenSendTaskIsCalled()
            throws Exception {
            // GIVEN
            doThrow(new java.io.IOException("connection reset"))
                .when(webSocketSession).sendMessage(any(TextMessage.class));

            // WHEN / THEN
            assertThatThrownBy(() -> adapter.sendTask(task))
                .isInstanceOf(TaskExecutionException.class)
                .hasMessageContaining("Failed to send WebSocket message");
        }

        @Test
        @DisplayName("shouldUnregisterTask_givenWebSocketSendFails_whenSendTaskIsCalled")
        void shouldUnregisterTask_givenWebSocketSendFails_whenSendTaskIsCalled()
            throws Exception {
            // GIVEN
            doThrow(new java.io.IOException("broken pipe"))
                .when(webSocketSession).sendMessage(any());

            // WHEN
            catchThrowable(() -> adapter.sendTask(task));

            // THEN
            verify(responseOrchestrator).unregisterTask(TASK_ID);
        }
    }

    // =========================================================================
    // sendTask() — future/timeout errors
    // =========================================================================

    @Nested
    @DisplayName("sendTask() — future and timeout errors")
    class SendTaskFutureErrors {

        @Test
        @DisplayName("shouldThrowTaskExecutionException_givenFutureTimesOut_whenSendTaskIsCalled")
        void shouldThrowTaskExecutionException_givenFutureTimesOut_whenSendTaskIsCalled() {
            // GIVEN
            ReflectionTestUtils.setField(adapter, "pluginTimeoutMs", 1L);

            // WHEN / THEN
            assertThatThrownBy(() -> adapter.sendTask(task))
                .isInstanceOf(TaskExecutionException.class)
                .hasMessageContaining("timed out");
        }

        @Test
        @DisplayName("shouldCancelFuture_givenFutureTimesOut_whenSendTaskIsCalled")
        void shouldCancelFuture_givenFutureTimesOut_whenSendTaskIsCalled() {
            // GIVEN
            ReflectionTestUtils.setField(adapter, "pluginTimeoutMs", 1L);

            // WHEN
            catchThrowable(() -> adapter.sendTask(task));

            // THEN
            assertThat(future.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("shouldUnregisterTask_givenFutureTimesOut_whenSendTaskIsCalled")
        void shouldUnregisterTask_givenFutureTimesOut_whenSendTaskIsCalled() {
            // GIVEN
            ReflectionTestUtils.setField(adapter, "pluginTimeoutMs", 1L);

            // WHEN
            catchThrowable(() -> adapter.sendTask(task));

            // THEN
            verify(responseOrchestrator).unregisterTask(TASK_ID);
        }

        @Test
        @DisplayName("shouldThrowTaskExecutionException_givenFutureCompletesExceptionally_whenSendTaskIsCalled")
        void shouldThrowTaskExecutionException_givenFutureCompletesExceptionally_whenSendTaskIsCalled() {
            // GIVEN
            future.completeExceptionally(new RuntimeException("plugin crashed"));

            // WHEN / THEN
            assertThatThrownBy(() -> adapter.sendTask(task))
                .isInstanceOf(TaskExecutionException.class)
                .hasMessageContaining("execution failed");
        }

        @Test
        @DisplayName("shouldWrapOriginalCause_givenFutureCompletesExceptionally_whenSendTaskIsCalled")
        void shouldWrapOriginalCause_givenFutureCompletesExceptionally_whenSendTaskIsCalled() {
            // GIVEN
            RuntimeException rootCause = new RuntimeException("plugin internal error");
            future.completeExceptionally(rootCause);

            // WHEN / THEN
            assertThatThrownBy(() -> adapter.sendTask(task))
                .isInstanceOf(TaskExecutionException.class)
                .hasMessageContaining("plugin internal error");
        }

        @Test
        @DisplayName("shouldRestoreInterruptFlag_givenThreadIsInterrupted_whenSendTaskIsCalled")
        void shouldRestoreInterruptFlag_givenThreadIsInterrupted_whenSendTaskIsCalled()
            throws Exception {
            // GIVEN
            CompletableFuture<PluginTaskResponse<?>> interruptibleFuture =
                mock(CompletableFuture.class);
            when(responseOrchestrator.registerTask(TASK_ID)).thenReturn(interruptibleFuture);
            when(interruptibleFuture.get(anyLong(), any()))
                .thenThrow(new InterruptedException("thread interrupted"));

            // WHEN
            catchThrowable(() -> adapter.sendTask(task));

            // THEN
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
            Thread.interrupted();
        }

        @Test
        @DisplayName("shouldThrowTaskExecutionException_givenThreadIsInterrupted_whenSendTaskIsCalled")
        void shouldThrowTaskExecutionException_givenThreadIsInterrupted_whenSendTaskIsCalled()
            throws Exception {
            // GIVEN
            CompletableFuture<PluginTaskResponse<?>> interruptibleFuture =
                mock(CompletableFuture.class);
            when(responseOrchestrator.registerTask(TASK_ID)).thenReturn(interruptibleFuture);
            when(interruptibleFuture.get(anyLong(), any()))
                .thenThrow(new InterruptedException("interrupted"));

            // WHEN / THEN
            assertThatThrownBy(() -> adapter.sendTask(task))
                .isInstanceOf(TaskExecutionException.class)
                .hasMessageContaining("interrupted");

            Thread.interrupted();
        }
    }

    // =========================================================================
    // hasActiveConnection()
    // =========================================================================

    @Nested
    @DisplayName("hasActiveConnection()")
    class HasActiveConnection {

        @Test
        @DisplayName("shouldReturnTrue_givenSessionManagerHasActiveSessions_whenHasActiveConnectionIsCalled")
        void shouldReturnTrue_givenSessionManagerHasActiveSessions_whenHasActiveConnectionIsCalled() {
            // GIVEN
            when(sessionManager.hasActiveSessions()).thenReturn(true);

            // WHEN
            boolean result = adapter.hasActiveConnection();

            // THEN
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("shouldReturnFalse_givenNoActiveSessions_whenHasActiveConnectionIsCalled")
        void shouldReturnFalse_givenNoActiveSessions_whenHasActiveConnectionIsCalled() {
            // GIVEN
            when(sessionManager.hasActiveSessions()).thenReturn(false);

            // WHEN
            boolean result = adapter.hasActiveConnection();

            // THEN
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("shouldDelegateToSessionManager_givenAnyState_whenHasActiveConnectionIsCalled")
        void shouldDelegateToSessionManager_givenAnyState_whenHasActiveConnectionIsCalled() {
            // GIVEN
            when(sessionManager.hasActiveSessions()).thenReturn(true);

            // WHEN
            adapter.hasActiveConnection();

            // THEN
            verify(sessionManager, times(1)).hasActiveSessions();
            verifyNoMoreInteractions(sessionManager);
        }
    }

    // =========================================================================
    // findSession()
    // =========================================================================

    @Nested
    @DisplayName("findSession()")
    class FindSessionMethod {

        @Test
        @DisplayName("shouldReturnSession_givenMatchingCriteria_whenFindSessionIsCalled")
        void shouldReturnSession_givenMatchingCriteria_whenFindSessionIsCalled() {
            // GIVEN
            SessionCriteria criteria = SessionCriteria.forUser(USER_TOKEN);
            when(sessionManager.findSession(criteria)).thenReturn(Optional.of(webSocketSession));

            // WHEN
            Optional<WebSocketSession> result = adapter.findSession(criteria);

            // THEN
            assertThat(result).contains(webSocketSession);
        }

        @Test
        @DisplayName("shouldReturnEmpty_givenNoMatchingSession_whenFindSessionIsCalled")
        void shouldReturnEmpty_givenNoMatchingSession_whenFindSessionIsCalled() {
            // GIVEN
            SessionCriteria criteria = SessionCriteria.forUser("unknown-token");
            when(sessionManager.findSession(criteria)).thenReturn(Optional.empty());

            // WHEN
            Optional<WebSocketSession> result = adapter.findSession(criteria);

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("shouldDelegateDirectlyToSessionManager_givenAnyCriteria_whenFindSessionIsCalled")
        void shouldDelegateDirectlyToSessionManager_givenAnyCriteria_whenFindSessionIsCalled() {
            // GIVEN
            SessionCriteria criteria = SessionCriteria.any();
            when(sessionManager.findSession(criteria)).thenReturn(Optional.empty());

            // WHEN
            adapter.findSession(criteria);

            // THEN
            verify(sessionManager, times(1)).findSession(criteria);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    @SuppressWarnings("unchecked")
    private <T> PluginTaskResponse<T> successResponse(T data) {
        PluginTaskResponse<T> response = mock(PluginTaskResponse.class);
        when(response.getSuccess()).thenReturn(true);
        when(response.getData()).thenReturn(data);
        return response;
    }
}