package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.support.JsScriptLoader;
import com.penpot.ai.core.domain.spec.SectionSpec;
import com.penpot.ai.shared.util.JsonUtils;

import java.util.Map;

/**
 * Moteur de rendu chargé de générer le code JavaScript correspondant
 * à la mise en page d’un document au format A4.
 *
 * <p>Cette classe sélectionne un template de layout défini par
 * {@link A4LayoutTemplate} et charge le script JavaScript associé
 * permettant de générer la structure visuelle du document dans Penpot.</p>
 *
 * <p>Selon le type de layout choisi, différentes informations
 * textuelles peuvent être injectées :</p>
 * <ul>
 *     <li>le titre uniquement</li>
 *     <li>le titre et un paragraphe descriptif</li>
 *     <li>aucun paramètre (pour les layouts entièrement structurés)</li>
 * </ul>
 */
public class A4LayoutRenderer {

    /** Chemin racine des templates JavaScript utilisés pour générer les layouts A4. */
    private static final String JS = "tools/a4engine/";

    /**
     * Génère le code JavaScript correspondant à un layout A4.
     *
     * <p>La méthode sélectionne un template en fonction du
     * {@link A4LayoutTemplate} fourni puis injecte les paramètres
     * nécessaires au rendu.</p>
     *
     * <p>Certains layouts nécessitent uniquement un titre, tandis que
     * d’autres utilisent à la fois un titre et un paragraphe.</p>
     *
     * @param template type de layout A4 à appliquer
     * @param spec spécification de section contenant les textes à afficher
     * @return code JavaScript permettant de générer la mise en page
     *         correspondante
     */
    public String render(A4LayoutTemplate template, SectionSpec spec) {
        return switch (template) {
            case CENTERED_MINIMAL,
                 HEADER_HERO_FULL -> loadWith("a4-layout-centered-hero.js", titleAndParagraph(spec));
            case SIDEBAR_LEFT_COL,
                 HERO_IMAGE_LEFT -> loadWith("a4-layout-image-left.js", titleAndParagraph(spec));
            case TWO_COLUMN_GRID,
                 HERO_IMAGE_RIGHT,
                 MAGAZINE_TEXT_WRAP -> loadWith("a4-layout-image-right.js", titleAndParagraph(spec));
            case PRODUCT_SHOWCASE -> loadWith("a4-layout-product-showcase.js", titleOnly(spec));
            case CTA_FOCUSED -> loadWith("a4-layout-cta-focus.js", titleOnly(spec));
            case STATS_HEAVY_POSTER, SALE_POSTER -> loadWith("a4-layout-poster.js", titleOnly(spec));
            case FEATURE_GRID_3 -> JsScriptLoader.load(JS + "a4-layout-feature-grid-3.js");
            case TESTIMONIAL_FOCUS -> JsScriptLoader.load(JS + "a4-layout-testimonial-focus.js");
            case PRICING_TABLE -> JsScriptLoader.load(JS + "a4-layout-pricing-table.js");
            default -> loadWith("a4-layout-centered-hero.js", titleAndParagraph(spec));
        };
    }

    /**
     * Charge un template JavaScript et injecte les paramètres nécessaires
     * à son exécution.
     *
     * @param filename nom du fichier JavaScript contenant le template
     * @param params paramètres à injecter dans le template
     * @return code JavaScript final généré
     */
    private static String loadWith(String filename, Map<String, String> params) {
        return JsScriptLoader.loadWith(JS + filename, params);
    }

    /**
     * Construit l’ensemble des paramètres nécessaires aux layouts
     * utilisant un titre et un paragraphe.
     *
     * @param spec spécification de section contenant les textes
     * @return map de paramètres contenant le titre et le paragraphe
     */
    private static Map<String, String> titleAndParagraph(SectionSpec spec) {
        return Map.of(
            "title", JsonUtils.escapeJson(spec.getTitle()),
            "paragraph", JsonUtils.escapeJson(spec.getParagraph())
        );
    }

    /**
     * Construit l’ensemble des paramètres nécessaires aux layouts
     * nécessitant uniquement un titre.
     *
     * @param spec spécification de section contenant le titre
     * @return map de paramètres contenant le titre échappé
     */
    private static Map<String, String> titleOnly(SectionSpec spec) {
        return Map.of("title", JsonUtils.escapeJson(spec.getTitle()));
    }
}