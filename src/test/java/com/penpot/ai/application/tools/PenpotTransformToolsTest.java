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
 * Integration tests for {@link PenpotTransformTools}.
 *
 * <p>Loads a limited Spring Boot context ({@link PenpotTransformTools} + {@link PenpotToolExecutor})
 * and mocks {@link ExecuteCodeUseCase} to verify:
 * <ul>
 *   <li>the JS code generated and forwarded to the executor,</li>
 *   <li>the JSON string returned to the caller (success / failure).</li>
 * </ul>
 * No real JavaScript is executed.</p>
 */
@SpringBootTest(classes = {PenpotTransformTools.class, PenpotToolExecutor.class})
@ActiveProfiles("test")
@DisplayName("PenpotTransformTools — Integration")
class PenpotTransformToolsTest {

    @MockitoBean
    private ExecuteCodeUseCase executeCodeUseCase;

    @Autowired
    private PenpotTransformTools penpotTransformTools;

    private static final String SHAPE_ID = "shape-transform-001";

    @BeforeEach
    void resetMocks() {
        reset(executeCodeUseCase);
    }

    // =========================================================================
    // rotateShape
    // =========================================================================

    @Nested
    @DisplayName("rotateShape")
    class RotateShapeTests {

        @Test
        @DisplayName("rotateShape — returns success JSON when execution succeeds")
        void rotateShape_returnsSuccessJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{\"id\": \"" + SHAPE_ID + "\", \"rotation\": 45}"));

            // WHEN
            String result = penpotTransformTools.rotateShape(SHAPE_ID, 45);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("rotated");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("rotateShape — returns error JSON when execution fails")
        void rotateShape_returnsErrorJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Penpot connection timeout"));

