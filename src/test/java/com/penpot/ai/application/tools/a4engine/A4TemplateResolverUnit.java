package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.engine.MarketingIntent;
import com.penpot.ai.core.domain.marketing.MarketingLayoutType;
import com.penpot.ai.core.domain.spec.SectionSpec;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("A4TemplateResolver — Unit")
class A4TemplateResolverUnit {

    private final A4TemplateResolver resolver = new A4TemplateResolver();

    @Test
    @DisplayName("shouldReturnHeroImageLeft_whenLayoutIsHeroSplit")
    void shouldReturnHeroImageLeft_whenLayoutIsHeroSplit() {
        // GIVEN
        SectionSpec spec = spec(MarketingLayoutType.HERO_SPLIT);
        MarketingIntent intent = intentGeneric();

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intent);

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.HERO_IMAGE_LEFT);
    }

    @Test
    @DisplayName("shouldReturnHeroCenterStack_whenLayoutIsHeroCentered")
    void shouldReturnHeroCenterStack_whenLayoutIsHeroCentered() {
        // GIVEN
        SectionSpec spec = spec(MarketingLayoutType.HERO_CENTERED);

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intentGeneric());

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.HERO_CENTER_STACK);
    }

    @Test
    @DisplayName("shouldReturnHeroImageRight_whenLayoutIsHeroWithImage")
    void shouldReturnHeroImageRight_whenLayoutIsHeroWithImage() {
        // GIVEN
        SectionSpec spec = spec(MarketingLayoutType.HERO_WITH_IMAGE);

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intentGeneric());

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.HERO_IMAGE_RIGHT);
    }

    @Test
    @DisplayName("shouldReturnStatsHeavyPoster_whenLayoutIsHeroWithStats")
    void shouldReturnStatsHeavyPoster_whenLayoutIsHeroWithStats() {
        // GIVEN
        SectionSpec spec = spec(MarketingLayoutType.HERO_WITH_STATS);

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intentGeneric());

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.STATS_HEAVY_POSTER);
    }

    @Test
    @DisplayName("shouldReturnFeatureGrid3_whenLayoutIsFeatureList")
    void shouldReturnFeatureGrid3_whenLayoutIsFeatureList() {
        // GIVEN
        SectionSpec spec = spec(MarketingLayoutType.FEATURE_LIST);

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intentGeneric());

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.FEATURE_GRID_3);
    }

    @Test
    @DisplayName("shouldReturnCtaFocused_whenLayoutIsLandingBlock")
    void shouldReturnCtaFocused_whenLayoutIsLandingBlock() {
        // GIVEN
        SectionSpec spec = spec(MarketingLayoutType.LANDING_BLOCK);

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intentGeneric());

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.CTA_FOCUSED);
    }

    @Test
    @DisplayName("shouldReturnSalePoster_whenLayoutIsPromoSection")
    void shouldReturnSalePoster_whenLayoutIsPromoSection() {
        // GIVEN
        SectionSpec spec = spec(MarketingLayoutType.PROMO_SECTION);

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intentGeneric());

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.SALE_POSTER);
    }

    @Test
    @DisplayName("shouldReturnCountdownPromo_whenCampaignIsFlashSaleAndUrgencyPresent")
    void shouldReturnCountdownPromo_whenCampaignIsFlashSaleAndUrgencyPresent() {
        // GIVEN
        SectionSpec spec = specNoLayout();
        MarketingIntent intent = intentBuilder()
            .conversion(true).urgency(true)
            .campaign(MarketingIntent.CampaignType.FLASH_SALE)
            .build();

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intent);

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.COUNTDOWN_PROMO);
    }

    @Test
    @DisplayName("shouldReturnSalePoster_whenCampaignIsFlashSaleButNoUrgency")
    void shouldReturnSalePoster_whenCampaignIsFlashSaleButNoUrgency() {
        // GIVEN
        SectionSpec spec = specNoLayout();
        MarketingIntent intent = intentBuilder()
            .campaign(MarketingIntent.CampaignType.FLASH_SALE)
            .build();

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intent);

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.SALE_POSTER);
    }

    @Test
    @DisplayName("shouldReturnProductShowcase_whenCampaignIsProductLaunchAndProductCentric")
    void shouldReturnProductShowcase_whenCampaignIsProductLaunchAndProductCentric() {
        // GIVEN
        SectionSpec spec = specNoLayout();
        MarketingIntent intent = intentBuilder()
            .campaign(MarketingIntent.CampaignType.PRODUCT_LAUNCH)
            .productCentric(true)
            .build();

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intent);

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.PRODUCT_SHOWCASE);
    }

    @Test
    @DisplayName("shouldReturnFeatureGrid3_whenCampaignIsProductLaunchButNotProductCentric")
    void shouldReturnFeatureGrid3_whenCampaignIsProductLaunchButNotProductCentric() {
        // GIVEN
        SectionSpec spec = specNoLayout();
        MarketingIntent intent = intentBuilder()
            .campaign(MarketingIntent.CampaignType.PRODUCT_LAUNCH)
            .build();

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intent);

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.FEATURE_GRID_3);
    }

    @Test
    @DisplayName("shouldReturnCenteredMinimal_whenLuxuryPositioned")
    void shouldReturnCenteredMinimal_whenLuxuryPositioned() {
        // GIVEN
        SectionSpec spec = specNoLayout();
        MarketingIntent intent = intentBuilder().luxury(true).build();

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intent);

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.CENTERED_MINIMAL);
    }

    @Test
    @DisplayName("shouldReturnTestimonialFocus_whenSocialProofAndConversionFocused")
    void shouldReturnTestimonialFocus_whenSocialProofAndConversionFocused() {
        // GIVEN
        SectionSpec spec = specNoLayout();
        MarketingIntent intent = intentBuilder()
            .socialProof(true).conversion(true)
            .build();

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intent);

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.TESTIMONIAL_FOCUS);
    }

    @Test
    @DisplayName("shouldReturnFeatureGrid3_whenSocialProofButNotConversionFocused")
    void shouldReturnFeatureGrid3_whenSocialProofButNotConversionFocused() {
        // GIVEN
        SectionSpec spec = specNoLayout();
        MarketingIntent intent = intentBuilder().socialProof(true).build();

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intent);

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.FEATURE_GRID_3);
    }

    @Test
    @DisplayName("shouldReturnPricingTable_whenCampaignIsLeadCapture")
    void shouldReturnPricingTable_whenCampaignIsLeadCapture() {
        // GIVEN
        SectionSpec spec = specNoLayout();
        MarketingIntent intent = intentBuilder()
            .campaign(MarketingIntent.CampaignType.LEAD_CAPTURE)
            .build();

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intent);

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.PRICING_TABLE);
    }

    @Test
    @DisplayName("shouldReturnCenteredMinimal_asSafeFallback_whenNoSignalMatches")
    void shouldReturnCenteredMinimal_asSafeFallback_whenNoSignalMatches() {
        // GIVEN
        SectionSpec spec = specNoLayout();
        MarketingIntent intent = intentGeneric();

        // WHEN
        A4LayoutTemplate result = resolver.resolveTemplate(spec, intent);

        // THEN
        assertThat(result).isEqualTo(A4LayoutTemplate.CENTERED_MINIMAL);
    }

    private SectionSpec spec(MarketingLayoutType layout) {
        SectionSpec spec = new SectionSpec();
        spec.setLayout(layout);
        return spec;
    }

    private SectionSpec specNoLayout() {
        return new SectionSpec();
    }

    private MarketingIntent intentGeneric() {
        return intentBuilder().build();
    }

    private IntentBuilder intentBuilder() {
        return new IntentBuilder();
    }

    /** Builder local pour construire des MarketingIntent lisiblement. */
    private static class IntentBuilder {
        private boolean conversion, urgency, socialProof, luxury, productCentric, minimal;
        private int titleScore = 55, persuasion;
        private MarketingIntent.CtaStrength cta = MarketingIntent.CtaStrength.SOFT;
        private MarketingIntent.HierarchyLevel hierarchy = MarketingIntent.HierarchyLevel.SOFT_BRANDING;
        private MarketingIntent.CampaignType campaign = MarketingIntent.CampaignType.GENERIC;

        IntentBuilder conversion(boolean v) { conversion = v; return this; }
        IntentBuilder urgency(boolean v) { urgency = v; return this; }
        IntentBuilder socialProof(boolean v) { socialProof = v; return this; }
        IntentBuilder luxury(boolean v) { luxury = v; return this; }
        IntentBuilder productCentric(boolean v) { productCentric = v; return this; }
        IntentBuilder campaign(MarketingIntent.CampaignType c) { campaign = c; return this; }

        MarketingIntent build() {
            return new MarketingIntent(
                conversion, urgency, socialProof, luxury,
                productCentric, minimal,
                titleScore, persuasion,
                cta, hierarchy, campaign
            );
        }
    }
}