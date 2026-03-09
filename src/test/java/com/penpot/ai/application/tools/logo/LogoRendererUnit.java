package com.penpot.ai.application.tools.logo;

import com.penpot.ai.application.tools.support.JsScriptLoader;
import com.penpot.ai.core.domain.logo.*;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("LogoRenderer — Unit")
class LogoRendererUnit {

    private LogoSymbolRenderer symbolRenderer;
    private LogoRenderer renderer;

    @BeforeEach
    void setUp() {
        symbolRenderer = mock(LogoSymbolRenderer.class);
        renderer = new LogoRenderer(symbolRenderer);
    }

    @Test
    @DisplayName("shouldCallLogoInitScriptWithCorrectCoordinates")
    void shouldCallLogoInitScriptWithCorrectCoordinates() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", null, LogoStyle.GEOMETRIQUE, LogoLayout.HORIZONTAL, 42, 88);
        LogoIntent intent = intent(false, false);

        when(symbolRenderer.render(any(), any())).thenReturn("// symbol");

        try (MockedStatic<JsScriptLoader> loader = mockStatic(JsScriptLoader.class)) {
            loader.when(() -> JsScriptLoader.loadWith(anyString(), anyMap())).thenReturn("");

            // WHEN
            renderer.render(spec, intent);

            // THEN
            loader.verify(() -> JsScriptLoader.loadWith(
                    eq("tools/logo/logo-init.js"),
                    eq(Map.of("x", "42", "y", "88"))
            ));
        }
    }

    @Test
    @DisplayName("shouldCallLogoFinalizeScriptWithBrandName")
    void shouldCallLogoFinalizeScriptWithBrandName() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", null, LogoStyle.GEOMETRIQUE, LogoLayout.HORIZONTAL, 0, 0);
        LogoIntent intent = intent(false, false);

        when(symbolRenderer.render(any(), any())).thenReturn("// symbol");

        try (MockedStatic<JsScriptLoader> loader = mockStatic(JsScriptLoader.class)) {
            loader.when(() -> JsScriptLoader.loadWith(anyString(), anyMap())).thenReturn("");

            // WHEN
            renderer.render(spec, intent);

            // THEN
            loader.verify(() -> JsScriptLoader.loadWith(
                    eq("tools/logo/logo-finalize.js"),
                    eq(Map.of("brandName", "Ollca"))
            ));
        }
    }

    @Test
    @DisplayName("shouldCallSymbolRendererWithSpecAndIntent")
    void shouldCallSymbolRendererWithSpecAndIntent() {
        // GIVEN
        LogoSpec spec = logoSpec("TechNova", "Innovation", LogoStyle.ABSTRAIT, LogoLayout.VERTICAL, 0, 0);
        LogoIntent intent = intent(true, true);

        when(symbolRenderer.render(spec, intent)).thenReturn("// symbol code");

        try (MockedStatic<JsScriptLoader> loader = mockStatic(JsScriptLoader.class)) {
            loader.when(() -> JsScriptLoader.loadWith(anyString(), anyMap())).thenReturn("");

            // WHEN
            renderer.render(spec, intent);

            // THEN
            verify(symbolRenderer).render(spec, intent);
        }
    }

    @Test
    @DisplayName("shouldIncludeSymbolRendererOutputInFinalCode")
    void shouldIncludeSymbolRendererOutputInFinalCode() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", null, LogoStyle.GEOMETRIQUE, LogoLayout.HORIZONTAL, 0, 0);
        LogoIntent intent = intent(false, false);
        String symbolCode = "// unique-symbol-marker";

        when(symbolRenderer.render(any(), any())).thenReturn(symbolCode);

        try (MockedStatic<JsScriptLoader> loader = mockStatic(JsScriptLoader.class)) {
            loader.when(() -> JsScriptLoader.loadWith(anyString(), anyMap())).thenReturn("");

            // WHEN
            String result = renderer.render(spec, intent);

            // THEN
            assertThat(result).contains(symbolCode);
        }
    }

    @Test
    @DisplayName("shouldLoadHorizontalLayoutScript_whenLayoutIsHorizontal")
    void shouldLoadHorizontalLayoutScript_whenLayoutIsHorizontal() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", "Tagline", LogoStyle.GEOMETRIQUE, LogoLayout.HORIZONTAL, 0, 0);
        LogoIntent intent = intent(false, false);

        when(symbolRenderer.render(any(), any())).thenReturn("");

        try (MockedStatic<JsScriptLoader> loader = mockStatic(JsScriptLoader.class)) {
            loader.when(() -> JsScriptLoader.loadWith(anyString(), anyMap())).thenReturn("");

            // WHEN
            renderer.render(spec, intent);

            // THEN
            loader.verify(() -> JsScriptLoader.loadWith(
                    eq("tools/logo/logo-layout-horizontal.js"),
                    anyMap()
            ));
        }
    }

    @Test
    @DisplayName("shouldLoadVerticalLayoutScript_whenLayoutIsVertical")
    void shouldLoadVerticalLayoutScript_whenLayoutIsVertical() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", null, LogoStyle.GEOMETRIQUE, LogoLayout.VERTICAL, 0, 0);
        LogoIntent intent = intent(false, false);

        when(symbolRenderer.render(any(), any())).thenReturn("");

        try (MockedStatic<JsScriptLoader> loader = mockStatic(JsScriptLoader.class)) {
            loader.when(() -> JsScriptLoader.loadWith(anyString(), anyMap())).thenReturn("");

            // WHEN
            renderer.render(spec, intent);

            // THEN
            loader.verify(() -> JsScriptLoader.loadWith(
                    eq("tools/logo/logo-layout-vertical.js"),
                    anyMap()
            ));
        }
    }

    @Test
    @DisplayName("shouldLoadStackedLayoutScript_whenLayoutIsStacked")
    void shouldLoadStackedLayoutScript_whenLayoutIsStacked() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", null, LogoStyle.GEOMETRIQUE, LogoLayout.STACKED, 0, 0);
        LogoIntent intent = intent(false, false);

        when(symbolRenderer.render(any(), any())).thenReturn("");

        try (MockedStatic<JsScriptLoader> loader = mockStatic(JsScriptLoader.class)) {
            loader.when(() -> JsScriptLoader.loadWith(anyString(), anyMap())).thenReturn("");

            // WHEN
            renderer.render(spec, intent);

            // THEN
            loader.verify(() -> JsScriptLoader.loadWith(
                    eq("tools/logo/logo-layout-stacked.js"),
                    anyMap()
            ));
        }
    }

    @Test
    @DisplayName("shouldLoadEmblemLayoutScript_whenLayoutIsEmblem")
    void shouldLoadEmblemLayoutScript_whenLayoutIsEmblem() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", null, LogoStyle.GEOMETRIQUE, LogoLayout.EMBLEM, 0, 0);
        LogoIntent intent = intent(false, false);

        when(symbolRenderer.render(any(), any())).thenReturn("");

        try (MockedStatic<JsScriptLoader> loader = mockStatic(JsScriptLoader.class)) {
            loader.when(() -> JsScriptLoader.loadWith(anyString(), anyMap())).thenReturn("");

            // WHEN
            renderer.render(spec, intent);

            // THEN
            loader.verify(() -> JsScriptLoader.loadWith(
                    eq("tools/logo/logo-layout-emblem.js"),
                    anyMap()
            ));
        }
    }

    @Test
    @DisplayName("shouldPassBoldFontWeight_whenIntentUsesBoldTypography")
    void shouldPassBoldFontWeight_whenIntentUsesBoldTypography() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", "Tagline", LogoStyle.GEOMETRIQUE, LogoLayout.HORIZONTAL, 0, 0);
        LogoIntent intent = intent(true, false);

        when(symbolRenderer.render(any(), any())).thenReturn("");

        try (MockedStatic<JsScriptLoader> loader = mockStatic(JsScriptLoader.class)) {
            loader.when(() -> JsScriptLoader.loadWith(anyString(), anyMap())).thenReturn("");

            // WHEN
            renderer.render(spec, intent);

            // THEN
            loader.verify(() -> JsScriptLoader.loadWith(
                    eq("tools/logo/logo-layout-horizontal.js"),
                    argThat(map -> "bold".equals(map.get("weight")))
            ));
        }
    }

    @Test
    @DisplayName("shouldPassNormalFontWeight_whenIntentDoesNotUseBoldTypography")
    void shouldPassNormalFontWeight_whenIntentDoesNotUseBoldTypography() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", "Tagline", LogoStyle.MINIMALISTE, LogoLayout.HORIZONTAL, 0, 0);
        LogoIntent intent = intent(false, false);

        when(symbolRenderer.render(any(), any())).thenReturn("");

        try (MockedStatic<JsScriptLoader> loader = mockStatic(JsScriptLoader.class)) {
            loader.when(() -> JsScriptLoader.loadWith(anyString(), anyMap())).thenReturn("");

            // WHEN
            renderer.render(spec, intent);

            // THEN
            loader.verify(() -> JsScriptLoader.loadWith(
                    eq("tools/logo/logo-layout-horizontal.js"),
                    argThat(map -> "normal".equals(map.get("weight")))
            ));
        }
    }

    @Test
    @DisplayName("shouldPassEmptyStringAsTagline_whenTaglineIsNull")
    void shouldPassEmptyStringAsTagline_whenTaglineIsNull() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", null, LogoStyle.GEOMETRIQUE, LogoLayout.HORIZONTAL, 0, 0);
        LogoIntent intent = intent(false, false);

        when(symbolRenderer.render(any(), any())).thenReturn("");

        try (MockedStatic<JsScriptLoader> loader = mockStatic(JsScriptLoader.class)) {
            loader.when(() -> JsScriptLoader.loadWith(anyString(), anyMap())).thenReturn("");

            // WHEN
            renderer.render(spec, intent);

            // THEN
            loader.verify(() -> JsScriptLoader.loadWith(
                    eq("tools/logo/logo-layout-horizontal.js"),
                    argThat(map -> "".equals(map.get("tagline")))
            ));
        }
    }

    @Test
    @DisplayName("shouldPassActualTagline_whenTaglineIsNotNull")
    void shouldPassActualTagline_whenTaglineIsNotNull() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", "Click & Collect", LogoStyle.GEOMETRIQUE, LogoLayout.HORIZONTAL, 0, 0);
        LogoIntent intent = intent(false, false);

        when(symbolRenderer.render(any(), any())).thenReturn("");

        try (MockedStatic<JsScriptLoader> loader = mockStatic(JsScriptLoader.class)) {
            loader.when(() -> JsScriptLoader.loadWith(anyString(), anyMap())).thenReturn("");

            // WHEN
            renderer.render(spec, intent);

            // THEN
            loader.verify(() -> JsScriptLoader.loadWith(
                    eq("tools/logo/logo-layout-horizontal.js"),
                    argThat(map -> "Click & Collect".equals(map.get("tagline")))
            ));
        }
    }

    private LogoSpec logoSpec(String brand, String tagline, LogoStyle style, LogoLayout layout, int x, int y) {
        return LogoSpec.builder()
                .brandName(brand).tagline(tagline)
                .style(style).layout(layout)
                .x(x).y(y)
                .build();
    }

    /**
     * @param useBold    → isUseBoldTypography
     * @param useGradient → isUseGradient
     */
    private LogoIntent intent(boolean useBold, boolean useGradient) {
        return new LogoIntent(false, 4, 1.0,
            "#FF5C00", "#000000", "#1A1A1A",
            useBold, useGradient);
    }
}