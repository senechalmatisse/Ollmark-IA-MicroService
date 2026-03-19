package com.penpot.ai.application.tools;

import com.penpot.ai.application.service.SessionContextHolder;
import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import com.penpot.ai.core.domain.TaskResult;
import com.penpot.ai.core.ports.in.ExecuteCodeUseCase;
import com.penpot.ai.infrastructure.factory.ResultFormatterFactory;
import com.penpot.ai.infrastructure.factory.TaskFactory;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    PenpotTransformTools.class,
    PenpotToolExecutor.class,
})
@DisplayName("PenpotTransformTools — Integration")
class PenpotTransformToolsIntegrationTest {

    // ── Mocks des collaborateurs ──────────────────────────────────────────────

    @MockitoBean
    private ExecuteCodeUseCase executeCodeUseCase;

    @MockitoBean
    private SessionContextHolder sessionContextHolder;

    @MockitoBean
    private ResultFormatterFactory resultFormatterFactory;

    @MockitoBean
    private TaskFactory taskFactory;

    // ── SUT ───────────────────────────────────────────────────────────────────

    @Autowired
    private PenpotTransformTools penpotTransformTools;

    // ── Constantes ────────────────────────────────────────────────────────────

    private static final String SHAPE_ID = "shape-integration-001";

    @BeforeEach
    void resetMocks() {
        reset(executeCodeUseCase);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // rotateShape
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("rotateShape")
    class RotateShapeIntegrationTests {

        @Test
        @DisplayName("retourne un résultat success avec un angle positif")
        void rotateShape_success() {
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success(
                    "{\"id\": \"" + SHAPE_ID + "\", \"rotation\": 45}"));

            String result = penpotTransformTools.rotateShape(SHAPE_ID, 45);

            assertThat(result)
                .contains("\"success\": true")
                .contains("rotated");
        }

        @Test
        @DisplayName("utilise penpot.selection[0] quand l'ID est vide")
        void rotateShape_fallbackToSelection() {
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success(
                    "{\"id\": \"selected-id\", \"rotation\": 90}"));

            String result = penpotTransformTools.rotateShape("", 90);

            assertThat(result).contains("\"success\": true");
            verify(executeCodeUseCase).execute(
                argThat(cmd -> cmd.getCode().contains("penpot.selection[0]")));
        }

        @Test
        @DisplayName("génère un code contenant l'angle négatif")
        void rotateShape_negativeAngle() {
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));

            penpotTransformTools.rotateShape(SHAPE_ID, -45);

            verify(executeCodeUseCase).execute(
                argThat(cmd -> cmd.getCode().contains("-45")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // scaleShape
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("scaleShape")
    class ScaleShapeIntegrationTests {

        @Test
        @DisplayName("retourne un résultat success avec les facteurs d'échelle")
        void scaleShape_success() {
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success(
                    "{\"id\": \"" + SHAPE_ID + "\"}"));

            String result = penpotTransformTools.scaleShape(SHAPE_ID, 2.0f, 0.5f);

            assertThat(result)
                .contains("\"success\": true")
                .contains("scaled");
        }

        @Test
        @DisplayName("génère un code contenant le facteur d'échelle négatif (mirroring)")
        void scaleShape_negativeScale() {
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));

            penpotTransformTools.scaleShape(SHAPE_ID, -1.0f, 1.0f);

            verify(executeCodeUseCase).execute(
                argThat(cmd -> cmd.getCode().contains("-1.00")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // moveShape
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("moveShape")
    class MoveShapeIntegrationTests {

        @Test
        @DisplayName("déplacement absolu — génère une assignation directe sans addition")
        void moveShape_absolute() {
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{\"x\": 100, \"y\": 100}"));

            String result = penpotTransformTools.moveShape(SHAPE_ID, 100f, 100f, false);

            assertThat(result).contains("\"success\": true");
            verify(executeCodeUseCase).execute(argThat(cmd ->
                cmd.getCode().contains("shape.x = 100.00")
                && !cmd.getCode().contains("+")));
        }

        @Test
        @DisplayName("déplacement relatif — génère un code avec addition d'offset")
        void moveShape_relative() {
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));

            penpotTransformTools.moveShape(SHAPE_ID, 10f, 10f, true);

            verify(executeCodeUseCase).execute(
                argThat(cmd -> cmd.getCode().contains("(shape.x || 0) + 10.00")));
        }

        @Test
        @DisplayName("relative=null est traité comme false (déplacement absolu)")
        void moveShape_relativeNull() {
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));

            penpotTransformTools.moveShape(SHAPE_ID, 50f, 50f, null);

            verify(executeCodeUseCase).execute(
                    argThat(cmd -> cmd.getCode().contains("shape.x = 50.00")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // resizeShape
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("resizeShape")
    class ResizeShapeIntegrationTests {

        @Test
        @DisplayName("formate les valeurs avec un point décimal (US Locale) et arrondi à 2 décimales")
        void resizeShape_formatting() {
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));

            penpotTransformTools.resizeShape(SHAPE_ID, 123.456f, 789.012f);

            verify(executeCodeUseCase).execute(
                argThat(cmd -> cmd.getCode().contains("123.46")));
        }

        @Test
        @DisplayName("propage l'erreur de executeCodeUseCase dans le résultat")
        void resizeShape_failure() {
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.failure("Penpot connection timeout"));

            String result = penpotTransformTools.resizeShape(SHAPE_ID, 100f, 100f);

            assertThat(result)
                .contains("\"success\": false")
                .contains("Penpot connection timeout");
        }
    }
}