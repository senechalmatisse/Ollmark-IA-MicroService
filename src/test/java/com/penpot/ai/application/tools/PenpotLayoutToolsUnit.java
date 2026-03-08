package com.penpot.ai.application.tools;

import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import org.junit.jupiter.api.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class PenpotLayoutToolsUnit {

    private PenpotToolExecutor executor;
    private PenpotLayoutTools tools;

    @BeforeEach
    void init() {
        executor = mock(PenpotToolExecutor.class);
        tools = new PenpotLayoutTools(executor);
    }

    // =========================
    // ALIGN
    // =========================

    @Test
    void alignRejectsInvalidAlignment() {
        String result = tools.alignShapes("id1,id2", "diagonal");
        assertTrue(result.contains("Invalid alignment"));
        verifyNoInteractions(executor);
    }

    @Test
    void alignGeneratesCenterLogic() {
        when(executor.execute(anyString(), eq("aligned"), any())).thenReturn("OK");

        tools.alignShapes("id1,id2", "center");

        verify(executor).execute(
                argThat(code ->
                        code.contains("Need at least 2 shapes")
                                && code.contains("const c =")
                                && code.contains("s.width / 2")
                                && code.contains("shapes.forEach")
                                && code.contains("s.x = c - s.width / 2")
                ),
                eq("aligned"),
                any()
        );
    }

    // =========================
    // DISTRIBUTE
    // =========================

    @Test
    void distributeHorizontalLogic() {
        when(executor.execute(anyString(), eq("distributed"), any())).thenReturn("OK");

        tools.distributeShapes("a,b,c", "horizontal");

        verify(executor).execute(
                argThat(code ->
                        code.contains("Need at least 3 shapes")
                                && code.contains("shapes.sort((a, b) => a.x - b.x)")
                                && code.contains("const gap =")
                                && code.contains("let cur = first.x")
                                && code.contains("s.x = cur")
                                && code.contains("cur += s.width + gap")
                ),
                eq("distributed"),
                any()
        );
    }

    @Test
    void distributeVerticalLogic() {
        when(executor.execute(anyString(), eq("distributed"), any())).thenReturn("OK");

        tools.distributeShapes("a,b,c", "vertical");

        verify(executor).execute(
                argThat(code ->
                        code.contains("Need at least 3 shapes")
                                && code.contains("shapes.sort((a, b) => a.y - b.y)")
                                && code.contains("const gap =")
                                && code.contains("let cur = first.y")
                                && code.contains("s.y = cur")
                                && code.contains("cur += s.height + gap")
                ),
                eq("distributed"),
                any()
        );
    }

    @Test
    void distributeRejectsInvalidAxis() {
        String result = tools.distributeShapes("selection", "diagonal");
        assertTrue(result.contains("Invalid axis"));
        verifyNoInteractions(executor);
    }

    // =========================
    // GROUP
    // =========================

    @Test
    void groupAddsGroupNameWhenProvided() {
        when(executor.execute(anyString(), eq("group"), any())).thenReturn("OK");

        tools.groupShapes("id1,id2", "TeamA");

        verify(executor).execute(
                argThat(code -> code.contains("group.name='TeamA'")),
                eq("group"),
                any()
        );
    }

    @Test
    void groupWithoutNameDoesNotAddNameJs() {
        when(executor.execute(anyString(), eq("group"), any())).thenReturn("OK");

        tools.groupShapes("id1,id2", "");

        verify(executor).execute(
                argThat(code -> !code.contains("group.name=")),
                eq("group"),
                any()
        );
    }

    // =========================
    // UNGROUP
    // =========================

    @Test
    void ungroupGeneratesFilterLogic() {
        when(executor.execute(anyString(), eq("ungrouped"), any())).thenReturn("OK");

        tools.ungroupShapes("g1,g2");

        verify(executor).execute(
                argThat(code ->
                        code.contains("const groups = shapes.filter")
                                && code.contains("groups.length")
                                && code.contains("penpot.ungroup(groups)")
                ),
                eq("ungrouped"),
                any()
        );
    }

    // =========================
    // Z-ORDER
    // =========================

    @Test
    void sendBackwardCallsCorrectMethod() {
        when(executor.execute(anyString(), eq("sent backward"), any())).thenReturn("OK");

        tools.sendShapeBackward("selection");

        verify(executor).execute(
                argThat(code -> code.contains("sendBackward")),
                eq("sent backward"),
                any()
        );
    }

    @Test
    void sendToFrontCallsCorrectMethod() {
        when(executor.execute(anyString(), eq("brought to front"), any())).thenReturn("OK");

        tools.sendShapeToTheFront("selection");

        verify(executor).execute(
                argThat(code -> code.contains("bringToFront")),
                eq("brought to front"),
                any()
        );
    }

    // =========================
    // MOVE TO BOARD
    // =========================

    @Test
    void moveShapeToBoardInjectsBoardId() {
        when(executor.execute(anyString(), eq("moved to board"), any())).thenReturn("OK");

        tools.addShapeToBoard("id1,id2", "board-123");

        verify(executor).execute(
                argThat(code ->
                        code.contains("getShapeById('board-123')")
                                && code.contains("Board not found")
                ),
                eq("moved to board"),
                any()
        );
    }

    // =========================
    // REMOVE
    // =========================

    @Test
    void removeGeneratesRemoveLogic() {
        when(executor.execute(anyString(), eq("removed"), any())).thenReturn("OK");

        tools.removeShapeFromParent("id1");

        verify(executor).execute(
                argThat(code -> code.contains("remove()")),
                eq("removed"),
                any()
        );
    }

    // =========================
    // CLONE
    // =========================

    @Test
    void cloneUsesDefaultOffsetsWhenNull() {
        when(executor.execute(anyString(), eq("clone"), any())).thenReturn("OK");

        tools.cloneShape("id1", null, null);

        verify(executor).execute(
                argThat(code ->
                        code.contains("clone.x = shape.x + 20")
                                && code.contains("clone.y = shape.y + 20")
                ),
                eq("clone"),
                any()
        );
    }

    @Test
    void cloneUsesCustomOffsets() {
        when(executor.execute(anyString(), eq("clone"), any())).thenReturn("OK");

        tools.cloneShape("id1", 45, 60);

        verify(executor).execute(
                argThat(code ->
                        code.contains("clone.x = shape.x + 45")
                                && code.contains("clone.y = shape.y + 60")
                ),
                eq("clone"),
                any()
        );
    }
}