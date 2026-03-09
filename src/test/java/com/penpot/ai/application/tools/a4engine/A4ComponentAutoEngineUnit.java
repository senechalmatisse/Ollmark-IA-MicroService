package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.engine.MarketingIntent;
import com.penpot.ai.core.domain.marketing.*;
import com.penpot.ai.core.domain.spec.SectionSpec;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;

@DisplayName("A4ComponentAutoEngine — Unit")
class A4ComponentAutoEngineUnit {

    private final A4ComponentAutoEngine engine = new A4ComponentAutoEngine();

    @Test
    @DisplayName("shouldThrow_whenSpecIsNull")
    void shouldThrow_whenSpecIsNull() {
        // GIVEN / WHEN / THEN
        assertThatThrownBy(() -> engine.enrich(null, intentGeneric()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SectionSpec cannot be null");
    }

    @Test
    @DisplayName("shouldThrow_whenIntentIsNull")
    void shouldThrow_whenIntentIsNull() {
        // GIVEN / WHEN / THEN
        assertThatThrownBy(() -> engine.enrich(new SectionSpec(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("MarketingIntent cannot be null");
    }

    @Test
    @DisplayName("shouldInitializeComponentsList_whenSpecComponentsAreNull")
    void shouldInitializeComponentsList_whenSpecComponentsAreNull() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setComponents(null);

        // WHEN
        engine.enrich(spec, intentGeneric());

        // THEN
        assertThat(spec.getComponents()).isNotNull();
    }

    @Test
    @DisplayName("shouldAddStatsBlock_whenWithStatsIsTrue")
    void shouldAddStatsBlock_whenWithStatsIsTrue() {
        // GIVEN
        SectionSpec spec = emptySpec();
        spec.setWithStats(true);

        // WHEN
        engine.enrich(spec, intentGeneric());

        // THEN
        assertThat(spec.getComponents()).contains(MarketingComponent.STATS_BLOCK);
    }

    @Test
    @DisplayName("shouldAddFeatureList_whenWithFeaturesIsTrue")
    void shouldAddFeatureList_whenWithFeaturesIsTrue() {
        // GIVEN
        SectionSpec spec = emptySpec();
        spec.setWithFeatures(true);

        // WHEN
        engine.enrich(spec, intentGeneric());

        // THEN
        assertThat(spec.getComponents()).contains(MarketingComponent.FEATURE_LIST);
    }

    @Test
    @DisplayName("shouldAddTestimonialCard_whenWithTestimonialIsTrue")
    void shouldAddTestimonialCard_whenWithTestimonialIsTrue() {
        // GIVEN
        SectionSpec spec = emptySpec();
        spec.setWithTestimonial(true);

        // WHEN
        engine.enrich(spec, intentGeneric());

        // THEN
        assertThat(spec.getComponents()).contains(MarketingComponent.TESTIMONIAL_CARD);
    }

    @Test
    @DisplayName("shouldAddPricingCardAndTrustLogos_whenConversionFocused")
    void shouldAddPricingCardAndTrustLogos_whenConversionFocused() {
        // GIVEN
        SectionSpec spec = emptySpec();
        MarketingIntent intent = intent(true, false, false, false, false, false);

        // WHEN
        engine.enrich(spec, intent);

        // THEN
        assertThat(spec.getComponents()).contains(
            MarketingComponent.PRICING_CARD_PREVIEW,
            MarketingComponent.TRUST_LOGOS
        );
    }

    @Test
    @DisplayName("shouldAddTestimonialCard_whenConversionAndSocialProofBothPresent")
    void shouldAddTestimonialCard_whenConversionAndSocialProofBothPresent() {
        // GIVEN
        SectionSpec spec = emptySpec();
        MarketingIntent intent = intent(true, false, true, false, false, false);

        // WHEN
        engine.enrich(spec, intent);

        // THEN
        assertThat(spec.getComponents()).contains(MarketingComponent.TESTIMONIAL_CARD);
    }

    @Test
    @DisplayName("shouldAddUrgencyComponents_whenConversionAndUrgencyBothPresent")
    void shouldAddUrgencyComponents_whenConversionAndUrgencyBothPresent() {
        // GIVEN
        SectionSpec spec = emptySpec();
        MarketingIntent intent = intent(true, true, false, false, false, false);

        // WHEN
        engine.enrich(spec, intent);

        // THEN
        assertThat(spec.getComponents()).contains(
            MarketingComponent.BADGE,
            MarketingComponent.DISCOUNT_RIBBON,
            MarketingComponent.COUNTDOWN_TIMER,
            MarketingComponent.ANNOUNCEMENT_BAR
        );
    }

    @Test
    @DisplayName("shouldAddTestimonialTrustLogosAndStatsBlock_whenSocialProofAndNotConversion")
    void shouldAddTestimonialTrustLogosAndStatsBlock_whenSocialProofAndNotConversion() {
        // GIVEN
        SectionSpec spec = emptySpec();
        MarketingIntent intent = intent(false, false, true, false, false, false);

        // WHEN
        engine.enrich(spec, intent);

        // THEN
        assertThat(spec.getComponents()).contains(
            MarketingComponent.TESTIMONIAL_CARD,
            MarketingComponent.TRUST_LOGOS,
            MarketingComponent.STATS_BLOCK
        );
    }

    @Test
    @DisplayName("shouldAddFeatureListAndBadge_whenProductCentricAndNotUrgent")
    void shouldAddFeatureListAndBadge_whenProductCentricAndNotUrgent() {
        // GIVEN
        SectionSpec spec = emptySpec();
        MarketingIntent intent = intent(false, false, false, false, true, false);

        // WHEN
        engine.enrich(spec, intent);

        // THEN
        assertThat(spec.getComponents()).contains(
            MarketingComponent.FEATURE_LIST,
            MarketingComponent.BADGE
        );
    }

    @Test
    @DisplayName("shouldAddTrustLogosAndTestimonial_whenLuxury")
    void shouldAddTrustLogosAndTestimonial_whenLuxury() {
        // GIVEN
        SectionSpec spec = emptySpec();
        MarketingIntent intent = intent(false, false, false, true, false, false);

        // WHEN
        engine.enrich(spec, intent);

        // THEN
        assertThat(spec.getComponents()).contains(
            MarketingComponent.TRUST_LOGOS,
            MarketingComponent.TESTIMONIAL_CARD
        );
    }

    @Test
    @DisplayName("shouldRemoveDiscountRibbonCountdownAndAnnouncementBar_whenLuxury")
    void shouldRemoveDiscountRibbonCountdownAndAnnouncementBar_whenLuxury() {
        // GIVEN
        SectionSpec spec = emptySpec();
        spec.getComponents().add(MarketingComponent.DISCOUNT_RIBBON);
        spec.getComponents().add(MarketingComponent.COUNTDOWN_TIMER);
        spec.getComponents().add(MarketingComponent.ANNOUNCEMENT_BAR);

        MarketingIntent intent = intent(false, false, false, true, false, false);

        // WHEN
        engine.enrich(spec, intent);

        // THEN
        assertThat(spec.getComponents()).doesNotContain(
            MarketingComponent.DISCOUNT_RIBBON,
            MarketingComponent.COUNTDOWN_TIMER,
            MarketingComponent.ANNOUNCEMENT_BAR
        );
    }

    @Test
    @DisplayName("shouldKeepOnlyMinimalComponents_whenIntentIsMinimalist")
    void shouldKeepOnlyMinimalComponents_whenIntentIsMinimalist() {
        // GIVEN
        SectionSpec spec = emptySpec();
        spec.getComponents().add(MarketingComponent.COUNTDOWN_TIMER);
        spec.getComponents().add(MarketingComponent.TRUST_LOGOS);
        spec.getComponents().add(MarketingComponent.BADGE);

        MarketingIntent intent = intent(false, false, false, false, false, true);

        // WHEN
        engine.enrich(spec, intent);

        // THEN
        assertThat(spec.getComponents()).contains(MarketingComponent.TRUST_LOGOS);
        assertThat(spec.getComponents()).doesNotContain(
            MarketingComponent.COUNTDOWN_TIMER,
            MarketingComponent.BADGE
        );
    }

    @Test
    @DisplayName("shouldAddAllUrgencyComponents_whenToneIsUrgent")
    void shouldAddAllUrgencyComponents_whenToneIsUrgent() {
        // GIVEN
        SectionSpec spec = emptySpec();
        spec.setTone(MarketingTone.URGENT);

        // WHEN
        engine.enrich(spec, intentGeneric());

        // THEN
        assertThat(spec.getComponents()).contains(
            MarketingComponent.BADGE,
            MarketingComponent.DISCOUNT_RIBBON,
            MarketingComponent.COUNTDOWN_TIMER,
            MarketingComponent.ANNOUNCEMENT_BAR
        );
    }

    @Test
    @DisplayName("shouldAddTrustLogosAndTestimonial_andRemovePromoComponents_whenToneIsPremium")
    void shouldAddTrustLogosAndTestimonial_andRemovePromoComponents_whenToneIsPremium() {
        // GIVEN
        SectionSpec spec = emptySpec();
        spec.setTone(MarketingTone.PREMIUM);
        spec.getComponents().add(MarketingComponent.DISCOUNT_RIBBON);

        // WHEN
        engine.enrich(spec, intentGeneric());

        // THEN
        assertThat(spec.getComponents()).contains(
            MarketingComponent.TRUST_LOGOS,
            MarketingComponent.TESTIMONIAL_CARD
        );
        assertThat(spec.getComponents()).doesNotContain(MarketingComponent.DISCOUNT_RIBBON);
    }

    @Test
    @DisplayName("shouldSortComponentsByRank_withAnnouncementBarFirst")
    void shouldSortComponentsByRank_withAnnouncementBarFirst() {
        // GIVEN
        SectionSpec spec = emptySpec();
        spec.getComponents().add(MarketingComponent.TRUST_LOGOS);    // rank 80
        spec.getComponents().add(MarketingComponent.ANNOUNCEMENT_BAR); // rank 10

        // WHEN
        engine.enrich(spec, intentGeneric());

        // THEN
        int idxAnn   = spec.getComponents().indexOf(MarketingComponent.ANNOUNCEMENT_BAR);
        int idxTrust = spec.getComponents().indexOf(MarketingComponent.TRUST_LOGOS);
        assertThat(idxAnn).isLessThan(idxTrust);
    }

    @Test
    @DisplayName("shouldCapComponentListAt7_whenMoreThan7ComponentsAreCollected")
    void shouldCapComponentListAt7_whenMoreThan7ComponentsAreCollected() {
        // GIVEN
        SectionSpec spec = emptySpec();
        spec.setWithStats(true);
        spec.setWithFeatures(true);
        spec.setWithTestimonial(true);
        spec.setTone(MarketingTone.URGENT);
        MarketingIntent intent = intent(true, true, true, false, true, false);

        // WHEN
        engine.enrich(spec, intent);

        // THEN
        assertThat(spec.getComponents()).hasSizeLessThanOrEqualTo(7);
    }

    @Test
    @DisplayName("shouldNotDuplicateComponents_whenSameComponentAddedByMultipleSources")
    void shouldNotDuplicateComponents_whenSameComponentAddedByMultipleSources() {
        // GIVEN
        SectionSpec spec = emptySpec();
        spec.setWithStats(true);
        MarketingIntent intent = intent(false, false, true, false, false, false);

        // WHEN
        engine.enrich(spec, intent);

        // THEN
        long count = spec.getComponents().stream()
            .filter(c -> c == MarketingComponent.STATS_BLOCK)
            .count();
        assertThat(count).isEqualTo(1);
    }

    private SectionSpec emptySpec() {
        SectionSpec spec = new SectionSpec();
        spec.setComponents(new ArrayList<>());
        return spec;
    }

    private MarketingIntent intentGeneric() {
        return intent(false, false, false, false, false, false);
    }

    private MarketingIntent intent(
        boolean conversion, boolean urgency,
        boolean social, boolean luxury,
        boolean productCentric, boolean minimal
    ) {
        return new MarketingIntent(
            conversion, urgency, social, luxury, productCentric, minimal,
            55, 0,
            MarketingIntent.CtaStrength.SOFT,
            MarketingIntent.HierarchyLevel.SOFT_BRANDING,
            MarketingIntent.CampaignType.GENERIC
        );
    }
}