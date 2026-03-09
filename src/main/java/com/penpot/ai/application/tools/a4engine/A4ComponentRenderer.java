package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.support.JsScriptLoader;
import com.penpot.ai.core.domain.marketing.MarketingComponent;
import com.penpot.ai.core.domain.spec.SectionSpec;

import java.util.*;
import java.util.function.Function;

/**
 * Composant technique responsable de la génération et de l'assemblage dynamique du code JavaScript 
 * matérialisant les éléments marketing au sein d'une section A4.
 */
public class A4ComponentRenderer {

    /** Dictionnaire immuable agissant comme un registre de fonctions de rendu. */
    private static final Map<MarketingComponent, Function<String, String>> RENDERERS;

    static {
        Map<MarketingComponent, Function<String, String>> m = new EnumMap<>(MarketingComponent.class);

        m.put(MarketingComponent.ANNOUNCEMENT_BAR, c -> loadWith("a4-component-announcement.js", c));
        m.put(MarketingComponent.BADGE, c -> loadWith("a4-component-badge.js", c));
        m.put(MarketingComponent.FEATURE_LIST, c -> loadWith("a4-component-feature-list.js", c));
        m.put(MarketingComponent.STATS_BLOCK, c -> loadWith("a4-component-stats.js", c));
        m.put(MarketingComponent.TRUST_LOGOS, c -> loadWith("a4-component-trust-logos.js", c));
        m.put(MarketingComponent.COUNTDOWN_TIMER, c -> loadWith("a4-component-countdown.js", c));
        m.put(MarketingComponent.TESTIMONIAL_CARD, c -> loadWith("a4-component-testimonial.js", c));
        m.put(MarketingComponent.PRICING_CARD_PREVIEW,c -> loadWith("a4-component-pricing-preview.js", c));

        /* 
         * Configuration spécifique pour le ruban de réduction, nécessitant l'injection de paramètres 
         * additionnels en plus de la couleur textuelle standard.
         */
        m.put(MarketingComponent.DISCOUNT_RIBBON, c -> JsScriptLoader.loadWith(
            "tools/a4engine/a4-component-discount-ribbon.js", Map.of(
                "textColor", c,
                "accentColor", "#DC2626",
                "discountValue", "20"
            )));

        RENDERERS = m;
    }

    /**
     * Orchestre la compilation séquentielle des fragments JavaScript en parcourant la spécification fournie.
     *
     * @param spec      La spécification de la section contenant la liste ordonnée des composants marketing à intégrer.
     * @param textColor Le code couleur (hexadécimal ou variable CSS) à appliquer systématiquement sur les textes des composants.
     * @return          Une chaîne de caractères concaténant l'ensemble du code JavaScript généré, prête à être exécutée.
     */
    public String render(SectionSpec spec, String textColor) {
        List<MarketingComponent> components = spec.getComponents();
        if (components == null || components.isEmpty()) return "";

        StringBuilder code = new StringBuilder("const COMPONENT_SPACING = -16;");
        for (MarketingComponent component : components) {
            Function<String, String> renderer = RENDERERS.get(component);
            if (renderer != null) code.append(renderer.apply(textColor));
        }

        return code.toString();
    }

    /**
     * Fonction utilitaire interne facilitant le chargement et l'hydratation des gabarits JavaScript standards.
     * Elle abstrait le chemin relatif du répertoire des modèles A4 et automatise la substitution de la variable de couleur.
     *
     * @param filename  Le nom exact du fichier gabarit JavaScript (incluant l'extension .js) situé dans le dossier des ressources.
     * @param textColor La valeur chromatique à injecter dans le gabarit lors de son chargement.
     * @return          Le fragment de code JavaScript dont les variables contextuelles ont été résolues.
     */
    private static String loadWith(String filename, String textColor) {
        return JsScriptLoader.loadWith("tools/a4engine/" + filename, Map.of("textColor", textColor));
    }
}