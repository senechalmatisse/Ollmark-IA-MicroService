package com.penpot.ai.application.tools;

import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link PenpotInspectorTools}.
 *
 * <p>Vérifie la génération du JavaScript et la délégation vers
 * {@link PenpotToolExecutor} à l'aide de mocks Mockito,
 * sans charger le contexte Spring.</p>
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("PenpotInspectorTools — Unit")
public class PenpotInspectorToolsUnit {

    @Mock
    private PenpotToolExecutor toolExecutor;

    @InjectMocks
    private PenpotInspectorTools penpotInspectorTools;

    private static final String SHAPE_ID = "5e6291f-9bcf-806a-8007-9fa47cc4e709";

    // =========================================================================
    // getPageContext
    // =========================================================================

    @Nested
    @DisplayName("getPageContext")
    class GetPageContextTests {

        @Test
        @DisplayName("getPageContext — delegates to toolExecutor with operation 'get page context'")
        void getPageContextDelegatesToToolExecutorWithCorrectOperationName() {
            when(toolExecutor.execute(anyString(), eq("get page context"), any()))
                    .thenReturn("[]");

            String result = penpotInspectorTools.getPageContext("compact");

            assertThat(result).isEqualTo("[]");
            verify(toolExecutor, times(1)).execute(anyString(), eq("get page context"), any());
        }

        @Test
        @DisplayName("getPageContext — uses default verbosity 'compact' when verbosity is null")
        void getPageContextUsesDefaultCompactWhenNull() {
            when(toolExecutor.execute(anyString(), anyString(), any()))
                    .thenReturn("[]");

            penpotInspectorTools.getPageContext(null);

            ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).execute(jsCaptor.capture(), eq("get page context"), any());

            assertThat(jsCaptor.getValue()).contains("\"compact\"");
            assertThat(jsCaptor.getValue()).contains("const verbosity");
        }

        @Test
        @DisplayName("getPageContext — trims verbosity (\"  full  \" -> \"full\")")
        void getPageContextTrimsVerbosity() {
            when(toolExecutor.execute(anyString(), anyString(), any()))
                    .thenReturn("[]");

            penpotInspectorTools.getPageContext("   full   ");

            ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).execute(jsCaptor.capture(), eq("get page context"), any());

