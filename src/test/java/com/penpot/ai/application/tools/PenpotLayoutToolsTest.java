package com.penpot.ai.application.tools;

import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import com.penpot.ai.core.domain.ExecuteCodeCommand;
import com.penpot.ai.core.domain.TaskResult;
import com.penpot.ai.core.ports.in.ExecuteCodeUseCase;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for {@link PenpotLayoutTools}.
 *
 * <p>Loads a limited Spring Boot context ({@link PenpotLayoutTools} + {@link PenpotToolExecutor})
 * and mocks {@link ExecuteCodeUseCase} to verify:
 * <ul>
 *   <li>the JS code generated and sent to the executor,</li>
 *   <li>the string output returned to the caller.</li>
 * </ul>
 * No real JavaScript is executed.</p>
 */
@SpringBootTest(classes = {PenpotLayoutTools.class, PenpotToolExecutor.class})
@ActiveProfiles("test")
@DisplayName("PenpotLayoutTools — Integration")
class PenpotLayoutToolsTest {

    @MockitoBean
    private ExecuteCodeUseCase executeCodeUseCase;

    @Autowired
    private PenpotLayoutTools penpotLayoutTools;

    private static final String SHAPE_ID_1 = "shape-layout-001";
    private static final String SHAPE_ID_2 = "shape-layout-002";
    private static final String SHAPE_ID_3 = "shape-layout-003";
    private static final String BOARD_ID   = "board-layout-001";
    private static final String GROUP_ID   = "group-layout-001";
    private static final String CLONE_ID   = "clone-layout-001";

    @BeforeEach
    void resetMocks() {
        reset(executeCodeUseCase);
    }

    // =========================================================================
    // addShapeToBoard
    // =========================================================================

    @Nested
    @DisplayName("addShapeToBoard")
    class AddShapeToBoardTests {

        @Test
        @DisplayName("addShapeToBoard — returns success JSON with IDs when execution succeeds")
        void addShapeToBoard_returnsSuccessJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:moved_to_board:2:" + SHAPE_ID_1 + "," + SHAPE_ID_2));

            // WHEN
            String result = penpotLayoutTools.addShapeToBoard(
                    SHAPE_ID_1 + "," + SHAPE_ID_2, BOARD_ID);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("moved to board");
            assertThat(result).contains(SHAPE_ID_1);
            assertThat(result).contains(SHAPE_ID_2);
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("addShapeToBoard — returns error JSON when execution fails")
        void addShapeToBoard_returnsErrorJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Board not found"));

