package com.penpot.ai.application.tools.engine;

import com.penpot.ai.core.domain.marketing.*;
import com.penpot.ai.core.domain.spec.SectionSpec;
import com.penpot.ai.shared.util.JsStringUtils;

import java.util.Locale;

/**
 * Résolveur de template chargé de déterminer le {@link LayoutTemplate}
 * le plus approprié pour une section en fonction :
 * <ul>
 *     <li>du contenu textuel de la section</li>
 *     <li>de la spécification métier {@link SectionSpec}</li>
 *     <li>de l'intention marketing analysée ({@link MarketingIntent})</li>
 * </ul>
 *
 * <p>
 * Si aucun cas spécifique n'est détecté, un layout générique
 * {@link LayoutTemplate#CENTERED_STACK} est utilisé par défaut.
 * </p>
 */
public class TemplateResolver {

    /**
     * Détermine le {@link LayoutTemplate} le plus adapté pour une section.
     *
     * @param spec spécification de la section contenant le contenu et les contraintes
     * @param intent intention marketing dérivée du contenu
     * @return template de layout sélectionné pour la section
     */
    public LayoutTemplate resolveTemplate(SectionSpec spec, MarketingIntent intent) {
        String title = JsStringUtils.safe(spec.getTitle()).toLowerCase(Locale.ROOT);
        String subtitle = JsStringUtils.safe(spec.getSubtitle()).toLowerCase(Locale.ROOT);
        String paragraph = JsStringUtils.safe(spec.getParagraph()).toLowerCase(Locale.ROOT);
        String full = title + " " + subtitle + " " + paragraph;

        boolean imageRight =
            JsStringUtils.containsAny(
                full,
                "image à droite", "image a droite",
                "visuel à droite", "visuel a droite"
            );

        boolean imageLeft =
            JsStringUtils.containsAny(
                full,
                "image à gauche", "image a gauche",
                "visuel à gauche", "visuel a gauche"
            );

        if (imageRight) {
            return intent.isConversionFocused
                ? LayoutTemplate.SPLIT_60_40_RIGHT
                : LayoutTemplate.SPLIT_RIGHT_IMAGE;
        }
        if (imageLeft) {
            return intent.isConversionFocused
                ? LayoutTemplate.SPLIT_60_40_LEFT
                : LayoutTemplate.SPLIT_LEFT_IMAGE;
        }

        if (spec.getGenerationMode() == SectionSpec.GenerationMode.USER_STRICT
                && spec.getLayout() != null) {
            return mapExplicitLayout(spec.getLayout(), intent);
        }

        boolean ecommerce = JsStringUtils.containsAny(full,
            "acheter", "prix", "commande", "panier");

        boolean luxury =
            JsStringUtils.containsAny(full, "premium", "exclusive", "élégance")
            || spec.getTone() == MarketingTone.PREMIUM;

        boolean minimal =
            title.length() < 25 && subtitle.isBlank() && !intent.isConversionFocused;

        if (intent.isConversionFocused) {
            return (ecommerce || intent.isProductCentric)
                ? LayoutTemplate.PRODUCT_FOCUS_LEFT
                : LayoutTemplate.SPLIT_60_40_RIGHT;
        }

        if (luxury) return LayoutTemplate.PREMIUM_LUXURY_CENTER;
        if (intent.containsSocialProof) return LayoutTemplate.TEXT_HEAVY;
        if (minimal) return LayoutTemplate.MINIMAL_ULTRA_CLEAN;
        if (spec.isWithPreview()) return LayoutTemplate.BACKGROUND_OVERLAY_CENTER;
        if (spec.getLayout() == MarketingLayoutType.HERO_SPLIT) return LayoutTemplate.SPLIT_LEFT_IMAGE;
        return LayoutTemplate.CENTERED_STACK;
    }

    /**
     * Convertit un {@link MarketingLayoutType} explicitement demandé
     * en {@link LayoutTemplate} concret utilisable par le moteur de rendu.
     *
     * @param type type de layout marketing explicitement défini
     * @param intent intention marketing analysée
     * @return template de layout correspondant
     */
    private LayoutTemplate mapExplicitLayout(MarketingLayoutType type, MarketingIntent intent) {
        return switch (type) {
            case HERO_SPLIT -> intent.isConversionFocused
                ? LayoutTemplate.SPLIT_60_40_RIGHT
                : LayoutTemplate.SPLIT_LEFT_IMAGE;
            case HERO_CENTERED -> LayoutTemplate.CENTERED_STACK;
            case HERO_WITH_STATS -> LayoutTemplate.PRODUCT_FOCUS_LEFT;
            case PROMO_SECTION -> LayoutTemplate.BACKGROUND_OVERLAY_CENTER;
            default -> LayoutTemplate.CENTERED_STACK;
        };
    }
}