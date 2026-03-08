package com.penpot.ai.application.tools.engine;

import com.penpot.ai.core.domain.marketing.*;
import com.penpot.ai.core.domain.spec.SectionSpec;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SpecNormalizer — Unit")
class SpecNormalizerUnit {

    private final SpecNormalizer specNormalizer = new SpecNormalizer();

    @Nested
    @DisplayName("normalize — null handling")
    class NullHandlingTests {

        @Test
        @DisplayName("normalize — throws IllegalArgumentException when spec is null")
        void normalize_throwsIllegalArgumentExceptionWhenSpecIsNull() {
            assertThatThrownBy(() -> specNormalizer.normalize(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("SectionSpec cannot be null");
        }
    }

    @Nested
    @DisplayName("normalize — default values")
    class DefaultValuesTests {

        @Test
        @DisplayName("normalize — fills missing defaults for generation mode, layout, tone, style and components")
        void normalize_fillsMissingDefaults() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(null);
            spec.setLayout(null);
            spec.setTone(null);
            spec.setStyle(null);
            spec.setComponents(null);

            specNormalizer.normalize(spec);

            assertThat(spec.getGenerationMode()).isEqualTo(SectionSpec.GenerationMode.SMART_ASSISTED);
            assertThat(spec.getLayout()).isEqualTo(MarketingLayoutType.HERO_SPLIT);
            assertThat(spec.getTone()).isEqualTo(MarketingTone.STARTUP);
            assertThat(spec.getStyle()).isEqualTo(MarketingStyle.BOLD_ECOMMERCE);
            assertThat(spec.getComponents()).isNotNull();
            assertThat(spec.getComponents()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("normalize — component cleanup")
    class ComponentCleanupTests {

        @Test
        @DisplayName("normalize — removes nulls, UNKNOWN values and duplicates")
            void normalize_removesNullsUnknownAndDuplicates() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
            spec.setComponents(new ArrayList<>(java.util.Arrays.asList(
            MarketingComponent.FEATURE_LIST,
            null,
            MarketingComponent.UNKNOWN,
            MarketingComponent.FEATURE_LIST,
            MarketingComponent.BADGE
        )));

        specNormalizer.normalize(spec);

        assertThat(spec.getComponents())
            .containsExactly(
                    MarketingComponent.BADGE,
                    MarketingComponent.FEATURE_LIST
            );
        }
    }

    @Nested
    @DisplayName("normalize — USER_STRICT mode")
    class UserStrictModeTests {

        @Test
        @DisplayName("normalize — keeps explicit user components only and sorts them")
        void normalize_keepsExplicitUserComponentsOnlyAndSortsThem() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
            spec.setWithStats(true);
            spec.setWithFeatures(true);
            spec.setWithTestimonial(true);
            spec.setTone(MarketingTone.URGENT);
            spec.setLayout(MarketingLayoutType.HERO_WITH_STATS);
            spec.setComponents(new ArrayList<>(List.of(
                    MarketingComponent.TESTIMONIAL_CARD,
                    MarketingComponent.BADGE
            )));

            specNormalizer.normalize(spec);

            assertThat(spec.getComponents())
                    .containsExactly(
                            MarketingComponent.BADGE,
                            MarketingComponent.TESTIMONIAL_CARD
                    );
        }
    }

    @Nested
    @DisplayName("normalize — flags to components")
    class FlagsToComponentsTests {

        @Test
        @DisplayName("normalize — adds components from boolean flags and HERO_WITH_STATS layout")
        void normalize_addsComponentsFromFlagsAndLayout() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
            spec.setLayout(MarketingLayoutType.HERO_WITH_STATS);
            spec.setWithFeatures(true);
            spec.setWithTestimonial(true);
            spec.setComponents(new ArrayList<>());

            specNormalizer.normalize(spec);

            assertThat(spec.getComponents()).contains(
                    MarketingComponent.FEATURE_LIST,
                    MarketingComponent.TESTIMONIAL_CARD,
                    MarketingComponent.STATS_BLOCK
            );
        }
    }

    @Nested
    @DisplayName("normalize — SMART_ASSISTED tone enrichment")
    class SmartAssistedTests {

        @Test
        @DisplayName("normalize — enriches URGENT tone with badge, discount ribbon and countdown timer")
        void normalize_enrichesUrgentTone() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
            spec.setTone(MarketingTone.URGENT);
            spec.setComponents(new ArrayList<>());

            specNormalizer.normalize(spec);

            assertThat(spec.getComponents()).contains(
                    MarketingComponent.BADGE,
                    MarketingComponent.DISCOUNT_RIBBON,
                    MarketingComponent.COUNTDOWN_TIMER
            );
        }

