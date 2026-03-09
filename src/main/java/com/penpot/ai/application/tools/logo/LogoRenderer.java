package com.penpot.ai.application.tools.logo;

import com.penpot.ai.application.tools.support.JsScriptLoader;
import com.penpot.ai.core.domain.logo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Composant responsable du rendu complet d'un logo dans l'environnement Penpot.
 *
 * <p>
 * Cette classe génère le code JavaScript nécessaire à la création d'un logo
 * en combinant plusieurs éléments :
 * </p>
 * <ul>
 *     <li>l'initialisation du conteneur du logo</li>
 *     <li>le rendu du symbole graphique</li>
 *     <li>le rendu du texte de marque (nom et slogan)</li>
 *     <li>l'application du layout choisi</li>
 *     <li>la finalisation du logo dans le canvas</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class LogoRenderer {

    /** Renderer chargé de générer le symbole graphique du logo. */
    private final LogoSymbolRenderer symbolRenderer;

    /**
     * Génère le code JavaScript complet permettant de créer un logo
     * dans le canvas Penpot.
     *
     * <p>
     * Le processus de génération suit les étapes suivantes :
     * </p>
     * <ol>
     *     <li>initialisation du conteneur du logo</li>
     *     <li>rendu du symbole graphique</li>
     *     <li>rendu du texte et application du layout</li>
     *     <li>finalisation du logo</li>
     * </ol>
     *
     * @param spec spécification du logo contenant les informations
     *             structurelles (position, nom de marque, slogan, layout)
     * @param intent intention de design du logo définissant
     *               certains choix stylistiques (couleurs, typographie)
     * @return code JavaScript complet permettant de générer le logo
     */
    public String render(LogoSpec spec, LogoIntent intent) {
        StringBuilder code = new StringBuilder();

        code.append(JsScriptLoader.loadWith(
            "tools/logo/logo-init.js", Map.of(
            "x", String.valueOf(spec.getX()),
            "y", String.valueOf(spec.getY())
        )));
        code.append(symbolRenderer.render(spec, intent));
        code.append(renderLayoutAndText(spec, intent));
        code.append(JsScriptLoader.loadWith(
            "tools/logo/logo-finalize.js", Map.of(
            "brandName", spec.getBrandName()
        )));

        return code.toString();
    }

    /**
     * Génère le code JavaScript responsable du rendu du texte
     * (nom de marque et slogan) ainsi que du layout du logo.
     *
     * <p>
     * Le layout détermine la disposition du symbole et du texte
     * dans le logo. Chaque type de layout correspond à un script
     * JavaScript spécifique.
     * </p>
     *
     * @param spec spécification du logo
     * @param intent intention stylistique du logo
     * @return code JavaScript généré pour le layout et le texte
     */
    private String renderLayoutAndText(LogoSpec spec, LogoIntent intent) {
        String brandName = spec.getBrandName();
        String tagline = (spec.getTagline() != null) ? spec.getTagline() : "";
        String fontWeight = intent.isUseBoldTypography() ? "bold" : "normal";
        String textColor = intent.getTextColor();

        return switch (spec.getLayout()) {
            case HORIZONTAL ->
                renderLayout(
                    "tools/logo/logo-layout-horizontal.js",
                    brandName,
                    tagline,
                    fontWeight,
                    textColor
                );
            case VERTICAL ->
                renderLayout(
                    "tools/logo/logo-layout-vertical.js",
                    brandName,
                    tagline,
                    fontWeight,
                    textColor
                );
            case STACKED ->
                renderLayout(
                    "tools/logo/logo-layout-stacked.js",
                    brandName,
                    tagline,
                    fontWeight,
                    textColor
                );
            case EMBLEM ->
                renderLayout(
                    "tools/logo/logo-layout-emblem.js",
                    brandName,
                    tagline,
                    fontWeight,
                    textColor
                );
        };
    }

    /**
     * Charge et paramètre un script JavaScript responsable
     * du rendu d'un layout de logo spécifique.
     *
     * @param scriptPath chemin du script JavaScript à charger
     * @param name nom de la marque
     * @param tagline slogan ou sous-titre du logo
     * @param weight poids typographique du texte
     * @param color couleur du texte
     * @return code JavaScript généré pour le layout
     */
    private String renderLayout(
        String scriptPath,
        String name,
        String tagline,
        String weight,
        String color
    ) {
        return JsScriptLoader.loadWith(scriptPath, Map.of(
            "name", name,
            "tagline", tagline,
            "weight", weight,
            "color", color
        ));
    }
}