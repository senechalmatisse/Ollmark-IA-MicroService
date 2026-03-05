package com.penpot.ai.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import java.util.*;


/**
 * Représente un template marketing pour Penpot.
 *
 * Format JSON attendu :
 * {
 *   "id": "poster_a4_mer",
 *   "type": "poster_a4",
 *   "description": "...",
 *   "tags": ["poster", "poisson", ...],
 *   "metadata": { "format": { "width": 1000.0, "height": 1414.0 } },
 *   "layout_structure": [
 *     { "element": "background", "tool": "penpotShapeTools.createRectangle", "params": {...} },
 *     { "element": "main_title", "tool": "penpotContentTools.createText",    "params": {...} },
 *     ...
 *   ],
 *   "text_placeholders": {
 *     "TITRE_PRODUIT": "Nom du produit en majuscules",
 *     "PRIX": "Prix avec unité",
 *     "NOM_MARQUE": "Nom de la boutique"
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketingTemplate {

    private String id;
    private String type;
    private String description;
    private List<String> tags;
    private Map<String, Object> metadata;

    /**
     * Structure des éléments à créer dans Penpot, dans l'ordre d'exécution.
     * Chaque élément contient : element (nom), tool (tool Java à appeler), params (paramètres).
     */
    @JsonProperty("layout_structure")
    private List<Map<String, Object>> layoutStructure;

    /**
     * Placeholders texte que le LLM doit remplacer par des valeurs contextuelles.
     * Ex: { "TITRE_PRODUIT": "Nom du produit en majuscules, ex: SAUMON FUMÉ" }
     * Les placeholders couleur n'existent plus — les hex sont directement dans params.
     */
    @JsonProperty("text_placeholders")
    private Map<String, String> textPlaceholders;
}