            // WHEN
            String result = penpotTransformTools.rotateShape(SHAPE_ID, 45);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Penpot connection timeout");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("rotateShape — generated JS contains rotation increment and getShapeById")
        void rotateShape_generatesCorrectJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.rotateShape(SHAPE_ID, 45);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("getShapeById('" + SHAPE_ID + "')")
                    .contains("shape.rotation = (shape.rotation || 0) + 45;")
                    .contains("return { id: shape.id, rotation: shape.rotation };");
        }

        @Test
        @DisplayName("rotateShape — generated JS contains negative angle correctly")
        void rotateShape_negativeAngle_generatesCorrectJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.rotateShape(SHAPE_ID, -90);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode())
                    .contains("shape.rotation = (shape.rotation || 0) + -90;");
        }

        @Test
        @DisplayName("rotateShape — generated JS falls back to selection when shapeId is empty")
        void rotateShape_emptyShapeId_fallsBackToSelection() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.rotateShape("", 30);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode())
                    .contains("penpot.selection[0]");
        }

        @Test
        @DisplayName("rotateShape — generated JS contains zero angle without error")
        void rotateShape_zeroAngle_generatesValidJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.rotateShape(SHAPE_ID, 0);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode())
                    .contains("shape.rotation = (shape.rotation || 0) + 0;");
        }
    }

    // =========================================================================
    // scaleShape
    // =========================================================================

    @Nested
    @DisplayName("scaleShape")
    class ScaleShapeTests {

        @Test
        @DisplayName("scaleShape — returns success JSON when execution succeeds")
        void scaleShape_returnsSuccessJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{\"id\": \"" + SHAPE_ID + "\"}"));

            // WHEN
            String result = penpotTransformTools.scaleShape(SHAPE_ID, 2.0f, 0.5f);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("scaled");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("scaleShape — returns error JSON when execution fails")
        void scaleShape_returnsErrorJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Scale operation failed"));

            // WHEN
            String result = penpotTransformTools.scaleShape(SHAPE_ID, 2.0f, 2.0f);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Scale operation failed");
        }

        @Test
        @DisplayName("scaleShape — generated JS multiplies current dimensions by scale factors")
        void scaleShape_generatesCorrectJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.scaleShape(SHAPE_ID, 2.0f, 0.5f);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("const currentWidth = shape.width || 1;")
                    .contains("const currentHeight = shape.height || 1;")
                    .contains("shape.resize(currentWidth * 2.00, currentHeight * 0.50);")
                    .contains("return { id: shape.id, width: shape.width, height: shape.height };");
        }

        @Test
        @DisplayName("scaleShape — generated JS handles negative scale factors (mirroring)")
        void scaleShape_negativeFactors_generatesCorrectJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.scaleShape(SHAPE_ID, -1.0f, 1.0f);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode())
                    .contains("shape.resize(currentWidth * -1.00, currentHeight * 1.00);");
        }

        @Test
        @DisplayName("scaleShape — generated JS falls back to selection when shapeId is empty")
        void scaleShape_emptyShapeId_fallsBackToSelection() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.scaleShape("", 1.5f, 1.5f);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode())
                    .contains("penpot.selection[0]")
                    .contains("shape.resize(currentWidth * 1.50, currentHeight * 1.50);");
        }
    }

    // =========================================================================
    // moveShape
    // =========================================================================

    @Nested
    @DisplayName("moveShape")
    class MoveShapeTests {

        @Test
        @DisplayName("moveShape — returns success JSON when execution succeeds")
        void moveShape_returnsSuccessJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{\"id\": \"" + SHAPE_ID + "\", \"x\": 100, \"y\": 200}"));

            // WHEN
            String result = penpotTransformTools.moveShape(SHAPE_ID, 100f, 200f, false);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("moved");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("moveShape — returns error JSON when execution fails")
        void moveShape_returnsErrorJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Move failed"));

            // WHEN
            String result = penpotTransformTools.moveShape(SHAPE_ID, 100f, 200f, false);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Move failed");
        }

        @Test
        @DisplayName("moveShape — generated JS uses direct assignment for absolute move (relative=false)")
        void moveShape_absolute_generatesDirectAssignmentJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.moveShape(SHAPE_ID, 100f, 200f, false);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("shape.x = 100.00;")
                    .contains("shape.y = 200.00;")
                    .doesNotContain("shape.x || 0")
                    .contains("return { id: shape.id, x: shape.x, y: shape.y };");
        }

        @Test
        @DisplayName("moveShape — generated JS uses offset addition for relative move (relative=true)")
        void moveShape_relative_generatesOffsetJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.moveShape(SHAPE_ID, 10f, 20f, true);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("shape.x = (shape.x || 0) + 10.00;")
                    .contains("shape.y = (shape.y || 0) + 20.00;");
        }

        @Test
        @DisplayName("moveShape — defaults to absolute move when relative is null")
        void moveShape_relativeNull_defaultsToAbsolute() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.moveShape(SHAPE_ID, 50f, 75f, null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("shape.x = 50.00;")
                    .contains("shape.y = 75.00;")
                    .doesNotContain("shape.x || 0");
        }

        @Test
        @DisplayName("moveShape — generated JS contains US locale decimal separator (dot, not comma)")
        void moveShape_usLocale_usesDotDecimalSeparator() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.moveShape(SHAPE_ID, 12.5f, 99.9f, false);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("shape.x = 12.50;")
                    .contains("shape.y = 99.90;");
        }
    }

    // =========================================================================
    // resizeShape
    // =========================================================================

    @Nested
    @DisplayName("resizeShape")
    class ResizeShapeTests {

        @Test
        @DisplayName("resizeShape — returns success JSON when execution succeeds")
        void resizeShape_returnsSuccessJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{\"id\": \"" + SHAPE_ID + "\"}"));

            // WHEN
            String result = penpotTransformTools.resizeShape(SHAPE_ID, 400f, 300f);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("resized");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("resizeShape — returns error JSON when execution fails")
        void resizeShape_returnsErrorJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Penpot connection timeout"));

            // WHEN
            String result = penpotTransformTools.resizeShape(SHAPE_ID, 100f, 100f);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Penpot connection timeout");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("resizeShape — generated JS calls shape.resize() with correct dimensions")
        void resizeShape_generatesCorrectResizeCall() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.resizeShape(SHAPE_ID, 800f, 600f);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("getShapeById('" + SHAPE_ID + "')")
                    .contains("shape.resize(800.00, 600.00);")
                    .contains("return { id: shape.id, width: shape.width, height: shape.height };");
        }

        @Test
        @DisplayName("resizeShape — generated JS uses US locale dot separator and two decimal places")
        void resizeShape_usLocale_formatsTwoDecimals() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN — float with more than 2 decimals should be rounded to 2
            penpotTransformTools.resizeShape(SHAPE_ID, 123.456f, 789.012f);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String js = captor.getValue().getCode();
            assertThat(js)
                    .contains("shape.resize(123.46");  // rounded, dot separator
                
        }

        @Test
        @DisplayName("resizeShape — generated JS falls back to selection when shapeId is empty")
        void resizeShape_emptyShapeId_fallsBackToSelection() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.resizeShape("", 200f, 100f);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode())
                    .contains("penpot.selection[0]")
                    .contains("shape.resize(200.00, 100.00);");
        }

        @Test
        @DisplayName("resizeShape — generated JS handles negative dimensions without error")
        void resizeShape_negativeDimensions_generatesValidJs() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor =
                    ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotTransformTools.resizeShape(SHAPE_ID, -50f, 200f);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode())
                    .contains("shape.resize(-50.00, 200.00);");
        }
    }
}