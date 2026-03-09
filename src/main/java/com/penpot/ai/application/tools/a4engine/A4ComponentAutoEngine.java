package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.engine.MarketingIntent;
import com.penpot.ai.core.domain.marketing.*;
import com.penpot.ai.core.domain.spec.SectionSpec;

import java.util.*;

/**
 * Engine qui enrichit automatiquement les composants marketing
 * d'une section A4 en fonction :
 *
 * - du MarketingIntent (analyse NLP)
 * - du tone
 * - des flags explicitement demandés dans la spec
 *
 * Objectif :
 * L'utilisateur décrit la section → le moteur choisit les composants optimaux.
 *
 * <h2>Source de vérité unique</h2>
 * {@link #RANK} encode à la fois l'ordre d'affichage final et la priorité de
 * conservation lors du trim. Cela remplace l'ancien double encodage (switch +
 * tableau {@code priority[]}) qui maintenait deux ordres légèrement divergents.
 */
public class A4ComponentAutoEngine {

    /** Seuil quantitatif définissant le nombre maximal de composants marketing admissibles sur une même section A4. */
    private static final int MAX_COMPONENTS = 7;

    /** Dictionnaire immuable établissant l'ordre de priorité canonique des composants, du plus stratégique au plus accessoire. */
    private static final Map<MarketingComponent, Integer> RANK;

    static {
        List<MarketingComponent> order = List.of(
            MarketingComponent.ANNOUNCEMENT_BAR,
            MarketingComponent.DISCOUNT_RIBBON,
            MarketingComponent.BADGE,
            MarketingComponent.COUNTDOWN_TIMER,
            MarketingComponent.PRICING_CARD_PREVIEW,
            MarketingComponent.FEATURE_LIST,
            MarketingComponent.STATS_BLOCK,
            MarketingComponent.TRUST_LOGOS,
            MarketingComponent.TESTIMONIAL_CARD
        );
        Map<MarketingComponent, Integer> map = new EnumMap<>(MarketingComponent.class);
        for (int i = 0; i < order.size(); i++) map.put(order.get(i), i);
        RANK = Collections.unmodifiableMap(map);
    }

    /**
     * Comparateur utilitaire s'appuyant sur la cartographie canonique pour ordonner efficacement les collections de composants.
     */
    private static final Comparator<MarketingComponent> BY_RANK =
        Comparator.comparingInt(c -> RANK.getOrDefault(c, 999));

    /**
     * Orchestre le pipeline complet d'enrichissement d'une spécification de section en y injectant les composants adéquats.
     *
     * @param spec   La spécification technique de la section A4 à enrichir, qui sera mutée au cours de l'opération.
     * @param intent L'objet encapsulant l'intention marketing extraite de l'analyse sémantique.
     * @throws IllegalArgumentException Si la spécification ou l'intention transmise est nulle.
     */
    public void enrich(SectionSpec spec, MarketingIntent intent) {
        if (spec == null) throw new IllegalArgumentException("SectionSpec cannot be null");
        if (intent == null) throw new IllegalArgumentException("MarketingIntent cannot be null");
        if (spec.getComponents() == null) spec.setComponents(new ArrayList<>());

        LinkedHashSet<MarketingComponent> components = new LinkedHashSet<>(spec.getComponents());

        applyFlags(spec, components);
        applyConversionSignals(intent, components);
        applySocialProofSignals(intent, components);
        applyProductCentricSignals(intent, components);
        applyLuxuryConstraints(intent, components);

        if (intent.isMinimalist) applyMinimalMode(components);
        applyToneRules(spec.getTone(), components);

        List<MarketingComponent> result = trim(components);
        result.sort(BY_RANK);
        spec.setComponents(result);
    }

    /**
     * Intègre les composants dont la présence a été explicitement requise par des drapeaux (flags) au sein de la spécification.
     *
     * @param spec       La spécification porteuse des requêtes explicites.
     * @param components L'ensemble courant des composants à enrichir.
     */
    private void applyFlags(SectionSpec spec, Set<MarketingComponent> components) {
        if (spec.isWithStats()) components.add(MarketingComponent.STATS_BLOCK);
        if (spec.isWithFeatures()) components.add(MarketingComponent.FEATURE_LIST);
        if (spec.isWithTestimonial()) components.add(MarketingComponent.TESTIMONIAL_CARD);
    }

    /**
     * Évalue les signaux orientés vers la conversion et greffe les éléments incitatifs correspondants.
     *
     * @param intent     Le contexte sémantique de la demande.
     * @param c          La collection mutative des composants marketing.
     */
    private void applyConversionSignals(MarketingIntent intent, Set<MarketingComponent> c) {
        if (!intent.isConversionFocused) return;

        c.add(MarketingComponent.PRICING_CARD_PREVIEW);
        c.add(MarketingComponent.TRUST_LOGOS);

        if (intent.containsSocialProof) c.add(MarketingComponent.TESTIMONIAL_CARD);
        if (intent.containsUrgency) {
            c.add(MarketingComponent.BADGE);
            c.add(MarketingComponent.DISCOUNT_RIBBON);
            c.add(MarketingComponent.COUNTDOWN_TIMER);
            c.add(MarketingComponent.ANNOUNCEMENT_BAR);
        }
    }

