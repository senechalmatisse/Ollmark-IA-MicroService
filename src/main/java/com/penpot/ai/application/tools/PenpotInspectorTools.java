package com.penpot.ai.application.tools;

import com.penpot.ai.application.tools.support.JsScriptLoader;
import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import com.penpot.ai.shared.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Ensemble de tools d'inspection (lecture seule) pour Penpot.
 *
 * <h2>Responsabilité</h2>
 * Fournir un accès fiable à l'état courant de la page Penpot :
 * propriétés, hiérarchie, géométrie et couleurs.
 *
 * <p>
 * Cette classe n'effectue <b>AUCUNE</b> modification du document.
 * Toutes les opérations sont strictement read-only.
 * </p>
 *
 * @see PenpotToolExecutor
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PenpotInspectorTools {

    private final PenpotToolExecutor toolExecutor;

    // ========================================================================
    // Page context
    // ========================================================================

    /**
     * Retourne un snapshot curé de la page courante.
     *
     * @param verbosity niveau de détail ("compact" ou "full")
     * @return JSON représentant l’état actuel de la page
     */
    @Tool(description = "Read-only. Returns a curated list of shapes from the current page.")
    public String getPageContext(
            @ToolParam(description = "Verbosity: compact|full.")
            String verbosity
    ) {
        log.info("Tool called: getPageContext({})", verbosity);

        String v = (verbosity == null || verbosity.isBlank())
                ? "compact"
                : verbosity.trim().toLowerCase();

        String js = JsScriptLoader.loadWith(
                "tools/get-page-context.js",
                Map.of("verbosity", JsonUtils.escapeJson(v))
        );

        return toolExecutor.execute(
                js,
                "get page context",
                r -> r.getData().map(Object::toString).orElse("Empty tool response")
        );
    }

    // ========================================================================
    // Shape properties
    // ========================================================================

    /**
     * Retourne les propriétés principales d’une shape spécifique.
     *
     * @param shapeId UUID de la shape ou "selection"
     * @param verbosity niveau de détail ("compact" ou "full")
     * @return résumé JSON des propriétés
     */
    @Tool(description = "Read-only. Returns the properties of a shape.")
    public String getPropertiesFromShape(
            @ToolParam(description = "Shape id (uuid) or 'selection'.")
            String shapeId,
            @ToolParam(description = "Verbosity: compact|full.")
            String verbosity
    ) {
        log.info("Tool called: getPropertiesFromShape({}, {})", shapeId, verbosity);

        String id = (shapeId == null || shapeId.isBlank())
                ? "selection"
                : shapeId.trim();

        String v = (verbosity == null || verbosity.isBlank())
                ? "compact"
                : verbosity.trim().toLowerCase();

        String js = JsScriptLoader.loadWith(
                "tools/get-properties-from-shape.js",
                Map.of(
                        "shapeId", JsonUtils.escapeJson(id),
                        "verbosity", JsonUtils.escapeJson(v)
                )
        );

        return toolExecutor.execute(
                js,
                "get properties from shape",
                r -> r.getData().map(Object::toString).orElse("Empty tool response")
        );
    }

    // ========================================================================
    // Geometry
    // ========================================================================

    /**
     * Retourne le centre géométrique (cx, cy) d’une shape.
     *
     * @param shapeId UUID de la shape ou "selection"
     * @return JSON contenant id, cx et cy
     */
    @Tool(description = "Read-only. Returns the center (cx, cy) of a shape.")
    public String getCenterFromShape(
            @ToolParam(description = "Shape id (uuid) or 'selection'.")
            String shapeId
    ) {
        log.info("Tool called: getCenterFromShape({})", shapeId);

        String id = (shapeId == null || shapeId.isBlank())
                ? "selection"
                : shapeId.trim();

        String js = JsScriptLoader.loadWith(
                "tools/get-center-from-shape.js",
                Map.of("shapeId", JsonUtils.escapeJson(id))
        );

        return toolExecutor.execute(
                js,
                "get center from shape",
                r -> r.getData().map(Object::toString).orElse("Empty tool response")
        );
    }

    // ========================================================================
    // Hierarchy
    // ========================================================================

    /**
     * Retourne le parent le plus haut d’une shape dans la hiérarchie.
     *
     * @param shapeId UUID de la shape ou "selection"
     * @return description du root et de la hiérarchie
     */
    @Tool(description = "Read-only. Returns the top-most parent of a shape in the hierarchy.")
    public String getComponentRoot(
            @ToolParam(description = "Shape id (uuid) or 'selection'.")
            String shapeId
    ) {
        log.info("Tool called: getComponentRoot({})", shapeId);

        String id = (shapeId == null || shapeId.isBlank())
                ? "selection"
                : shapeId.trim();

        String js = JsScriptLoader.loadWith(
                "tools/get-component-root.js",
                Map.of("shapeId", JsonUtils.escapeJson(id))
        );

        return toolExecutor.execute(
                js,
                "get component root",
                r -> r.getData().map(Object::toString).orElse("Empty tool response")
        );
    }

    /**
     * Retourne l’index d’une shape dans son parent (ordre Layers).
     *
     * @param shapeId UUID de la shape ou "selection"
     * @return parentId et index calculé
     */
    @Tool(description = "Read-only. Returns the index of a shape in its parent (Layers order).")
    public String getShapeParentIndex(
            @ToolParam(description = "Shape id (uuid) or 'selection'.")
            String shapeId
    ) {
        log.info("Tool called: getShapeParentIndex({})", shapeId);

        String id = (shapeId == null || shapeId.isBlank())
                ? "selection"
                : shapeId.trim();

        String js = JsScriptLoader.loadWith(
                "tools/get-shape-parent-index.js",
                Map.of("shapeId", JsonUtils.escapeJson(id))
        );

        return toolExecutor.execute(
                js,
                "get shape parent index",
                r -> r.getData().map(Object::toString).orElse("Empty tool response")
        );
    }

    /**
     * Retourne le parent direct d’une shape.
     */
    @Tool(description = "Read-only. Returns the direct parent of a shape.")
    public String getParentFromShape(
            @ToolParam(description = "Shape id (uuid) or 'selection'.")
            String shapeId
    ) {
        log.info("Tool called: getParentFromShape({})", shapeId);

        String id = (shapeId == null || shapeId.isBlank())
                ? "selection"
                : shapeId.trim();

        String js = JsScriptLoader.loadWith(
                "tools/get-parent-from-shape.js",
                Map.of("shapeId", JsonUtils.escapeJson(id))
        );

        return toolExecutor.execute(
                js,
                "get parent from shape",
                r -> r.getData().map(Object::toString).orElse("Empty tool response")
        );
    }

    /**
     * Retourne les enfants directs d’une shape conteneur.
     */
    @Tool(description = "Read-only. Returns the direct children of a shape.")
    public String getChildrenFromShape(
            @ToolParam(description = "Shape id (uuid) or 'selection'.")
            String shapeId
    ) {
        log.info("Tool called: getChildrenFromShape({})", shapeId);

        String id = (shapeId == null || shapeId.isBlank())
                ? "selection"
                : shapeId.trim();

        String js = JsScriptLoader.loadWith(
                "tools/get-children-from-shape.js",
                Map.of("shapeId", JsonUtils.escapeJson(id))
        );

        return toolExecutor.execute(
                js,
                "get children from shape",
                r -> r.getData().map(Object::toString).orElse("Empty tool response")
        );
    }

    /**
     * Retourne les couleurs (fills et strokes) utilisées par une shape.
     */
    @Tool(description = "Read-only. Returns the fills/strokes colors used by a shape.")
    public String getShapesColors(
            @ToolParam(description = "Shape id (uuid) or 'selection'.")
            String shapeId
    ) {
        log.info("Tool called: getShapesColors({})", shapeId);

        String id = (shapeId == null || shapeId.isBlank())
                ? "selection"
                : shapeId.trim();

        String js = JsScriptLoader.loadWith(
                "tools/get-shapes-colors.js",
                Map.of("shapeId", JsonUtils.escapeJson(id))
        );

        return toolExecutor.execute(
                js,
                "get shapes colors",
                r -> r.getData().map(Object::toString).orElse("Empty tool response")
        );
    }
}