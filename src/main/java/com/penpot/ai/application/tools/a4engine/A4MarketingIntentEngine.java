package com.penpot.ai.application.tools.a4engine;

import com.penpot.ai.application.tools.engine.MarketingIntent;
import com.penpot.ai.core.domain.marketing.MarketingTone;
import com.penpot.ai.core.domain.spec.SectionSpec;
import com.penpot.ai.shared.util.JsStringUtils;

/**
 * Moteur d'analyse sémantique et marketing dédié aux sections au format A4.
 */
public class A4MarketingIntentEngine {

    /**
     * Matrice définissant les seuils de longueur du titre et les scores d'impact correspondants.
     */
    private static final int[][] TITLE_IMPACT_THRESHOLDS = {
        { 30, 95 },
        { 60, 80 },
        { 90, 70 },
    };

    /** Score d'impact de repli attribué par défaut lorsque le titre excède tous les seuils de concision définis. */
    private static final int TITLE_IMPACT_FALLBACK = 55;

    /**
     * Évalue de manière exhaustive la spécification d'une section pour en extraire une intention marketing structurée.
     *
     * @param spec La spécification technique et textuelle de la section à analyser.
     * @return     L'objet {@link MarketingIntent} encapsulant les drapeaux sémantiques et les scores d'impact calculés.
     */
    public MarketingIntent analyze(SectionSpec spec) {
        String title = JsStringUtils.safe(spec.getTitle()).toLowerCase();
        String fullText = (title + " " + JsStringUtils.safe(spec.getParagraph())).toLowerCase();

        boolean urgency = JsStringUtils.containsAny(fullText,
            "aujourd", "maintenant", "vite", "limited",
            "offre", "promotion", "promo",
            "dernière chance", "plus que", "dernier jour");

        boolean socialProof = JsStringUtils.containsAny(fullText,
            "clients", "avis", "noté",
            "témoignage", "témoignages", "confiance",
            "utilisateurs", "entreprises", "recommandé", "note moyenne");

        boolean luxury =
            spec.getTone() == MarketingTone.LUXURY
            || JsStringUtils.containsAny(fullText,
                "premium", "luxury", "exclusif",
                "haut de gamme", "élite", "prestige");

        boolean minimal =
            JsStringUtils.containsAny(fullText, "minimal", "simple", "épuré")
            || (spec.getStyle() != null && spec.getStyle().name().contains("MINIMAL"));

        boolean productCentric = JsStringUtils.containsAny(fullText,
            "nouveau", "produit", "collection",
            "solution", "plateforme", "lancement", "découvrez");

        boolean saas = JsStringUtils.containsAny(fullText,
            "logiciel", "plateforme", "outil",
            "application", "dashboard", "interface");

        boolean pricing = JsStringUtils.containsAny(fullText,
            "€", "prix", "tarif",
            "mois", "abonnement", "annuel", "mensuel");

        boolean conversion = urgency || spec.getTone() == MarketingTone.URGENT || pricing;

        int titleImpactScore = titleImpact(title.length());
        int persuasionScore  = persuasion(conversion, socialProof, urgency, luxury, productCentric);

        return new MarketingIntent(
            conversion, urgency, socialProof, luxury, productCentric, minimal,
            titleImpactScore, persuasionScore,
            ctaStrength(urgency, conversion, socialProof),
            hierarchy(conversion, socialProof),
            campaignType(urgency, conversion, luxury, productCentric, pricing, saas)
        );
    }

    /**
     * Calcule le score d'impact théorique d'un titre en se basant sur sa concision.
     *
     * @param length Le nombre de caractères composant le titre analysé.
     * @return       Un score entier représentant l'impact estimé (plus le titre est court, plus le score est élevé).
     */
    private static int titleImpact(int length) {
        for (int[] threshold : TITLE_IMPACT_THRESHOLDS) {
            if (length < threshold[0]) return threshold[1];
        }
        return TITLE_IMPACT_FALLBACK;
    }

    /**
     * Établit un score global de persuasion en pondérant l'accumulation des différents signaux marketing détectés.
     *
     * @param conversion     Indique si la section est clairement orientée vers l'acte d'achat.
     * @param socialProof    Indique la présence de réassurance via les retours clients.
     * @param urgency        Indique l'utilisation de leviers temporels ou de rareté.
     * @param luxury         Indique un positionnement haut de gamme.
     * @param productCentric Indique une focalisation sur les caractéristiques du produit.
     * @return               Le score de persuasion calculé, compris entre 0 et 100.
     */
    private static int persuasion(
        boolean conversion, boolean socialProof,
        boolean urgency, boolean luxury, boolean productCentric
    ) {
        int score = 0;
        if (conversion) score += 35;
        if (socialProof) score += 30;
        if (urgency) score += 20;
        if (luxury) score += 15;
        if (productCentric)score += 10;
        return Math.min(score, 100);
    }

    /**
     * Détermine la configuration d'agressivité optimale pour l'appel à l'action (CTA) en fonction du contexte sémantique.
     *
     * @param urgency     Indique un contexte temporel limité.
     * @param conversion  Indique une finalité transactionnelle.
     * @param socialProof Indique la présence de réassurance client.
     * @return            Le niveau de force recommandé pour les boutons d'action.
     */
    private static MarketingIntent.CtaStrength ctaStrength(
        boolean urgency, boolean conversion, boolean socialProof
    ) {
        if (urgency && conversion) return MarketingIntent.CtaStrength.AGGRESSIVE;
        if (conversion) return MarketingIntent.CtaStrength.STRONG;
        if (socialProof) return MarketingIntent.CtaStrength.MEDIUM;
        return MarketingIntent.CtaStrength.SOFT;
    }

    /**
     * Définit la structure de la hiérarchie visuelle globale que devra adopter la mise en page.
     *
     * @param conversion  Indique si l'interface doit générer de l'engagement transactionnel.
     * @param socialProof Indique si l'interface doit rassurer l'utilisateur.
     * @return            La directive de structuration visuelle recommandée.
     */
    private static MarketingIntent.HierarchyLevel hierarchy(
        boolean conversion, boolean socialProof
    ) {
        if (conversion) return MarketingIntent.HierarchyLevel.STRONG_HERO;
        if (socialProof) return MarketingIntent.HierarchyLevel.BALANCED;
        return MarketingIntent.HierarchyLevel.SOFT_BRANDING;
    }

    /**
     * Catégorise le type de campagne marketing en s'appuyant sur les signaux sémantiques dominants.
     *
     * @param urgency        Signal d'incitation temporelle.
     * @param conversion     Signal d'incitation à l'achat.
     * @param luxury         Signal de positionnement haut de gamme.
     * @param productCentric Signal orienté sur le produit lui-même.
     * @param pricing        Signal révélant une notion de tarification.
     * @param saas           Signal identifiant un service logiciel en ligne.
     * @return               La catégorie de campagne identifiée, ou générique par défaut.
     */
    private static MarketingIntent.CampaignType campaignType(
        boolean urgency, boolean conversion, boolean luxury,
        boolean productCentric, boolean pricing, boolean saas
    ) {
        if (urgency && conversion) return MarketingIntent.CampaignType.FLASH_SALE;
        if (luxury) return MarketingIntent.CampaignType.PREMIUM_SHOWCASE;
        if (productCentric) return MarketingIntent.CampaignType.PRODUCT_LAUNCH;
        if (pricing) return MarketingIntent.CampaignType.LEAD_CAPTURE;
        if (saas) return MarketingIntent.CampaignType.SAAS_PROMOTION;
        return MarketingIntent.CampaignType.GENERIC;
    }
}