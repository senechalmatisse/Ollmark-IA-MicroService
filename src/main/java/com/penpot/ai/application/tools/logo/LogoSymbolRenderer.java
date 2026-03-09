package com.penpot.ai.application.tools.logo;

import com.penpot.ai.application.tools.support.JsScriptLoader;
import com.penpot.ai.core.domain.logo.*;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Composant responsable de la génération du code JavaScript permettant
 * de dessiner le symbole graphique d’un logo dans l’environnement Penpot.
 *
 * <p>Le moteur utilise :</p>
 * <ul>
 *     <li>la spécification du logo {@link LogoSpec} (style, nom de la marque, etc.)</li>
 *     <li>l’intention graphique {@link LogoIntent} enrichie par le moteur de thème</li>
 * </ul>
 */
@Component
public class LogoSymbolRenderer {

    /**
     * Génère le code JavaScript permettant de dessiner le symbole du logo.
     *
     * <p>La génération se déroule en deux étapes :</p>
     * <ol>
     *     <li>Création d’un prologue JavaScript contenant des variables
     *     communes (taille du symbole, position de base).</li>
     *     <li>Génération du symbole via un template correspondant
     *     au style défini dans {@link LogoSpec}.</li>
     * </ol>
     *
     * <p>La taille du symbole est calculée dynamiquement en fonction du
     * facteur d’échelle défini dans {@link LogoIntent#getScalingFactor()}.</p>
     *
     * @param spec spécification du logo contenant notamment le style graphique
     * @param intent intention graphique enrichie contenant les paramètres visuels
     * @return code JavaScript permettant de dessiner le symbole du logo
     */
    public String render(LogoSpec spec, LogoIntent intent) {
        String prologue = "const symbolSize = " + (80 * intent.getScalingFactor()) + ";\n"
            + "const columnX = x;\n"
            + "const columnY = y;\n";
        return prologue + renderSymbol(spec, intent);
    }

    /**
     * Sélectionne le template JavaScript correspondant au style du logo
     * puis injecte les paramètres nécessaires au rendu.
     *
     * @param spec spécification du logo
     * @param intent intention graphique contenant les paramètres visuels
     * @return code JavaScript généré pour le symbole
     */
    private String renderSymbol(LogoSpec spec, LogoIntent intent) {
        return switch (spec.getStyle()) {
            case GEOMETRIQUE -> loadSymbol("logo-symbol-geometric.js", baseParams(intent));
            case MONOGRAMME -> loadSymbol("logo-symbol-monogram.js",  monogramParams(spec, intent));
            case ABSTRAIT -> loadSymbol("logo-symbol-abstract.js",   abstractParams(intent));
            case MINIMALISTE -> loadSymbol("logo-symbol-minimalist.js", minimalistParams(intent));
            case EMBLEME -> loadSymbol("logo-symbol-emblem.js",     emblemParams(intent));
            default -> loadSymbol("logo-symbol-geometric.js", baseParams(intent));
        };
    }

    /**
     * Construit l’ensemble des paramètres communs utilisés par
     * plusieurs styles de symboles (géométrique, abstrait, minimaliste).
     *
     * @param intent intention graphique contenant les paramètres visuels
     * @return map de paramètres injectés dans le template JavaScript
     */
    private Map<String, String> baseParams(LogoIntent intent) {
        return Map.of(
            "borderRadius", String.valueOf(intent.getBorderRadius()),
            "fillStyle", fillStyle(intent),
            "secondaryColor", intent.getSecondaryColor()
        );
    }

    /**
     * Génère les paramètres spécifiques au style monogramme.
     *
     * <p>Dans ce cas, la première lettre du nom de la marque est utilisée
     * comme élément central du symbole.</p>
     *
     * @param spec spécification du logo
     * @param intent intention graphique
     * @return paramètres injectés dans le template monogramme
     */
    private Map<String, String> monogramParams(LogoSpec spec, LogoIntent intent) {
        String letter = spec.getBrandName().substring(0, 1).toUpperCase();
        return Map.of(
            "borderRadius", String.valueOf(intent.getBorderRadius()),
            "fillStyle", fillStyle(intent),
            "letter", letter,
            "textColor", intent.getTextColor()
        );
    }

    /**
     * Génère les paramètres utilisés pour un symbole abstrait.
     *
     * @param intent intention graphique
     * @return paramètres injectés dans le template abstrait
     */
    private Map<String, String> abstractParams(LogoIntent intent) {
        return Map.of(
            "fillStyle", fillStyle(intent),
            "secondaryColor", intent.getSecondaryColor()
        );
    }

    /**
     * Génère les paramètres pour un symbole minimaliste.
     *
     * @param intent intention graphique
     * @return paramètres injectés dans le template minimaliste
     */
    private Map<String, String> minimalistParams(LogoIntent intent) {
        return Map.of(
            "secondaryColor", intent.getSecondaryColor(),
            "fillStyle", fillStyle(intent)
        );
    }

    /**
     * Génère les paramètres nécessaires au rendu d’un emblème.
     *
     * <p>Dans ce cas particulier, un rayon de bordure spécifique est
     * appliqué afin de garantir un rendu adapté à la forme d’emblème.</p>
     *
     * @param intent intention graphique
     * @return paramètres injectés dans le template emblème
     */
    private Map<String, String> emblemParams(LogoIntent intent) {
        int radius = intent.getBorderRadius() == 999 ? 24 : intent.getBorderRadius();
        return Map.of(
            "borderRadius", String.valueOf(radius),
            "fillStyle", fillStyle(intent),
            "textColor", intent.getTextColor()
        );
    }

    /**
     * Génère le fragment JavaScript décrivant le style de remplissage
     * du symbole.
     *
     * <p>Deux types de remplissage sont supportés :</p>
     * <ul>
     *     <li>remplissage simple avec une couleur unie</li>
     *     <li>remplissage avec dégradé linéaire</li>
     * </ul>
     *
     * @param intent intention graphique contenant les couleurs à appliquer
     * @return fragment JavaScript représentant le style de remplissage
     */
    private String fillStyle(LogoIntent intent) {
        if (intent.isUseGradient()) {
            return String.format(
                "{ fillGradient: { type: 'linear', stops: [{ offset: 0, color: '%s' }, { offset: 1, color: '%s' }] } }",
                intent.getPrimaryColor(), intent.getSecondaryColor()
            );
        }
        return String.format("{ fillColor: '%s' }", intent.getPrimaryColor());
    }

    /**
     * Charge un template JavaScript de symbole et injecte les paramètres
     * nécessaires à son exécution.
     *
     * @param filename nom du fichier JavaScript contenant le template
     * @param params paramètres à injecter dans le template
     * @return code JavaScript final généré
     */
    private String loadSymbol(String filename, Map<String, String> params) {
        return JsScriptLoader.loadWith("tools/logo/" + filename, params);
    }
}