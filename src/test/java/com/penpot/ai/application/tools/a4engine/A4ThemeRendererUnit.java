package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.engine.Theme;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("A4ThemeRenderer — Unit")
class A4ThemeRendererUnit {

    private final A4ThemeRenderer renderer = new A4ThemeRenderer();

    @Test
    @DisplayName("shouldUseTextOnDark_whenThemeIsGradient")
    void shouldUseTextOnDark_whenThemeIsGradient() {
        // GIVEN
        Theme theme = Theme.STARTUP_PURPLE;

        // WHEN
        String js = renderer.render(theme);

        // THEN
        assertThat(js).contains("const TEXT_COLOR = \"" + theme.textOnDark + "\"");
    }

    @Test
    @DisplayName("shouldUseTextOnLight_whenThemeIsSolid")
    void shouldUseTextOnLight_whenThemeIsSolid() {
        // GIVEN
        Theme theme = Theme.MINIMAL_LIGHT;

        // WHEN
        String js = renderer.render(theme);

        // THEN
        assertThat(js).contains("const TEXT_COLOR = \"" + theme.textOnLight + "\"");
    }

    @Test
    @DisplayName("shouldUseAccentColorAsPrimary_forAllThemes")
    void shouldUseAccentColorAsPrimary_forAllThemes() {
        // GIVEN
        Theme theme = Theme.ECOMMERCE_RED;

        // WHEN
        String js = renderer.render(theme);

        // THEN
        assertThat(js).contains("const PRIMARY = \"" + theme.accent + "\"");
    }

    @Test
    @DisplayName("shouldRenderLinearGradientBackground_whenThemeIsGradient")
    void shouldRenderLinearGradientBackground_whenThemeIsGradient() {
        // GIVEN
        Theme theme = Theme.DARK_SAAS;

        // WHEN
        String js = renderer.render(theme);

        // THEN
        assertThat(js)
            .contains("fillColorGradient")
            .contains("type: \"linear\"")
            .contains(theme.g1)
            .contains(theme.g2);
    }

    @Test
    @DisplayName("shouldNotRenderSolidBackground_whenThemeIsGradient")
    void shouldNotRenderSolidBackground_whenThemeIsGradient() {
        // GIVEN
        Theme theme = Theme.MODERN_GREEN;

        // WHEN
        String js = renderer.render(theme);

        // THEN
        assertThat(js).doesNotContain("THEME SOLID BACKGROUND");
    }

    @Test
    @DisplayName("shouldRenderSolidBackground_whenThemeIsSolid")
    void shouldRenderSolidBackground_whenThemeIsSolid() {
        // GIVEN
        Theme theme = Theme.CORPORATE_BLUE;

        // WHEN
        String js = renderer.render(theme);

        // THEN
        assertThat(js)
            .contains("fillColor: \"" + theme.bgSolid + "\"");
    }

    @Test
    @DisplayName("shouldNotRenderGradientBackground_whenThemeIsSolid")
    void shouldNotRenderGradientBackground_whenThemeIsSolid() {
        // GIVEN
        Theme theme = Theme.LUXURY_BLACK;

        // WHEN
        String js = renderer.render(theme);

        // THEN
        assertThat(js).doesNotContain("fillColorGradient");
    }

    @Test
    @DisplayName("shouldAlwaysRenderAccentLine_regardlessOfThemeType")
    void shouldAlwaysRenderAccentLine_regardlessOfThemeType() {
        // GIVEN
        Theme gradient = Theme.NEON_FUTURE;
        Theme solid    = Theme.MINIMAL_LIGHT;

        // WHEN
        String jsGradient = renderer.render(gradient);
        String jsSolid    = renderer.render(solid);

        // THEN
        assertThat(jsGradient).contains("accent.resize(80, 4)");
        assertThat(jsSolid).contains("accent.resize(80, 4)");
    }

    @Test
    @DisplayName("shouldRenderSubtleDecorationWithThemeSubtleColor")
    void shouldRenderSubtleDecorationWithThemeSubtleColor() {
        // GIVEN
        Theme theme = Theme.OCEAN_TEAL;

        // WHEN
        String js = renderer.render(theme);

        // THEN
        assertThat(js)
            .contains("deco.resize(140, 140)")
            .contains(theme.subtle);
    }

    @Test
    @DisplayName("shouldAlwaysRenderMarketingGlow_regardlessOfThemeType")
    void shouldAlwaysRenderMarketingGlow_regardlessOfThemeType() {
        // GIVEN
        Theme theme = Theme.PREMIUM_GOLD;

        // WHEN
        String js = renderer.render(theme);

        // THEN
        assertThat(js)
            .contains("glow.resize(260, 260)");
    }

    @Test
    @DisplayName("shouldAlwaysDefineConstantsSurfaceAndMuted")
    void shouldAlwaysDefineConstantsSurfaceAndMuted() {
        // GIVEN
        Theme theme = Theme.STARTUP_PURPLE;

        // WHEN
        String js = renderer.render(theme);

        // THEN
        assertThat(js)
            .contains("const SURFACE = \"#FFFFFF\"")
            .contains("const MUTED = \"#F1F5F9\"");
    }
}