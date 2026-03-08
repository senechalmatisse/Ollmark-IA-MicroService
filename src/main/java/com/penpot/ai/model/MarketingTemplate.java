package com.penpot.ai.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import java.util.*;

/**
 * Représente un template marketing utilisé pour générer automatiquement
 * des compositions graphiques dans l’environnement Penpot.
 *
 * <p>Un {@code MarketingTemplate} décrit la structure d’un visuel marketing
 * sous forme de métadonnées et d’une liste ordonnée d’éléments à créer dans
 * l’outil de design. Ces éléments sont ensuite interprétés par le moteur
 * d’exécution afin d’appeler les outils Penpot correspondants.</p>
 *
 * <p>Le template définit :</p>
 * <ul>
 *     <li>Un identifiant unique permettant de référencer le template.</li>
 *     <li>Un type de template (ex : poster, bannière, carte promotionnelle).</li>
 *     <li>Une description fonctionnelle.</li>
 *     <li>Des tags permettant la recherche ou la catégorisation.</li>
 *     <li>Des métadonnées associées (format, dimensions, etc.).</li>
 *     <li>Une structure de layout décrivant les éléments graphiques à créer.</li>
 *     <li>Des placeholders textuels destinés à être remplacés par des données
 *     contextuelles (produit, prix, marque, etc.).</li>
 * </ul>
 *
 * <h2>Exemple de format JSON</h2>
 * <pre>{@code
 * {
 *   "id": "poster_a4_mer",
 *   "type": "poster_a4",
 *   "description": "Affiche A4 pour produits de la mer",
 *   "tags": ["poster", "poisson"],
 *   "metadata": {
 *     "format": { "width": 1000.0, "height": 1414.0 }
 *   },
 *   "layout_structure": [
 *     {
 *       "element": "background",
 *       "tool": "penpotShapeTools.createRectangle",
 *       "params": { ... }
 *     },
 *     {
 *       "element": "main_title",
 *       "tool": "penpotContentTools.createText",
 *       "params": { ... }
 *     }
 *   ],
 *   "text_placeholders": {
 *     "TITRE_PRODUIT": "Nom du produit en majuscules",
 *     "PRIX": "Prix avec unité",
 *     "NOM_MARQUE": "Nom de la boutique"
 *   }
 * }
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketingTemplate {

    /**
     * Identifiant unique du template.
     * <p>Exemple : {@code poster_a4_mer}</p>
     */
    private String id;

    /**
     * Type fonctionnel du template.
     * <p>Exemples :</p>
     * <ul>
     *     <li>{@code poster_a4}</li>
     *     <li>{@code social_banner}</li>
     *     <li>{@code product_card}</li>
     * </ul>
     */
    private String type;

    /**
     * Description textuelle du template.
     * <p>Utilisée pour documenter l’usage du template et faciliter
     * sa sélection par un utilisateur ou un système d’IA.</p>
     */
    private String description;

    /**
     * Liste de mots-clés associés au template.
     * <p>Exemples :</p>
     * <ul>
     *     <li>{@code poster}</li>
     *     <li>{@code poisson}</li>
     *     <li>{@code promotion}</li>
     * </ul>
     */
    private List<String> tags;

    /**
     * Métadonnées associées au template.
     * <p>Exemple :</p>
     * <pre>{@code
     * {
     *   "format": {
     *     "width": 1000.0,
     *     "height": 1414.0
     *   }
     * }
     * }</pre>
     */
    private Map<String, Object> metadata;

    /**
     * Structure du layout décrivant les éléments graphiques à créer
     * dans le document Penpot.
     * <p>Exemple :</p>
     * <pre>{@code
     * {
     *   "element": "background",
     *   "tool": "penpotShapeTools.createRectangle",
     *   "params": { ... }
     * }
     * }</pre>
     */
    @JsonProperty("layout_structure")
    private List<Map<String, Object>> layoutStructure;

    /**
     * Placeholders textuels présents dans le template.
     * <p>Exemples de placeholders :</p>
     * <ul>
     *     <li>{@code TITRE_PRODUIT}</li>
     *     <li>{@code PRIX}</li>
     *     <li>{@code NOM_MARQUE}</li>
     * </ul>
     */
    @JsonProperty("text_placeholders")
    private Map<String, String> textPlaceholders;
}