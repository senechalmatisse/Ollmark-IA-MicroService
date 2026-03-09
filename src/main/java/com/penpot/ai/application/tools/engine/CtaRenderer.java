package com.penpot.ai.application.tools.engine;

import com.penpot.ai.application.tools.support.JsScriptLoader;
import com.penpot.ai.core.domain.spec.SectionSpec;
import com.penpot.ai.shared.util.JsStringUtils;

import java.util.Map;

/**
 * Composant spécialisé dans la génération du code JavaScript dédié aux appels à l'action (Call-to-Action - CTA).
 * <p>
 * Cette classe assure l'interface entre les spécifications de contenu définies dans le domaine et les 
 * gabarits (templates) de scripts nécessaires au rendu visuel.
 * </p>
 */
public class CtaRenderer {

    /**
     * Génère la séquence JavaScript permettant l'affichage des boutons d'action d'une section.
     *
     * @param spec  L'objet de spécification contenant les labels des boutons.
     * @param theme Le thème graphique en vigueur pour l'extraction des codes couleurs.
     * @return Une chaîne de caractères contenant le code JS de rendu, ou une chaîne vide si aucun bouton n'est défini.
     */
    public String render(SectionSpec spec, Theme theme) {
        String primary = JsStringUtils.jsSafe(spec.getPrimaryButton());
        String secondary = JsStringUtils.jsSafe(spec.getSecondaryButton());
        if (primary.isBlank() && secondary.isBlank()) return "";

        String secondaryText = (theme.gradient || "Dark SaaS".equalsIgnoreCase(theme.name))
            ? theme.textOnDark
            : theme.textOnLight;

        return JsScriptLoader.loadWith("tools/engine/section-cta.js", Map.of(
            "primary", primary,
            "secondary", secondary,
            "primaryBg", theme.ctaPrimary,
            "primaryText", theme.ctaText,
            "secondaryText", secondaryText
        ));
    }
}