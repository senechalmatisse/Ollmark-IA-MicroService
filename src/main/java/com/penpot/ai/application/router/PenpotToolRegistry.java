package com.penpot.ai.application.router;

import com.penpot.ai.application.tools.*;
import com.penpot.ai.core.domain.ToolCategory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry des tools Penpot organisés par catégorie fonctionnelle.
 *
 * <h2>Pattern Registry</h2>
 * Ce composant maintient une {@link Map} statique catégorie → liste de tools.
 * C'est le point central de configuration du routing : modifier les associations
 * ne nécessite de toucher qu'à cette classe.
 *
 * Pour ajouter un nouveau tool :
 * <ol>
 *   <li>Créer la classe tool ({@code @Component}).</li>
 *   <li>L'injecter dans le constructeur via {@code @RequiredArgsConstructor}.</li>
 *   <li>L'associer à une ou plusieurs {@link ToolCategory} dans {@link #buildRegistry()}.</li>
 * </ol>
 * Aucune autre classe ne doit être modifiée.
 *
 * <h2>Déduplication automatique</h2>
 * Un même bean peut apparaître dans plusieurs catégories (ex: {@code PenpotShapeTools}
 * couvre {@code SHAPE_CREATION} et {@code SHAPE_MODIFICATION}). La méthode
 * {@link ToolCategoryResolver#resolveTools(Set)} déduplique via {@code distinct()} sur les
 * références d'objet Java (identité, pas égalité structurelle).
 */
@Slf4j
@Component
public class PenpotToolRegistry implements ToolCategoryResolver {

    private final PenpotShapeTools shapeTools;
    private final PenpotTransformTools transformTools;
    private final PenpotAssetTools assetTools;
    private final PenpotLayoutTools layoutTools;
    private final PenpotContentTools contentTools;
    private final PenpotDeleteTools deleteTools;
    private final PenpotInspectorTools inspectorTools;
    private final TemplateSearchTools  templateSearchTools;

    /**
     * Le dictionnaire immuable consignant le mapping entre catégories et outils,
     * scellé au démarrage de l'application.
     */
    private Map<ToolCategory, List<Object>> registry;

    /**
     * Initialise le registre en injectant l'ensemble des outils disponibles dans le contexte applicatif.
     *
     * @param shapeTools          Outils dédiés à la génération de formes géométriques.
     * @param transformTools      Outils gérant les altérations spatiales.
     * @param assetTools          Outils administrant les ressources graphiques.
     * @param layoutTools         Outils orchestrant le positionnement et l'alignement des éléments.
     * @param contentTools        Outils responsables de la création de contenus textuels ou médias.
     * @param deleteTools         Outils dédiés à la suppression d'entités sur le canevas.
     * @param inspectorTools      Outils d'analyse et d'extraction des propriétés des objets.
     * @param templateSearchTools Outils interrogeant la base de connaissances (RAG) pour la recherche de gabarits.
     */
    public PenpotToolRegistry(
        PenpotShapeTools shapeTools,
        PenpotTransformTools transformTools,
        PenpotAssetTools assetTools,
        PenpotLayoutTools layoutTools,
        PenpotContentTools contentTools,
        PenpotDeleteTools deleteTools,
        PenpotInspectorTools inspectorTools,
        TemplateSearchTools templateSearchTools
    ) {
        this.shapeTools = shapeTools;
        this.transformTools = transformTools;
        this.assetTools = assetTools;
        this.layoutTools = layoutTools;
        this.contentTools = contentTools;
        this.deleteTools = deleteTools;
        this.inspectorTools = inspectorTools;
        this.templateSearchTools = templateSearchTools;
    }

    /**
     * Construit et verrouille la cartographie des outils lors de la phase d'initialisation du composant.
     *
     * <p>Utilisation de {@link Map#copyOf} pour rendre le registry immuable
     * après construction — aucune modification possible à l'exécution.</p>
     */
    @PostConstruct
    void buildRegistry() {
        Map<ToolCategory, List<Object>> mutable = new EnumMap<>(ToolCategory.class);

        // --- Création de formes ---
        mutable.put(ToolCategory.SHAPE_CREATION, List.of(shapeTools, contentTools, templateSearchTools));

        // --- Modification de formes (transform + shape partagés) ---
        mutable.put(ToolCategory.SHAPE_MODIFICATION, List.of(shapeTools, transformTools, inspectorTools, layoutTools));

        // --- Couleurs et styles (assets gère les palettes) ---
        mutable.put(ToolCategory.COLOR_AND_STYLE, List.of(assetTools, inspectorTools, templateSearchTools));

        // --- Mise en page et alignement ---
        mutable.put(ToolCategory.LAYOUT_AND_ALIGNMENT, List.of(layoutTools, inspectorTools));

        // --- Contenu marketing textuel et médias ---
        mutable.put(ToolCategory.CONTENT_AND_TEXT, List.of(contentTools, inspectorTools, templateSearchTools));

        // --- Inspection (toujours inclus comme contexte) ---
        mutable.put(ToolCategory.INSPECTION, List.of(inspectorTools));

        // --- Suppression ---
        mutable.put(ToolCategory.DELETION, List.of(deleteTools, inspectorTools, assetTools, layoutTools));

        // --- Recherche de templates RAG ---
        mutable.put(ToolCategory.TEMPLATE_SEARCH, List.of(templateSearchTools));

        this.registry = Map.copyOf(mutable);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Algorithme :
     * <ol>
     *   <li>Pour chaque catégorie, récupère la liste de tools du registry.</li>
     *   <li>Aplatit les listes en un seul stream.</li>
     *   <li>Déduplique sur l'identité d'objet (référence Java).</li>
     *   <li>Retourne un tableau {@code Object[]} attendu par {@code ChatClient.tools(...)}.</li>
     * </ol>
     * 
     * @param categories L'ensemble des catégories d'outils réclamées par le routeur d'intention.
     * @return           Un tableau d'objets contenant les instances uniques d'outils à injecter dans le contexte du LLM.
     */
    @Override
    public Object[] resolveTools(Set<ToolCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            log.warn("[Registry] Empty categories set, returning empty tools array");
            return new Object[0];
        }

        Object[] tools = categories.stream()
            .flatMap(category -> {
                List<Object> toolsForCategory = registry.getOrDefault(category, List.of());
                if (toolsForCategory.isEmpty()) {
                    log.warn("[Registry] No tools registered for category: {}", category);
                }
                return toolsForCategory.stream();
            })
            .collect(Collectors.toCollection(() ->
                Collections.newSetFromMap(new IdentityHashMap<>())))
            .toArray();

        log.debug("[Registry] Resolved {} tool instance(s) for categories: {}",
            tools.length, categories);
        return tools;
    }
}