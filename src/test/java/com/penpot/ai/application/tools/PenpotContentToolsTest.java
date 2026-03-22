package com.penpot.ai.application.tools;

import com.penpot.ai.application.tools.pipeline.*;
import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import com.penpot.ai.core.domain.logo.*;
import com.penpot.ai.core.domain.spec.SectionSpec;
import com.penpot.ai.core.ports.in.ExecuteCodeUseCase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PenpotContentTools.class)
@DisplayName("PenpotContentTools — Integration")
public class PenpotContentToolsTest {

    @MockitoBean
    private ExecuteCodeUseCase executeCodeUseCase;

    @MockitoBean
    private PenpotToolExecutor toolExecutor;

    @MockitoBean
    private LogoPipeline logoPipeline;

    @MockitoBean
    private A4SectionPipeline a4Pipeline;

    @MockitoBean
    private SectionPipeline sectionPipeline;

    @Autowired
    private PenpotContentTools penpotContentTools;

    private static final String BRAND = "Ollca";
    private static final String TAGLINE = "Click & Collect Local";

    @BeforeEach
    void resetMocks() {
        reset(executeCodeUseCase, toolExecutor, logoPipeline, a4Pipeline, sectionPipeline);
        when(toolExecutor.createContent(anyString(), anyString()))
            .thenAnswer(inv -> "{\"success\": true, \"type\": \"" + inv.getArgument(1) + "\"}");
    }

    // =========================================================================
    // TEXT CONTENT
    // =========================================================================

    @Nested
    @DisplayName("Text Generation Tools")
    class TextIntegrationTests {

        @Test
        @DisplayName("createTitle — integration success")
        void createTitle_integration() {
            String result = penpotContentTools.createTitle("Hello Title", 100, 200, "#FF0000");

            assertThat(result).contains("\"success\": true").contains("title");
            verify(toolExecutor).createContent(
                argThat(code ->
                    code.contains("text.fontSize = 48") &&
                    code.contains("Hello Title")),
                eq("title")
            );
        }

        @Test
        @DisplayName("createSubtitle — integration success")
        void createSubtitle_integration() {
            String result = penpotContentTools.createSubtitle("Subtitle", 0, 0, null);

            assertThat(result).contains("\"success\": true");
            verify(toolExecutor).createContent(
                argThat(code -> code.contains("text.fontSize = 32")),
                eq("subtitle")
            );
        }

        @Test
        @DisplayName("createParagraph — integration success")
        void createParagraph_integration() {
            String result = penpotContentTools.createParagraph("Paragraph", 50, 50, "#000000");

            assertThat(result).contains("\"success\": true");
            verify(toolExecutor).createContent(
                argThat(code ->
                    code.contains("text.fontSize = 16") &&
                    code.contains("fontWeight = 'normal'")),
                eq("paragraph")
            );
        }
    }

    // =========================================================================
    // IMAGES
    // =========================================================================

    @Nested
    @DisplayName("createImage")
    class ImageIntegrationTests {

        @Test
        @DisplayName("createImage — success with custom dimensions")
        void createImage_success() {
            String result = penpotContentTools.createImage("http://image.url", 10, 20, 500, 300);

            assertThat(result).contains("\"success\": true");
            verify(toolExecutor).createContent(
                argThat(code ->
                    code.contains("penpot.uploadMediaUrl") &&
                    code.contains("rect.resize(500, 300)")),
                eq("image")
            );
        }

        @Test
        @DisplayName("createImage — handles execution failure")
        void createImage_failure() {
            when(toolExecutor.createContent(anyString(), eq("image")))
                .thenReturn("{\"success\": false, \"error\": \"Network error\"}");

            String result = penpotContentTools.createImage("url", 0, 0, null, null);

            assertThat(result).contains("\"success\": false").contains("Network error");
        }
    }

    // =========================================================================
    // BUTTONS
    // =========================================================================

    @Nested
    @DisplayName("createButton")
    class ButtonIntegrationTests {

        @Test
        @DisplayName("createButton — success with full parameters")
        void createButton_fullParams() {
            String result = penpotContentTools.createButton(
                "Order Now", 100, 100, 250, 80, "#FF5733", "#FFFFFF", 20);

            assertThat(result).contains("\"success\": true").contains("button");
            verify(toolExecutor).createContent(
                argThat(code ->
                    code.contains("const bg = '#FF5733'") &&
                    code.contains("const radius = 20") &&
                    code.contains("const label = 'Order Now'")),
                eq("button")
            );
        }

        @Test
        @DisplayName("createButton — validation of text width estimation JS")
        void createButton_logicCheck() {
            penpotContentTools.createButton("Small", 0, 0, null, null, null, null, null);

            verify(toolExecutor).createContent(
                argThat(code ->
                    code.contains("label.length * 9") &&
                    code.contains("Math.max(minW, estimatedTextWidth + padX * 2)")),
                eq("button")
            );
        }