        @Test
        @DisplayName("normalize — enriches PREMIUM tone with trust logos and testimonial")
        void normalize_enrichesPremiumTone() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
            spec.setTone(MarketingTone.PREMIUM);
            spec.setComponents(new ArrayList<>());

            specNormalizer.normalize(spec);

            assertThat(spec.getComponents()).contains(
                    MarketingComponent.TRUST_LOGOS,
                    MarketingComponent.TESTIMONIAL_CARD
            );
        }

        @Test
        @DisplayName("normalize — enriches LUXURY tone with trust logos and testimonial")
        void normalize_enrichesLuxuryTone() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
            spec.setTone(MarketingTone.LUXURY);
            spec.setComponents(new ArrayList<>());

            specNormalizer.normalize(spec);

            assertThat(spec.getComponents()).contains(
                    MarketingComponent.TRUST_LOGOS,
                    MarketingComponent.TESTIMONIAL_CARD
            );
        }

        @Test
        @DisplayName("normalize — enriches STARTUP tone with trust logos and stats block")
        void normalize_enrichesStartupTone() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
            spec.setTone(MarketingTone.STARTUP);
            spec.setComponents(new ArrayList<>());

            specNormalizer.normalize(spec);

            assertThat(spec.getComponents()).contains(
                    MarketingComponent.TRUST_LOGOS,
                    MarketingComponent.STATS_BLOCK
            );
        }

        @Test
        @DisplayName("normalize — enriches CORPORATE tone with trust logos only")
        void normalize_enrichesCorporateTone() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
            spec.setTone(MarketingTone.CORPORATE);
            spec.setComponents(new ArrayList<>());

            specNormalizer.normalize(spec);

            assertThat(spec.getComponents()).contains(MarketingComponent.TRUST_LOGOS);
        }

        @Test
        @DisplayName("normalize — enriches FRIENDLY tone with badge")
        void normalize_enrichesFriendlyTone() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
            spec.setTone(MarketingTone.FRIENDLY);
            spec.setComponents(new ArrayList<>());

            specNormalizer.normalize(spec);

            assertThat(spec.getComponents()).contains(MarketingComponent.BADGE);
        }

        @Test
        @DisplayName("normalize — enriches FEMININE tone with badge")
        void normalize_enrichesFeminineTone() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
            spec.setTone(MarketingTone.FEMININE);
            spec.setComponents(new ArrayList<>());

            specNormalizer.normalize(spec);

            assertThat(spec.getComponents()).contains(MarketingComponent.BADGE);
        }
    }

    @Nested
    @DisplayName("normalize — AUTO_CREATIVE mode")
    class AutoCreativeModeTests {

        @Test
        @DisplayName("normalize — auto-fills a default component set when AUTO_CREATIVE and components are empty")
        void normalize_autoFillsDefaultComponentSetWhenAutoCreativeAndEmpty() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.AUTO_CREATIVE);
            spec.setComponents(new ArrayList<>());

            specNormalizer.normalize(spec);

            assertThat(spec.getComponents()).contains(
                    MarketingComponent.ANNOUNCEMENT_BAR,
                    MarketingComponent.BADGE,
                    MarketingComponent.FEATURE_LIST,
                    MarketingComponent.STATS_BLOCK,
                    MarketingComponent.TRUST_LOGOS,
                    MarketingComponent.TESTIMONIAL_CARD
            );
        }

        @Test
        @DisplayName("normalize — does not auto-fill when AUTO_CREATIVE already has components")
        void normalize_doesNotAutoFillWhenAutoCreativeAlreadyHasComponents() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.AUTO_CREATIVE);
            spec.setComponents(new ArrayList<>(List.of(MarketingComponent.FEATURE_GRID)));

            specNormalizer.normalize(spec);

            assertThat(spec.getComponents()).contains(MarketingComponent.FEATURE_GRID);
            assertThat(spec.getComponents()).doesNotContain(MarketingComponent.ANNOUNCEMENT_BAR);
        }
    }

    @Nested
    @DisplayName("normalize — final stable sorting")
    class FinalSortingTests {

        @Test
        @DisplayName("normalize — sorts components by enum name in stable final output")
        void normalize_sortsComponentsByEnumNameInFinalOutput() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
            spec.setComponents(new ArrayList<>(List.of(
                    MarketingComponent.TRUST_LOGOS,
                    MarketingComponent.ANNOUNCEMENT_BAR,
                    MarketingComponent.BADGE
            )));

            specNormalizer.normalize(spec);

            assertThat(spec.getComponents()).containsExactly(
                    MarketingComponent.ANNOUNCEMENT_BAR,
                    MarketingComponent.BADGE,
                    MarketingComponent.TRUST_LOGOS
            );
        }
    }
}