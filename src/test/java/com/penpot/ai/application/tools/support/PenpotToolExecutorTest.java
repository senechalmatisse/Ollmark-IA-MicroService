package com.penpot.ai.application.tools.support;

import com.penpot.ai.core.domain.*;
import com.penpot.ai.core.ports.in.ExecuteCodeUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenpotToolExecutorTest {

    @Mock
    private ExecuteCodeUseCase executeCodeUseCase;

    @InjectMocks
    private PenpotToolExecutor penpotToolExecutor;

    private static final String SAMPLE_CODE = "penpot.createRectangle()";
    private static final String SHAPE_ID = "shape-123";
    private static final String GROUP_ID = "group-456";
    private static final String CLONE_ID = "clone-789";

    @Test
    void shouldReturnMappedResultWhenExecuteSucceeds() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(null));

        // WHEN
        String response = penpotToolExecutor.execute(SAMPLE_CODE, "test operation", result -> "custom-response");

        // THEN
        assertThat(response).isEqualTo("custom-response");
    }

    @Test
    void shouldReturnErrorResponseWhenExecuteCodeUseCaseReturnsFailure() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.failure("Execution failed"));

        // WHEN
        String response = penpotToolExecutor.execute(SAMPLE_CODE, "failing operation", result -> "should-not-reach");

        // THEN
        assertThat(response).contains("Execution failed");
    }

    @Test
    void shouldReturnUnknownErrorMessageWhenExecuteCodeUseCaseReturnsFailureWithNoMessage() {
        // GIVEN
        TaskResult failureWithNoMessage = TaskResult.builder().success(false).build();
        when(executeCodeUseCase.execute(any())).thenReturn(failureWithNoMessage);

        // WHEN
        String response = penpotToolExecutor.execute(SAMPLE_CODE, "failing operation", result -> "should-not-reach");

        // THEN
        assertThat(response).contains("Unknown error");
    }

    @Test
    void shouldReturnErrorResponseWhenExecuteCodeUseCaseThrowsException() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenThrow(new RuntimeException("Unexpected crash"));

        // WHEN
        String response = penpotToolExecutor.execute(SAMPLE_CODE, "crashing operation", result -> "should-not-reach");

        // THEN
        assertThat(response).contains("Unexpected crash");
    }

    @Test
    void shouldForwardExactCodeToExecuteCodeUseCase() {
        // GIVEN
        String specificCode = "penpot.getPage().findById('abc')";
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(null));
        ArgumentCaptor<ExecuteCodeCommand> commandCaptor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

        // WHEN
        penpotToolExecutor.execute(specificCode, "op", result -> "ok");

        // THEN
        verify(executeCodeUseCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().getCode()).isEqualTo(specificCode);
    }

    @Test
    void shouldReturnShapeCreatedResponseWithIdWhenCreatingShapeSucceeds() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(Map.of("result", SHAPE_ID)));

        // WHEN
        String response = penpotToolExecutor.createShape(SAMPLE_CODE, "rectangle");

        // THEN
        assertThat(response).contains(SHAPE_ID);
        assertThat(response).contains("rectangle");
    }

    @Test
    void shouldExtractIdFromResultMapWhenCreatingShape() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(Map.of("result", "nested-id-abc")));

        // WHEN
        String response = penpotToolExecutor.createShape(SAMPLE_CODE, "ellipse");

        // THEN
        assertThat(response).contains("nested-id-abc");
    }

    @Test
    void shouldFallbackToStringRepresentationWhenResultDataIsNotAMap() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success("direct-string-id"));

        // WHEN
        String response = penpotToolExecutor.createShape(SAMPLE_CODE, "board");

        // THEN
        assertThat(response).contains("direct-string-id");
    }

    @Test
    void shouldReturnUnknownIdWhenResultDataIsAbsentDuringShapeCreation() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(null));

        // WHEN
        String response = penpotToolExecutor.createShape(SAMPLE_CODE, "path");

        // THEN
        assertThat(response).contains("unknown");
    }

    @Test
    void shouldFallbackToMapToStringWhenResultMapDoesNotContainResultKey() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(Map.of("otherKey", "ignored")));

        // WHEN
        String response = penpotToolExecutor.createShape(SAMPLE_CODE, "frame");

        // THEN
        assertThat(response).contains("otherKey=ignored");
    }

    @Test
    void shouldReturnErrorResponseWhenCreatingShapeFails() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.failure("Canvas not found"));

        // WHEN
        String response = penpotToolExecutor.createShape(SAMPLE_CODE, "rectangle");

        // THEN
        assertThat(response).contains("Canvas not found");
    }

    @Test
    void shouldReturnTransformedShapeResponseWithActualIdWhenTransformSucceeds() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(Map.of("id", SHAPE_ID)));

        // WHEN
        String response = penpotToolExecutor.transformShape(SAMPLE_CODE, "rotated", "fallback-id");

        // THEN
        assertThat(response).contains(SHAPE_ID);
        assertThat(response).contains("rotated");
    }

    @Test
    void shouldUseFallbackIdWhenResultDataDoesNotContainIdKeyDuringTransform() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(Map.of("otherKey", "other-value")));

        // WHEN
        String response = penpotToolExecutor.transformShape(SAMPLE_CODE, "scaled", "expected-shape-id");

        // THEN
        assertThat(response).contains("expected-shape-id");
    }

    @Test
    void shouldUseFallbackIdWhenResultDataIsNotAMapDuringTransform() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success("not-a-map"));

        // WHEN
        String response = penpotToolExecutor.transformShape(SAMPLE_CODE, "moved", "my-fallback-id");

        // THEN
        assertThat(response).contains("my-fallback-id");
    }

    @Test
    void shouldReturnErrorResponseWhenTransformShapeFails() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.failure("Shape not found"));

        // WHEN
        String response = penpotToolExecutor.transformShape(SAMPLE_CODE, "resized", SHAPE_ID);

        // THEN
        assertThat(response).contains("Shape not found");
    }

    @Test
    void shouldReturnStyleAppliedResponseWithShapeIdAndDetailsWhenApplyStyleSucceeds() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(null));

        // WHEN
        String response = penpotToolExecutor.applyStyle(SAMPLE_CODE, "fillApplied", SHAPE_ID, "color: #FF0000");

        // THEN
        assertThat(response).contains(SHAPE_ID);
        assertThat(response).contains("fillApplied");
    }

    @Test
    void shouldReturnErrorResponseWhenApplyStyleFails() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.failure("Invalid color format"));

        // WHEN
        String response = penpotToolExecutor.applyStyle(SAMPLE_CODE, "gradientApplied", SHAPE_ID, "gradient details");

        // THEN
        assertThat(response).contains("Invalid color format");
    }

    @Test
    void shouldReturnGroupCreatedResponseWithGroupIdWhenGroupCreationSucceeds() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(Map.of("groupId", GROUP_ID)));

        // WHEN
        String response = penpotToolExecutor.createGroup(SAMPLE_CODE);

        // THEN
        assertThat(response).contains(GROUP_ID);
    }

    @Test
    void shouldReturnUnknownGroupIdWhenResultDataDoesNotContainGroupIdKey() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(Map.of("otherId", "some-id")));

        // WHEN
        String response = penpotToolExecutor.createGroup(SAMPLE_CODE);

        // THEN
        assertThat(response).contains("unknown");
    }

    @Test
    void shouldReturnErrorResponseWhenGroupCreationFails() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.failure("No shapes selected"));

        // WHEN
        String response = penpotToolExecutor.createGroup(SAMPLE_CODE);

        // THEN
        assertThat(response).contains("No shapes selected");
    }

    @Test
    void shouldReturnShapeClonedResponseWithCloneIdWhenCloningSucceeds() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(Map.of("cloneId", CLONE_ID)));

        // WHEN
        String response = penpotToolExecutor.cloneShape(SAMPLE_CODE);

        // THEN
        assertThat(response).contains(CLONE_ID);
    }

    @Test
    void shouldReturnUnknownCloneIdWhenResultDataDoesNotContainCloneIdKey() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(Map.of("wrongKey", "value")));

        // WHEN
        String response = penpotToolExecutor.cloneShape(SAMPLE_CODE);

        // THEN
        assertThat(response).contains("unknown");
    }

    @Test
    void shouldReturnErrorResponseWhenCloningShapeFails() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.failure("Source shape not found"));

        // WHEN
        String response = penpotToolExecutor.cloneShape(SAMPLE_CODE);

        // THEN
        assertThat(response).contains("Source shape not found");
    }

    @Test
    void shouldReturnSuccessResponseWithDataStringWhenDeleteSucceeds() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success("deleted-ok"));

        // WHEN
        String response = penpotToolExecutor.executeDelete(SAMPLE_CODE, "deleteShape");

        // THEN
        assertThat(response).contains("deleted-ok");
    }

    @Test
    void shouldReturnOperationCompletedFallbackWhenDeleteResultDataIsAbsent() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(null));

        // WHEN
        String response = penpotToolExecutor.executeDelete(SAMPLE_CODE, "deleteLayer");

        // THEN
        assertThat(response).contains("Operation completed");
    }

    @Test
    void shouldReturnErrorResponseWhenDeleteFails() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.failure("Shape is locked"));

        // WHEN
        String response = penpotToolExecutor.executeDelete(SAMPLE_CODE, "deleteShape");

        // THEN
        assertThat(response).contains("Shape is locked");
    }

    @Test
    void shouldReturnContentCreatedResponseWithIdWhenCreatingContentSucceeds() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success("text-shape-id-999"));

        // WHEN
        String response = penpotToolExecutor.createContent(SAMPLE_CODE, "title");

        // THEN
        assertThat(response).contains("text-shape-id-999");
        assertThat(response).contains("title");
    }

    @Test
    void shouldReturnUnknownIdWhenCreateContentResultDataIsAbsent() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.success(null));

        // WHEN
        String response = penpotToolExecutor.createContent(SAMPLE_CODE, "paragraph");

        // THEN
        assertThat(response).contains("unknown");
    }

    @Test
    void shouldReturnErrorResponseWhenCreatingContentFails() {
        // GIVEN
        when(executeCodeUseCase.execute(any())).thenReturn(TaskResult.failure("Text layer creation failed"));

        // WHEN
        String response = penpotToolExecutor.createContent(SAMPLE_CODE, "subtitle");

        // THEN
        assertThat(response).contains("Text layer creation failed");
    }
}