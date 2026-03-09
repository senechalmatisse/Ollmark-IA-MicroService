package com.penpot.ai.application.tools.engine;

import com.penpot.ai.core.domain.marketing.MarketingLayoutType;
import com.penpot.ai.core.domain.marketing.MarketingTone;
import com.penpot.ai.core.domain.spec.SectionSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TemplateResolver — Unit")
class TemplateResolverUnit {

    private final TemplateResolver templateResolver = new TemplateResolver();

    private MarketingIntent defaultIntent() {
        return new MarketingIntent(
                false,  // conversion
                false,  // urgency
                false,  // social
                false,  // luxury
                false,  // productCentric
                false,  // minimal
                50,     // titleImpactScore
                50,     // persuasionScore
                MarketingIntent.CtaStrength.MEDIUM,
                MarketingIntent.HierarchyLevel.BALANCED,
                MarketingIntent.CampaignType.GENERIC
        );
    }

    @Nested
    @DisplayName("resolveTemplate — explicit user direction")
    class ExplicitDirectionTests {

        @Test
        @DisplayName("resolveTemplate — returns right image layout when text explicitly asks for image on the right")
        void resolveTemplate_returnsRightImageLayoutWhenUserRequestsImageRight() {
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Hero avec image à droite");
            spec.setSubtitle("Texte simple");

            LayoutTemplate template = templateResolver.resolveTemplate(spec, defaultIntent());

            assertThat(template).isEqualTo(LayoutTemplate.SPLIT_RIGHT_IMAGE);
        }

        @Test
        @DisplayName("resolveTemplate — returns left image layout when text explicitly asks for image on the left")
        void resolveTemplate_returnsLeftImageLayoutWhenUserRequestsImageLeft() {
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Hero avec image à gauche");
            spec.setSubtitle("Texte simple");

            LayoutTemplate template = templateResolver.resolveTemplate(spec, defaultIntent());

            assertThat(template).isEqualTo(LayoutTemplate.SPLIT_LEFT_IMAGE);
        }

        @Test
        @DisplayName("resolveTemplate — returns conversion right layout when image right and conversion focused")
        void resolveTemplate_returnsConversionRightLayoutWhenImageRightAndConversionFocused() {
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Offre spéciale avec image à droite");

            MarketingIntent intent = new MarketingIntent(
                    true, false, false, false, false, false,
                    50, 80,
                    MarketingIntent.CtaStrength.STRONG,
                    MarketingIntent.HierarchyLevel.BALANCED,
                    MarketingIntent.CampaignType.GENERIC
            );

            LayoutTemplate template = templateResolver.resolveTemplate(spec, intent);

            assertThat(template).isEqualTo(LayoutTemplate.SPLIT_60_40_RIGHT);
        }
    }

    @Nested
    @DisplayName("resolveTemplate — USER_STRICT mapping")
    class UserStrictTests {

        @Test
        @DisplayName("resolveTemplate — maps HERO_SPLIT to split left image when not conversion focused")
        void resolveTemplate_mapsHeroSplitWhenNotConversionFocused() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
            spec.setLayout(MarketingLayoutType.HERO_SPLIT);

            LayoutTemplate template = templateResolver.resolveTemplate(spec, defaultIntent());

            assertThat(template).isEqualTo(LayoutTemplate.SPLIT_LEFT_IMAGE);
        }

        @Test
        @DisplayName("resolveTemplate — maps HERO_SPLIT to split 60 40 right when conversion focused")
        void resolveTemplate_mapsHeroSplitWhenConversionFocused() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
            spec.setLayout(MarketingLayoutType.HERO_SPLIT);

            MarketingIntent intent = new MarketingIntent(
                    true, false, false, false, false, false,
                    60, 75,
                    MarketingIntent.CtaStrength.STRONG,
                    MarketingIntent.HierarchyLevel.BALANCED,
                    MarketingIntent.CampaignType.GENERIC
            );

            LayoutTemplate template = templateResolver.resolveTemplate(spec, intent);

