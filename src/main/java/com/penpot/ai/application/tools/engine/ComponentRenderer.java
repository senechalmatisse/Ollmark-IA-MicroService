package com.penpot.ai.application.tools.engine;

import com.penpot.ai.application.tools.support.JsScriptLoader;
import com.penpot.ai.core.domain.marketing.MarketingComponent;
import com.penpot.ai.core.domain.spec.SectionSpec;

import java.util.*;

/**
 * Responsable du rendu des composants marketing en orchestrant le chargement des scripts JavaScript associés.
 * <p>
 * Cette classe assure la transition entre les spécifications de haut niveau et le code exécutable. 
 * Elle s'appuie sur un mécanisme de dispatch statique pour lier chaque type de composant 
 * {@link MarketingComponent} à son fichier de définition logique respectif.
 * </p>
 */
public class ComponentRenderer {

    /** Chemin racine pour l'accès aux ressources JavaScript du moteur de rendu. */
    private static final String JS = "tools/engine/";

    /**
     * Table de correspondance immuable associant chaque type de composant marketing 
     * à son fichier de script correspondant.
     */
    private static final Map<MarketingComponent, String> JS_FILES;

    static {
        Map<MarketingComponent, String> m = new EnumMap<>(MarketingComponent.class);
        m.put(MarketingComponent.ANNOUNCEMENT_BAR, JS + "component-announcement.js");
        m.put(MarketingComponent.BADGE, JS + "component-badge.js");
        m.put(MarketingComponent.FEATURE_LIST, JS + "component-feature-list.js");
        m.put(MarketingComponent.STATS_BLOCK, JS + "component-stats.js");
        m.put(MarketingComponent.COUNTDOWN_TIMER, JS + "component-countdown.js");
        m.put(MarketingComponent.TESTIMONIAL_CARD, JS + "component-testimonial.js");
        JS_FILES = Collections.unmodifiableMap(m);
    }

    /**
     * Génère la chaîne de caractères contenant l'intégralité du code JavaScript nécessaire 
     * au rendu d'une section.
     *
     * @param spec   L'objet de spécification contenant la liste brute des composants souhaités.
     * @param theme  Le thème graphique appliqué pour définir les propriétés visuelles.
     * @param intent L'intention marketing permettant d'ajuster dynamiquement le contenu.
     * @return Une chaîne de caractères représentant le code JS complet, ou une chaîne vide si aucun composant n'est présent.
     */
    public String render(SectionSpec spec, Theme theme, MarketingIntent intent) {
        List<MarketingComponent> components = buildComponentList(spec, intent);
        if (components.isEmpty()) return "";

        String textColor = resolveTextColor(theme);
        StringBuilder code = new StringBuilder(
            JsScriptLoader.loadWith("snippets/component-header.js",
                Map.of("textColor", textColor))
        );

        for (MarketingComponent component : components) {
            String jsFile = JS_FILES.get(component);
            if (jsFile != null) code.append(JsScriptLoader.load(jsFile));
        }

        return code.toString();
    }

    /**
     * Construit la liste finale des composants à rendre en fusionnant les données de spécification 
     * et les exigences liées à l'intention marketing.
     *
     * @param spec   La spécification de section.
     * @param intent L'intention marketing.
     * @return Une liste ordonnée de {@link MarketingComponent}.
     */
    private static List<MarketingComponent> buildComponentList(
        SectionSpec spec, MarketingIntent intent
    ) {
        List<MarketingComponent> components = new ArrayList<>();
        if (spec.getComponents() != null) components.addAll(spec.getComponents());
        if (intent.containsSocialProof) addIfAbsent(components, MarketingComponent.TESTIMONIAL_CARD);
        return components;
    }

    /**
     * Détermine la couleur de texte appropriée en fonction des propriétés du thème fourni.
     *
     * @param theme Le thème à analyser.
     * @return La valeur hexadécimale ou le nom de la couleur de texte à utiliser.
     */
    private static String resolveTextColor(Theme theme) {
        return (theme.gradient || Theme.DARK_SAAS.name.equalsIgnoreCase(theme.name))
            ? theme.textOnDark
            : theme.textOnLight;
    }

    /**
     * Ajoute un composant à la liste spécifiée uniquement s'il n'y figure pas déjà.
     *
     * @param list      La liste cible.
     * @param component Le composant à ajouter.
     */
    private static void addIfAbsent(List<MarketingComponent> list, MarketingComponent component) {
        if (!list.contains(component)) list.add(component);
    }
}