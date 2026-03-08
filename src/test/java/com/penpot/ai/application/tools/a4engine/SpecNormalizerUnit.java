package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.core.domain.marketing.*;
import com.penpot.ai.core.domain.spec.SectionSpec;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SpecNormalizer — Unit")
class SpecNormalizerUnit {

    private final SpecNormalizer normalizer = new SpecNormalizer();

    @Test
    @DisplayName("shouldThrowIllegalArgumentException_whenSpecIsNull")
    void shouldThrowIllegalArgumentException_whenSpecIsNull() {
        // GIVEN
        SectionSpec spec = null;

        // WHEN / THEN
        assertThatThrownBy(() -> normalizer.normalize(spec))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SectionSpec cannot be null");
    }

    @Test
    @DisplayName("shouldDefaultGenerationModeToSmartAssisted_whenNull")
    void shouldDefaultGenerationModeToSmartAssisted_whenNull() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setGenerationMode(null);
        spec.setTitle("Titre");

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getGenerationMode()).isEqualTo(SectionSpec.GenerationMode.SMART_ASSISTED);
    }

    @Test
    @DisplayName("shouldDefaultLayoutToHeroSplit_whenNull")
    void shouldDefaultLayoutToHeroSplit_whenNull() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setLayout(null);

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getLayout()).isEqualTo(MarketingLayoutType.HERO_SPLIT);
    }

    @Test
    @DisplayName("shouldDefaultToneToStartup_whenNull")
    void shouldDefaultToneToStartup_whenNull() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setTone(null);

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getTone()).isEqualTo(MarketingTone.STARTUP);
    }

    @Test
    @DisplayName("shouldDefaultToneToStartup_whenToneIsUnknown")
    void shouldDefaultToneToStartup_whenToneIsUnknown() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setTone(MarketingTone.UNKNOWN);

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getTone()).isEqualTo(MarketingTone.STARTUP);
    }

    @Test
    @DisplayName("shouldInitializeComponentsList_whenNull")
    void shouldInitializeComponentsList_whenNull() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setComponents(null);

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).isNotNull();
    }

    @Test
    @DisplayName("shouldRemoveNullComponents")
    void shouldRemoveNullComponents() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
        List<MarketingComponent> components = new ArrayList<>();
        components.add(null);
        components.add(MarketingComponent.BADGE);
        spec.setComponents(components);

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).doesNotContainNull();
        assertThat(spec.getComponents()).contains(MarketingComponent.BADGE);
    }

    @Test
    @DisplayName("shouldRemoveUnknownComponents")
    void shouldRemoveUnknownComponents() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
        List<MarketingComponent> components = new ArrayList<>();
        components.add(MarketingComponent.UNKNOWN);
        components.add(MarketingComponent.STATS_BLOCK);
        spec.setComponents(components);

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents())
            .doesNotContain(MarketingComponent.UNKNOWN)
            .contains(MarketingComponent.STATS_BLOCK);
    }

    @Test
    @DisplayName("shouldDeduplicateComponents")
    void shouldDeduplicateComponents() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
        List<MarketingComponent> components = new ArrayList<>();
        components.add(MarketingComponent.BADGE);
        components.add(MarketingComponent.BADGE);
        spec.setComponents(components);

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).containsExactlyInAnyOrder(MarketingComponent.BADGE);
    }

    @Test
    @DisplayName("shouldOnlySortComponents_whenModeIsUserStrict")
    void shouldOnlySortComponents_whenModeIsUserStrict() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
        spec.setWithStats(true);
        spec.setComponents(new ArrayList<>());

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).doesNotContain(MarketingComponent.STATS_BLOCK);
    }

    @Test
    @DisplayName("shouldSortComponentsAlphabetically_whenModeIsUserStrict")
    void shouldSortComponentsAlphabetically_whenModeIsUserStrict() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
        List<MarketingComponent> components = new ArrayList<>();
        components.add(MarketingComponent.TRUST_LOGOS);
        components.add(MarketingComponent.BADGE);
        components.add(MarketingComponent.FEATURE_LIST);
        spec.setComponents(components);

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).containsExactly(
            MarketingComponent.BADGE,
            MarketingComponent.FEATURE_LIST,
            MarketingComponent.TRUST_LOGOS
        );
    }

    @Test
    @DisplayName("shouldAddStatsBlock_whenWithStatsIsTrue")
    void shouldAddStatsBlock_whenWithStatsIsTrue() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
        spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
        spec.setTone(MarketingTone.CORPORATE);
        spec.setWithStats(true);
        spec.setComponents(new ArrayList<>());

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).contains(MarketingComponent.STATS_BLOCK);
    }

    @Test
    @DisplayName("shouldAddStatsBlock_whenLayoutIsHeroWithStats")
    void shouldAddStatsBlock_whenLayoutIsHeroWithStats() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
        spec.setTone(MarketingTone.CORPORATE);
        spec.setLayout(MarketingLayoutType.HERO_WITH_STATS);
        spec.setComponents(new ArrayList<>());

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).contains(MarketingComponent.STATS_BLOCK);
    }

    @Test
    @DisplayName("shouldAddFeatureList_whenWithFeaturesIsTrue")
    void shouldAddFeatureList_whenWithFeaturesIsTrue() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
        spec.setTone(MarketingTone.CORPORATE);
        spec.setWithFeatures(true);
        spec.setComponents(new ArrayList<>());

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).contains(MarketingComponent.FEATURE_LIST);
    }

    @Test
    @DisplayName("shouldAddTestimonialCard_whenWithTestimonialIsTrue")
    void shouldAddTestimonialCard_whenWithTestimonialIsTrue() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
        spec.setTone(MarketingTone.CORPORATE);
        spec.setWithTestimonial(true);
        spec.setComponents(new ArrayList<>());

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).contains(MarketingComponent.TESTIMONIAL_CARD);
    }

    @Test
    @DisplayName("shouldAddBadgeDiscountRibbonAndCountdownTimer_whenToneIsUrgent")
    void shouldAddBadgeDiscountRibbonAndCountdownTimer_whenToneIsUrgent() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
        spec.setTone(MarketingTone.URGENT);
        spec.setComponents(new ArrayList<>());

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).contains(
            MarketingComponent.BADGE,
            MarketingComponent.DISCOUNT_RIBBON,
            MarketingComponent.COUNTDOWN_TIMER
        );
    }

    @Test
    @DisplayName("shouldAddTestimonialAndTrustLogos_whenToneIsPremium")
    void shouldAddTestimonialAndTrustLogos_whenToneIsPremium() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
        spec.setTone(MarketingTone.PREMIUM);
        spec.setComponents(new ArrayList<>());

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).contains(
            MarketingComponent.TESTIMONIAL_CARD,
            MarketingComponent.TRUST_LOGOS
        );
    }

    @Test
    @DisplayName("shouldAddTestimonialAndTrustLogos_whenToneIsLuxury")
    void shouldAddTestimonialAndTrustLogos_whenToneIsLuxury() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
        spec.setTone(MarketingTone.LUXURY);
        spec.setComponents(new ArrayList<>());

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).contains(
            MarketingComponent.TESTIMONIAL_CARD,
            MarketingComponent.TRUST_LOGOS
        );
    }

    @Test
    @DisplayName("shouldAddStatsBlockTrustLogosAndFeatureList_whenToneIsStartup")
    void shouldAddStatsBlockTrustLogosAndFeatureList_whenToneIsStartup() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
        spec.setTone(MarketingTone.STARTUP);
        spec.setComponents(new ArrayList<>());

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).contains(
            MarketingComponent.STATS_BLOCK,
            MarketingComponent.TRUST_LOGOS,
            MarketingComponent.FEATURE_LIST
        );
    }

    @Test
    @DisplayName("shouldAddOnlyTrustLogos_whenToneIsCorporate")
    void shouldAddOnlyTrustLogos_whenToneIsCorporate() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
        spec.setTone(MarketingTone.CORPORATE);
        spec.setComponents(new ArrayList<>());

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).contains(MarketingComponent.TRUST_LOGOS);
        assertThat(spec.getComponents()).doesNotContain(
            MarketingComponent.BADGE,
            MarketingComponent.COUNTDOWN_TIMER
        );
    }

    @Test
    @DisplayName("shouldSetWithPreviewToTrue_whenLayoutIsHeroSplit_andModeIsSmartAssisted")
    void shouldSetWithPreviewToTrue_whenLayoutIsHeroSplit_andModeIsSmartAssisted() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
        spec.setLayout(MarketingLayoutType.HERO_SPLIT);
        spec.setTone(MarketingTone.CORPORATE);
        spec.setComponents(new ArrayList<>());

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.isWithPreview()).isTrue();
    }

    @Test
    @DisplayName("shouldAddAllDefaultComponents_whenModeIsAutoCreativeAndComponentsEmpty")
    void shouldAddAllDefaultComponents_whenModeIsAutoCreativeAndComponentsEmpty() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.AUTO_CREATIVE);
        spec.setComponents(new ArrayList<>());

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).contains(
            MarketingComponent.ANNOUNCEMENT_BAR,
            MarketingComponent.BADGE,
            MarketingComponent.FEATURE_LIST,
            MarketingComponent.STATS_BLOCK,
            MarketingComponent.TRUST_LOGOS,
            MarketingComponent.TESTIMONIAL_CARD,
            MarketingComponent.PRICING_CARD_PREVIEW
        );
    }

    @Test
    @DisplayName("shouldNotReplaceExistingComponents_whenModeIsAutoCreativeButComponentsNotEmpty")
    void shouldNotReplaceExistingComponents_whenModeIsAutoCreativeButComponentsNotEmpty() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.AUTO_CREATIVE);
        List<MarketingComponent> existing = new ArrayList<>();
        existing.add(MarketingComponent.BADGE);
        spec.setComponents(existing);

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).doesNotContain(MarketingComponent.ANNOUNCEMENT_BAR);
    }

    @Test
    @DisplayName("shouldSortAllComponentsAlphabetically_afterNormalization")
    void shouldSortAllComponentsAlphabetically_afterNormalization() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
        List<MarketingComponent> components = new ArrayList<>();
        components.add(MarketingComponent.TRUST_LOGOS);
        components.add(MarketingComponent.ANNOUNCEMENT_BAR);
        components.add(MarketingComponent.BADGE);
        spec.setComponents(components);

        // WHEN
        normalizer.normalize(spec);

        // THEN
        assertThat(spec.getComponents()).containsExactly(
            MarketingComponent.ANNOUNCEMENT_BAR,
            MarketingComponent.BADGE,
            MarketingComponent.TRUST_LOGOS
        );
    }
}