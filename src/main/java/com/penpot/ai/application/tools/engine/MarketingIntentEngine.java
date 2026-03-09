package com.penpot.ai.application.tools.engine;

import com.penpot.ai.core.domain.marketing.MarketingTone;
import com.penpot.ai.core.domain.spec.SectionSpec;
import com.penpot.ai.shared.util.JsStringUtils;

/**
 * Moteur d’analyse marketing chargé d’interpréter le contenu textuel
 * d’une {@link SectionSpec} afin d’en déduire une intention marketing
 * structurée sous forme d’un objet {@link MarketingIntent}.
 *
 * <p>Cette analyse permet de transformer un contenu textuel brut
 * (titre, paragraphe, ton marketing) en un ensemble de signaux
 * exploitables par le moteur de génération de layout et de contenu.</p>
 */
public class MarketingIntentEngine {

    /**
     * Analyse une spécification de section afin d'en extraire
     * une intention marketing structurée.
     *
     * @param spec spécification de section contenant le contenu textuel
     *             et le ton marketing associé
     * @return objet {@link MarketingIntent} décrivant l’intention marketing
     *         détectée à partir du contenu
     */
    public MarketingIntent analyze(SectionSpec spec) {
        String text = (JsStringUtils.safe(spec.getTitle()) + " "
                    + JsStringUtils.safe(spec.getParagraph())).toLowerCase();

        boolean urgency = JsStringUtils.containsAny(text, "aujourd", "offre", "promo", "maintenant", "limited")
                        || spec.getTone() == MarketingTone.URGENT;
        boolean socialProof = JsStringUtils.containsAny(text, "clients", "avis", "review", "noté");
        boolean luxury = JsStringUtils.containsAny(text, "premium", "luxury", "exclusif", "haut de gamme");
        boolean productCentric = JsStringUtils.containsAny(text, "collection", "produit", "modèle", "nouvelle");
        boolean minimalist = JsStringUtils.containsAny(text, "minimal", "épuré", "simple");
        boolean hasCTA = JsStringUtils.containsAny(text, "acheter", "buy", "shop", "commander");
        boolean conversion = urgency || (hasCTA && productCentric);

        int titleImpact = titleImpact(JsStringUtils.safe(spec.getTitle()).length());
        int persuasion = persuasion(conversion, urgency, socialProof);

        return new MarketingIntent(
            conversion, urgency, socialProof, luxury, productCentric, minimalist,
            titleImpact, persuasion,
            ctaStrength(urgency, persuasion, conversion, productCentric),
            hierarchy(conversion, titleImpact, productCentric, socialProof),
            campaignType(urgency, luxury, productCentric, conversion)
        );
    }

    /**
     * Calcule un score représentant l’impact potentiel du titre
     * en fonction de sa longueur.
     *
     * @param length longueur du titre en nombre de caractères
     * @return score d’impact du titre compris approximativement
     *         entre 0 et 100
     */
    private static int titleImpact(int length) {
        int score = 80;
        if (length > 60) score -= 20;
        if (length < 20) score -= 10;
        return score;
    }

    /**
     * Calcule un score de persuasion marketing.
     * 
     * @param conversion indique si le contenu est orienté conversion
     * @param urgency indique la présence d’un sentiment d’urgence
     * @param socialProof indique la présence de preuve sociale
     * @return score de persuasion limité à une valeur maximale de 100
     */
    private static int persuasion(boolean conversion, boolean urgency, boolean socialProof) {
        int score = 50;
        if (conversion) score += 20;
        if (urgency) score += 20;
        if (socialProof) score += 10;
        return Math.min(score, 100);
    }

    /**
     * Détermine la force de l’appel à l’action (CTA).
     *
     * @param urgency présence d’un sentiment d’urgence
     * @param persuasion score de persuasion calculé
     * @param conversion orientation vers la conversion
     * @param productCentric contenu centré sur un produit
     * @return niveau de force du CTA
     */
    private static MarketingIntent.CtaStrength ctaStrength(
        boolean urgency, int persuasion, boolean conversion, boolean productCentric
    ) {
        if (urgency && persuasion > 80) return MarketingIntent.CtaStrength.AGGRESSIVE;
        if (conversion) return MarketingIntent.CtaStrength.STRONG;
        if (productCentric) return MarketingIntent.CtaStrength.MEDIUM;
        return MarketingIntent.CtaStrength.SOFT;
    }

    /**
     * Détermine la hiérarchie visuelle à appliquer à la section.
     *
     * @param conversion indique si la section vise une conversion directe
     * @param titleImpact score d’impact du titre
     * @param productCentric contenu centré sur un produit
     * @param socialProof présence de preuve sociale
     * @return niveau de hiérarchie visuelle à appliquer
     */
    private static MarketingIntent.HierarchyLevel hierarchy(
        boolean conversion, int titleImpact, boolean productCentric, boolean socialProof
    ) {
        if (conversion && titleImpact > 70) return MarketingIntent.HierarchyLevel.STRONG_HERO;
        if (productCentric || socialProof) return MarketingIntent.HierarchyLevel.BALANCED;
        return MarketingIntent.HierarchyLevel.SOFT_BRANDING;
    }

    /**
     * Détermine le type de campagne marketing associé au contenu.
     *
     * @param urgency présence d’un sentiment d’urgence
     * @param luxury positionnement haut de gamme
     * @param productCentric contenu centré sur un produit
     * @param conversion orientation vers la conversion
     * @return type de campagne marketing détecté
     */
    private static MarketingIntent.CampaignType campaignType(
        boolean urgency, boolean luxury, boolean productCentric, boolean conversion
    ) {
        if (urgency) return MarketingIntent.CampaignType.FLASH_SALE;
        if (luxury) return MarketingIntent.CampaignType.PREMIUM_SHOWCASE;
        if (productCentric) return MarketingIntent.CampaignType.PRODUCT_LAUNCH;
        if (conversion) return MarketingIntent.CampaignType.LEAD_CAPTURE;
        return MarketingIntent.CampaignType.BRANDING;
    }
}