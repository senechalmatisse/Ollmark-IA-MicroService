package com.penpot.ai.core.domain.marketing;

/**
 * Définit les modèles d'agencement structurel pour les interfaces de communication et de marketing.
 */
public enum MarketingLayoutType {

    /** Agencement scindé en deux zones distinctes. */
    HERO_SPLIT,

    /** Composition à alignement centralisé. */
    HERO_CENTERED,

    /** Agencement valorisant une iconographie dominante. */
    HERO_WITH_IMAGE,

    /**
     * Section d'en-tête intégrant des indicateurs de performance.
     */
    HERO_WITH_STATS,

    /** Grille ou liste descriptive des fonctionnalités. */
    FEATURE_LIST,

    /** Bloc modulaire standard pour page d'atterrissage. */
    LANDING_BLOCK,

    /** Espace dédié aux offres commerciales temporaires. */
    PROMO_SECTION
}