    /**
     * Renforce la crédibilité du document en y ajoutant des éléments de preuve sociale, à condition que la section 
     * ne soit pas déjà saturée par des objectifs de conversion directe.
     *
     * @param intent Le profil sémantique analysé.
     * @param c      L'ensemble des composants en cours d'assemblage.
     */
    private void applySocialProofSignals(MarketingIntent intent, Set<MarketingComponent> c) {
        if (intent.isConversionFocused || !intent.containsSocialProof) return;
        c.add(MarketingComponent.TESTIMONIAL_CARD);
        c.add(MarketingComponent.TRUST_LOGOS);
        c.add(MarketingComponent.STATS_BLOCK);
    }

    /**
     * Valorise les caractéristiques intrinsèques du produit lorsque l'intention est explicitement centrée sur ce dernier.
     *
     * @param intent Le contexte sémantique ciblé.
     * @param c      Le registre temporaire des composants.
     */
    private void applyProductCentricSignals(MarketingIntent intent, Set<MarketingComponent> c) {
        if (intent.containsUrgency || !intent.isProductCentric) return;
        c.add(MarketingComponent.FEATURE_LIST);
        c.add(MarketingComponent.BADGE);
    }

    /**
     * Applique un filtrage rigoureux requis par le positionnement haut de gamme ou luxueux.
     *
     * @param intent L'intention qualifiant le niveau de gamme.
     * @param c      L'agrégat de composants à filtrer.
     */
    private void applyLuxuryConstraints(MarketingIntent intent, Set<MarketingComponent> c) {
        if (!intent.isLuxuryPositioning) return;
        c.add(MarketingComponent.TRUST_LOGOS);
        c.add(MarketingComponent.TESTIMONIAL_CARD);
        removeUrgencyComponents(c);
    }

    /**
     * Module la sélection finale en adéquation avec la tonalité éditoriale spécifiée.
     *
     * @param tone La tonalité marketing imposée à la section.
     * @param c    La liste des composants soumise à altération.
     */
    private void applyToneRules(MarketingTone tone, Set<MarketingComponent> c) {
        if (tone == null) return;
        switch (tone) {
            case URGENT -> {
                c.add(MarketingComponent.BADGE);
                c.add(MarketingComponent.DISCOUNT_RIBBON);
                c.add(MarketingComponent.COUNTDOWN_TIMER);
                c.add(MarketingComponent.ANNOUNCEMENT_BAR);
            }
            case PREMIUM, LUXURY -> {
                c.add(MarketingComponent.TRUST_LOGOS);
                c.add(MarketingComponent.TESTIMONIAL_CARD);
                removeUrgencyComponents(c);
            }
        }
    }

    /**
     * Purge l'ensemble des composants associés à la notion d'urgence commerciale.
     *
     * @param c La collection à nettoyer.
     */
    private void removeUrgencyComponents(Set<MarketingComponent> c) {
        c.remove(MarketingComponent.DISCOUNT_RIBBON);
        c.remove(MarketingComponent.COUNTDOWN_TIMER);
        c.remove(MarketingComponent.ANNOUNCEMENT_BAR);
    }

    /**
     * Réduit drastiquement la densité visuelle en ne conservant qu'une poignée de composants essentiels.
     *
     * @param components Le registre complet qui sera substitué par sa version restreinte.
     */
    private void applyMinimalMode(Set<MarketingComponent> components) {
        LinkedHashSet<MarketingComponent> minimal = new LinkedHashSet<>();

        if (components.contains(MarketingComponent.TRUST_LOGOS))
            minimal.add(MarketingComponent.TRUST_LOGOS);
        if (components.contains(MarketingComponent.TESTIMONIAL_CARD))
            minimal.add(MarketingComponent.TESTIMONIAL_CARD);
        if (components.contains(MarketingComponent.FEATURE_LIST))
            minimal.add(MarketingComponent.FEATURE_LIST);
        if (minimal.isEmpty() && components.contains(MarketingComponent.STATS_BLOCK))
            minimal.add(MarketingComponent.STATS_BLOCK);

        components.clear();
        components.addAll(minimal);
    }

    /**
     * Restreint la collection de composants pour qu'elle n'excède pas le quota de lisibilité défini par {@link #MAX_COMPONENTS}.
     *
     * @param components L'ensemble potentiellement excédentaire des éléments à traiter.
     * @return           Une liste formellement contrainte en taille et ordonnée par priorité.
     */
    private List<MarketingComponent> trim(Set<MarketingComponent> components) {
        List<MarketingComponent> list = new ArrayList<>(components);
        if (list.size() <= MAX_COMPONENTS) return list;
        list.sort(BY_RANK);
        return new ArrayList<>(list.subList(0, MAX_COMPONENTS));
    }
}