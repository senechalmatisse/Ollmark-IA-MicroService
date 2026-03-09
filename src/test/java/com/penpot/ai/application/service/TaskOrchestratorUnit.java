package com.penpot.ai.application.service;

import com.penpot.ai.model.PluginTaskResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.*;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskOrchestrator — Unit")
public class TaskOrchestratorUnit {

    private TaskOrchestrator orchestrator;

    @BeforeEach
    void setUp() { orchestrator = new TaskOrchestrator(); }

    @Nested @DisplayName("registerTask")
    class RegisterTaskTests {

        @Test @DisplayName("registerTask — throws IllegalArgumentException when taskId is null")
        void registerTask_throwsWhenTaskIdIsNull() {
            assertThatThrownBy(() -> orchestrator.registerTask(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task ID cannot be null or empty");
        }

        @Test @DisplayName("registerTask — throws IllegalArgumentException when taskId is blank")
        void registerTask_throwsWhenTaskIdIsBlank() {
            assertThatThrownBy(() -> orchestrator.registerTask("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task ID cannot be null or empty");
        }

        @Test @DisplayName("registerTask — returns non-null incomplete CompletableFuture for valid taskId")
        void registerTask_returnsNonNullIncompleteFutureForValidTaskId() {
            // GIVEN / WHEN
            CompletableFuture<PluginTaskResponse<?>> future = orchestrator.registerTask("task-001");

            // THEN
            assertThat(future).isNotNull();
            assertThat(future.isDone()).isFalse();
        }
    }

    @Nested @DisplayName("unregisterTask")
    class UnregisterTaskTests {

        @Test @DisplayName("unregisterTask — returns false when taskId is null")
        void unregisterTask_returnsFalseWhenTaskIdIsNull() {
            assertThat(orchestrator.unregisterTask(null)).isFalse();
        }

        @Test @DisplayName("unregisterTask — returns false when taskId is blank")
        void unregisterTask_returnsFalseWhenTaskIdIsBlank() {
            assertThat(orchestrator.unregisterTask("   ")).isFalse();
        }

        @Test @DisplayName("unregisterTask — returns false when taskId is unknown")
        void unregisterTask_returnsFalseWhenTaskIdIsUnknown() {
            assertThat(orchestrator.unregisterTask("nonexistent")).isFalse();
        }

        @Test @DisplayName("unregisterTask — does not cancel future that is already completed")
        void unregisterTask_doesNotCancelFutureThatIsAlreadyCompleted() {
            // GIVEN
            CompletableFuture<PluginTaskResponse<?>> future = orchestrator.registerTask("task-done");
            future.complete(null);

            // WHEN
            boolean result = orchestrator.unregisterTask("task-done");

            // THEN
            assertThat(result).isTrue();
            assertThat(future.isCancelled()).isFalse();
        }
    }

    @Nested @DisplayName("notifyResponse")
    class NotifyResponseTests {

        @Mock private PluginTaskResponse<Object> response;

        @Test @DisplayName("notifyResponse — returns false when response is null")
        void notifyResponse_returnsFalseWhenResponseIsNull() {
            assertThat(orchestrator.notifyResponse(null)).isFalse();
        }

        @Test @DisplayName("notifyResponse — returns false when response taskId is null")
        void notifyResponse_returnsFalseWhenResponseTaskIdIsNull() {
            // GIVEN
            when(response.getId()).thenReturn(null);

            // WHEN / THEN
            assertThat(orchestrator.notifyResponse(response)).isFalse();
        }

        @Test @DisplayName("notifyResponse — returns false when response taskId is blank")
        void notifyResponse_returnsFalseWhenResponseTaskIdIsBlank() {
            // GIVEN
            when(response.getId()).thenReturn("   ");

            // WHEN / THEN
            assertThat(orchestrator.notifyResponse(response)).isFalse();
        }

        @Test @DisplayName("notifyResponse — returns false when taskId has no registered future")
        void notifyResponse_returnsFalseWhenTaskIdHasNoRegisteredFuture() {
            // GIVEN
            when(response.getId()).thenReturn("unknown-task");

            // WHEN / THEN
            assertThat(orchestrator.notifyResponse(response)).isFalse();
        }

        @Test @DisplayName("notifyResponse — returns true, completes future and future.get() returns the response")
        void notifyResponse_returnsTrueAndCompletesFutureWhenTaskIdIsRegistered()
                throws ExecutionException, InterruptedException {
            // GIVEN
            String taskId = "task-pending";
            CompletableFuture<PluginTaskResponse<?>> future = orchestrator.registerTask(taskId);
            when(response.getId()).thenReturn(taskId);
            when(response.getSuccess()).thenReturn(true);

            // WHEN
            boolean result = orchestrator.notifyResponse(response);

            // THEN
            assertThat(result).isTrue();
            assertThat(future.isDone()).isTrue();
            assertThat(future.get()).isSameAs(response);
        }

        @Test @DisplayName("notifyResponse — returns false when future is already completed")
        void notifyResponse_returnsFalseWhenFutureIsAlreadyCompleted() {
            // GIVEN
            String taskId = "task-already-done";
            CompletableFuture<PluginTaskResponse<?>> future = orchestrator.registerTask(taskId);
            future.complete(null);
            when(response.getId()).thenReturn(taskId);

            // WHEN / THEN
            assertThat(orchestrator.notifyResponse(response)).isFalse();
        }

        @Test @DisplayName("notifyResponse — returns false and catches exception when future fails to complete")
        void notifyResponse_returnsFalseWhenFutureThrowsException() throws NoSuchFieldException, IllegalAccessException {
            // GIVEN
            String taskId = "task-simulate-completion-error";
            when(response.getId()).thenReturn(taskId);
            CompletableFuture<PluginTaskResponse<?>> mockFuture = mock(CompletableFuture.class);
            when(mockFuture.complete(any())).thenThrow(new RuntimeException("Simulated completion error"));
            Field mapField = TaskOrchestrator.class.getDeclaredField("pendingTasks");
            mapField.setAccessible(true);
            Map<String, CompletableFuture<PluginTaskResponse<?>>> pendingTasksMap = 
                (Map<String, CompletableFuture<PluginTaskResponse<?>>>) mapField.get(orchestrator);
            pendingTasksMap.put(taskId, mockFuture);

            // WHEN
            boolean result = orchestrator.notifyResponse(response);

            // THEN
            assertThat(result).isFalse();
        }
    }

    @Nested @DisplayName("notifyError")
    class NotifyErrorTests {

        @Test @DisplayName("notifyError — returns false when taskId is null")
        void notifyError_returnsFalseWhenTaskIdIsNull() {
            assertThat(orchestrator.notifyError(null, new RuntimeException("e"))).isFalse();
        }

        @Test @DisplayName("notifyError — returns false when taskId is blank")
        void notifyError_returnsFalseWhenTaskIdIsBlank() {
            assertThat(orchestrator.notifyError("  ", new RuntimeException("e"))).isFalse();
        }

        @Test @DisplayName("notifyError — returns false when taskId has no registered future")
        void notifyError_returnsFalseWhenTaskIdHasNoRegisteredFuture() {
            assertThat(orchestrator.notifyError("unknown", new RuntimeException("e"))).isFalse();
        }

        @Test @DisplayName("notifyError — returns true and completes future exceptionally when taskId is registered")
        void notifyError_returnsTrueAndCompletesFutureExceptionallyWhenTaskIdIsRegistered() {
            // GIVEN
            CompletableFuture<PluginTaskResponse<?>> future = orchestrator.registerTask("task-error");

            // WHEN
            boolean result = orchestrator.notifyError("task-error", new RuntimeException("plugin error"));

            // THEN
            assertThat(result).isTrue();
            assertThat(future.isCompletedExceptionally()).isTrue();
        }

        @Test @DisplayName("notifyError — returns false when future is already completed")
        void notifyError_returnsFalseWhenFutureIsAlreadyCompleted() {
            // GIVEN
            CompletableFuture<PluginTaskResponse<?>> future = orchestrator.registerTask("task-done");
            future.complete(null);

            // WHEN / THEN
            assertThat(orchestrator.notifyError("task-done", new RuntimeException("late"))).isFalse();
        }

        @Test @DisplayName("notifyError — returns false and catches exception when future fails to complete exceptionally")
        void notifyError_returnsFalseWhenFutureThrowsException() throws NoSuchFieldException, IllegalAccessException {
            // GIVEN
            String taskId = "task-simulate-exceptional-error";
            Throwable simulatedError = new RuntimeException("Initial task failure");
            CompletableFuture<PluginTaskResponse<?>> mockFuture = mock(CompletableFuture.class);
            when(mockFuture.completeExceptionally(any(Throwable.class)))
                .thenThrow(new RuntimeException("Simulated execution error"));
            Field mapField = TaskOrchestrator.class.getDeclaredField("pendingTasks");
            mapField.setAccessible(true);
            Map<String, CompletableFuture<PluginTaskResponse<?>>> pendingTasksMap = 
                (Map<String, CompletableFuture<PluginTaskResponse<?>>>) mapField.get(orchestrator);
            pendingTasksMap.put(taskId, mockFuture);

            // WHEN
            boolean result = orchestrator.notifyError(taskId, simulatedError);

            // THEN
            assertThat(result).isFalse();
        }
    }
}