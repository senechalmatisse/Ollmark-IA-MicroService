package com.penpot.ai.core.domain.marketing;

import com.fasterxml.jackson.annotation.*;

import java.util.Locale;

/**
 * Enumération représentant les différents composants marketing
 * pouvant être utilisés dans une section ou un template de design.
 *
 * <p>
 * Ces composants correspondent à des éléments visuels ou fonctionnels
 * fréquemment utilisés dans les interfaces marketing telles que :
 * </p>
 * <ul>
 *     <li>les badges promotionnels</li>
 *     <li>les barres d'annonce</li>
 *     <li>les listes de fonctionnalités</li>
 *     <li>les blocs de statistiques</li>
 *     <li>les témoignages clients</li>
 *     <li>les éléments de preuve sociale</li>
 *     <li>les appels à l'action (CTA)</li>
 * </ul>
 *
 * <p>
 * Si la valeur fournie ne correspond à aucun composant connu,
 * la valeur {@link #UNKNOWN} est retournée.
 * </p>
 */
public enum MarketingComponent {

    /**
     * Badge promotionnel ou indicateur visuel court (ex : "New", "-20%", "Limited").
     */
    BADGE,

    /**
     * Barre d'annonce affichée généralement en haut d'une page
     * pour promouvoir une information ou une promotion.
     */
    ANNOUNCEMENT_BAR,

    /**
     * Liste verticale de fonctionnalités ou avantages d'un produit ou service.
     */
    FEATURE_LIST,

    /**
     * Grille visuelle de fonctionnalités présentées sous forme de cartes ou blocs alignés.
     */
    FEATURE_GRID,

    /**
     * Bloc de statistiques mettant en avant des métriques clés
     * (ex : nombre de clients, taux de satisfaction).
     */
    STATS_BLOCK,

    /** Carte contenant un témoignage client ou une citation. */
    TESTIMONIAL_CARD,

    /**
     * Ensemble de logos de partenaires, clients ou marques
     * permettant d'apporter de la crédibilité (trust logos).
     */
    TRUST_LOGOS,

    /**
     * Ruban promotionnel affichant une réduction ou une offre spéciale.
     */
    DISCOUNT_RIBBON,

    /**
     * Image avec superposition d'un texte ou d'un élément graphique.
     */
    IMAGE_OVERLAY,

    /** Emplacement réservé pour une vidéo ou un lecteur vidéo. */
    VIDEO_PLACEHOLDER,

    /**
     * Éléments décoratifs flottants utilisés pour enrichir visuellement une section.
     */
    FLOATING_DECORATIONS,

    /** Arrière-plan utilisant un dégradé de couleurs. */
    BACKGROUND_GRADIENT,

    /**
     * Élément graphique servant de séparation entre deux sections
     * (souvent une forme SVG).
     */
    SHAPE_DIVIDER,

    /**
     * Élément mettant en avant une preuve sociale (avis, nombre d'utilisateurs, recommandations).
     */
    SOCIAL_PROOF,

    /**
     * Bloc d'appel à l'action contenant deux boutons ou actions principales.
     */
    CTA_DOUBLE,

    /** Bloc d'appel à l'action centré avec un bouton principal. */
    CTA_CENTERED,

    /**
     * Compteur ou minuteur affichant le temps restant
     * pour une offre ou un événement.
     */
    COUNTDOWN_TIMER,

    /** Aperçu d'une carte de tarification ou d'un plan tarifaire. */
    PRICING_CARD_PREVIEW,

    /**
     * Emplacement générique réservé pour un média
     * (image, vidéo ou autre contenu visuel).
     */
    MEDIA_PLACEHOLDER,

    /**
     * Valeur par défaut utilisée lorsque le composant
     * ne correspond à aucun type connu.
     */
    @JsonEnumDefaultValue
    UNKNOWN;

    /**
     * Convertit une chaîne de caractères en {@link MarketingComponent}.
     *
     * <p>
     * La conversion suit les étapes suivantes :
     * </p>
     * <ol>
     *     <li>normalisation de la chaîne (trim + majuscules)</li>
     *     <li>tentative de correspondance directe avec l'énumération</li>
     *     <li>application d'un mapping de synonymes fréquents</li>
     *     <li>retour de {@link #UNKNOWN} si aucune correspondance n'est trouvée</li>
     * </ol>
     *
     * @param value chaîne représentant le nom d'un composant marketing
     * @return le composant correspondant ou {@link #UNKNOWN} si non reconnu
     */
    @JsonCreator
    public static MarketingComponent from(String value) {
        if (value == null || value.isBlank()) return UNKNOWN;
        String normalized = value.trim().toUpperCase(Locale.ROOT);

        try {
            return MarketingComponent.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
        }

        switch (normalized) {
            case "PROMO_BANNER":
            case "PROMOTION_BANNER": return ANNOUNCEMENT_BAR;
            case "HERO_BANNER": return CTA_CENTERED;
            case "SOCIAL_LOGOS":
            case "LOGO_STRIP": return TRUST_LOGOS;
            case "PRICING_PREVIEW": return PRICING_CARD_PREVIEW;
            case "FEATURES": return FEATURE_LIST;
            case "TESTIMONIAL": return TESTIMONIAL_CARD;
            case "STATS": return STATS_BLOCK;
            default: return UNKNOWN;
        }
    }
}