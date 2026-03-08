package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.common.AbstractSpecNormalizer;
import com.penpot.ai.core.domain.marketing.*;
import com.penpot.ai.core.domain.spec.SectionSpec;

import java.util.ArrayList;

/**
 * Composant spécialisé dans la normalisation et l'enrichissement des spécifications de section, 
 * spécifiquement conçu pour le pipeline de génération de documents A4 (package {@code a4engine}).
 *
 * @see com.penpot.ai.application.tools.common.AbstractSpecNormalizer
 */
public class SpecNormalizer extends AbstractSpecNormalizer {

    /**
     * Initialise et sécurise les attributs vitaux de la spécification avant tout traitement approfondi.
     *
     * @param spec La spécification technique de la section, dont les attributs manquants seront complétés.
     */
    @Override
    protected void applyDefaultValues(SectionSpec spec) {
        if (spec.getGenerationMode() == null) {
            spec.setGenerationMode(SectionSpec.GenerationMode.SMART_ASSISTED);
        }
        if (spec.getLayout() == null) spec.setLayout(MarketingLayoutType.HERO_SPLIT);
        if (spec.getTone() == null || spec.getTone() == MarketingTone.UNKNOWN) {
            spec.setTone(MarketingTone.STARTUP);
        }
        if (spec.getComponents() == null) spec.setComponents(new ArrayList<>());
    }

    /**
     * Enrichit intelligemment la maquette en injectant des composants additionnels selon la tonalité marketing.
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
                add(spec, MarketingComponent.TESTIMONIAL_CARD);
                add(spec, MarketingComponent.TRUST_LOGOS);
            }
            case STARTUP -> {
                add(spec, MarketingComponent.STATS_BLOCK);
                add(spec, MarketingComponent.TRUST_LOGOS);
                add(spec, MarketingComponent.FEATURE_LIST);
            }
            case CORPORATE -> add(spec, MarketingComponent.TRUST_LOGOS);
            default -> { }
        }

        if (spec.getLayout() == MarketingLayoutType.HERO_SPLIT) {
            spec.setWithPreview(true);
        }
    }

    /**
     * Déploie une composition marketing maximale et autonome lorsque le mode créatif est activé.
     *
     * @param spec La spécification cible, qui recevra la composition exhaustive si les conditions sont réunies.
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
            add(spec, MarketingComponent.PRICING_CARD_PREVIEW);
        }
    }
}