package com.penpot.ai.application.tools.engine;

/**
 * Représente l'intention marketing et la stratégie de communication associées à une section ou une campagne.
 */
public class MarketingIntent {

    /* =========================
            CORE FLAGS
       ========================= */

    /** Indique si l'objectif principal est la conversion directe de l'utilisateur. */
    public final boolean isConversionFocused;

    /** Définit si la communication doit intégrer des éléments créant un sentiment d'urgence. */
    public final boolean containsUrgency;

    /** Précise si des éléments de preuve sociale (témoignages, logos) doivent être inclus. */
    public final boolean containsSocialProof;

    /** Indique si le positionnement doit refléter une image de marque haut de gamme ou de luxe. */
    public final boolean isLuxuryPositioning;

    /** Détermine si l'accent visuel doit être mis prioritairement sur le produit lui-même. */
    public final boolean isProductCentric;

    /** Indique si le design doit adopter une approche épurée et minimaliste. */
    public final boolean isMinimalist;

    /* =========================
            SCORES
       ========================= */

    /** Score numérique (généralement sur 100) évaluant la force de persuasion globale du contenu. */
    public final int persuasionScore;

    /** Score numérique mesurant l'impact visuel et sémantique du titre principal. */
    public final int titleImpactScore;

    /* =========================
            CTA INTENSITY
       ========================= */

    /** Niveau d'intensité et de visibilité des appels à l'action (Call-to-Action). */
    public final CtaStrength ctaStrength;

    /**
     * Définit la force visuelle et textuelle d'un appel à l'action.
     */
    public enum CtaStrength {

        /** Approche discrète, souvent textuelle ou secondaire. */
        SOFT,

        /** Intensité standard pour une navigation classique. */
        MEDIUM,

        /** Mise en avant prononcée par la couleur ou la taille. */
        STRONG,

        /** Style très contrasté conçu pour attirer l'attention de manière prioritaire. */
        AGGRESSIVE
    }

    /* =========================
            HIERARCHY
       ========================= */

    /** Niveau de hiérarchie visuelle appliqué à la structure de la section. */
    public final HierarchyLevel hierarchy;

    /**
     * Énumère les niveaux de priorité structurelle d'une section.
     */
    public enum HierarchyLevel {

        /** Section dominante occupant une place majeure dans la vue. */
        STRONG_HERO,

        /** Équilibre entre les différents éléments de la page. */
        BALANCED,

        /** Mise en retrait au profit de l'image de marque globale. */
        SOFT_BRANDING
    }

    /* =========================
            CAMPAIGN TYPE
       ========================= */

    /** Type spécifique de campagne marketing dictant les règles de rendu globales. */
    public final CampaignType campaignType;

    /**
     * Catégorise les types de campagnes gérés par le moteur.
     */
    public enum CampaignType {

        /** Focus exclusif sur l'identité de marque. */
        BRANDING,

        /** Optimisé pour la présentation d'une nouveauté. */
        PRODUCT_LAUNCH,

        /** Conçu pour les offres limitées dans le temps (urgence élevée). */
        FLASH_SALE,

        /** Mise en scène de produits premium. */
        PREMIUM_SHOWCASE,

        /** Orienté vers la collecte de données utilisateurs (leads). */
        LEAD_CAPTURE,

        /** Configuration standard polyvalente. */
        GENERIC,

        /** Adapté aux codes visuels des logiciels en tant que service. */
        SAAS_PROMOTION
    }

    /* =========================
            CONSTRUCTOR
       ========================= */

    /**
     * Initialise une nouvelle instance d'intention marketing avec l'ensemble des paramètres stratégiques.
     *
     * @param conversion       Flag de focus conversion.
     * @param urgency          Flag d'urgence.
     * @param social           Flag de preuve sociale.
     * @param luxury           Flag de positionnement luxe.
     * @param productCentric   Flag de focalisation produit.
     * @param minimal          Flag de style minimaliste.
     * @param titleImpactScore Score d'impact du titre.
     * @param persuasionScore  Score de persuasion.
     * @param ctaStrength      Force des CTA.
     * @param hierarchy        Niveau hiérarchique.
     * @param campaignType     Type de campagne.
     */
    public MarketingIntent(
        boolean conversion,
        boolean urgency,
        boolean social,
        boolean luxury,
        boolean productCentric,
        boolean minimal,
        int titleImpactScore,
        int persuasionScore,
        CtaStrength ctaStrength,
        HierarchyLevel hierarchy,
        CampaignType campaignType
    ) {
        this.isConversionFocused = conversion;
        this.containsUrgency = urgency;
        this.containsSocialProof = social;
        this.isLuxuryPositioning = luxury;
        this.isProductCentric = productCentric;
        this.isMinimalist = minimal;

        this.titleImpactScore = titleImpactScore;
        this.persuasionScore = persuasionScore;

        this.ctaStrength = ctaStrength != null ? ctaStrength : CtaStrength.MEDIUM;
        this.hierarchy = hierarchy != null ? hierarchy : HierarchyLevel.BALANCED;
        this.campaignType = campaignType != null ? campaignType : CampaignType.GENERIC;
    }

    /**
     * Détermine si la campagne actuelle applique une pression marketing élevée.
     *
     * @return {@code true} si la pression marketing est forte, sinon {@code false}.
     */
    public boolean isHighPressureCampaign() {
        return isConversionFocused
            && containsUrgency
            && persuasionScore > 70;
    }

    /**
     * Vérifie si l'intention correspond à une stratégie de marque premium.
     *
     * @return {@code true} pour un branding premium pur, sinon {@code false}.
     */
    public boolean isPremiumBranding() {
        return isLuxuryPositioning && !containsUrgency;
    }

    /**
     * Évalue si la section doit utiliser un format de type "Hero" de grande taille.
     *
     * @return {@code true} si un format large est recommandé.
     */
    public boolean shouldUseLargeHero() {
        return hierarchy == HierarchyLevel.STRONG_HERO || titleImpactScore > 75;
    }

    /**
     * Suggère si la mise en page doit être centrée pour maximiser l'impact visuel.
     *
     * @return {@code true} si l'alignement centré est préférable.
     */
    public boolean shouldCenterLayout() {
        return campaignType == CampaignType.BRANDING || isMinimalist;
    }
}