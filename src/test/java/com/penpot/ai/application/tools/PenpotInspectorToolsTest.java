package com.penpot.ai.application.tools;

import com.penpot.ai.core.domain.TaskResult;
import com.penpot.ai.core.ports.in.ExecuteCodeUseCase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.penpot.ai.core.domain.ExecuteCodeCommand;
import org.mockito.ArgumentCaptor;

/**
 * Tests d'intégration pour {@link PenpotInspectorTools}.
 *
 * <p>Charge le contexte Spring Boot et mocke {@link ExecuteCodeUseCase}
 * afin de vérifier la délégation correcte et la gestion des retours
 * (succès / échec) sans exécuter de JavaScript réel.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PenpotInspectorTools — Integration")
class PenpotInspectorToolsTest {

    @MockitoBean
    private ExecuteCodeUseCase executeCodeUseCase;

    @Autowired
    private PenpotInspectorTools penpotInspectorTools;

    private static final String SHAPE_ID = "shape-integration-001";

    @BeforeEach
    void resetMocks() {
        reset(executeCodeUseCase);
    }

    // =========================================================================
    // getPageContext
    // =========================================================================

    @Nested
    @DisplayName("getPageContext")
    class GetPageContextIntegrationTests {

        @Test
        @DisplayName("getPageContext — returns raw JSON when execution succeeds")
        void getPageContextReturnsJsonWhenSuccess() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("[{\"id\":\"" + SHAPE_ID + "\",\"type\":\"rectangle\"}]"));

            // WHEN
            String result = penpotInspectorTools.getPageContext("compact");

            // THEN
            assertThat(result).contains("\"id\":\"" + SHAPE_ID + "\"");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("getPageContext — returns error JSON when execution fails")
        void getPageContextReturnsErrorJsonWhenFailure() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("JS execution failed"));

            // WHEN
            String result = penpotInspectorTools.getPageContext("compact");

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("JS execution failed");
            verify(executeCodeUseCase, times(1)).execute(any());
        }
    }

    // =========================================================================
    // Geometry tools
    // =========================================================================

    
        @Test
        @DisplayName("getCenterFromShape — returns {cx,cy} when success")
        void getCenterFromShapeReturnsCenter() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{\"id\":\"" + SHAPE_ID + "\",\"cx\":60,\"cy\":45}"));

            // WHEN
            String result = penpotInspectorTools.getCenterFromShape(SHAPE_ID);

            // THEN
            assertThat(result).contains("\"cx\":60").contains("\"cy\":45");
            verify(executeCodeUseCase, times(1)).execute(any());
        }
    

    // =========================================================================
    // Hierarchy & style tools
    // =========================================================================

    @Nested
    @DisplayName("Hierarchy & style tools")
    class HierarchyAndStyleIntegrationTests {

        @Test
        @DisplayName("getPropertiesFromShape — returns JSON when success")
        void getPropertiesFromShapeReturnsJson() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{\"id\":\"" + SHAPE_ID + "\",\"type\":\"rectangle\",\"name\":\"Rect\"}"));

            // WHEN
            String result = penpotInspectorTools.getPropertiesFromShape(SHAPE_ID, "compact");

            // THEN
            assertThat(result).contains("\"id\":\"" + SHAPE_ID + "\"").contains("\"type\":\"rectangle\"");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("getParentFromShape — returns parentId when success")
        void getParentFromShapeReturnsParentId() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{\"id\":\"" + SHAPE_ID + "\",\"parentId\":\"p-001\"}"));

            // WHEN
            String result = penpotInspectorTools.getParentFromShape(SHAPE_ID);

            // THEN
            assertThat(result).contains("\"parentId\":\"p-001\"");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("getChildrenFromShape — returns childrenIds when success")
        void getChildrenFromShapeReturnsChildrenIds() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{\"id\":\"" + SHAPE_ID + "\",\"childrenIds\":[\"c1\",\"c2\"]}"));

            // WHEN
            String result = penpotInspectorTools.getChildrenFromShape(SHAPE_ID);

            // THEN
            assertThat(result).contains("childrenIds").contains("c1").contains("c2");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("getShapesColors — returns colors list when success")
        void getShapesColorsReturnsColors() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{\"id\":\"" + SHAPE_ID + "\",\"fillsState\":\"ok\",\"colors\":[\"#0137da\"]}"));

            // WHEN
            String result = penpotInspectorTools.getShapesColors(SHAPE_ID);

            // THEN
            assertThat(result).contains("\"fillsState\":\"ok\"").contains("#0137da");
            verify(executeCodeUseCase, times(1)).execute(any());
        }
    }

    // =========================================================================
    // New hierarchy tools
    // =========================================================================

    @Nested
    @DisplayName("New hierarchy tools")
    class NewHierarchyToolsIntegrationTests {

        @Test
        @DisplayName("getComponentRoot — returns {root,depth,chain} when success")
        void getComponentRootReturnsRootObject() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success(
                            "{\"id\":\"" + SHAPE_ID + "\",\"root\":{\"id\":\"board-001\",\"type\":\"board\"},\"depth\":3,\"chain\":[]}"
                    ));

            // WHEN
            String result = penpotInspectorTools.getComponentRoot(SHAPE_ID);

            // THEN
            assertThat(result).contains("\"root\"").contains("\"depth\":3");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("getShapeParentIndex — returns {parentId,parentIndex} when success")
        void getShapeParentIndexReturnsParentIndex() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{\"id\":\"" + SHAPE_ID + "\",\"parentId\":\"g-001\",\"parentIndex\":2}"));

            // WHEN
            String result = penpotInspectorTools.getShapeParentIndex(SHAPE_ID);

            // THEN
            assertThat(result).contains("\"parentIndex\":2").contains("\"parentId\":\"g-001\"");
            verify(executeCodeUseCase, times(1)).execute(any());
        }
    }
// =========================================================================
// Geometry tools — JS output validation
// =========================================================================

    @Nested
    @DisplayName("Geometry tools — JS output")
    class GeometryToolsJsOutputTests {

        @Test
        @DisplayName("getCenterFromShape — generated JS contains shapeId")
        void getCenterFromShape_generatedJsContainsShapeId() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{\"cx\":0,\"cy\":0}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getCenterFromShape(SHAPE_ID);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains(SHAPE_ID);
        }

        @Test
        @DisplayName("getCenterFromShape — generated JS uses default shapeId 'selection' when null")
        void getCenterFromShape_generatedJsUsesDefaultShapeIdWhenNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{\"cx\":0,\"cy\":0}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getCenterFromShape(null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("selection");
        }

        @Test
        @DisplayName("getCenterFromShape — returns error JSON when execution fails")
        void getCenterFromShape_returnsErrorJsonWhenFailure() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.failure("Script error"));

            // WHEN
            String result = penpotInspectorTools.getCenterFromShape(SHAPE_ID);

            // THEN
            assertThat(result).contains("\"success\": false").contains("Script error");
        }
    }

    // =========================================================================
    // getPageContext — JS output validation
    // =========================================================================

    @Nested
    @DisplayName("getPageContext — JS output")
    class GetPageContextJsOutputTests {

        @Test
        @DisplayName("getPageContext — generated JS contains verbosity 'full'")
        void getPageContext_generatedJsContainsVerbosity() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("[]"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getPageContext("full");

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("full");
        }

        @Test
        @DisplayName("getPageContext — generated JS uses default verbosity 'compact' when null")
        void getPageContext_generatedJsUsesDefaultVerbosityWhenNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("[]"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getPageContext(null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("compact");
        }
    }

    // =========================================================================
    // Hierarchy & style tools — JS output validation
    // =========================================================================

    @Nested
    @DisplayName("Hierarchy & style tools — JS output")
    class HierarchyAndStyleJsOutputTests {

        @Test
        @DisplayName("getPropertiesFromShape — generated JS contains shapeId and verbosity")
        void getPropertiesFromShape_generatedJsContainsShapeIdAndVerbosity() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getPropertiesFromShape(SHAPE_ID, "full");

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains(SHAPE_ID);
            assertThat(code).contains("full");
        }

        @Test
        @DisplayName("getPropertiesFromShape — generated JS uses defaults when null params")
        void getPropertiesFromShape_generatedJsUsesDefaultsWhenNullParams() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getPropertiesFromShape(null, null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("selection");
            assertThat(code).contains("compact");
        }

        @Test
        @DisplayName("getParentFromShape — generated JS contains shapeId")
        void getParentFromShape_generatedJsContainsShapeId() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getParentFromShape(SHAPE_ID);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains(SHAPE_ID);
        }

        @Test
        @DisplayName("getParentFromShape — generated JS uses default shapeId 'selection' when null")
        void getParentFromShape_generatedJsUsesDefaultWhenNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getParentFromShape(null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("selection");
        }

        @Test
        @DisplayName("getChildrenFromShape — generated JS contains shapeId")
        void getChildrenFromShape_generatedJsContainsShapeId() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("[]"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getChildrenFromShape(SHAPE_ID);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains(SHAPE_ID);
        }

        @Test
        @DisplayName("getChildrenFromShape — generated JS uses default shapeId 'selection' when null")
        void getChildrenFromShape_generatedJsUsesDefaultWhenNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("[]"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getChildrenFromShape(null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("selection");
        }

        @Test
        @DisplayName("getShapesColors — generated JS contains shapeId")
        void getShapesColors_generatedJsContainsShapeId() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("[]"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getShapesColors(SHAPE_ID);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains(SHAPE_ID);
        }

        @Test
        @DisplayName("getShapesColors — generated JS uses default shapeId 'selection' when null")
        void getShapesColors_generatedJsUsesDefaultWhenNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("[]"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getShapesColors(null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("selection");
        }
    }

    // =========================================================================
    // New hierarchy tools — JS output validation
    // =========================================================================

    @Nested
    @DisplayName("New hierarchy tools — JS output")
    class NewHierarchyToolsJsOutputTests {

        @Test
        @DisplayName("getComponentRoot — generated JS contains shapeId")
        void getComponentRoot_generatedJsContainsShapeId() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getComponentRoot(SHAPE_ID);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains(SHAPE_ID);
        }

        @Test
        @DisplayName("getComponentRoot — generated JS uses default shapeId 'selection' when null")
        void getComponentRoot_generatedJsUsesDefaultWhenNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getComponentRoot(null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("selection");
        }

        @Test
        @DisplayName("getShapeParentIndex — generated JS contains shapeId")
        void getShapeParentIndex_generatedJsContainsShapeId() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getShapeParentIndex(SHAPE_ID);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains(SHAPE_ID);
        }

        @Test
        @DisplayName("getShapeParentIndex — generated JS uses default shapeId 'selection' when null")
        void getShapeParentIndex_generatedJsUsesDefaultWhenNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                    .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotInspectorTools.getShapeParentIndex(null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("selection");
        }
    }
}