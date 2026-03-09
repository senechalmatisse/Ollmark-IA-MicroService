package com.penpot.ai.application.tools.logo;

import com.penpot.ai.core.domain.logo.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LogoIntentEngine — Unit")
class LogoIntentEngineUnit {

    private final LogoIntentEngine engine = new LogoIntentEngine();

    @ParameterizedTest(name = "shouldDetectStartupVibe_whenBrandNameContains [{0}]")
    @ValueSource(strings = {"app", "digital", "livraison", "local", "click", "smart", "bio", "shop"})
    void shouldDetectStartupVibe_whenBrandNameContainsStartupKeyword(String keyword) {
        // GIVEN
        LogoSpec spec = specWithBrand(keyword + "Co", LogoStyle.GEOMETRIQUE);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isStartupVibe()).isTrue();
    }

    @Test
    @DisplayName("shouldDetectStartupVibe_whenTaglineContainsStartupKeyword")
    void shouldDetectStartupVibe_whenTaglineContainsStartupKeyword() {
        // GIVEN
        LogoSpec spec = logoSpec("Acme", "Livraison express", LogoStyle.GEOMETRIQUE);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isStartupVibe()).isTrue();
    }

    @Test
    @DisplayName("shouldNotDetectStartupVibe_whenNeitherBrandNorTaglineContainsKeyword")
    void shouldNotDetectStartupVibe_whenNeitherBrandNorTaglineContainsKeyword() {
        // GIVEN
        LogoSpec spec = logoSpec("Meunier", "Artisan boulanger depuis 1920", LogoStyle.GEOMETRIQUE);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isStartupVibe()).isFalse();
    }

    @Test
    @DisplayName("shouldHandleNullBrandNameAndNullTagline_withoutThrowing")
    void shouldHandleNullBrandNameAndNullTagline_withoutThrowing() {
        // GIVEN
        LogoSpec spec = logoSpec(null, null, LogoStyle.GEOMETRIQUE);

        // WHEN / THEN
        LogoIntent intent = engine.analyze(spec);
        assertThat(intent).isNotNull();
        assertThat(intent.isStartupVibe()).isFalse();
    }

    @Test
    @DisplayName("shouldSetBorderRadius999_whenStartupVibe")
    void shouldSetBorderRadius999_whenStartupVibe() {
        // GIVEN
        LogoSpec spec = specWithBrand("MyApp", LogoStyle.GEOMETRIQUE);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.getBorderRadius()).isEqualTo(999);
    }

    @Test
    @DisplayName("shouldSetBorderRadius4_whenNotStartupVibe")
    void shouldSetBorderRadius4_whenNotStartupVibe() {
        // GIVEN
        LogoSpec spec = logoSpec("Meunier", "Artisan boulanger", LogoStyle.GEOMETRIQUE);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.getBorderRadius()).isEqualTo(4);
    }

    @Test
    @DisplayName("shouldUseBoldTypography_whenStyleIsNotMinimaliste")
    void shouldUseBoldTypography_whenStyleIsNotMinimaliste() {
        // GIVEN
        LogoSpec spec = specWithBrand("Ollca", LogoStyle.ABSTRAIT);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isUseBoldTypography()).isTrue();
    }

    @Test
    @DisplayName("shouldNotUseBoldTypography_whenStyleIsMinimaliste")
    void shouldNotUseBoldTypography_whenStyleIsMinimaliste() {
        // GIVEN
        LogoSpec spec = specWithBrand("Ollca", LogoStyle.MINIMALISTE);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isUseBoldTypography()).isFalse();
    }

    @Test
    @DisplayName("shouldUseGradient_whenStartupVibe")
    void shouldUseGradient_whenStartupVibe() {
        // GIVEN
        LogoSpec spec = specWithBrand("MyApp", LogoStyle.GEOMETRIQUE);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isUseGradient()).isTrue();
    }

    @Test
    @DisplayName("shouldUseGradient_whenStyleIsAbstrait_evenWithoutStartupVibe")
    void shouldUseGradient_whenStyleIsAbstrait_evenWithoutStartupVibe() {
        // GIVEN
        LogoSpec spec = specWithBrand("Meunier", LogoStyle.ABSTRAIT);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isUseGradient()).isTrue();
    }

    @Test
    @DisplayName("shouldNotUseGradient_whenNotStartupAndNotAbstrait")
    void shouldNotUseGradient_whenNotStartupAndNotAbstrait() {
        // GIVEN
        LogoSpec spec = specWithBrand("Meunier", LogoStyle.GEOMETRIQUE);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isUseGradient()).isFalse();
    }

    @Test
    @DisplayName("shouldSetScalingFactor1_2_whenBrandNameShorterThan6Chars")
    void shouldSetScalingFactor1_2_whenBrandNameShorterThan6Chars() {
        // GIVEN
        LogoSpec spec = specWithBrand("Neo", LogoStyle.GEOMETRIQUE);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.getScalingFactor()).isEqualTo(1.2);
    }

    @Test
    @DisplayName("shouldSetScalingFactor1_0_whenBrandNameIsExactly6Chars")
    void shouldSetScalingFactor1_0_whenBrandNameIsExactly6Chars() {
        // GIVEN
        LogoSpec spec = specWithBrand("Ollcaz", LogoStyle.GEOMETRIQUE);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.getScalingFactor()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("shouldSetScalingFactor1_0_whenBrandNameIsLongerThan6Chars")
    void shouldSetScalingFactor1_0_whenBrandNameIsLongerThan6Chars() {
        // GIVEN
        LogoSpec spec = specWithBrand("TechNova", LogoStyle.GEOMETRIQUE);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.getScalingFactor()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("shouldReturnDefaultColors_beforeThemeEngineIsApplied")
    void shouldReturnDefaultColors_beforeThemeEngineIsApplied() {
        // GIVEN
        LogoSpec spec = specWithBrand("Ollca", LogoStyle.GEOMETRIQUE);

        // WHEN
        LogoIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.getPrimaryColor()).isEqualTo("#FF5C00");
        assertThat(intent.getSecondaryColor()).isEqualTo("#000000");
        assertThat(intent.getTextColor()).isEqualTo("#1A1A1A");
    }

    private LogoSpec specWithBrand(String brandName, LogoStyle style) {
        return logoSpec(brandName, null, style);
    }

    private LogoSpec logoSpec(String brandName, String tagline, LogoStyle style) {
        return LogoSpec.builder()
            .brandName(brandName)
            .tagline(tagline)
            .style(style)
            .layout(LogoLayout.HORIZONTAL)
            .x(0).y(0)
            .build();
    }
}