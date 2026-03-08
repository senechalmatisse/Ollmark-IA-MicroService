package com.penpot.ai.application.tools.engine;

import com.penpot.ai.application.tools.common.AbstractSpecNormalizer;
import com.penpot.ai.core.domain.marketing.*;
import com.penpot.ai.core.domain.spec.SectionSpec;

import java.util.ArrayList;

/**
 * Composant spécialisé dans la normalisation et l'enrichissement des spécifications, 
 * spécifiquement conçu pour le pipeline de génération des sections d'appel principales (hero sections) du package {@code engine}.
 */
public class SpecNormalizer extends AbstractSpecNormalizer {

    /**
     * Initialise et sécurise les attributs fondamentaux de la spécification avant son traitement par le moteur de rendu.
     *
     * @param spec La spécification technique de la section hero, dont les attributs manquants seront automatiquement complétés.
     */
    @Override
    protected void applyDefaultValues(SectionSpec spec) {
        if (spec.getGenerationMode() == null) {
            spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
        }
        if (spec.getLayout() == null) spec.setLayout(MarketingLayoutType.HERO_SPLIT);
        if (spec.getTone() == null) spec.setTone(MarketingTone.STARTUP);
        if (spec.getStyle() == null) spec.setStyle(MarketingStyle.BOLD_ECOMMERCE);
        if (spec.getComponents() == null) spec.setComponents(new ArrayList<>());
    }

    /**
     * Enrichit contextuellement la maquette en injectant des composants marketing spécifiques à la tonalité requise, 
     * sous réserve que le mode d'assistance intelligente ({@code SMART_ASSISTED}) soit actif.
     *
     * @param spec L'objet de spécification en cours de traitement, soumis à l'enrichissement contextuel.
     */
    @Override
    protected void applySmartAssistedMode(SectionSpec spec) {
        if (spec.getGenerationMode() != SectionSpec.GenerationMode.SMART_ASSISTED) return;

        switch (spec.getTone()) {
            case URGENT -> {
                add(spec, MarketingComponent.BADGE);
                add(spec, MarketingComponent.DISCOUNT_RIBBON);
                add(spec, MarketingComponent.COUNTDOWN_TIMER);
            }
            case PREMIUM, LUXURY -> {
                add(spec, MarketingComponent.TRUST_LOGOS);
                add(spec, MarketingComponent.TESTIMONIAL_CARD);
            }
            case STARTUP -> {
                add(spec, MarketingComponent.TRUST_LOGOS);
                add(spec, MarketingComponent.STATS_BLOCK);
            }
            case CORPORATE -> add(spec, MarketingComponent.TRUST_LOGOS);
            case FRIENDLY, FEMININE -> add(spec, MarketingComponent.BADGE);
            default -> { }
        }
    }

    /**
     * Déploie une composition marketing riche et autonome lorsque le système opère en mode purement créatif ({@code AUTO_CREATIVE}).
     *
     * @param spec La spécification cible, qui recevra la composition exhaustive si le mode créatif est engagé et la liste vide.
     */
    @Override
    protected void applyAutoCreativeMode(SectionSpec spec) {
        if (spec.getGenerationMode() == SectionSpec.GenerationMode.AUTO_CREATIVE &&
            spec.getComponents().isEmpty()
        ) {
            add(spec, MarketingComponent.ANNOUNCEMENT_BAR);
            add(spec, MarketingComponent.BADGE);
            add(spec, MarketingComponent.FEATURE_LIST);
            add(spec, MarketingComponent.STATS_BLOCK);
            add(spec, MarketingComponent.TRUST_LOGOS);
            add(spec, MarketingComponent.TESTIMONIAL_CARD);
        }
    }
}