        @Test
        @DisplayName("createButton — handles special characters in integration")
        void createButton_escaping() {
            penpotContentTools.createButton("L'action", 0, 0, null, null, null, null, null);

            verify(toolExecutor).createContent(
                argThat(code -> code.contains("const label = 'L\\'action'")),
                eq("button")
            );
        }
    }

    // =========================================================================
    // GLOBAL ERROR HANDLING
    // =========================================================================

    @Test
    @DisplayName("Global — should return formatted failure when executor throws exception")
    void global_exceptionHandling() {
        when(toolExecutor.createContent(anyString(), anyString()))
            .thenReturn("{\"success\": false, \"error\": \"Unexpected Crash\"}");

        String result = penpotContentTools.createParagraph("Error test", 0, 0, null);

        assertThat(result).contains("\"success\": false").contains("Unexpected Crash");
    }

    // =========================================================================
    // LOGO
    // =========================================================================

    @Nested
    @DisplayName("createLogo")
    class CreateLogoIntegrationTests {

        @Test
        void createLogo_executesFullPipeline() {
            when(logoPipeline.execute(any())).thenReturn("{\"success\": true}");

            String result = penpotContentTools.createLogo(
                BRAND, TAGLINE, LogoStyle.ABSTRAIT, LogoLayout.HORIZONTAL, 200, 300);

            assertThat(result).contains("\"success\": true");
            verify(logoPipeline).execute(any());
        }

        @Test
        void createLogo_usesDefaultCoordinates() {
            when(logoPipeline.execute(any())).thenReturn("{\"success\": true}");
            ArgumentCaptor<LogoSpec> captor = ArgumentCaptor.forClass(LogoSpec.class);

            penpotContentTools.createLogo(
                BRAND, TAGLINE, LogoStyle.GEOMETRIQUE, LogoLayout.VERTICAL, null, null);

            verify(logoPipeline).execute(captor.capture());
            assertThat(captor.getValue().getX()).isEqualTo(100);
            assertThat(captor.getValue().getY()).isEqualTo(100);
        }

        @Test
        void createLogo_generatedSpecContainsBrandName() {
            when(logoPipeline.execute(any())).thenReturn("{\"success\": true}");
            ArgumentCaptor<LogoSpec> captor = ArgumentCaptor.forClass(LogoSpec.class);

            penpotContentTools.createLogo(
                "Village Bio", "Produits locaux", LogoStyle.EMBLEME, LogoLayout.EMBLEM, 50, 80);

            verify(logoPipeline).execute(captor.capture());
            assertThat(captor.getValue().getBrandName()).isEqualTo("Village Bio");
        }

        @Test
        void createLogo_worksWithoutTagline() {
            when(logoPipeline.execute(any())).thenReturn("{\"success\": true}");

            String result = penpotContentTools.createLogo(
                "TechNova", null, LogoStyle.MINIMALISTE, LogoLayout.STACKED, 10, 20);

            assertThat(result).contains("\"success\": true");
            verify(logoPipeline).execute(any());
        }
    }

    // =========================================================================
    // A4 SECTION
    // =========================================================================

    @Nested
    @DisplayName("createA4Section")
    class CreateA4SectionIntegrationTests {

        @Test
        @DisplayName("createA4Section — delegates to pipeline and returns result")
        void createA4Section_delegatesToPipeline() {
            when(a4Pipeline.execute(any())).thenReturn("a4-section-id");

            SectionSpec spec = new SectionSpec();
            spec.setTitle("Mon titre A4");

            String result = penpotContentTools.createA4Section(spec, 100, 200);

            assertThat(result).isEqualTo("a4-section-id");
            verify(a4Pipeline).execute(any());
            verify(toolExecutor, never()).createContent(any(), any());
        }

        @Test
        @DisplayName("createA4Section — passes provided coordinates to pipeline")
        void createA4Section_passesCoordinatesToPipeline() {
            when(a4Pipeline.execute(any())).thenReturn("a4-section-id");
            ArgumentCaptor<A4SectionPipeline.A4SectionRequest> captor =
                ArgumentCaptor.forClass(A4SectionPipeline.A4SectionRequest.class);

            penpotContentTools.createA4Section(new SectionSpec(), 150, 300);

            verify(a4Pipeline).execute(captor.capture());
            assertThat(captor.getValue().x()).isEqualTo(150);
            assertThat(captor.getValue().y()).isEqualTo(300);
        }