            assertThat(jsCaptor.getValue()).contains("\"full\"");
        }

        @Test
        @DisplayName("getPageContext — generated JS contains curated fields & JSON.stringify(out)")
        void getPageContextGeneratedJsContainsCuratedFields() {
            when(toolExecutor.execute(anyString(), anyString(), any()))
                    .thenReturn("[]");

            penpotInspectorTools.getPageContext("compact");

            ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).execute(jsCaptor.capture(), eq("get page context"), any());

            String js = jsCaptor.getValue();
            assertThat(js)
                    .contains("penpot.currentPage")
                    .contains("findShapes")
                    .contains("id: s.id")
                    .contains("width: s.width")
                    .contains("height: s.height")
                    .contains("return JSON.stringify(out)");
        }
    }

    // =========================================================================
    // getPropertiesFromShape
    // =========================================================================

    @Nested
    @DisplayName("getPropertiesFromShape")
    class GetPropertiesFromShapeTests {

        @Test
        @DisplayName("getPropertiesFromShape — delegates to toolExecutor with operation 'get properties from shape'")
        void getPropertiesFromShapeDelegatesToToolExecutor() {
            String stub = "Shape " + SHAPE_ID + " (...)";
            when(toolExecutor.execute(anyString(), eq("get properties from shape"), any()))
                    .thenReturn(stub);

            String result = penpotInspectorTools.getPropertiesFromShape(SHAPE_ID, "compact");

            assertThat(result).isEqualTo(stub);
            verify(toolExecutor, times(1)).execute(anyString(), eq("get properties from shape"), any());
        }

        @Test
        @DisplayName("getPropertiesFromShape — defaults verbosity to 'compact' when verbosity is blank")
        void getPropertiesFromShapeFefaultsVerbosityWhenBlank() {
            when(toolExecutor.execute(anyString(), anyString(), any()))
                    .thenReturn("ok");

            penpotInspectorTools.getPropertiesFromShape(SHAPE_ID, "   ");

            ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).execute(jsCaptor.capture(), eq("get properties from shape"), any());

            assertThat(jsCaptor.getValue()).contains("\"compact\"");
            assertThat(jsCaptor.getValue()).contains("const verbosity");
        }

        @Test
        @DisplayName("getPropertiesFromShape — lowercases verbosity (\"FULL\" -> \"full\")")
        void getPropertiesFromShapeLowercasesVerbosity() {
            when(toolExecutor.execute(anyString(), anyString(), any()))
                    .thenReturn("ok");

            penpotInspectorTools.getPropertiesFromShape(SHAPE_ID, "FULL");

            ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).execute(jsCaptor.capture(), eq("get properties from shape"), any());

            assertThat(jsCaptor.getValue()).contains("\"full\"");
        }

        @Test
        @DisplayName("getPropertiesFromShape — generated JS contains fills/strokes/text handling")
        void getPropertiesFromShapeGeneratedJsContainsFillsAndStrokesHandling() {
            when(toolExecutor.execute(anyString(), anyString(), any()))
                    .thenReturn("ok");

            penpotInspectorTools.getPropertiesFromShape(SHAPE_ID, "compact");

            ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).execute(jsCaptor.capture(), eq("get properties from shape"), any());

            String js = jsCaptor.getValue();
            assertThat(js)
                    .contains("let fillsSummary")
                    .contains("let strokesSummary")
                    .contains("shape.fills === \"mixed\"")
                    .contains("Array.isArray(shape.strokes)")
                    .contains("type === \"text\"");
        }
    }

    // =========================================================================
    // Geometry tools
    // =========================================================================

    @Nested
    @DisplayName("Geometry tools")
    class GeometryToolsTests {

        @Test
        @DisplayName("getCenterFromShape — generated JS computes fallback center from x/y/width/height")
        void getCenterFromShapeGeneratedJsComputesFallbackCenter() {
            when(toolExecutor.execute(anyString(), eq("get center from shape"), any()))
                    .thenReturn("Center of " + SHAPE_ID + ": cx=5, cy=5");

            penpotInspectorTools.getCenterFromShape(SHAPE_ID);

            ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).execute(jsCaptor.capture(), eq("get center from shape"), any());

            String js = jsCaptor.getValue();
            assertThat(js)
                    .contains("return \"Center of \"")
                    .contains("cx = (shape.x ?? 0) + ((shape.width ?? 0) / 2)")
                    .contains("cy = (shape.y ?? 0) + ((shape.height ?? 0) / 2)");
        }
    }

    // =========================================================================
    // Hierarchy + style tools
    // =========================================================================

    @Nested
    @DisplayName("Hierarchy & style tools")
    class HierarchyAndStyleToolsTests {

        @Test
        @DisplayName("getParentFromShape — delegates to toolExecutor with operation 'get parent from shape'")
        void getParentFromShapeDelegates() {
            when(toolExecutor.execute(anyString(), eq("get parent from shape"), any()))
                    .thenReturn("Parent of " + SHAPE_ID + ": p1");

            String result = penpotInspectorTools.getParentFromShape(SHAPE_ID);

            assertThat(result).contains("Parent of");
            verify(toolExecutor, times(1)).execute(anyString(), eq("get parent from shape"), any());
        }

        @Test
        @DisplayName("getChildrenFromShape — delegates to toolExecutor with operation 'get children from shape'")
        void getChildrenFromShapeDelegates() {
            when(toolExecutor.execute(anyString(), eq("get children from shape"), any()))
                    .thenReturn("Children of " + SHAPE_ID + ": c1, c2");

            String result = penpotInspectorTools.getChildrenFromShape(SHAPE_ID);

            assertThat(result).contains("Children of");
            verify(toolExecutor, times(1)).execute(anyString(), eq("get children from shape"), any());
        }

        @Test
        @DisplayName("getShapesColors — generated JS includes fillsState and Set-based aggregation")
        void getShapesColorsGeneratedJsContainsFillsStateAndSet() {
            when(toolExecutor.execute(anyString(), eq("get shapes colors"), any()))
                    .thenReturn("Colors of " + SHAPE_ID + ": #fff");

            penpotInspectorTools.getShapesColors(SHAPE_ID);

            ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).execute(jsCaptor.capture(), eq("get shapes colors"), any());

            String js = jsCaptor.getValue();
            assertThat(js)
                    .contains("let fillsState = \"ok\"")
                    .contains("const colors = new Set()")
                    .contains("fillsState = \"mixed\"")
                    .contains("Array.from(colors)");
        }
    }

    // =========================================================================
    // New hierarchy tools (coverage for new methods)
    // =========================================================================

    @Nested
    @DisplayName("New hierarchy tools")
    class NewHierarchyToolsTests {

        @Test
        @DisplayName("getComponentRoot — delegates correctly")
        void getComponentRootDelegates() {
            when(toolExecutor.execute(anyString(), eq("get component root"), any()))
                    .thenReturn("Root result");

            String result = penpotInspectorTools.getComponentRoot(SHAPE_ID);

            assertThat(result).isEqualTo("Root result");
            verify(toolExecutor, times(1))
                    .execute(anyString(), eq("get component root"), any());
        }

        @Test
        @DisplayName("getComponentRoot — JS contains hierarchy traversal logic")
        void getComponentRootContainsTraversalLogic() {
            when(toolExecutor.execute(anyString(), anyString(), any()))
                    .thenReturn("Root result");

            penpotInspectorTools.getComponentRoot(SHAPE_ID);

            ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).execute(jsCaptor.capture(), eq("get component root"), any());

            String js = jsCaptor.getValue();
            assertThat(js)
                    .contains("while (current)")
                    .contains("chain.push")
                    .contains("parent");
        }

        @Test
        @DisplayName("getShapeParentIndex — delegates correctly")
        void getShapeParentIndexDelegates() {
            when(toolExecutor.execute(anyString(), eq("get shape parent index"), any()))
                    .thenReturn("Index result");

            String result = penpotInspectorTools.getShapeParentIndex(SHAPE_ID);

            assertThat(result).isEqualTo("Index result");
            verify(toolExecutor, times(1))
                    .execute(anyString(), eq("get shape parent index"), any());
        }
    }
}