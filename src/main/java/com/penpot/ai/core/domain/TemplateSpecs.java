package com.penpot.ai.core.domain;

import java.util.*;

import com.penpot.ai.core.domain.spec.DimensionsSpec;

import lombok.*;

/**
 * Value Object immuable encapsulant les spécifications structurelles
 * et sémantiques d'un modèle (template) marketing.
 */
@Value
@Builder
public class TemplateSpecs {

    /** Identifiant unique et technique du modèle (par exemple, "flyer_a5_boulangerie"). */
    String templateId;

    /** Catégorie ou format structurel définissant la nature du document (ex. "flyer_a5", "social_media_post"). */
    String type;

    /** Synthèse textuelle détaillant l'usage prévu, la composition visuelle et l'intention promotionnelle du modèle. */
    String description;

    /** Ensemble de mots-clés associés au modèle, facilitant ainsi son indexation et sa recherche sémantique au sein du catalogue. */
    List<String> tags;

    /**
     * Spécifications géométriques de la zone de travail. 
     * Celles-ci sont directement extraites et standardisées depuis le nœud "metadata.format" du fichier source.
     */
    DimensionsSpec dimensions;

    /**
     * Séquence chronologique et ordonnée des primitives graphiques à instancier dans Penpot.
     * <p>
     * Chaque dictionnaire (Map) correspond à un nœud de la "layout_structure" et intègre le nom 
     * de l'élément, l'outil cible de l'API (ex. "penpotShapeTools.createRectangle"), ainsi que les 
     * paramètres d'exécution. Par conséquent, les valeurs de ces paramètres, telles que les 
     * dimensions exactes ou les codes couleurs hexadécimaux, sont déjà résolues et prêtes à l'emploi.
     * </p>
     */
    List<Map<String, Object>> elements;

    /**
     * Dictionnaire des variables dynamiques que le modèle de langage (LLM) est chargé de 
     * substituer par un contenu textuel pertinent.
     * <p>
     * Par exemple, la clé "TITRE_PRODUIT" sera remplacée par le véritable nom du produit ciblé. 
     * À noter que cette collection est strictement réservée au traitement sémantique textuel ; 
     * par conséquent, les attributs stylistiques tels que les couleurs ne constituent pas des 
     * espaces réservés (placeholders) et demeurent nativement intégrés dans les paramètres 
     * de la propriété {@code elements}.
     * </p>
     */
    Map<String, String> textPlaceholders;
}