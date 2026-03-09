package com.penpot.ai.application.tools.logo;

import com.penpot.ai.core.domain.logo.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LogoSymbolRenderer — Unit")
class LogoSymbolRendererUnit {

    private final LogoSymbolRenderer renderer = new LogoSymbolRenderer();

    @Test
    @DisplayName("shouldComputeSymbolSizeAs80TimesScalingFactor_whenScalingFactorIs1_0")
    void shouldComputeSymbolSizeAs80TimesScalingFactor_whenScalingFactorIs1_0() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.GEOMETRIQUE);
        LogoIntent intent = intent(1.0, 4, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js).contains("const symbolSize = 80.0;");
    }

    @Test
    @DisplayName("shouldComputeSymbolSizeAs96_whenScalingFactorIs1_2")
    void shouldComputeSymbolSizeAs96_whenScalingFactorIs1_2() {
        // GIVEN
        LogoSpec spec = spec("Neo", LogoStyle.GEOMETRIQUE);
        LogoIntent intent = intent(1.2, 4, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js).contains("const symbolSize = 96.0;");
    }

    @Test
    @DisplayName("shouldAlwaysIncludeColumnXAndColumnY_inAllStyles")
    void shouldAlwaysIncludeColumnXAndColumnY_inAllStyles() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.MINIMALISTE);
        LogoIntent intent = intent(1.0, 4, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js)
            .contains("const columnX = x;")
            .contains("const columnY = y;");
    }

    @Test
    @DisplayName("shouldRenderRectangleRotated45Degrees_whenStyleIsGeometrique")
    void shouldRenderRectangleRotated45Degrees_whenStyleIsGeometrique() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.GEOMETRIQUE);
        LogoIntent intent = intent(1.0, 4, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js)
            .contains("penpot.createRectangle()")
            .contains("base.rotation = 45;");
    }

    @Test
    @DisplayName("shouldRenderAccentEllipse_whenStyleIsGeometrique")
    void shouldRenderAccentEllipse_whenStyleIsGeometrique() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.GEOMETRIQUE);
        LogoIntent intent = intent(1.0, 4, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js)
            .contains("penpot.createEllipse()")
            .contains("accent.fills");
    }

    @Test
    @DisplayName("shouldUseBorderRadiusFromIntent_whenStyleIsGeometrique")
    void shouldUseBorderRadiusFromIntent_whenStyleIsGeometrique() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.GEOMETRIQUE);
        LogoIntent intent = intent(1.0, 999, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js).contains("base.borderRadius = 999;");
    }

    @Test
    @DisplayName("shouldRenderFirstLetterUppercased_whenStyleIsMonogramme")
    void shouldRenderFirstLetterUppercased_whenStyleIsMonogramme() {
        // GIVEN
        LogoSpec spec = spec("ollca", LogoStyle.MONOGRAMME);
        LogoIntent intent = intent(1.0, 4, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js).contains("penpot.createText(\"O\")");
    }

    @Test
    @DisplayName("shouldRenderFirstLetterOfBrandName_whenStyleIsMonogramme")
    void shouldRenderFirstLetterOfBrandName_whenStyleIsMonogramme() {
        // GIVEN
        LogoSpec spec = spec("TechNova", LogoStyle.MONOGRAMME);
        LogoIntent intent = intent(1.0, 4, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js).contains("penpot.createText(\"T\")");
    }

    @Test
    @DisplayName("shouldRenderFrameRectangleWithTextColor_whenStyleIsMonogramme")
    void shouldRenderFrameRectangleWithTextColor_whenStyleIsMonogramme() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.MONOGRAMME);
        LogoIntent intent = intent(1.0, 4, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js)
            .contains("penpot.createRectangle()")
            .contains("char.fontWeight = \"bold\"")
            .contains(intent.getTextColor());
    }

    @Test
    @DisplayName("shouldUseBorderRadiusFromIntent_whenStyleIsMonogramme")
    void shouldUseBorderRadiusFromIntent_whenStyleIsMonogramme() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.MONOGRAMME);
        LogoIntent intent = intent(1.0, 12, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js).contains("frame.borderRadius = 12;");
    }

    @Test
    @DisplayName("shouldRenderTwoOverlappingEllipses_whenStyleIsAbstrait")
    void shouldRenderTwoOverlappingEllipses_whenStyleIsAbstrait() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.ABSTRAIT);
        LogoIntent intent = intent(1.0, 4, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js)
            .contains("shape1 = penpot.createEllipse()")
            .contains("shape2 = penpot.createEllipse()");
    }

    @Test
    @DisplayName("shouldApplySecondaryColorWithOpacity07_toSecondShape_whenStyleIsAbstrait")
    void shouldApplySecondaryColorWithOpacity07_toSecondShape_whenStyleIsAbstrait() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.ABSTRAIT);
        LogoIntent intent = intent(1.0, 4, false, false); // secondary = #000000

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js).contains("fillColor: '#000000', fillOpacity: 0.7");
    }

    @Test
    @DisplayName("shouldRenderHorizontalTrackAndBullet_whenStyleIsMinimaliste")
    void shouldRenderHorizontalTrackAndBullet_whenStyleIsMinimaliste() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.MINIMALISTE);
        LogoIntent intent = intent(1.0, 4, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js)
            .contains("track = penpot.createRectangle()")
            .contains("track.resize(symbolSize, 6)")
            .contains("track.borderRadius = 99")
            .contains("bullet = penpot.createEllipse()");
    }

    @Test
    @DisplayName("shouldApplySecondaryColorWithLowOpacity_toTrack_whenStyleIsMinimaliste")
    void shouldApplySecondaryColorWithLowOpacity_toTrack_whenStyleIsMinimaliste() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.MINIMALISTE);
        LogoIntent intent = intent(1.0, 4, false, false); // secondary = #000000

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js).contains("fillColor: '#000000', fillOpacity: 0.2");
    }

    @Test
    @DisplayName("shouldRenderContainerAndStrip_whenStyleIsEmbleme")
    void shouldRenderContainerAndStrip_whenStyleIsEmbleme() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.EMBLEME);
        LogoIntent intent = intent(1.0, 4, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js)
            .contains("container = penpot.createRectangle()")
            .contains("strip = penpot.createRectangle()")
            .contains("strip.resize(symbolSize, symbolSize * 0.25)");
    }

    @Test
    @DisplayName("shouldCapBorderRadiusAt24_whenIntentBorderRadiusIs999_andStyleIsEmbleme")
    void shouldCapBorderRadiusAt24_whenIntentBorderRadiusIs999_andStyleIsEmbleme() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.EMBLEME);
        LogoIntent intent = intent(1.0, 999, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js).contains("container.borderRadius = 24;");
    }

    @Test
    @DisplayName("shouldUseActualBorderRadius_whenBorderRadiusIsNot999_andStyleIsEmbleme")
    void shouldUseActualBorderRadius_whenBorderRadiusIsNot999_andStyleIsEmbleme() {
        // GIVEN
        LogoSpec spec = spec("Meunier", LogoStyle.EMBLEME);
        LogoIntent intent = intent(1.0, 4, false, false);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js).contains("container.borderRadius = 4;");
    }

    @Test
    @DisplayName("shouldRenderLinearGradientFill_whenUseGradientIsTrue")
    void shouldRenderLinearGradientFill_whenUseGradientIsTrue() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.GEOMETRIQUE);
        LogoIntent intent = intent(1.0, 4, false, true); // useGradient = true

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js)
            .contains("fillGradient")
            .contains("type: 'linear'")
            .contains("stops:");
    }

    @Test
    @DisplayName("shouldRenderSolidFill_whenUseGradientIsFalse")
    void shouldRenderSolidFill_whenUseGradientIsFalse() {
        // GIVEN
        LogoSpec spec = spec("Meunier", LogoStyle.GEOMETRIQUE);
        LogoIntent intent = intent(1.0, 4, false, false); // useGradient = false

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js)
            .contains("fillColor: '#FF5C00'")
            .doesNotContain("fillGradient");
    }

    @Test
    @DisplayName("shouldIncludePrimaryAndSecondaryColor_inGradientStops")
    void shouldIncludePrimaryAndSecondaryColor_inGradientStops() {
        // GIVEN
        LogoSpec spec = spec("Ollca", LogoStyle.GEOMETRIQUE);
        LogoIntent intent = intent(1.0, 4, false, true);

        // WHEN
        String js = renderer.render(spec, intent);

        // THEN
        assertThat(js)
            .contains("color: '#FF5C00'")
            .contains("color: '#000000'");
    }

    @Test
    @DisplayName("shouldApplyGradientFill_acrossAllStylesThatSupportIt")
    void shouldApplyGradientFill_acrossAllStylesThatSupportIt() {
        // GIVEN
        LogoIntent gradientIntent = intent(1.0, 4, false, true);

        for (LogoStyle style : new LogoStyle[]{
            LogoStyle.GEOMETRIQUE, LogoStyle.ABSTRAIT,
            LogoStyle.MINIMALISTE, LogoStyle.EMBLEME}) {

            LogoSpec spec = spec("Ollca", style);

            // WHEN
            String js = renderer.render(spec, gradientIntent);

            // THEN
            assertThat(js)
                .as("Style %s should use gradient fill", style)
                .contains("fillGradient");
        }
    }

    private LogoSpec spec(String brandName, LogoStyle style) {
        return LogoSpec.builder()
            .brandName(brandName)
            .style(style)
            .layout(LogoLayout.HORIZONTAL)
            .x(0).y(0)
            .build();
    }

    /**
     * @param scalingFactor  → getScalingFactor()
     * @param borderRadius   → getBorderRadius()
     * @param useBold        → isUseBoldTypography()
     * @param useGradient    → isUseGradient()
     */
    private LogoIntent intent(double scalingFactor, int borderRadius, boolean useBold, boolean useGradient) {
        return new LogoIntent(
            false,
            borderRadius,
            scalingFactor,
            "#FF5C00",
            "#000000",
            "#1A1A1A",
            useBold,
            useGradient
        );
    }
}