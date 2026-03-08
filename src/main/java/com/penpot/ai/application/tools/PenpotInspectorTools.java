package com.penpot.ai.application.tools;

import com.penpot.ai.application.tools.support.*;
import com.penpot.ai.shared.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Ensemble de tools d'inspection (lecture seule) pour Penpot.
 *
 * <h2>Responsabilité</h2>
 * <p>
 * Fournir un accès à l'état courant de la page Penpot :
 * propriétés, hiérarchie, géométrie et couleurs.
 * Cette classe n'effectue <b>AUCUNE</b> modification du document.
 * Toutes les opérations sont strictement read-only.
 * </p>
 *
 * <h2>Utilisation</h2>
 * <p>
 * Ces tools sont conçus pour être appelés par le LLM lorsqu'il a besoin
 * d'inspecter le contenu d'une page Penpot avant d'effectuer des actions.
 * Chaque méthode charge un script JavaScript dédié via {@link JsScriptLoader},
 * l'exécute dans le contexte Penpot via {@link PenpotToolExecutor},
 * et retourne un résultat sérialisé en JSON.
 * </p>
 *
 * <h2>Valeurs par défaut</h2>
 * <ul>
 *   <li>{@code shapeId} : si {@code null} ou vide, la valeur {@value #DEFAULT_SHAPE_ID}
 *       est utilisée pour cibler la sélection courante.</li>
 *   <li>{@code verbosity} : si {@code null} ou vide, la valeur {@value #DEFAULT_VERBOSITY}
 *       est utilisée.</li>
 * </ul>
 *
 * @see PenpotToolExecutor
 * @see JsScriptLoader
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PenpotInspectorTools {

    /** Réponse retournée lorsque l'exécuteur ne produit aucune donnée. */
    private static final String EMPTY_RESPONSE = "Empty tool response";

    /**
     * Valeur par défaut de l'identifiant de shape utilisée lorsqu'aucun
     * {@code shapeId} explicite n'est fourni. La valeur {@code "selection"}
     * correspond à la sélection active dans Penpot.
     */
    private static final String DEFAULT_SHAPE_ID = "selection";

    /**
     * Niveau de verbosité par défaut appliqué lorsqu'aucun paramètre
     * {@code verbosity} n'est fourni ({@code "compact"}).
     */
    private static final String DEFAULT_VERBOSITY = "compact";

    /**
     * Clé utilisée dans les maps de substitution des scripts JavaScript
     * pour injecter l'identifiant de la shape cible.
     */
    private static final String SHAPE_ID_KEY = "shapeId";

    /**
     * Exécuteur responsable de l'appel aux scripts JavaScript dans le
     * contexte Penpot et de la gestion des erreurs d'exécution.
     */
    private final PenpotToolExecutor toolExecutor;

    /**
     * Retourne un snapshot curé de la page courante.
     *
     * <p>
     * Charge et exécute le script {@code tools/inspector/get-page-context.js} en y
     * injectant le niveau de verbosité résolu. Le résultat est un objet JSON
     * contenant la liste des shapes présentes sur la page avec leurs
     * métadonnées principales (identifiant, type, nom, dimensions, etc.).
     * </p>
     *
     * @param verbosity niveau de détail de la réponse JSON.
     *                  Valeurs acceptées : {@code "compact"} (par défaut) ou {@code "full"}.
     *                  Toute valeur {@code null} ou vide sera remplacée par {@value #DEFAULT_VERBOSITY}.
     * @return chaîne JSON représentant l'état actuel de la page ;
     *         {@value #EMPTY_RESPONSE} si aucune donnée n'est retournée par l'exécuteur.
     */
    @Tool(description = """
        Read-only. Returns a curated list of shapes from the current page.
        Use this tool to get an overview of all elements present on the active Penpot page.
    """)
    public String getPageContext(
        @ToolParam(description = """
            Controls the amount of detail returned for each shape.
            - 'compact' (default): returns only essential fields (id, name, type, dimensions).
            - 'full': returns all available properties including styles, constraints and metadata.
        """)
        String verbosity
    ) {
        log.info("Tool called: getPageContext({})", verbosity);
        String v = resolveVerbosity(verbosity);
        String js = JsScriptLoader.loadWith(
            "tools/inspector/get-page-context.js",
            Map.of("verbosity", JsonUtils.escapeJson(v))
        );

        return toolExecutor.execute(
            js,
            "get page context",
            r -> r.getData().map(Object::toString).orElse(EMPTY_RESPONSE)
        );
    }

    /**
     * Retourne les propriétés principales d'une shape spécifique.
     *
     * <p>
     * Charge et exécute le script {@code tools/inspector/get-properties-from-shape.js}
     * en y injectant l'identifiant de la shape résolu et le niveau de
     * verbosité. Le résultat est un objet JSON décrivant les propriétés
     * visuelles et structurelles de la shape (type, dimensions, fills,
     * strokes, contraintes de layout, etc.).
     * </p>
     *
     * @param shapeId  identifiant UUID de la shape à inspecter,
     *                 ou {@code "selection"} pour cibler la sélection active.
     *                 Toute valeur {@code null} ou vide sera remplacée par {@value #DEFAULT_SHAPE_ID}.
     * @param verbosity niveau de détail de la réponse JSON.
     *                  Valeurs acceptées : {@code "compact"} (par défaut) ou {@code "full"}.
     *                  Toute valeur {@code null} ou vide sera remplacée par {@value #DEFAULT_VERBOSITY}.
     * @return résumé JSON des propriétés de la shape ;
     *         {@value #EMPTY_RESPONSE} si aucune donnée n'est retournée.
     */
    @Tool(description = """
        Read-only. Returns the properties of a specific shape identified by its UUID or the current selection.
        Use this tool to inspect a shape's visual and structural attributes (type, size, fills, strokes, constraints).
    """)
    public String getPropertiesFromShape(
        @ToolParam(description = """
            The unique identifier (UUID) of the shape to inspect,
            or 'selection' to target the currently selected shape.
        """)
        String shapeId,
        @ToolParam(description = """
            Controls the amount of detail returned.
            - 'compact' (default): essential fields only.
            - 'full': all properties including advanced styles and metadata.
        """)
        String verbosity
    ) {
        log.info("Tool called: getPropertiesFromShape({}, {})", shapeId, verbosity);
        String id = resolveShapeId(shapeId);
        String v = resolveVerbosity(verbosity);
        String js = JsScriptLoader.loadWith(
            "tools/inspector/get-properties-from-shape.js",
            Map.of(
                SHAPE_ID_KEY, JsonUtils.escapeJson(id),
                "verbosity", JsonUtils.escapeJson(v)
            )
        );

        return toolExecutor.execute(
            js,
            "get properties from shape",
            r -> r.getData().map(Object::toString).orElse(EMPTY_RESPONSE)
        );
    }

    /**
     * Retourne le centre géométrique (cx, cy) d'une shape.
     *
     * <p>
     * Charge et exécute le script {@code tools/inspector/get-center-from-shape.js}.
     * Le centre est calculé à partir de la bounding box de la shape.
     * </p>
     *
     * @param shapeId identifiant UUID de la shape,
     *                ou {@code "selection"} pour cibler la sélection active.
     *                Toute valeur {@code null} ou vide sera remplacée par {@value #DEFAULT_SHAPE_ID}.
     * @return objet JSON contenant les champs {@code id}, {@code cx} et {@code cy} ;
     *         {@value #EMPTY_RESPONSE} si aucune donnée n'est retournée.
     */
    @Tool(description = """
        Read-only. Returns the geometric center coordinates (cx, cy) of a shape.
        Use this tool when you need to align, distribute, or position shapes relative to one another.
    """)
    public String getCenterFromShape(
        @ToolParam(description = """
            The unique identifier (UUID) of the shape, or 'selection' to target the current selection.
        """)
        String shapeId
    ) {
        log.info("Tool called: getCenterFromShape({})", shapeId);
        String id = resolveShapeId(shapeId);
        String js = JsScriptLoader.loadWith(
                "tools/inspector/get-center-from-shape.js",
                Map.of(SHAPE_ID_KEY, JsonUtils.escapeJson(id))
        );

        return toolExecutor.execute(
            js,
            "get center from shape",
            r -> r.getData().map(Object::toString).orElse(EMPTY_RESPONSE)
        );
    }

    /**
     * Retourne le parent le plus haut d'une shape dans la hiérarchie des calques.
     *
     * <p>
     * Charge et exécute le script {@code tools/inspector/get-component-root.js}.
     * Remonte la hiérarchie jusqu'au nœud racine (frame ou page root)
     * et retourne sa description ainsi que le chemin parcouru.
     * </p>
     *
     * @param shapeId identifiant UUID de la shape,
     *                ou {@code "selection"} pour cibler la sélection active.
     *                Toute valeur {@code null} ou vide sera remplacée par {@value #DEFAULT_SHAPE_ID}.
     * @return objet JSON décrivant le nœud racine et la hiérarchie remontée ;
     *         {@value #EMPTY_RESPONSE} si aucune donnée n'est retournée.
     */
    @Tool(description = """
        Read-only. Returns the top-most ancestor of a shape in the layer hierarchy.
        Use this tool to identify which top-level frame or component owns a given shape.
    """)
    public String getComponentRoot(
        @ToolParam(description = """
            The unique identifier (UUID) of the shape, or 'selection' to target the current selection.
        """)
        String shapeId
    ) {
        log.info("Tool called: getComponentRoot({})", shapeId);
        String id = resolveShapeId(shapeId);
        String js = JsScriptLoader.loadWith(
                "tools/inspector/get-component-root.js",
                Map.of(SHAPE_ID_KEY, JsonUtils.escapeJson(id))
        );

        return toolExecutor.execute(
            js,
            "get component root",
            r -> r.getData().map(Object::toString).orElse(EMPTY_RESPONSE)
        );
    }

    /**
     * Retourne l'index d'une shape dans son parent direct (ordre du panneau Layers).
     *
     * <p>
     * Charge et exécute le script {@code tools/inspector/get-shape-parent-index.js}.
     * L'index est calculé selon l'ordre d'empilement visible dans le
     * panneau Layers de Penpot (0 = premier enfant en bas de la pile).
     * </p>
     *
     * @param shapeId identifiant UUID de la shape,
     *                ou {@code "selection"} pour cibler la sélection active.
     *                Toute valeur {@code null} ou vide sera remplacée par {@value #DEFAULT_SHAPE_ID}.
     * @return objet JSON contenant {@code parentId} et {@code index} ;
     *         {@value #EMPTY_RESPONSE} si aucune donnée n'est retournée.
     */
    @Tool(description = """
        Read-only. Returns the z-order index of a shape within its parent container (Layers panel order).
        Use this tool to determine a shape's stacking position before inserting, moving, or reordering elements.
        Index 0 corresponds to the bottom-most child in the layer stack.
    """)
    public String getShapeParentIndex(
        @ToolParam(description = """
            The unique identifier (UUID) of the shape, or 'selection' to target the current selection.
        """)
        String shapeId
    ) {
        log.info("Tool called: getShapeParentIndex({})", shapeId);
        String id = resolveShapeId(shapeId);
        String js = JsScriptLoader.loadWith(
                "tools/inspector/get-shape-parent-index.js",
                Map.of(SHAPE_ID_KEY, JsonUtils.escapeJson(id))
        );

        return toolExecutor.execute(
            js,
            "get shape parent index",
            r -> r.getData().map(Object::toString).orElse(EMPTY_RESPONSE)
        );
    }

    /**
     * Retourne le parent direct d'une shape dans la hiérarchie des calques.
     *
     * <p>
     * Charge et exécute le script {@code tools/inspector/get-parent-from-shape.js}.
     * Le parent peut être un frame, un groupe, un composant ou la racine de la page.
     * </p>
     *
     * @param shapeId identifiant UUID de la shape,
     *                ou {@code "selection"} pour cibler la sélection active.
     *                Toute valeur {@code null} ou vide sera remplacée par {@value #DEFAULT_SHAPE_ID}.
     * @return objet JSON décrivant le parent direct (id, type, nom) ;
     *         {@value #EMPTY_RESPONSE} si aucune donnée n'est retournée.
     */
    @Tool(description = """
        Read-only. Returns the direct parent container of a shape (frame, group, component, or page root).
        Use this tool to navigate upward in the layer hierarchy by one level.
    """)
    public String getParentFromShape(
        @ToolParam(description = """
            The unique identifier (UUID) of the shape, or 'selection' to target the current selection.
        """)
        String shapeId
    ) {
        log.info("Tool called: getParentFromShape({})", shapeId);
        String id = resolveShapeId(shapeId);
        String js = JsScriptLoader.loadWith(
                "tools/inspector/get-parent-from-shape.js",
                Map.of(SHAPE_ID_KEY, JsonUtils.escapeJson(id))
        );

        return toolExecutor.execute(
            js,
            "get parent from shape",
            r -> r.getData().map(Object::toString).orElse(EMPTY_RESPONSE)
        );
    }

    /**
     * Retourne les enfants directs d'une shape conteneur.
     *
     * <p>
     * Charge et exécute le script {@code tools/inspector/get-children-from-shape.js}.
     * Seuls les enfants de premier niveau sont retournés (pas de récursion).
     * Si la shape n'est pas un conteneur (frame, groupe, composant),
     * la liste retournée sera vide.
     * </p>
     *
     * @param shapeId identifiant UUID de la shape conteneur,
     *                ou {@code "selection"} pour cibler la sélection active.
     *                Toute valeur {@code null} ou vide sera remplacée par {@value #DEFAULT_SHAPE_ID}.
     * @return tableau JSON des enfants directs (id, type, nom) ;
     *         {@value #EMPTY_RESPONSE} si aucune donnée n'est retournée.
     */
    @Tool(description = """
        Read-only. Returns the direct (first-level) children of a container shape (frame, group, or component).
        Returns an empty list if the target shape has no children or is not a container.
    """)
    public String getChildrenFromShape(
        @ToolParam(description = """
            The unique identifier (UUID) of the container shape, or 'selection' to target the current selection.
        """)
        String shapeId
    ) {
        log.info("Tool called: getChildrenFromShape({})", shapeId);
        String id = resolveShapeId(shapeId);
        String js = JsScriptLoader.loadWith(
                "tools/inspector/get-children-from-shape.js",
                Map.of(SHAPE_ID_KEY, JsonUtils.escapeJson(id))
        );

        return toolExecutor.execute(
            js,
            "get children from shape",
            r -> r.getData().map(Object::toString).orElse(EMPTY_RESPONSE)
        );
    }

    /**
     * Retourne les couleurs (fills et strokes) utilisées par une shape et ses descendants.
     *
     * <p>
     * Charge et exécute le script {@code tools/inspector/get-shapes-colors.js}.
     * Pour chaque couleur trouvée, le résultat inclut la valeur hexadécimale,
     * l'opacité, le type (fill ou stroke) et l'identifiant de la shape source.
     * </p>
     *
     * @param shapeId identifiant UUID de la shape racine à inspecter,
     *                ou {@code "selection"} pour cibler la sélection active.
     *                Toute valeur {@code null} ou vide sera remplacée par {@value #DEFAULT_SHAPE_ID}.
     * @return tableau JSON des couleurs utilisées ;
     *         {@value #EMPTY_RESPONSE} si aucune donnée n'est retournée.
     */
    @Tool(description = """
        Read-only. Returns all fill and stroke colors used by a shape and its descendants.
        Use this tool to audit colors before applying a theme or checking brand consistency.
    """)
    public String getShapesColors(
        @ToolParam(description = """
            The unique identifier (UUID) of the root shape to inspect, or 'selection' to target
            the current selection.
        """)
        String shapeId
    ) {
        log.info("Tool called: getShapesColors({})", shapeId);
        String id = resolveShapeId(shapeId);
        String js = JsScriptLoader.loadWith(
                "tools/inspector/get-shapes-colors.js",
                Map.of(SHAPE_ID_KEY, JsonUtils.escapeJson(id))
        );

        return toolExecutor.execute(
            js,
            "get shapes colors",
            r -> r.getData().map(Object::toString).orElse(EMPTY_RESPONSE)
        );
    }

    /**
     * Résout l'identifiant de la forme cible.
     *
     * <p>
     * Si {@code shapeId} est {@code null} ou ne contient que des espaces,
     * retourne la valeur par défaut {@value #DEFAULT_SHAPE_ID} ({@code "selection"}).
     * Sinon retourne la valeur fournie après suppression des espaces en début
     * et en fin de chaîne.
     * </p>
     *
     * @param shapeId identifiant brut fourni par l'appelant, peut être {@code null}.
     * @return identifiant résolu, jamais {@code null} ni vide.
     */
    private String resolveShapeId(String shapeId) {
        return (shapeId == null || shapeId.isBlank()) ? DEFAULT_SHAPE_ID : shapeId.trim();
    }

    /**
     * Résout le niveau de verbosité.
     *
     * <p>
     * Si {@code verbosity} est {@code null} ou ne contient que des espaces,
     * retourne la valeur par défaut {@value #DEFAULT_VERBOSITY} ({@code "compact"}).
     * Sinon retourne la valeur fournie en minuscules et sans espaces superflus.
     * </p>
     *
     * @param verbosity niveau de verbosité brut fourni par l'appelant, peut être {@code null}.
     * @return niveau de verbosité résolu en minuscules, jamais {@code null} ni vide.
     */
    private String resolveVerbosity(String verbosity) {
        return (verbosity == null || verbosity.isBlank()) ? DEFAULT_VERBOSITY : verbosity.trim().toLowerCase();
    }
}