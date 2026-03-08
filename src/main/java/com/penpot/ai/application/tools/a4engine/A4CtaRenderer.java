package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.engine.Theme;
import com.penpot.ai.application.tools.support.JsScriptLoader;
import com.penpot.ai.core.domain.spec.SectionSpec;
import com.penpot.ai.shared.util.JsStringUtils;

import java.util.Map;

/**
 * Composant technique dédié à la génération du code JavaScript régissant l'affichage des appels à l'action (Call-To-Action) au sein d'une maquette au format A4.
 */
public class A4CtaRenderer {

    /**
     * Produit le fragment JavaScript exécutable correspondant aux boutons interactifs définis dans la spécification.
     *
     * @param spec  L'objet de spécification de la section contenant les libellés bruts des boutons principal et secondaire.
     * @param theme L'objet encapsulant les propriétés visuelles et chromatiques à appliquer sur le document.
     * @return      Une chaîne de caractères représentant le code JavaScript finalisé, prête à être interprétée par le canevas, 
     * ou une chaîne vide si aucun appel à l'action n'est requis.
     */
    public String render(SectionSpec spec, Theme theme) {
        String primary = JsStringUtils.jsSafe(spec.getPrimaryButton());
        String secondary = JsStringUtils.jsSafe(spec.getSecondaryButton());

        if (primary.isBlank() && secondary.isBlank()) return "";
        String secondaryText = theme.gradient ? theme.textOnDark : theme.textOnLight;

        return JsScriptLoader.loadWith("tools/a4engine/a4-cta.js", Map.of(
            "primary", primary,
            "secondary", secondary,
            "primaryBg", theme.ctaPrimary,
            "primaryText", theme.ctaText,
            "secondaryText", secondaryText
        ));
    }
}