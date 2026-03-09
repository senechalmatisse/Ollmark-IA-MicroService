package com.penpot.ai.core.domain.marketing;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 * Définit l'identité visuelle et l'orientation esthétique globale appliquées aux composants marketing.
 */
public enum MarketingStyle {

    /**
     * Approche contemporaine caractérisée par l'utilisation de dégradés de couleurs fluides et vibrants.
     */
    MODERN_GRADIENT,

    /**
     * Esthétique matérielle basée sur le principe de l'extrusion de surface ("Neumorphisme").
     */
    NEUMORPHIC,

    /**
     * Conception visuelle à fort contraste, structurellement optimisée pour la conversion.
     */
    BOLD_ECOMMERCE,

    /**
     * Style architectural reposant sur l'illusion optique du verre dépoli ("Glassmorphism").
     */
    GLASSMORPHISM,

    /**
     * Approche épurée et lumineuse, maximisant l'utilisation de l'espace blanc (respiration visuelle).
     */
    MINIMAL_LIGHT,

    /**
     * Thème sombre spécifiquement calibré pour les applications logicielles (SaaS) et les tableaux de bord.
     */
    DARK_SAAS,

    /**
     * Valeur de repli (fallback) technique utilisée lors du traitement des flux de données.
     * <p>Ainsi, si une chaîne de caractères non reconnue ou mal formatée est rencontrée lors de la 
     * désérialisation JSON, l'annotation {@code @JsonEnumDefaultValue} forcera le système à basculer 
     * de manière sécurisée sur cet état. Cela évite la levée d'exceptions bloquantes et permet une 
     * gestion gracieuse des erreurs au sein du pipeline de rendu.</p>
     */
    @JsonEnumDefaultValue
    UNKNOWN
}