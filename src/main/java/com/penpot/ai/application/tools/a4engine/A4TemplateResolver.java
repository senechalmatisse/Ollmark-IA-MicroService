package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.engine.MarketingIntent;
import com.penpot.ai.core.domain.spec.SectionSpec;

/**
 * Composant décisionnel responsable de la sélection du gabarit de mise en page (layout) le plus approprié pour une section au format A4.
 * <p>
 * Ce moteur de résolution opère tel un arbre de décision séquentiel. Il confronte les exigences explicites formulées 
 * dans la spécification technique ({@link SectionSpec}) avec les déductions sémantiques issues de l'analyse marketing 
 * ({@link MarketingIntent}).
 * </p>
 */
public class A4TemplateResolver {

    /**
     * Évalue séquentiellement les critères de la spécification et de l'intention marketing pour aboutir à une recommandation de gabarit unique.
     *
     * @param spec   La spécification technique de la section, porteuse des contraintes d'affichage explicites ou des options souhaitées (ex: présence d'un aperçu).
     * @param intent L'objet encapsulant l'intention marketing déduite par le moteur d'analyse sémantique (type de campagne, force du CTA, urgence).
     * @return       Le gabarit de mise en page ({@link A4LayoutTemplate}) optimalement sélectionné pour répondre au double contexte technique et commercial.
     */
    public A4LayoutTemplate resolveTemplate(SectionSpec spec, MarketingIntent intent) {

        // 1. Layout forcé explicitement par la spec
        if (spec.getLayout() != null) {
            A4LayoutTemplate forced = switch (spec.getLayout()) {
                case HERO_SPLIT -> A4LayoutTemplate.HERO_IMAGE_LEFT;
                case HERO_CENTERED -> A4LayoutTemplate.HERO_CENTER_STACK;
                case HERO_WITH_IMAGE -> A4LayoutTemplate.HERO_IMAGE_RIGHT;
                case HERO_WITH_STATS -> A4LayoutTemplate.STATS_HEAVY_POSTER;
                case FEATURE_LIST -> A4LayoutTemplate.FEATURE_GRID_3;
                case LANDING_BLOCK -> A4LayoutTemplate.CTA_FOCUSED;
                case PROMO_SECTION -> A4LayoutTemplate.SALE_POSTER;
                default -> null;
            };
            if (forced != null) return forced;
        }

        // 2. Campagne flash / urgence
        if (intent.campaignType == MarketingIntent.CampaignType.FLASH_SALE) {
            return intent.containsUrgency
                ? A4LayoutTemplate.COUNTDOWN_PROMO
                : A4LayoutTemplate.SALE_POSTER;
        }

        // 3. Lancement produit
        if (intent.campaignType == MarketingIntent.CampaignType.PRODUCT_LAUNCH) {
            return intent.isProductCentric
                ? A4LayoutTemplate.PRODUCT_SHOWCASE
                : A4LayoutTemplate.FEATURE_GRID_3;
        }

        // 4. Positionnement luxe
        if (intent.isLuxuryPositioning) return A4LayoutTemplate.CENTERED_MINIMAL;

        // 5. Preuve sociale
        if (intent.containsSocialProof) {
            return intent.isConversionFocused
                ? A4LayoutTemplate.TESTIMONIAL_FOCUS
                : A4LayoutTemplate.FEATURE_GRID_3;
        }

        // 6. CTA agressif ou conversion forte
        if (intent.ctaStrength == MarketingIntent.CtaStrength.AGGRESSIVE
            || (intent.ctaStrength == MarketingIntent.CtaStrength.STRONG
                && intent.isConversionFocused)) {
            return A4LayoutTemplate.CTA_FOCUSED;
        }

        // 7. Capture de leads / pricing
        if (intent.campaignType == MarketingIntent.CampaignType.LEAD_CAPTURE) {
            return A4LayoutTemplate.PRICING_TABLE;
        }

        // 8. Preview demandé
        if (spec.isWithPreview()) return A4LayoutTemplate.HERO_IMAGE_RIGHT;

        // 9. Hiérarchie visuelle
        if (intent.hierarchy != null) {
            return switch (intent.hierarchy) {
                case STRONG_HERO -> A4LayoutTemplate.STATS_HEAVY_POSTER;
                case BALANCED -> A4LayoutTemplate.HERO_IMAGE_LEFT;
                case SOFT_BRANDING -> A4LayoutTemplate.CENTERED_MINIMAL;
            };
        }

        return A4LayoutTemplate.CENTERED_MINIMAL;
    }
}