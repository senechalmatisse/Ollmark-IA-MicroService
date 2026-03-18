package com.penpot.ai.application.tools;

import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenpotDeleteToolsUnit {

    @Mock
    private PenpotToolExecutor toolExecutor;

    @InjectMocks
    private PenpotDeleteTools penpotDeleteTools;

    @Test
    void shouldDelegateToExecutorWithDeleteSelectionLabelWhenDeletingSelection() {
        // GIVEN
        when(toolExecutor.execute(anyString(), eq("delete selection"), any()))
            .thenReturn("{\"success\": true, \"message\": \"Deleted 2 items\"}");

        // WHEN
        penpotDeleteTools.deleteSelection();

        // THEN
        verify(toolExecutor).execute(anyString(), eq("delete selection"), any());
    }

    @Test
    void shouldReturnSuccessJsonWhenDeletingSelectionSucceeds() {
        // GIVEN
        when(toolExecutor.execute(anyString(), anyString(), any()))
            .thenReturn("{\"success\": true, \"message\": \"Deleted 2 items\"}");

        // WHEN
        String response = penpotDeleteTools.deleteSelection();

        // THEN
        assertThat(response).contains("\"success\": true");
    }

    @Test
    void shouldPassCodeContainingPenpotSelectionAccessWhenDeletingSelection() {
        // GIVEN
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotDeleteTools.deleteSelection();

        // THEN
        verify(toolExecutor).execute(codeCaptor.capture(), anyString(), any());
        assertThat(codeCaptor.getValue()).contains("penpot.selection");
        assertThat(codeCaptor.getValue()).contains("shape.remove()");
    }

    @Test
    void shouldPassCodeReturningErrPrefixWhenSelectionIsEmptyDuringDeleteSelection() {
        // GIVEN
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotDeleteTools.deleteSelection();

        // THEN
        verify(toolExecutor).execute(codeCaptor.capture(), anyString(), any());
        assertThat(codeCaptor.getValue()).contains("ERR:No items selected");
    }

    @Test
    void shouldPassCodeReturningOkPrefixWhenItemsAreDeletedDuringDeleteSelection() {
        // GIVEN
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotDeleteTools.deleteSelection();

        // THEN
        verify(toolExecutor).execute(codeCaptor.capture(), anyString(), any());
        assertThat(codeCaptor.getValue()).contains("OK:Deleted");
    }

    @Test
    void shouldPassShapeIdInJsCodeWhenDeletingShapeById() {
        // GIVEN
        String shapeId = "11111111-1111-1111-1111-111111111111";
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotDeleteTools.deleteShapeById(shapeId);

        // THEN
        verify(toolExecutor).execute(codeCaptor.capture(), anyString(), any());
        assertThat(codeCaptor.getValue()).contains("11111111-1111-1111-1111-111111111111");
    }

    @Test
    void shouldIncludeShapeIdInOperationLabelWhenDeletingShapeById() {
        // GIVEN
        String shapeId = "11111111-1111-1111-1111-111111111111";
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");
        ArgumentCaptor<String> labelCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotDeleteTools.deleteShapeById(shapeId);

        // THEN
        verify(toolExecutor).execute(anyString(), labelCaptor.capture(), any());
        assertThat(labelCaptor.getValue()).contains("11111111-1111-1111-1111-111111111111");
    }

    @Test
    void shouldPassCodeReturningErrPrefixWhenShapeNotFoundDuringDeleteById() {
        // GIVEN
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotDeleteTools.deleteShapeById("11111111-1111-1111-1111-111111111111");

        // THEN
        verify(toolExecutor).execute(codeCaptor.capture(), anyString(), any());
        assertThat(codeCaptor.getValue()).contains("ERR:Shape not found");
    }

    @Test
    void shouldPassCodeReturningOkPrefixWhenShapeIsFoundDuringDeleteById() {
        // GIVEN
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotDeleteTools.deleteShapeById("11111111-1111-1111-1111-111111111111");

        // THEN
        verify(toolExecutor).execute(codeCaptor.capture(), anyString(), any());
        assertThat(codeCaptor.getValue()).contains("OK:Shape deleted");
    }

    @Test
    void shouldDelegateToExecutorWithCorrectLabelWhenDeletingManualShapesOnBoard() {
        // GIVEN
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");

        // WHEN
        penpotDeleteTools.deleteManualShapesOnBoard();

        // THEN
        verify(toolExecutor).execute(anyString(), eq("delete manual shapes on board"), any());
    }

    @Test
    void shouldPassCodeFilteringByComponentIdAbsenceWhenDeletingManualShapesOnBoard() {
        // GIVEN
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotDeleteTools.deleteManualShapesOnBoard();

        // THEN
        verify(toolExecutor).execute(codeCaptor.capture(), anyString(), any());
        assertThat(codeCaptor.getValue()).contains("componentId");
        assertThat(codeCaptor.getValue()).contains("isManual");
    }

    @Test
    void shouldPassCodeReturningErrPrefixWhenNoManualShapesFoundOnBoard() {
        // GIVEN
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotDeleteTools.deleteManualShapesOnBoard();

        // THEN
        verify(toolExecutor).execute(codeCaptor.capture(), anyString(), any());
        assertThat(codeCaptor.getValue()).contains("ERR:");
    }

    @Test
    void shouldDelegateToExecutorWithCorrectLabelWhenDeletingAllManualShapesFromBoard() {
        // GIVEN
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");

        // WHEN
        penpotDeleteTools.deleteAllManualShapesFromBoard();

        // THEN
        verify(toolExecutor).execute(anyString(), eq("delete all manual shapes"), any());
    }

    @Test
    void shouldPassCodeWithRecursiveChildrenDeletionWhenDeletingAllManualShapesFromBoard() {
        // GIVEN
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotDeleteTools.deleteAllManualShapesFromBoard();

        // THEN
        verify(toolExecutor).execute(codeCaptor.capture(), anyString(), any());
        assertThat(codeCaptor.getValue()).contains("penpot.currentPage");
        assertThat(codeCaptor.getValue()).contains("children");
        assertThat(codeCaptor.getValue()).contains("deleteManualShapes");
    }

    @Test
    void shouldPassCodeReturningErrPrefixWhenPageIsAbsentDuringDeleteAllManualShapes() {
        // GIVEN
        when(toolExecutor.execute(anyString(), anyString(), any())).thenReturn("ok");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotDeleteTools.deleteAllManualShapesFromBoard();

        // THEN
        verify(toolExecutor).execute(codeCaptor.capture(), anyString(), any());
        assertThat(codeCaptor.getValue()).contains("ERR:No active page found");
    }
}