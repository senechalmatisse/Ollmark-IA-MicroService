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

        @Test @DisplayName("registerTask — increments pending task count after each registration")
        void registerTask_incrementsPendingTaskCountAfterEachRegistration() {
            // GIVEN
            assertThat(orchestrator.getPendingTaskCount()).isZero();

            // WHEN
            orchestrator.registerTask("task-001");
            orchestrator.registerTask("task-002");

            // THEN
            assertThat(orchestrator.getPendingTaskCount()).isEqualTo(2);
        }

        @Test @DisplayName("registerTask — returns existing future and count stays 1 when taskId is already registered")
        void registerTask_returnsExistingFutureWhenTaskIdAlreadyRegistered() {
            // GIVEN
            String taskId = "task-duplicate";
            CompletableFuture<PluginTaskResponse<?>> first = orchestrator.registerTask(taskId);

            // WHEN
            CompletableFuture<PluginTaskResponse<?>> second = orchestrator.registerTask(taskId);

            // THEN
            assertThat(second).isSameAs(first);
            assertThat(orchestrator.getPendingTaskCount()).isEqualTo(1);
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

        @Test @DisplayName("unregisterTask — returns true and decrements count when taskId is registered")
        void unregisterTask_returnsTrueAndDecrementsCountWhenTaskIdIsRegistered() {
            // GIVEN
            orchestrator.registerTask("task-001");

            // WHEN
            boolean result = orchestrator.unregisterTask("task-001");

            // THEN
            assertThat(result).isTrue();
            assertThat(orchestrator.getPendingTaskCount()).isZero();
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

    @Nested @DisplayName("cancelAllPendingTasks")
    class CancelAllPendingTasksTests {

        @Test @DisplayName("cancelAllPendingTasks — count stays 0 when no tasks are pending")
        void cancelAllPendingTasks_countStaysZeroWhenNoTasksArePending() {
            // GIVEN / WHEN
            orchestrator.cancelAllPendingTasks("test");

            // THEN
            assertThat(orchestrator.getPendingTaskCount()).isZero();
        }

        @Test @DisplayName("cancelAllPendingTasks — count becomes 0 after cancelling 3 registered tasks")
        void cancelAllPendingTasks_countBecomesZeroAfterCancelling3RegisteredTasks() {
            // GIVEN
            orchestrator.registerTask("t-1");
            orchestrator.registerTask("t-2");
            orchestrator.registerTask("t-3");

            // WHEN
            orchestrator.cancelAllPendingTasks("server shutdown");

            // THEN
            assertThat(orchestrator.getPendingTaskCount()).isZero();
        }

        @Test @DisplayName("cancelAllPendingTasks — does not throw when some tasks are already completed")
        void cancelAllPendingTasks_doesNotThrowWhenSomeTasksAreAlreadyCompleted() {
            // GIVEN
            CompletableFuture<PluginTaskResponse<?>> done = orchestrator.registerTask("done");
            done.complete(null);
            orchestrator.registerTask("pending");

            // WHEN / THEN
            assertThatCode(() -> orchestrator.cancelAllPendingTasks("reason"))
                .doesNotThrowAnyException();
            assertThat(orchestrator.getPendingTaskCount()).isZero();
        }
    }

    @Nested @DisplayName("isTaskPending")
    class IsTaskPendingTests {

        @Test @DisplayName("isTaskPending — returns false when taskId is null")
        void isTaskPending_returnsFalseWhenNull() {
            assertThat(orchestrator.isTaskPending(null)).isFalse();
        }

        @Test @DisplayName("isTaskPending — returns false when taskId is blank")
        void isTaskPending_returnsFalseWhenBlank() {
            assertThat(orchestrator.isTaskPending("  ")).isFalse();
        }

        @Test @DisplayName("isTaskPending — returns false when taskId is not registered")
        void isTaskPending_returnsFalseWhenNotRegistered() {
            assertThat(orchestrator.isTaskPending("ghost")).isFalse();
        }
    }

    @Nested @DisplayName("cleanupCompletedTasks")
    class CleanupCompletedTasksTests {

        @Test @DisplayName("cleanupCompletedTasks — returns 0 when no tasks are registered")
        void cleanupCompletedTasks_returnsZeroWhenEmpty() {
            assertThat(orchestrator.cleanupCompletedTasks()).isZero();
        }

        @Test @DisplayName("cleanupCompletedTasks — returns 0 when all tasks are still pending")
        void cleanupCompletedTasks_returnsZeroWhenAllTasksPending() {
            // GIVEN
            orchestrator.registerTask("p1");
            orchestrator.registerTask("p2");

            // WHEN / THEN
            assertThat(orchestrator.cleanupCompletedTasks()).isZero();
            assertThat(orchestrator.getPendingTaskCount()).isEqualTo(2);
        }

        @Test @DisplayName("cleanupCompletedTasks — removes 2 completed tasks and leaves 1 pending")
        void cleanupCompletedTasks_removes2CompletedTasksAndLeaves1Pending() {
            // GIVEN
            CompletableFuture<PluginTaskResponse<?>> f1 = orchestrator.registerTask("done-1");
            CompletableFuture<PluginTaskResponse<?>> f2 = orchestrator.registerTask("done-2");
            orchestrator.registerTask("still-pending");
            f1.complete(null);
            f2.complete(null);

            // WHEN
            int cleaned = orchestrator.cleanupCompletedTasks();

            // THEN
            assertThat(cleaned).isEqualTo(2);
            assertThat(orchestrator.getPendingTaskCount()).isEqualTo(1);
            assertThat(orchestrator.isTaskPending("still-pending")).isTrue();
        }
    }

    @Nested @DisplayName("getStatistics")
    class GetStatisticsTests {

        @Test @DisplayName("getStatistics — returns map containing keys 'total', 'pending' and 'completed'")
        void getStatistics_returnsMapContainingExpectedKeys() {
            // GIVEN / WHEN
            Map<String, Object> stats = orchestrator.getStatistics();

            // THEN
            assertThat(stats).containsKeys("total", "pending", "completed");
        }
    }
}