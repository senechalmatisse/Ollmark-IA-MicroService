package com.penpot.ai.application.tools.common;

import com.penpot.ai.core.domain.marketing.*;
import com.penpot.ai.core.domain.spec.SectionSpec;

import java.util.*;

/**
 * Squelette commun pour la normalisation d'une {@link SectionSpec}.
 *
 * <p>{@link #normalize} fixe l'algorithme et délègue les étapes variables
 * aux sous-classes :</p>
 *
 * <pre>
 *  normalize()
 *   ├─ applyDefaultValues()      ← abstraite  (valeurs de repli différentes par contexte)
 *   ├─ cleanComponents()         ← partagée
 *   ├─ [court-circuit USER_STRICT + sortStable()]
 *   ├─ applyFlags()              ← partagée, surchargeable
 *   ├─ applySmartAssistedMode()  ← abstraite  (composants par tone différents)
 *   ├─ applyAutoCreativeMode()   ← abstraite  (jeu de composants différent)
 *   └─ sortStable()              ← partagée
 * </pre>
 *
 * <p>Sous-classes concrètes :
 * {@code engine.SpecNormalizer} et {@code a4engine.SpecNormalizer}.</p>
 */
public abstract class AbstractSpecNormalizer {

    /**
     * Point d'entrée : orchestre la normalisation complète.
     * Méthode {@code final} pour garantir l'immuabilité de l'ordre des étapes.
     *
     * @param spec la spécification à normaliser
     * @throws IllegalArgumentException si {@code spec} est {@code null}
     */
    public final void normalize(SectionSpec spec) {
        if (spec == null) throw new IllegalArgumentException("SectionSpec cannot be null");

        applyDefaultValues(spec);
        cleanComponents(spec);

        if (spec.getGenerationMode() == SectionSpec.GenerationMode.USER_STRICT) {
            sortStable(spec);
            return;
        }

        applyFlags(spec);
        applySmartAssistedMode(spec);
        applyAutoCreativeMode(spec);
        sortStable(spec);
    }

    /** Initialise les attributs {@code null} avec des valeurs de repli cohérentes. */
    protected abstract void applyDefaultValues(SectionSpec spec);

    /**
     * Enrichit la spec par tone, uniquement en mode {@code SMART_ASSISTED}.
     * Les composants ajoutés par tone diffèrent entre les deux contextes.
     */
    protected abstract void applySmartAssistedMode(SectionSpec spec);

    /**
     * Injecte une structure exhaustive par défaut en mode {@code AUTO_CREATIVE}
     * lorsque la liste de composants est vide.
     * Le jeu de composants diffère entre les deux contextes.
     */
    protected abstract void applyAutoCreativeMode(SectionSpec spec);

    /**
     * Intègre les composants requis par les flags booléens de la spec.
     * Surchargeable si un contexte requiert une logique différente.
     */
    protected void applyFlags(SectionSpec spec) {
        if (spec.isWithStats() || spec.getLayout() == MarketingLayoutType.HERO_WITH_STATS) {
            add(spec, MarketingComponent.STATS_BLOCK);
        }
        if (spec.isWithFeatures()) add(spec, MarketingComponent.FEATURE_LIST);
        if (spec.isWithTestimonial()) add(spec, MarketingComponent.TESTIMONIAL_CARD);
    }

    /**
     * Purge la liste des composants de toute valeur {@code null} ou {@code UNKNOWN}.
     * Préserve l'ordre d'insertion et élimine les doublons via {@link LinkedHashSet}.
     */
    protected void cleanComponents(SectionSpec spec) {
        LinkedHashSet<MarketingComponent> clean = new LinkedHashSet<>();
        for (MarketingComponent c : spec.getComponents()) {
            if (c != null && c != MarketingComponent.UNKNOWN) clean.add(c);
        }
        spec.setComponents(new ArrayList<>(clean));
    }

    /** Insère un composant de façon idempotente. Initialise la liste si {@code null}. */
    protected void add(SectionSpec spec, MarketingComponent component) {
        if (component == null) return;
        if (spec.getComponents() == null) spec.setComponents(new ArrayList<>());
        if (!spec.getComponents().contains(component)) spec.getComponents().add(component);
    }

    /** Tri lexicographique stable sur {@link Enum#name()} — rendu déterministe. */
    protected void sortStable(SectionSpec spec) {
        if (spec.getComponents() == null) return;
        spec.getComponents().sort(Comparator.comparing(Enum::name));
    }
}