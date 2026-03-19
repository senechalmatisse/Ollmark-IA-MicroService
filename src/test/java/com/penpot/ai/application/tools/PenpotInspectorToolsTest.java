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
import static org.mockito.Mockito.*;

/**
 * Tests d'intégration pour {@link PenpotInspectorTools}.
 *
 * <p>Charge le contexte Spring Boot et mocke {@link ExecuteCodeUseCase}
 * afin de vérifier la délégation correcte et la gestion des retours
 * (succès / échec) sans exécuter de JavaScript réel.</p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    PenpotInspectorTools.class,
    PenpotToolExecutor.class
})
@DisplayName("PenpotInspectorTools — Integration")
class PenpotInspectorToolsTest {

    @MockitoBean
    private ExecuteCodeUseCase executeCodeUseCase;

    @MockitoBean
    private SessionContextHolder sessionContextHolder;

    @MockitoBean
    private ResultFormatterFactory resultFormatterFactory;

    @MockitoBean
    private TaskFactory taskFactory;

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
}