        @Test
        @DisplayName("createA4Section — defaults coordinates to 0,0 when null")
        void createA4Section_defaultsCoordinates() {
            when(a4Pipeline.execute(any())).thenReturn("a4-section-id");
            ArgumentCaptor<A4SectionPipeline.A4SectionRequest> captor =
                ArgumentCaptor.forClass(A4SectionPipeline.A4SectionRequest.class);

            penpotContentTools.createA4Section(new SectionSpec(), null, null);

            verify(a4Pipeline).execute(captor.capture());
            assertThat(captor.getValue().x()).isEqualTo(0);
            assertThat(captor.getValue().y()).isEqualTo(0);
        }

        @Test
        @DisplayName("createA4Section — replaces null spec with empty SectionSpec")
        void createA4Section_replacesNullSpecWithEmpty() {
            when(a4Pipeline.execute(any())).thenReturn("a4-section-id");
            ArgumentCaptor<A4SectionPipeline.A4SectionRequest> captor =
                ArgumentCaptor.forClass(A4SectionPipeline.A4SectionRequest.class);

            penpotContentTools.createA4Section(null, 0, 0);

            verify(a4Pipeline).execute(captor.capture());
            assertThat(captor.getValue().spec()).isNotNull()
                .isInstanceOf(SectionSpec.class);
        }

        @Test
        @DisplayName("createA4Section — passes spec unchanged when not null")
        void createA4Section_passesSpecUnchanged() {
            when(a4Pipeline.execute(any())).thenReturn("a4-section-id");
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Titre");
            spec.setSubtitle("Sous-titre");
            ArgumentCaptor<A4SectionPipeline.A4SectionRequest> captor =
                ArgumentCaptor.forClass(A4SectionPipeline.A4SectionRequest.class);

            penpotContentTools.createA4Section(spec, 0, 0);

            verify(a4Pipeline).execute(captor.capture());
            assertThat(captor.getValue().spec()).isSameAs(spec);
        }
    }

    // =========================================================================
    // SECTION
    // =========================================================================

    @Nested
    @DisplayName("createSection")
    class CreateSectionIntegrationTests {

        @Test
        @DisplayName("createSection — delegates to pipeline and returns result")
        void createSection_delegatesToPipeline() {
            when(sectionPipeline.execute(any())).thenReturn("section-id");

            String result = penpotContentTools.createSection(new SectionSpec(), 10, 20);

            assertThat(result).isEqualTo("section-id");
            verify(sectionPipeline).execute(any());
            verify(toolExecutor, never()).createContent(any(), any());
        }

        @Test
        @DisplayName("createSection — passes provided coordinates to pipeline")
        void createSection_passesCoordinatesToPipeline() {
            when(sectionPipeline.execute(any())).thenReturn("section-id");
            ArgumentCaptor<SectionPipeline.SectionRequest> captor =
                ArgumentCaptor.forClass(SectionPipeline.SectionRequest.class);

            penpotContentTools.createSection(new SectionSpec(), 15, 25);

            verify(sectionPipeline).execute(captor.capture());
            assertThat(captor.getValue().x()).isEqualTo(15);
            assertThat(captor.getValue().y()).isEqualTo(25);
        }

        @Test
        @DisplayName("createSection — defaults coordinates to 80,120 when null")
        void createSection_defaultsCoordinates() {
            when(sectionPipeline.execute(any())).thenReturn("section-id");
            ArgumentCaptor<SectionPipeline.SectionRequest> captor =
                ArgumentCaptor.forClass(SectionPipeline.SectionRequest.class);

            penpotContentTools.createSection(new SectionSpec(), null, null);

            verify(sectionPipeline).execute(captor.capture());
            assertThat(captor.getValue().x()).isEqualTo(80);
            assertThat(captor.getValue().y()).isEqualTo(120);
        }

        @Test
        @DisplayName("createSection — passes null spec directly to pipeline for validation")
        void createSection_passesNullSpecDirectlyToPipeline() {
            when(sectionPipeline.execute(any())).thenReturn("section-id");
            ArgumentCaptor<SectionPipeline.SectionRequest> captor =
                ArgumentCaptor.forClass(SectionPipeline.SectionRequest.class);

            penpotContentTools.createSection(null, 0, 0);

            verify(sectionPipeline).execute(captor.capture());
            assertThat(captor.getValue().spec()).isNull();
        }

        @Test
        @DisplayName("createSection — passes spec unchanged to pipeline")
        void createSection_passesSpecUnchanged() {
            when(sectionPipeline.execute(any())).thenReturn("section-id");
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Titre hero");
            spec.setSubtitle("Sous-titre");
            ArgumentCaptor<SectionPipeline.SectionRequest> captor =
                ArgumentCaptor.forClass(SectionPipeline.SectionRequest.class);

            penpotContentTools.createSection(spec, 0, 0);

            verify(sectionPipeline).execute(captor.capture());
            assertThat(captor.getValue().spec()).isSameAs(spec);
        }
    }
}