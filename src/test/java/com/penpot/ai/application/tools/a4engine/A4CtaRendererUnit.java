package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.engine.Theme;
import com.penpot.ai.core.domain.spec.SectionSpec;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("A4CtaRenderer — Unit")
class A4CtaRendererUnit {

    private final A4CtaRenderer renderer = new A4CtaRenderer();

    @Test
    @DisplayName("shouldReturnEmptyString_whenBothButtonsAreNull")
    void shouldReturnEmptyString_whenBothButtonsAreNull() {
        // GIVEN
        SectionSpec spec = spec(null, null);

        // WHEN
        String js = renderer.render(spec, Theme.MINIMAL_LIGHT);

        // THEN
        assertThat(js).isEmpty();
    }

    @Test
    @DisplayName("shouldReturnEmptyString_whenBothButtonsAreBlank")
    void shouldReturnEmptyString_whenBothButtonsAreBlank() {
        // GIVEN
        SectionSpec spec = spec("   ", "   ");

        // WHEN
        String js = renderer.render(spec, Theme.MINIMAL_LIGHT);

        // THEN
        assertThat(js).isEmpty();
    }

    @Test
    @DisplayName("shouldReturnEmptyString_whenPrimaryIsNullAndSecondaryIsBlank")
    void shouldReturnEmptyString_whenPrimaryIsNullAndSecondaryIsBlank() {
        // GIVEN
        SectionSpec spec = spec(null, "  ");

        // WHEN
        String js = renderer.render(spec, Theme.MINIMAL_LIGHT);

        // THEN
        assertThat(js).isEmpty();
    }

    @Test
    @DisplayName("shouldIncludePrimaryButtonLabel_whenPrimaryIsProvided")
    void shouldIncludePrimaryButtonLabel_whenPrimaryIsProvided() {
        // GIVEN
        SectionSpec spec = spec("Commander maintenant", null);

        // WHEN
        String js = renderer.render(spec, Theme.ECOMMERCE_RED);

        // THEN
        assertThat(js).contains("Commander maintenant");
    }

    @Test
    @DisplayName("shouldUseChtaPrimaryAsBackground_forPrimaryButton")
    void shouldUseCtaPrimaryAsBackground_forPrimaryButton() {
        // GIVEN
        SectionSpec spec = spec("Acheter", null);
        Theme theme = Theme.ECOMMERCE_RED;

        // WHEN
        String js = renderer.render(spec, theme);

        // THEN
        assertThat(js).contains(theme.ctaPrimary);
    }

    @Test
    @DisplayName("shouldUseCtaTextAsForeground_forPrimaryButton")
    void shouldUseCtaTextAsForeground_forPrimaryButton() {
        // GIVEN
        SectionSpec spec = spec("Essayer", null);
        Theme theme = Theme.STARTUP_PURPLE;

        // WHEN
        String js = renderer.render(spec, theme);

        // THEN
        assertThat(js).contains(theme.ctaText);
    }

    @Test
    @DisplayName("shouldIncludeSecondaryButtonLabel_whenSecondaryIsProvided")
    void shouldIncludeSecondaryButtonLabel_whenSecondaryIsProvided() {
        // GIVEN
        SectionSpec spec = spec(null, "En savoir plus");

        // WHEN
        String js = renderer.render(spec, Theme.CORPORATE_BLUE);

        // THEN
        assertThat(js).contains("En savoir plus");
    }

    @Test
    @DisplayName("shouldUseTextOnDark_asSecondaryColor_whenThemeIsGradient")
    void shouldUseTextOnDark_asSecondaryColor_whenThemeIsGradient() {
        // GIVEN
        SectionSpec spec = spec(null, "Découvrir");
        Theme theme = Theme.DARK_SAAS;

        // WHEN
        String js = renderer.render(spec, theme);

        // THEN
        assertThat(js).contains(theme.textOnDark);
    }

    @Test
    @DisplayName("shouldUseTextOnLight_asSecondaryColor_whenThemeIsSolid")
    void shouldUseTextOnLight_asSecondaryColor_whenThemeIsSolid() {
        // GIVEN
        SectionSpec spec = spec(null, "Voir plus");
        Theme theme = Theme.MINIMAL_LIGHT;

        // WHEN
        String js = renderer.render(spec, theme);

        // THEN
        assertThat(js).contains(theme.textOnLight);
    }

    @Test
    @DisplayName("shouldIncludeBothButtonLabels_whenBothAreProvided")
    void shouldIncludeBothButtonLabels_whenBothAreProvided() {
        // GIVEN
        SectionSpec spec = spec("Acheter", "En savoir plus");

        // WHEN
        String js = renderer.render(spec, Theme.MODERN_GREEN);

        // THEN
        assertThat(js)
            .contains("Acheter")
            .contains("En savoir plus");
    }

    @Test
    @DisplayName("shouldIncludeCTASpacingAndCreateButtonFunction_whenAnyButtonIsPresent")
    void shouldIncludeCTASpacingAndCreateButtonFunction_whenAnyButtonIsPresent() {
        // GIVEN
        SectionSpec spec = spec("Go", null);

        // WHEN
        String js = renderer.render(spec, Theme.NEON_FUTURE);

        // THEN
        assertThat(js)
            .contains("CTA_SPACING")
            .contains("createA4Button");
    }

    @Test
    @DisplayName("shouldEscapeApostropheInPrimaryButton")
    void shouldEscapeApostropheInPrimaryButton() {
        // GIVEN
        SectionSpec spec = spec("J'achète", null);

        // WHEN
        String js = renderer.render(spec, Theme.MINIMAL_LIGHT);

        // THEN
        assertThat(js).contains("J\\'achète");
    }

    @Test
    @DisplayName("shouldEscapeNewlineInSecondaryButton")
    void shouldEscapeNewlineInSecondaryButton() {
        // GIVEN
        SectionSpec spec = spec(null, "Ligne1\nLigne2");

        // WHEN
        String js = renderer.render(spec, Theme.MINIMAL_LIGHT);

        // THEN
        assertThat(js).contains("Ligne1\\nLigne2");
    }

    @Test
    @DisplayName("shouldEscapeBackslashInButton")
    void shouldEscapeBackslashInButton() {
        // GIVEN
        SectionSpec spec = spec("Dossier\\Fichier", null);

        // WHEN
        String js = renderer.render(spec, Theme.MINIMAL_LIGHT);

        // THEN
        assertThat(js).contains("Dossier\\\\Fichier");
    }

    private SectionSpec spec(String primary, String secondary) {
        SectionSpec spec = new SectionSpec();
        spec.setPrimaryButton(primary);
        spec.setSecondaryButton(secondary);
        return spec;
    }
}