            // WHEN
            String result = penpotLayoutTools.addShapeToBoard(SHAPE_ID_1, BOARD_ID);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Board not found");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("addShapeToBoard — generated JS injects boardId and uses appendChild logic")
        void addShapeToBoard_generatesCorrectJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:moved_to_board:1:" + SHAPE_ID_1));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotLayoutTools.addShapeToBoard(SHAPE_ID_1, BOARD_ID);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("getShapeById('" + BOARD_ID + "')")
                    .contains("Board not found")
                    .contains("appendChild")
                    .contains("OK_MULTISHAPE:moved_to_board");
        }
    }

    // =========================================================================
    // removeShapeFromParent
    // =========================================================================

    @Nested
    @DisplayName("removeShapeFromParent")
    class RemoveShapeFromParentTests {

        @Test
        @DisplayName("removeShapeFromParent — returns success JSON with ID when execution succeeds")
        void removeShapeFromParent_returnsSuccessJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:removed:1:" + SHAPE_ID_1));

            // WHEN
            String result = penpotLayoutTools.removeShapeFromParent(SHAPE_ID_1);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("removed");
            assertThat(result).contains(SHAPE_ID_1);
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("removeShapeFromParent — returns error JSON when execution fails")
        void removeShapeFromParent_returnsErrorJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Shape not found"));

            // WHEN
            String result = penpotLayoutTools.removeShapeFromParent(SHAPE_ID_1);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Shape not found");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("removeShapeFromParent — generated JS calls s.remove() on each shape")
        void removeShapeFromParent_generatesCorrectJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:removed:1:" + SHAPE_ID_1));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotLayoutTools.removeShapeFromParent(SHAPE_ID_1);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("s.remove()")
                    .contains("OK_MULTISHAPE:removed");
        }
    }

    // =========================================================================
    // cloneShape
    // =========================================================================

    @Nested
    @DisplayName("cloneShape")
    class CloneShapeTests {

        @Test
        @DisplayName("cloneShape — returns success JSON containing clone ID when execution succeeds")
        void cloneShape_returnsSuccessJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("OK_CLONE:" + CLONE_ID));

            // WHEN
            String result = penpotLayoutTools.cloneShape(SHAPE_ID_1, null, null);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains(CLONE_ID);
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("cloneShape — returns error JSON when execution fails")
        void cloneShape_returnsErrorJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Clone failed"));

            // WHEN
            String result = penpotLayoutTools.cloneShape(SHAPE_ID_1, null, null);

            // THEN
            assertThat(result).contains("\"success\": false");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("cloneShape — generated JS uses default offset (20, 20) when offsets are null")
        void cloneShape_usesDefaultOffset() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("OK_CLONE:" + CLONE_ID));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotLayoutTools.cloneShape(SHAPE_ID_1, null, null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("shape.clone()")
                    .contains("shape.x + 20")
                    .contains("shape.y + 20");
        }

        @Test
        @DisplayName("cloneShape — generated JS uses custom offset when provided")
        void cloneShape_usesCustomOffset() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("OK_CLONE:" + CLONE_ID));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotLayoutTools.cloneShape(SHAPE_ID_1, 50, -10);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("shape.x + 50")
                    .contains("shape.y + -10");
        }
    }

    // =========================================================================
    // alignShapes
    // =========================================================================

    @Nested
    @DisplayName("alignShapes")
    class AlignShapesTests {

        @Test
        @DisplayName("alignShapes — returns error JSON for invalid alignment without calling executor")
        void alignShapes_invalidAlignment_returnsError() {
            // GIVEN — no mock needed: validation is done before executor is called

            // WHEN
            String result = penpotLayoutTools.alignShapes(
                    SHAPE_ID_1 + "," + SHAPE_ID_2, "diagonal");

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Invalid alignment");
            verifyNoInteractions(executeCodeUseCase);
        }

        @Test
        @DisplayName("alignShapes — returns success JSON for 'left' alignment")
        void alignShapes_left_returnsSuccessJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:aligned:2:" + SHAPE_ID_1 + "," + SHAPE_ID_2));

            // WHEN
            String result = penpotLayoutTools.alignShapes(
                    SHAPE_ID_1 + "," + SHAPE_ID_2, "left");

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("aligned");
            assertThat(result).contains(SHAPE_ID_1);
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("alignShapes — French alias 'gauche' is normalized to 'left' and executes correctly")
        void alignShapes_frenchAlias_gauche_normalizedToLeft() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:aligned:2:" + SHAPE_ID_1 + "," + SHAPE_ID_2));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            String result = penpotLayoutTools.alignShapes(
                    SHAPE_ID_1 + "," + SHAPE_ID_2, "gauche");

            // THEN
            assertThat(result).contains("\"success\": true");
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode())
                    .contains("Math.min(...shapes.map(s => s.x))");
        }

        @Test
        @DisplayName("alignShapes — 'center' alignment generates JS with width/2 centering logic")
        void alignShapes_center_generatesCorrectJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:aligned:2:" + SHAPE_ID_1 + "," + SHAPE_ID_2));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotLayoutTools.alignShapes(SHAPE_ID_1 + "," + SHAPE_ID_2, "center");

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("s.width / 2")
                    .contains("s.x = c - s.width / 2");
        }

        @Test
        @DisplayName("alignShapes — 'bottom' alignment generates JS with s.y + s.height logic")
        void alignShapes_bottom_generatesCorrectJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:aligned:2:" + SHAPE_ID_1 + "," + SHAPE_ID_2));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotLayoutTools.alignShapes(SHAPE_ID_1 + "," + SHAPE_ID_2, "bottom");

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("s.y + s.height")
                    .contains("s.y = b - s.height");
        }

        @Test
        @DisplayName("alignShapes — returns error JSON when executor fails")
        void alignShapes_returnsErrorJson_onExecutionFailure() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Execution error"));

            // WHEN
            String result = penpotLayoutTools.alignShapes(
                    SHAPE_ID_1 + "," + SHAPE_ID_2, "top");

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Execution error");
        }
    }

    // =========================================================================
    // distributeShapes
    // =========================================================================

    @Nested
    @DisplayName("distributeShapes")
    class DistributeShapesTests {

        @Test
        @DisplayName("distributeShapes — returns error JSON for invalid axis without calling executor")
        void distributeShapes_invalidAxis_returnsError() {
            // GIVEN — no mock needed: validation is done before executor is called

            // WHEN
            String result = penpotLayoutTools.distributeShapes(
                    SHAPE_ID_1 + "," + SHAPE_ID_2 + "," + SHAPE_ID_3, "diagonal");

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Invalid axis");
            verifyNoInteractions(executeCodeUseCase);
        }

        @Test
        @DisplayName("distributeShapes — returns success JSON for 'horizontal' distribution")
        void distributeShapes_horizontal_returnsSuccessJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:distributed:3:"
                                    + SHAPE_ID_1 + "," + SHAPE_ID_2 + "," + SHAPE_ID_3));

            // WHEN
            String result = penpotLayoutTools.distributeShapes(
                    SHAPE_ID_1 + "," + SHAPE_ID_2 + "," + SHAPE_ID_3, "horizontal");

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("distributed");
            assertThat(result).contains(SHAPE_ID_1);
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("distributeShapes — horizontal generates JS sorted by X with cur/gap logic")
        void distributeShapes_horizontal_generatesCorrectJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:distributed:3:"
                                    + SHAPE_ID_1 + "," + SHAPE_ID_2 + "," + SHAPE_ID_3));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotLayoutTools.distributeShapes(
                    SHAPE_ID_1 + "," + SHAPE_ID_2 + "," + SHAPE_ID_3, "horizontal");

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("shapes.sort((a, b) => a.x - b.x)")
                    .contains("let cur = first.x")
                    .contains("s.x = cur")
                    .contains("cur += s.width + gap");
        }

        @Test
        @DisplayName("distributeShapes — vertical generates JS sorted by Y with cur/gap logic")
        void distributeShapes_vertical_generatesCorrectJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:distributed:3:"
                                    + SHAPE_ID_1 + "," + SHAPE_ID_2 + "," + SHAPE_ID_3));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotLayoutTools.distributeShapes(
                    SHAPE_ID_1 + "," + SHAPE_ID_2 + "," + SHAPE_ID_3, "vertical");

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("shapes.sort((a, b) => a.y - b.y)")
                    .contains("let cur = first.y")
                    .contains("s.y = cur")
                    .contains("cur += s.height + gap");
        }

        @Test
        @DisplayName("distributeShapes — returns error JSON when executor fails")
        void distributeShapes_returnsErrorJson_onExecutionFailure() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Distribute failed"));

            // WHEN
            String result = penpotLayoutTools.distributeShapes(
                    SHAPE_ID_1 + "," + SHAPE_ID_2 + "," + SHAPE_ID_3, "vertical");

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Distribute failed");
        }
    }

    // =========================================================================
    // groupShapes
    // =========================================================================

    @Nested
    @DisplayName("groupShapes")
    class GroupShapesTests {

        @Test
        @DisplayName("groupShapes — returns success JSON with group ID when execution succeeds")
        void groupShapes_returnsSuccessJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("OK_GROUP:" + GROUP_ID + ":2"));

            // WHEN
            String result = penpotLayoutTools.groupShapes(
                    SHAPE_ID_1 + "," + SHAPE_ID_2, null);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains(GROUP_ID);
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("groupShapes — generated JS includes group.name when name is provided")
        void groupShapes_withName_generatesGroupNameJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("OK_GROUP:" + GROUP_ID + ":2"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotLayoutTools.groupShapes(SHAPE_ID_1 + "," + SHAPE_ID_2, "MyGroup");

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("penpot.group(shapes)")
                    .contains("group.name='MyGroup'");
        }

        @Test
        @DisplayName("groupShapes — generated JS does not include group.name when name is blank")
        void groupShapes_withBlankName_omitsGroupNameJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("OK_GROUP:" + GROUP_ID + ":2"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotLayoutTools.groupShapes(SHAPE_ID_1 + "," + SHAPE_ID_2, "");

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("penpot.group(shapes)")
                    .doesNotContain("group.name=");
        }

        @Test
        @DisplayName("groupShapes — returns error JSON when execution fails")
        void groupShapes_returnsErrorJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Group failed"));

            // WHEN
            String result = penpotLayoutTools.groupShapes(
                    SHAPE_ID_1 + "," + SHAPE_ID_2, null);

            // THEN
            assertThat(result).contains("\"success\": false");
            verify(executeCodeUseCase, times(1)).execute(any());
        }
    }

    // =========================================================================
    // ungroupShapes
    // =========================================================================

    @Nested
    @DisplayName("ungroupShapes")
    class UngroupShapesTests {

        @Test
        @DisplayName("ungroupShapes — returns success JSON with ungrouped ID when execution succeeds")
        void ungroupShapes_returnsSuccessJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("OK_MULTISHAPE:ungrouped:1:" + GROUP_ID));

            // WHEN
            String result = penpotLayoutTools.ungroupShapes(GROUP_ID);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("ungrouped");
            assertThat(result).contains(GROUP_ID);
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("ungroupShapes — generated JS filters groups and calls penpot.ungroup")
        void ungroupShapes_generatesCorrectJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("OK_MULTISHAPE:ungrouped:1:" + GROUP_ID));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotLayoutTools.ungroupShapes(GROUP_ID);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("const groups = shapes.filter")
                    .contains("penpot.ungroup(groups)")
                    .contains("No groups found");
        }

        @Test
        @DisplayName("ungroupShapes — returns error JSON when execution fails")
        void ungroupShapes_returnsErrorJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Ungroup failed"));

            // WHEN
            String result = penpotLayoutTools.ungroupShapes(GROUP_ID);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Ungroup failed");
        }
    }

    // =========================================================================
    // Z-Order tools
    // =========================================================================

    @Nested
    @DisplayName("Z-Order tools")
    class ZOrderTests {

        @Test
        @DisplayName("sendShapeBackward — returns success JSON and JS contains 'sendBackward' mode")
        void sendShapeBackward_returnsSuccessAndJsContainsMode() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:sent backward:1:" + SHAPE_ID_1));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            String result = penpotLayoutTools.sendShapeBackward(SHAPE_ID_1);

            // THEN
            assertThat(result).contains("\"success\": true");
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("sendBackward");
        }

        @Test
        @DisplayName("sendShapeFrontward — returns success JSON and JS contains 'bringForward' mode")
        void sendShapeFrontward_returnsSuccessAndJsContainsMode() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:brought forward:1:" + SHAPE_ID_1));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            String result = penpotLayoutTools.sendShapeFrontward(SHAPE_ID_1);

            // THEN
            assertThat(result).contains("\"success\": true");
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("bringForward");
        }

        @Test
        @DisplayName("sendShapeToTheBack — returns success JSON and JS contains 'sendToBack' mode")
        void sendShapeToTheBack_returnsSuccessAndJsContainsMode() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:sent to back:1:" + SHAPE_ID_1));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            String result = penpotLayoutTools.sendShapeToTheBack(SHAPE_ID_1);

            // THEN
            assertThat(result).contains("\"success\": true");
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("sendToBack");
        }

        @Test
        @DisplayName("sendShapeToTheFront — returns success JSON and JS contains 'bringToFront' mode")
        void sendShapeToTheFront_returnsSuccessAndJsContainsMode() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:brought to front:1:" + SHAPE_ID_1));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            String result = penpotLayoutTools.sendShapeToTheFront(SHAPE_ID_1);

            // THEN
            assertThat(result).contains("\"success\": true");
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("bringToFront");
        }

        @Test
        @DisplayName("sendShapeToTheBack — returns error JSON when execution fails")
        void sendShapeToTheBack_returnsErrorJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Z-order failed"));

            // WHEN
            String result = penpotLayoutTools.sendShapeToTheBack(SHAPE_ID_1);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Z-order failed");
        }

        @Test
        @DisplayName("Z-order — generated JS contains forceRedraw, byParent grouping, and setParentIndex")
        void zOrder_generatesRobustReorderingJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "OK_MULTISHAPE:sent to back:1:" + SHAPE_ID_1));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotLayoutTools.sendShapeToTheBack(SHAPE_ID_1);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("forceRedraw")
                    .contains("byParent")
                    .contains("setParentIndex")
                    .contains("No shapes to reorder");
        }
    }
}