package com.penpot.ai.application.tools.logo;

import com.penpot.ai.core.domain.logo.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LogoThemeEngine — Unit")
class LogoThemeEngineUnit {

    private final LogoThemeEngine engine = new LogoThemeEngine();

    @Test
    @DisplayName("shouldSelectNaturePalette_whenBrandNameContainsBio")
    void shouldSelectNaturePalette_whenBrandNameContainsBio() {
        // GIVEN
        LogoSpec spec = logoSpec("BioMarch");
        LogoIntent intent = intentWithStartup(true);

        // WHEN
        LogoIntent result = engine.applyTheme(spec, intent);

        // THEN
        assertThat(result.getPrimaryColor()).isEqualTo("#059669");
    }

    @Test
    @DisplayName("shouldSelectNaturePalette_whenBrandNameContainsVert")
    void shouldSelectNaturePalette_whenBrandNameContainsVert() {
        // GIVEN
        LogoSpec spec = logoSpec("Vertura");
        LogoIntent intent = intentWithStartup(false);

        // WHEN
        LogoIntent result = engine.applyTheme(spec, intent);

        // THEN
        assertThat(result.getPrimaryColor()).isEqualTo("#059669");
    }

    @Test
    @DisplayName("shouldSelectNaturePalette_whenBrandNameContainsFrais")
    void shouldSelectNaturePalette_whenBrandNameContainsFrais() {
        // GIVEN
        LogoSpec spec = logoSpec("Fraisette");
        LogoIntent intent = intentWithStartup(false);

        // WHEN
        LogoIntent result = engine.applyTheme(spec, intent);

        // THEN
        assertThat(result.getPrimaryColor()).isEqualTo("#059669");
    }

    @Test
    @DisplayName("shouldSelectLuxePalette_whenBrandNameContainsLuxe")
    void shouldSelectLuxePalette_whenBrandNameContainsLuxe() {
        // GIVEN
        LogoSpec spec = logoSpec("LuxeParis");
        LogoIntent intent = intentWithStartup(false);

        // WHEN
        LogoIntent result = engine.applyTheme(spec, intent);

        // THEN
        assertThat(result.getPrimaryColor()).isEqualTo("#111827");
    }

    @Test
    @DisplayName("shouldSelectLuxePalette_whenBrandNameContainsPremium")
    void shouldSelectLuxePalette_whenBrandNameContainsPremium() {
        // GIVEN
        LogoSpec spec = logoSpec("PremiumCo");
        LogoIntent intent = intentWithStartup(false);

        // WHEN
        LogoIntent result = engine.applyTheme(spec, intent);

        // THEN
        assertThat(result.getPrimaryColor()).isEqualTo("#111827");
    }

    @Test
    @DisplayName("shouldSelectPassionPalette_whenBrandNameContainsBoucherie")
    void shouldSelectPassionPalette_whenBrandNameContainsBoucherie() {
        // GIVEN
        LogoSpec spec = logoSpec("BoucherieMartin");
        LogoIntent intent = intentWithStartup(false);

        // WHEN
        LogoIntent result = engine.applyTheme(spec, intent);

        // THEN
        assertThat(result.getPrimaryColor()).isEqualTo("#E11D48");
    }

    @Test
    @DisplayName("shouldSelectEnergiePalette_whenStartupVibeAndNoSpecificKeyword")
    void shouldSelectEnergiePalette_whenStartupVibeAndNoSpecificKeyword() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca");
        LogoIntent intent = intentWithStartup(true);

        // WHEN
        LogoIntent result = engine.applyTheme(spec, intent);

        // THEN
        assertThat(result.getPrimaryColor()).isEqualTo("#FF5C00");
    }

    @Test
    @DisplayName("shouldSelectTechPalette_whenNotStartupAndNoSpecificKeyword")
    void shouldSelectTechPalette_whenNotStartupAndNoSpecificKeyword() {
        // GIVEN
        LogoSpec spec = logoSpec("Meunier");
        LogoIntent intent = intentWithStartup(false);

        // WHEN
        LogoIntent result = engine.applyTheme(spec, intent);

        // THEN
        assertThat(result.getPrimaryColor()).isEqualTo("#4F46E5");
    }

    @Test
    @DisplayName("shouldPreserveAllNonColorFields_whenApplyingTheme")
    void shouldPreserveAllNonColorFields_whenApplyingTheme() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca");
        LogoIntent intent = new LogoIntent(true, 999, 1.2,
                "#000000", "#000000", "#000000", true, true);

        // WHEN
        LogoIntent result = engine.applyTheme(spec, intent);

        // THEN
        assertThat(result.isStartupVibe()).isTrue();
        assertThat(result.getBorderRadius()).isEqualTo(999);
        assertThat(result.getScalingFactor()).isEqualTo(1.2);
        assertThat(result.isUseBoldTypography()).isTrue();
        assertThat(result.isUseGradient()).isTrue();
    }

    @Test
    @DisplayName("shouldReturnNewIntentInstance_notSameReferenceAsInput")
    void shouldReturnNewIntentInstance_notSameReferenceAsInput() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca");
        LogoIntent intent = intentWithStartup(true);

        // WHEN
        LogoIntent result = engine.applyTheme(spec, intent);

        // THEN
        assertThat(result).isNotSameAs(intent);
    }

    @Test
    @DisplayName("shouldUpdateAllThreeColorFields_whenThemeIsApplied")
    void shouldUpdateAllThreeColorFields_whenThemeIsApplied() {
        // GIVEN
        LogoSpec spec = logoSpec("TechCorp");
        LogoIntent intent = intentWithStartup(false);

        // WHEN
        LogoIntent result = engine.applyTheme(spec, intent);

        // THEN
        assertThat(result.getPrimaryColor()).isEqualTo("#4F46E5");
        assertThat(result.getSecondaryColor()).isEqualTo("#06B6D4");
        assertThat(result.getTextColor()).isEqualTo("#FFFFFF");
    }

    private LogoSpec logoSpec(String brandName) {
        return LogoSpec.builder()
            .brandName(brandName)
            .style(LogoStyle.GEOMETRIQUE)
            .layout(LogoLayout.HORIZONTAL)
            .x(0).y(0)
            .build();
    }

    private LogoIntent intentWithStartup(boolean isStartup) {
        return new LogoIntent(isStartup, isStartup ? 999 : 4, 1.0,
                "#FF5C00", "#000000", "#1A1A1A", true, isStartup);
    }
}