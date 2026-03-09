package com.penpot.ai.application.tools.engine;

import com.penpot.ai.application.tools.support.JsScriptLoader;
import com.penpot.ai.core.domain.spec.SectionSpec;
import com.penpot.ai.shared.util.JsStringUtils;

import java.util.Map;

/**
 * Moteur de rendu chargé de générer le script JavaScript correspondant
 * à la structure visuelle d'une section de page.
 */
public class LayoutRenderer {

    /** Chemin racine des scripts JavaScript de layout. */
    private static final String JS = "tools/engine/";

    /**
     * Génère le script JavaScript correspondant à une section complète.
     *
     * @param template type de layout à utiliser pour la section
     * @param spec spécification du contenu textuel de la section
     * @param theme thème visuel appliqué à la section (couleurs, contraste)
     * @param intent intention marketing définissant la hiérarchie visuelle
     *               et l'importance du titre
     * @return script JavaScript complet permettant de générer la section
     *         dans Penpot
     */
    public String render(
        LayoutTemplate template,
        SectionSpec spec,
        Theme theme,
        MarketingIntent intent
    ) {
        String hero = heroText(spec, theme, intent);
        return switch (template) {
            case SPLIT_LEFT_IMAGE -> loadWith("section-layout-split.js",         Map.of("imageLeft", "true"))  + hero;
            case SPLIT_RIGHT_IMAGE -> loadWith("section-layout-split.js",         Map.of("imageLeft", "false")) + hero;
            case SPLIT_60_40_LEFT -> loadWith("section-layout-split-6040.js",    Map.of("imageLeft", "true"))  + hero;
            case SPLIT_60_40_RIGHT -> loadWith("section-layout-split-6040.js",    Map.of("imageLeft", "false")) + hero;
            case PRODUCT_FOCUS_LEFT -> loadWith("section-layout-product-focus.js", Map.of("left", "true"))       + hero;
            case PRODUCT_FOCUS_RIGHT -> loadWith("section-layout-product-focus.js", Map.of("left", "false"))      + hero;
            case CENTERED_STACK -> load("section-layout-centered.js")  + hero;
            case IMAGE_TOP_STACK -> load("section-layout-image-top.js") + hero;
            case TEXT_HEAVY -> load("section-layout-text-heavy.js")+ hero;
            case BACKGROUND_OVERLAY_CENTER -> load("section-layout-overlay.js")  + hero;
            case PREMIUM_LUXURY_CENTER -> load("section-layout-luxury.js")   + hero;
            case MINIMAL_ULTRA_CLEAN -> load("section-layout-minimal.js")  + hero;
            default -> loadWith("section-layout-split.js", Map.of("imageLeft", "true")) + hero;
        };
    }

    /**
     * Génère le bloc de texte principal ("hero text") d'une section.
     *
     * @param spec spécification contenant le contenu textuel de la section
     * @param theme thème graphique déterminant notamment les couleurs de texte
     * @param intent intention marketing déterminant la hiérarchie visuelle
     * @return script JavaScript permettant de générer le bloc de texte
     */
    private static String heroText(SectionSpec spec, Theme theme, MarketingIntent intent) {
        int titleSize = switch (intent.hierarchy) {
            case STRONG_HERO -> 72;
            case BALANCED -> 56;
            case SOFT_BRANDING -> 46;
        };

        String title = JsStringUtils.jsSafe(spec.getTitle());
        String subtitle = JsStringUtils.jsSafe(spec.getSubtitle());
        String paragraph = JsStringUtils.jsSafe(spec.getParagraph());

        return JsScriptLoader.loadWith("snippets/section-hero-text-block.js", Map.of(
            "textColor", theme.gradient ? theme.textOnDark : theme.textOnLight,
            "hasTitle", String.valueOf(!title.isBlank()),
            "title", title,
            "titleSize", String.valueOf(titleSize),
            "hasSubtitle", String.valueOf(!subtitle.isBlank()),
            "subtitle", subtitle,
            "hasParagraph", String.valueOf(!paragraph.isBlank()),
            "paragraph", paragraph
        ));
    }

    /**
     * Charge un script JavaScript de layout sans paramètre.
     *
     * @param filename nom du fichier JavaScript à charger
     * @return contenu du script JavaScript
     */
    private static String load(String filename) {
        return JsScriptLoader.load(JS + filename);
    }

    /**
     * Charge un script JavaScript de layout en injectant des paramètres.
     *
     * @param filename nom du fichier JavaScript à charger
     * @param params paramètres à injecter dans le script
     * @return contenu du script JavaScript paramétré
     */
    private static String loadWith(String filename, Map<String, String> params) {
        return JsScriptLoader.loadWith(JS + filename, params);
    }
}