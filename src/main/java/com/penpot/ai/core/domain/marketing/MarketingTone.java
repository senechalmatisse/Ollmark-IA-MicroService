package com.penpot.ai.core.domain.marketing;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 * Définit la ligne éditoriale et l'empreinte émotionnelle appliquées aux contenus marketing.
 */
public enum MarketingTone {

    /**
     * Caractérise une communication axée sur l'immédiateté et le principe de rareté.
     */
    URGENT,

    /**
     * Adopte un positionnement formel, institutionnel et éminemment rassurant.
     */
    CORPORATE,

    /**
     * Incarne une proposition de valeur supérieure, alliant qualité et exigence.
     */
    PREMIUM,

    /**
     * Privilégie une approche chaleureuse, empathique et résolument conversationnelle.
     */
    FRIENDLY,

    /**
     * Véhicule une image d'exclusivité, de haute sophistication et de prestige.
     */
    LUXURY,

    /**
     * Traduit le dynamisme, l'agilité et l'esprit de disruption technologique.
     */
    STARTUP,

    /**
     * S'appuie sur des codes visuels et sémantiques évoquant la douceur, l'élégance et la subtilité.
     */
    FEMININE,

    /**
     * Assure la résilience du système lors de la désérialisation des flux de données.
     * <p>L'annotation {@code @JsonEnumDefaultValue} garantit que toute valeur entrante 
     * inconnue, obsolète ou mal orthographiée sera automatiquement redirigée vers cet état. 
     * Ce mécanisme de repli prévient les erreurs critiques d'exécution et permet au pipeline 
     * de continuer son traitement de manière sécurisée.</p>
     */
    @JsonEnumDefaultValue
    UNKNOWN
}