            assertThat(template).isEqualTo(LayoutTemplate.SPLIT_60_40_RIGHT);
        }

        @Test
        @DisplayName("resolveTemplate — maps HERO_CENTERED to centered stack")
        void resolveTemplate_mapsHeroCenteredToCenteredStack() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
            spec.setLayout(MarketingLayoutType.HERO_CENTERED);

            LayoutTemplate template = templateResolver.resolveTemplate(spec, defaultIntent());

            assertThat(template).isEqualTo(LayoutTemplate.CENTERED_STACK);
        }

        @Test
        @DisplayName("resolveTemplate — maps HERO_WITH_STATS to product focus left")
        void resolveTemplate_mapsHeroWithStatsToProductFocusLeft() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
            spec.setLayout(MarketingLayoutType.HERO_WITH_STATS);

            LayoutTemplate template = templateResolver.resolveTemplate(spec, defaultIntent());

            assertThat(template).isEqualTo(LayoutTemplate.PRODUCT_FOCUS_LEFT);
        }

        @Test
        @DisplayName("resolveTemplate — maps PROMO_SECTION to background overlay center")
        void resolveTemplate_mapsPromoSectionToBackgroundOverlayCenter() {
            SectionSpec spec = new SectionSpec();
            spec.setGenerationMode(SectionSpec.GenerationMode.USER_STRICT);
            spec.setLayout(MarketingLayoutType.PROMO_SECTION);

            LayoutTemplate template = templateResolver.resolveTemplate(spec, defaultIntent());

            assertThat(template).isEqualTo(LayoutTemplate.BACKGROUND_OVERLAY_CENTER);
        }
    }

    @Nested
    @DisplayName("resolveTemplate — marketing intent heuristics")
    class IntentHeuristicsTests {

        @Test
        @DisplayName("resolveTemplate — returns product focus left for conversion ecommerce copy")
        void resolveTemplate_returnsProductFocusLeftForConversionEcommerceCopy() {
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Acheter maintenant");
            spec.setSubtitle("Meilleur prix du marché");

            MarketingIntent intent = new MarketingIntent(
                    true, false, false, false, false, false,
                    70, 80,
                    MarketingIntent.CtaStrength.STRONG,
                    MarketingIntent.HierarchyLevel.BALANCED,
                    MarketingIntent.CampaignType.GENERIC
            );

            LayoutTemplate template = templateResolver.resolveTemplate(spec, intent);

            assertThat(template).isEqualTo(LayoutTemplate.PRODUCT_FOCUS_LEFT);
        }

        @Test
        @DisplayName("resolveTemplate — returns product focus left for conversion product centric intent")
        void resolveTemplate_returnsProductFocusLeftForProductCentricIntent() {
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Nouveau produit");

            MarketingIntent intent = new MarketingIntent(
                    true, false, false, false, true, false,
                    60, 80,
                    MarketingIntent.CtaStrength.STRONG,
                    MarketingIntent.HierarchyLevel.BALANCED,
                    MarketingIntent.CampaignType.PRODUCT_LAUNCH
            );

            LayoutTemplate template = templateResolver.resolveTemplate(spec, intent);

            assertThat(template).isEqualTo(LayoutTemplate.PRODUCT_FOCUS_LEFT);
        }

        @Test
        @DisplayName("resolveTemplate — returns premium luxury center for premium tone")
        void resolveTemplate_returnsPremiumLuxuryCenterForPremiumTone() {
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Collection exclusive");
            spec.setTone(MarketingTone.PREMIUM);

            LayoutTemplate template = templateResolver.resolveTemplate(spec, defaultIntent());

            assertThat(template).isEqualTo(LayoutTemplate.PREMIUM_LUXURY_CENTER);
        }

        @Test
        @DisplayName("resolveTemplate — returns text heavy when social proof is present")
        void resolveTemplate_returnsTextHeavyWhenSocialProofPresent() {
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Ils nous font confiance");

            MarketingIntent intent = new MarketingIntent(
                    false, false, true, false, false, false,
                    50, 50,
                    MarketingIntent.CtaStrength.MEDIUM,
                    MarketingIntent.HierarchyLevel.BALANCED,
                    MarketingIntent.CampaignType.GENERIC
            );

            LayoutTemplate template = templateResolver.resolveTemplate(spec, intent);

            assertThat(template).isEqualTo(LayoutTemplate.TEXT_HEAVY);
        }

        @Test
        @DisplayName("resolveTemplate — returns minimal ultra clean for short minimal copy")
        void resolveTemplate_returnsMinimalUltraCleanForShortMinimalCopy() {
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Simple");
            spec.setSubtitle("");
            spec.setParagraph("");

            LayoutTemplate template = templateResolver.resolveTemplate(spec, defaultIntent());

            assertThat(template).isEqualTo(LayoutTemplate.MINIMAL_ULTRA_CLEAN);
        }

        @Test
        @DisplayName("resolveTemplate — returns background overlay center when preview is enabled")
        void resolveTemplate_returnsBackgroundOverlayCenterWhenPreviewEnabled() {
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Présentation complète de notre nouvelle solution marketing");
            spec.setSubtitle("Une plateforme pensée pour la performance et la visibilité");
            spec.setParagraph("Découvrez une expérience plus immersive avec un aperçu clair du produit.");
            spec.setWithPreview(true);

            LayoutTemplate template = templateResolver.resolveTemplate(spec, defaultIntent());

            assertThat(template).isEqualTo(LayoutTemplate.BACKGROUND_OVERLAY_CENTER);
        }
        
        @Test
        @DisplayName("resolveTemplate — returns split left image when layout is HERO_SPLIT outside user strict")
        void resolveTemplate_returnsSplitLeftImageForHeroSplitOutsideUserStrict() {
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Titre assez long pour éviter le minimal automatique");
            spec.setLayout(MarketingLayoutType.HERO_SPLIT);

            LayoutTemplate template = templateResolver.resolveTemplate(spec, defaultIntent());

            assertThat(template).isEqualTo(LayoutTemplate.SPLIT_LEFT_IMAGE);
        }

        @Test
        @DisplayName("resolveTemplate — returns centered stack as safe default")
        void resolveTemplate_returnsCenteredStackAsSafeDefault() {
            SectionSpec spec = new SectionSpec();
            spec.setTitle("Titre assez long pour éviter le minimal automatique");
            spec.setSubtitle("Sous-titre présent");
            spec.setParagraph("Paragraphe présent");

            LayoutTemplate template = templateResolver.resolveTemplate(spec, defaultIntent());

            assertThat(template).isEqualTo(LayoutTemplate.CENTERED_STACK);
        }
    }
}