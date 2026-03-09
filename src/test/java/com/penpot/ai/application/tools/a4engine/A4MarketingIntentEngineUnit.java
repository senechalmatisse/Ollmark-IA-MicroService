package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.engine.MarketingIntent;
import com.penpot.ai.core.domain.marketing.*;
import com.penpot.ai.core.domain.spec.SectionSpec;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("A4MarketingIntentEngine — Unit")
class A4MarketingIntentEngineUnit {

    private final A4MarketingIntentEngine engine = new A4MarketingIntentEngine();

    @ParameterizedTest(name = "shouldDetectUrgency_whenTitleContains [{0}]")
    @ValueSource(strings = {"aujourd", "maintenant", "vite", "offre", "promo", "dernière chance"})
    void shouldDetectUrgency_whenTitleContainsUrgencyKeyword(String keyword) {
        // GIVEN
        SectionSpec spec = spec("Achetez " + keyword + " !", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.containsUrgency).isTrue();
    }

    @Test
    @DisplayName("shouldDetectUrgency_whenParagraphContainsUrgencyKeyword")
    void shouldDetectUrgency_whenParagraphContainsUrgencyKeyword() {
        // GIVEN
        SectionSpec spec = spec("Titre", "Plus que 3 places disponibles");

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.containsUrgency).isTrue();
    }

    @Test
    @DisplayName("shouldNotDetectUrgency_whenNoKeywordPresent")
    void shouldNotDetectUrgency_whenNoKeywordPresent() {
        // GIVEN
        SectionSpec spec = spec("Notre application", "Une solution simple et efficace");

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.containsUrgency).isFalse();
    }

    @ParameterizedTest(name = "shouldDetectSocialProof_whenTextContains [{0}]")
    @ValueSource(strings = {"clients", "avis", "témoignage", "confiance", "recommandé", "note moyenne"})
    void shouldDetectSocialProof_whenTextContainsSocialKeyword(String keyword) {
        // GIVEN
        SectionSpec spec = spec(keyword + " vérifiés", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.containsSocialProof).isTrue();
    }

    @Test
    @DisplayName("shouldNotDetectSocialProof_whenNoKeywordPresent")
    void shouldNotDetectSocialProof_whenNoKeywordPresent() {
        // GIVEN
        SectionSpec spec = spec("Découvrez notre produit", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.containsSocialProof).isFalse();
    }

    @Test
    @DisplayName("shouldDetectLuxury_whenToneIsLuxury")
    void shouldDetectLuxury_whenToneIsLuxury() {
        // GIVEN
        SectionSpec spec = spec("Titre", null);
        spec.setTone(MarketingTone.LUXURY);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isLuxuryPositioning).isTrue();
    }

    @ParameterizedTest(name = "shouldDetectLuxury_whenTextContains [{0}]")
    @ValueSource(strings = {"premium", "luxury", "exclusif", "haut de gamme", "élite", "prestige"})
    void shouldDetectLuxury_whenTextContainsLuxuryKeyword(String keyword) {
        // GIVEN
        SectionSpec spec = spec("Collection " + keyword, null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isLuxuryPositioning).isTrue();
    }

    @ParameterizedTest(name = "shouldDetectMinimal_whenTextContains [{0}]")
    @ValueSource(strings = {"minimal", "simple", "épuré"})
    void shouldDetectMinimal_whenTextContainsMinimalKeyword(String keyword) {
        // GIVEN
        SectionSpec spec = spec("Design " + keyword, null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isMinimalist).isTrue();
    }

    @ParameterizedTest(name = "shouldDetectProductCentric_whenTextContains [{0}]")
    @ValueSource(strings = {"nouveau", "produit", "collection", "solution", "lancement", "découvrez"})
    void shouldDetectProductCentric_whenTextContainsProductKeyword(String keyword) {
        // GIVEN
        SectionSpec spec = spec("Notre " + keyword + " révolutionnaire", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isProductCentric).isTrue();
    }

    @Test
    @DisplayName("shouldBeConversionFocused_whenUrgencyDetected")
    void shouldBeConversionFocused_whenUrgencyDetected() {
        // GIVEN
        SectionSpec spec = spec("Offre limitée", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isConversionFocused).isTrue();
    }

    @Test
    @DisplayName("shouldBeConversionFocused_whenToneIsUrgent")
    void shouldBeConversionFocused_whenToneIsUrgent() {
        // GIVEN
        SectionSpec spec = spec("Notre application", null);
        spec.setTone(MarketingTone.URGENT);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isConversionFocused).isTrue();
    }

    @Test
    @DisplayName("shouldBeConversionFocused_whenPricingKeywordDetected")
    void shouldBeConversionFocused_whenPricingKeywordDetected() {
        // GIVEN
        SectionSpec spec = spec("À partir de 29€ / mois", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.isConversionFocused).isTrue();
    }

    @Test
    @DisplayName("shouldReturnImpactScore95_whenTitleIsShorterThan30Chars")
    void shouldReturnImpactScore95_whenTitleIsShorterThan30Chars() {
        // GIVEN
        SectionSpec spec = spec("Boostez-vous", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.titleImpactScore).isEqualTo(95);
    }

    @Test
    @DisplayName("shouldReturnImpactScore80_whenTitleIsBetween30And60Chars")
    void shouldReturnImpactScore80_whenTitleIsBetween30And60Chars() {
        // GIVEN
        SectionSpec spec = spec("A".repeat(35), null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.titleImpactScore).isEqualTo(80);
    }

    @Test
    @DisplayName("shouldReturnImpactScore70_whenTitleIsBetween60And90Chars")
    void shouldReturnImpactScore70_whenTitleIsBetween60And90Chars() {
        // GIVEN
        SectionSpec spec = spec("A".repeat(65), null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.titleImpactScore).isEqualTo(70);
    }

    @Test
    @DisplayName("shouldReturnImpactScore55_whenTitleIsLongerThan90Chars")
    void shouldReturnImpactScore55_whenTitleIsLongerThan90Chars() {
        // GIVEN
        SectionSpec spec = spec("A".repeat(100), null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.titleImpactScore).isEqualTo(55);
    }

    @Test
    @DisplayName("shouldCapPersuasionScoreAt100_evenWhenMultipleSignalsStack")
    void shouldCapPersuasionScoreAt100_evenWhenMultipleSignalsStack() {
        // GIVEN
        SectionSpec spec = spec("Offre exclusive premium — clients satisfaits — nouveau produit", null);
        spec.setTone(MarketingTone.LUXURY);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.persuasionScore).isLessThanOrEqualTo(100);
    }

    @Test
    @DisplayName("shouldHavePersuasionScore0_whenNoSignalDetected")
    void shouldHavePersuasionScore0_whenNoSignalDetected() {
        // GIVEN
        SectionSpec spec = spec("Bienvenue", "Nous sommes là pour vous");

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.persuasionScore).isEqualTo(0);
    }

    @Test
    @DisplayName("shouldReturnAggressiveCta_whenUrgencyAndConversionBothPresent")
    void shouldReturnAggressiveCta_whenUrgencyAndConversionBothPresent() {
        // GIVEN
        SectionSpec spec = spec("Offre limitée maintenant", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.ctaStrength).isEqualTo(MarketingIntent.CtaStrength.AGGRESSIVE);
    }

    @Test
    @DisplayName("shouldReturnMediumCta_whenOnlySocialProofDetected")
    void shouldReturnMediumCta_whenOnlySocialProofDetected() {
        // GIVEN
        SectionSpec spec = spec("Nos clients sont satisfaits", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.ctaStrength).isEqualTo(MarketingIntent.CtaStrength.MEDIUM);
    }

    @Test
    @DisplayName("shouldReturnSoftCta_whenNoUrgencyNoConversionNoSocialProof")
    void shouldReturnSoftCta_whenNoUrgencyNoConversionNoSocialProof() {
        // GIVEN
        SectionSpec spec = spec("Bienvenue chez nous", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.ctaStrength).isEqualTo(MarketingIntent.CtaStrength.SOFT);
    }

    @Test
    @DisplayName("shouldDetectFlashSaleCampaign_whenUrgencyAndConversionBothPresent")
    void shouldDetectFlashSaleCampaign_whenUrgencyAndConversionBothPresent() {
        // GIVEN
        SectionSpec spec = spec("Offre flash maintenant", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.campaignType).isEqualTo(MarketingIntent.CampaignType.FLASH_SALE);
    }

    @Test
    @DisplayName("shouldDetectProductLaunchCampaign_whenProductCentricAndNoUrgency")
    void shouldDetectProductLaunchCampaign_whenProductCentricAndNoUrgency() {
        // GIVEN
        SectionSpec spec = spec("Le lancement de notre solution", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.campaignType).isEqualTo(MarketingIntent.CampaignType.PRODUCT_LAUNCH);
    }

    @Test
    @DisplayName("shouldDetectPremiumShowcaseCampaign_whenLuxuryPositioned")
    void shouldDetectPremiumShowcaseCampaign_whenLuxuryPositioned() {
        // GIVEN
        SectionSpec spec = spec("Collection prestige", null);

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.campaignType).isEqualTo(MarketingIntent.CampaignType.PREMIUM_SHOWCASE);
    }

    @Test
    @DisplayName("shouldDetectGenericCampaign_whenNoSignalDetected")
    void shouldDetectGenericCampaign_whenNoSignalDetected() {
        // GIVEN
        SectionSpec spec = spec("Bienvenue", "Nous sommes là");

        // WHEN
        MarketingIntent intent = engine.analyze(spec);

        // THEN
        assertThat(intent.campaignType).isEqualTo(MarketingIntent.CampaignType.GENERIC);
    }

    @Test
    @DisplayName("shouldHandleNullTitleAndParagraph_withoutThrowing")
    void shouldHandleNullTitleAndParagraph_withoutThrowing() {
        // GIVEN
        SectionSpec spec = spec(null, null);

        // WHEN / THEN
        assertThat(engine.analyze(spec)).isNotNull();
    }

    private SectionSpec spec(String title, String paragraph) {
        SectionSpec spec = new SectionSpec();
        spec.setTitle(title);
        spec.setParagraph(paragraph);
        return spec;
    }
}