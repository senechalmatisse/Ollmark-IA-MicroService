package com.penpot.ai.application.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.application.tools.support.JsScriptLoader;
import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import com.penpot.ai.application.tools.support.ToolResponseBuilder;
import com.penpot.ai.core.domain.TaskResult;
import com.penpot.ai.shared.util.JsStringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tools dédiés aux opérations de mise en page et d'organisation des calques dans Penpot.
 *
 * <p>Cette classe regroupe les actions d'alignement, de distribution, de groupement,
 * de changement de parent et de gestion de l'ordre d'empilement. L'exécution des scripts
 * JavaScript est centralisée via {@link PenpotToolExecutor} afin d'uniformiser la gestion
 * des retours et des erreurs.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PenpotLayoutTools {

    private static final String UNKNOWN_ID = "unknown";

    private final ObjectMapper objectMapper;
    private final PenpotToolExecutor toolExecutor;

    /**
     * Déplace une ou plusieurs formes existantes dans un board déjà présent sur la page courante.
     *
     * @param shapeIds liste d'UUID séparés par des virgules pour les formes à déplacer
     * @param boardId UUID du board de destination
     * @return résultat JSON standardisé de l'opération de déplacement
     */
    @Tool(description = """
        Move one or more existing shapes into an existing board on the current page.
        Use this only when the destination board already exists.
    """)
    public String addShapeToBoard(
        @ToolParam(description = "Comma-separated shape UUIDs to move.") String shapeIds,
        @ToolParam(description = "UUID of the destination board.") String boardId
    ) {
        log.info("Tool called: addShapeToBoard({}, {})", shapeIds, boardId);

        String code = buildResolvedShapesPrelude(resolveIds(shapeIds))
            + jsString("boardId", boardId)
            + JsScriptLoader.load("tools/layout/add-shapes-to-board.js");

        return executeMultiShape(code, "moved to board");
    }

    /**
     * Extrait une ou plusieurs formes de leur parent courant pour les replacer à la racine de la page.
     *
     * @param shapeIds liste d'UUID séparés par des virgules pour les formes à extraire
     * @return résultat JSON standardisé de l'opération d'extraction
     */
    @Tool(description = """
        Remove one or more shapes from their current parent and place them back at the page root.
        This does not delete the shapes.
    """)
    public String removeShapeFromParent(
        @ToolParam(description = "Comma-separated shape UUIDs to extract.") String shapeIds
    ) {
        log.info("Tool called: removeShapeFromParent({})", shapeIds);

        String code = buildResolvedShapesPrelude(resolveIds(shapeIds))
            + JsScriptLoader.load("tools/layout/remove-shapes.js");

        return executeMultiShape(code, "extracted");
    }

    /**
     * Duplique une forme et applique un décalage X/Y à la copie.
     *
     * @param shapeId UUID de la forme source à cloner
     * @param offsetX décalage horizontal en pixels (20 par défaut)
     * @param offsetY décalage vertical en pixels (20 par défaut)
     * @return résultat JSON contenant l'identifiant de la copie créée
     */
    @Tool(description = """
        Duplicate a shape and offset the copy by a number of pixels on the X and Y axes.
    """)
    public String cloneShape(
        @ToolParam(description = "UUID of the shape to duplicate.") String shapeId,
        @ToolParam(required = false, description = "Horizontal offset in pixels. Default is 20.") Integer offsetX,
        @ToolParam(required = false, description = "Vertical offset in pixels. Default is 20.") Integer offsetY
    ) {
        log.info("Tool called: cloneShape({}, {}, {})", shapeId, offsetX, offsetY);

        int dx = offsetX != null ? offsetX : 20;
        int dy = offsetY != null ? offsetY : 20;

        String code = buildSingleShapePrelude(shapeId)
            + jsNumber("offsetX", dx)
            + jsNumber("offsetY", dy)
            + JsScriptLoader.load("tools/layout/clone-shape.js");

        return executeClone(code);
    }

    /**
     * Aligne plusieurs formes selon un mode d'alignement horizontal ou vertical.
     *
     * @param shapeIds liste d'UUID séparés par des virgules pour les formes à aligner
     * @param alignment mode d'alignement demandé
     * @return résultat JSON de l'alignement, ou une erreur si le mode est invalide
     */
    @Tool(description = """
        Align two or more shapes on the current page.
        Valid alignment modes are: left, center, right, top, middle, bottom.
    """)
    public String alignShapes(
        @ToolParam(description = "Comma-separated shape UUIDs to align.") String shapeIds,
        @ToolParam(description = "Alignment mode: left, center, right, top, middle, or bottom.") String alignment
    ) {
        log.info("Tool called: alignShapes({}, {})", shapeIds, alignment);

        String normalized = normalizeAlignment(alignment);
        if (!List.of("left", "center", "right", "top", "middle", "bottom").contains(normalized)) {
            return ToolResponseBuilder.error("Invalid alignment: " + alignment);
        }

        String scriptPath = switch (normalized) {
            case "left", "center", "right" -> "tools/layout/align-horizontal.js";
            case "top", "middle", "bottom" -> "tools/layout/align-vertical.js";
            default -> throw new IllegalArgumentException("Unsupported alignment: " + normalized);
        };

        String jsAlignment = normalized.equals("middle") ? "center" : normalized;

        String code = buildResolvedShapesPrelude(resolveIds(shapeIds))
            + jsString("alignment", jsAlignment)
            + JsScriptLoader.load(scriptPath);

        return executeMultiShape(code, "aligned");
    }

    /**
     * Distribue des formes de manière homogène sur un axe donné.
     *
     * @param shapeIds liste d'UUID séparés par des virgules pour les formes à distribuer
     * @param axis axe de distribution attendu ({@code horizontal} ou {@code vertical})
     * @return résultat JSON de distribution, ou une erreur si l'axe est invalide
     */
    @Tool(description = """
        Distribute three or more shapes evenly along one axis.
        Valid axes are: horizontal or vertical.
    """)
    public String distributeShapes(
        @ToolParam(description = "Comma-separated shape UUIDs to distribute.") String shapeIds,
        @ToolParam(description = "Distribution axis: horizontal or vertical.") String axis
    ) {
        log.info("Tool called: distributeShapes({}, {})", shapeIds, axis);

        if (axis == null) {
            return ToolResponseBuilder.error("Invalid axis: null");
        }

        String normalizedAxis = axis.trim().toLowerCase(Locale.ROOT);
        String scriptPath = switch (normalizedAxis) {
            case "horizontal" -> "tools/layout/distribute-horizontal.js";
            case "vertical" -> "tools/layout/distribute-vertical.js";
            default -> null;
        };

        if (scriptPath == null) {
            return ToolResponseBuilder.error("Invalid axis: " + axis);
        }

        String code = buildResolvedShapesPrelude(resolveIds(shapeIds))
            + JsScriptLoader.load(scriptPath);

        return executeMultiShape(code, "distributed");
    }

    /**
     * Regroupe plusieurs formes dans un groupe Penpot unique.
     *
     * @param shapeIds liste d'UUID séparés par des virgules pour les formes à regrouper
     * @param groupName nom optionnel du groupe créé
     * @return résultat JSON contenant l'identifiant du groupe créé
     */
    @Tool(description = """
        Group two or more shapes into a single Penpot group.
    """)
    public String groupShapes(
        @ToolParam(description = "Comma-separated shape UUIDs to group together.") String shapeIds,
        @ToolParam(description = "Optional name for the new group.", required = false) String groupName
    ) {
        log.info("Tool called: groupShapes({}, {})", shapeIds, groupName);

        String code = buildResolvedShapesPrelude(resolveIds(shapeIds))
            + jsNullableString("groupName", groupName)
            + JsScriptLoader.load("tools/layout/group-shapes.js");

        return executeGroup(code);
    }

    /**
     * Dégroupe un ou plusieurs groupes Penpot existants.
     *
     * @param groupIds liste d'UUID séparés par des virgules pour les groupes à dissoudre
     * @return résultat JSON listant les éléments traités
     */
    @Tool(description = """
        Ungroup one or more existing Penpot groups and release their children back to the parent layer.
    """)
    public String ungroupShapes(
        @ToolParam(description = "Comma-separated group UUIDs to ungroup.") String groupIds
    ) {
        log.info("Tool called: ungroupShapes({})", groupIds);

        String code = buildResolvedShapesPrelude(resolveIds(groupIds))
            + JsScriptLoader.load("tools/layout/ungroup-shapes.js");

        return executeMultiShape(code, "ungrouped");
    }

    /**
     * Envoie une ou plusieurs formes d'un niveau vers l'arrière dans l'ordre des calques.
     *
     * @param shapeIds liste d'UUID séparés par des virgules pour les formes ciblées
     * @return résultat JSON de l'opération de z-order
     */
    @Tool(description = """
        Send one or more shapes one step backward in the layer order on the current page.
        Penpot equivalent: Send backward.
    """)
    public String sendBackward(
        @ToolParam(description = "Comma-separated shape UUIDs to move backward.") String shapeIds
    ) {
        log.info("Tool called: sendBackward({})", shapeIds);
        return executeZOrder(shapeIds, "sendBackward", "sent backward");
    }

    /**
     * Fait avancer une ou plusieurs formes d'un niveau dans l'ordre des calques.
     *
     * @param shapeIds liste d'UUID séparés par des virgules pour les formes ciblées
     * @return résultat JSON de l'opération de z-order
     */
    @Tool(description = """
        Bring one or more shapes one step forward in the layer order on the current page.
        Penpot equivalent: Bring forward.
    """)
    public String bringForward(
        @ToolParam(description = "Comma-separated shape UUIDs to move forward.") String shapeIds
    ) {
        log.info("Tool called: bringForward({})", shapeIds);
        return executeZOrder(shapeIds, "bringForward", "brought forward");
    }

    /**
     * Place une ou plusieurs formes tout au fond de l'ordre des calques.
     *
     * @param shapeIds liste d'UUID séparés par des virgules pour les formes ciblées
     * @return résultat JSON de l'opération de z-order
     */
    @Tool(description = """
        Send one or more shapes to the very back of the layer order on the current page.
        Penpot equivalent: Send to back.
    """)
    public String sendToBack(
        @ToolParam(description = "Comma-separated shape UUIDs to send to the back.") String shapeIds
    ) {
        log.info("Tool called: sendToBack({})", shapeIds);
        return executeZOrder(shapeIds, "sendToBack", "sent to back");
    }

    /**
     * Place une ou plusieurs formes tout au premier plan de l'ordre des calques.
     *
     * @param shapeIds liste d'UUID séparés par des virgules pour les formes ciblées
     * @return résultat JSON de l'opération de z-order
     */
    @Tool(description = """
        Bring one or more shapes to the very front of the layer order on the current page.
        Penpot equivalent: Bring to front.
    """)
    public String bringToFront(
        @ToolParam(description = "Comma-separated shape UUIDs to bring to the front.") String shapeIds
    ) {
        log.info("Tool called: bringToFront({})", shapeIds);
        return executeZOrder(shapeIds, "bringToFront", "brought to front");
    }

    /**
     * Exécute une action de z-order sur une collection de formes.
     *
     * @param shapeIds liste brute des identifiants ciblés
     * @param action action Penpot à exécuter
     * @param operationLabel libellé fonctionnel utilisé dans la réponse
     * @return résultat JSON standardisé pour l'opération demandée
     */
    private String executeZOrder(String shapeIds, String action, String operationLabel) {
        String code = buildResolvedShapesPrelude(resolveIds(shapeIds))
            + jsString("action", action)
            + JsScriptLoader.load("tools/layout/z-order.js");

        return executeMultiShape(code, operationLabel);
    }

    /**
     * Exécute un script orienté multi-formes et formate la réponse de sortie.
     *
     * @param code script JavaScript à exécuter
     * @param operationLabel libellé de l'opération métier
     * @return réponse sérialisée pour l'orchestrateur de tools
     */
    private String executeMultiShape(String code, String operationLabel) {
        return toolExecutor.execute(code, operationLabel, result -> {
            Object rawData = result.getData().orElse(null);
            log.info("[PenpotLayoutTools] Raw task result data for '{}': {}", operationLabel, rawData);

            return ToolResponseBuilder.multiShapeOperation(operationLabel, readIds(result));
        });
    }

    /**
     * Exécute un script de groupement et extrait l'identifiant de groupe créé.
     *
     * @param code script JavaScript de groupement
     * @return réponse sérialisée de création de groupe
     */
    private String executeGroup(String code) {
        return toolExecutor.execute(code, "group shapes", result ->
            ToolResponseBuilder.groupCreated(readString(result, "groupId", UNKNOWN_ID))
        );
    }

    /**
     * Exécute un script de duplication et extrait l'identifiant du clone créé.
     *
     * @param code script JavaScript de duplication
     * @return réponse sérialisée de clonage
     */
    private String executeClone(String code) {
        return toolExecutor.execute(code, "clone shape", result ->
            ToolResponseBuilder.shapeCloned(readString(result, "cloneId", UNKNOWN_ID))
        );
    }

    /**
     * Construit le préambule JavaScript pour résoudre plusieurs formes sur la page courante.
     *
     * @param ids identifiants des formes à résoudre
     * @return fragment JavaScript prêt à concaténer avec le script métier
     */
    private String buildResolvedShapesPrelude(List<String> ids) {
        return """
            const page = penpot.currentPage;
            if (!page) throw new Error('No current page');
            const shapeIds = %s;
            const resolvedShapes = shapeIds.map(id => page.getShapeById(id)).filter(Boolean);
            """.formatted(toJsArray(ids));
    }

    /**
     * Construit le préambule JavaScript pour résoudre une forme unique.
     *
     * @param shapeId identifiant de la forme cible
     * @return fragment JavaScript initialisant {@code shape}
     */
    private String buildSingleShapePrelude(String shapeId) {
        return """
            const page = penpot.currentPage;
            if (!page) throw new Error('No current page');
            const shapeId = '%s';
            const shape = page.getShapeById(shapeId);
            """.formatted(JsStringUtils.jsSafe(shapeId));
    }

    /**
     * Génère une déclaration JavaScript de constante chaîne avec échappement sécurisé.
     *
     * @param variableName nom de la variable JavaScript
     * @param value valeur textuelle à injecter
     * @return ligne JavaScript déclarant la constante
     */
    private String jsString(String variableName, String value) {
        return "const %s = '%s';\n".formatted(variableName, JsStringUtils.jsSafe(value));
    }

    /**
     * Génère une constante JavaScript nullable pour une valeur texte optionnelle.
     *
     * @param variableName nom de la variable JavaScript
     * @param value valeur textuelle optionnelle
     * @return déclaration JavaScript avec {@code null} ou chaîne échappée
     */
    private String jsNullableString(String variableName, String value) {
        if (value == null || value.isBlank()) {
            return "const %s = null;\n".formatted(variableName);
        }
        return jsString(variableName, value);
    }

    /**
     * Génère une déclaration JavaScript de constante numérique.
     *
     * @param variableName nom de la variable JavaScript
     * @param value valeur numérique à injecter
     * @return ligne JavaScript déclarant la constante numérique
     */
    private String jsNumber(String variableName, Number value) {
        return "const %s = %s;\n".formatted(variableName, value);
    }

    /**
     * Convertit une liste d'identifiants en tableau JavaScript littéral.
     *
     * @param ids liste d'identifiants source
     * @return représentation JavaScript du tableau d'identifiants
     */
    private String toJsArray(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return "[]";
        }
        return ids.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(id -> "'" + JsStringUtils.jsSafe(id) + "'")
            .collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * Extrait la liste d'identifiants depuis un résultat de tâche normalisé.
     *
     * @param result résultat brut renvoyé par l'exécution du script
     * @return liste des identifiants exploitables, potentiellement vide
     */
    private List<String> readIds(TaskResult result) {
        Object raw = unwrapResultData(result);
        log.info("[PenpotLayoutTools] Unwrapped result data: {}", raw);

        if (!(raw instanceof Map<?, ?> map)) {
            log.warn("[PenpotLayoutTools] Result data is not a Map: {}", raw);
            return Collections.emptyList();
        }

        Object ids = map.get("ids");
        log.info("[PenpotLayoutTools] Raw ids field: {}", ids);

        if (ids instanceof List<?> list) {
            return list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .toList();
        }

        Object resultsNode = map.get("results");
        log.info("[PenpotLayoutTools] Raw results field: {}", resultsNode);

        if (resultsNode instanceof List<?> resultsList) {
            return resultsList.stream()
                .filter(Objects::nonNull)
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(entry -> entry.get("id"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .toList();
        }

        log.warn("[PenpotLayoutTools] Neither ids nor results contain usable IDs");
        return Collections.emptyList();
    }

    /**
     * Lit une valeur texte dans la charge utile de résultat avec une valeur de secours.
     *
     * @param result résultat brut renvoyé par l'exécution du script
     * @param key clé attendue dans l'objet de données
     * @param fallback valeur retournée en cas d'absence ou d'invalidité
     * @return valeur texte lue ou valeur de secours
     */
    private String readString(TaskResult result, String key, String fallback) {
        Object raw = unwrapResultData(result);
        if (!(raw instanceof Map<?, ?> map)) {
            return fallback;
        }

        Object value = map.get(key);
        if (value == null) {
            return fallback;
        }

        String text = value.toString();
        return text.isBlank() ? fallback : text;
    }

    /**
     * Déplie et normalise la structure de données d'un {@link TaskResult}.
     *
     * <p>Gère le cas où la charge utile contient un niveau d'imbrication supplémentaire
     * sous la clé {@code result}.</p>
     *
     * @param result résultat brut à normaliser
     * @return objet normalisé prêt à être interprété
     */
    private Object unwrapResultData(TaskResult result) {
        Object data = result.getData().orElse(null);

        if (data == null) {
            return null;
        }

        Object normalized = normalizeJsonLikeData(data);

        if (normalized instanceof Map<?, ?> outer && outer.containsKey("result")) {
            Object nested = outer.get("result");
            return normalizeJsonLikeData(nested);
        }

        return normalized;
    }

    /**
     * Normalise une valeur potentiellement JSON sérialisée sous forme de chaîne.
     *
     * @param value valeur à inspecter et normaliser
     * @return valeur convertie en objet/list/map lorsque possible, sinon valeur d'origine
     */
    private Object normalizeJsonLikeData(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map<?, ?> || value instanceof List<?>) {
            return value;
        }

        if (value instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.isEmpty()) {
                return text;
            }

            if ((trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
                try {
                    return objectMapper.readValue(trimmed, new TypeReference<Object>() {});
                } catch (Exception e) {
                    log.warn("[PenpotLayoutTools] Failed to parse JSON string: {}", trimmed, e);
                    return text;
                }
            }
        }

        return value;
    }

    /**
     * Normalise les libellés d'alignement en anglais/français vers les valeurs attendues.
     *
     * @param alignment libellé brut fourni par l'appelant
     * @return valeur normalisée exploitable par les scripts d'alignement
     */
    private String normalizeAlignment(String alignment) {
        if (alignment == null) {
            return "";
        }

        String value = alignment.toLowerCase(Locale.ROOT).trim();

        if (value.contains("axe x") || value.contains("en x") || value.contains("horizontal center")
            || value.contains("centre horizontal")) {
            return "center";
        }
        if (value.contains("axe y") || value.contains("en y") || value.contains("vertical center")
            || value.contains("milieu vertical")) {
            return "middle";
        }

        return switch (value) {
            case "centre" -> "center";
            case "milieu" -> "middle";
            case "haut" -> "top";
            case "bas" -> "bottom";
            case "gauche" -> "left";
            case "droite" -> "right";
            default -> value;
        };
    }

    /**
     * Découpe une chaîne d'identifiants séparés par des virgules.
     *
     * @param raw chaîne source potentiellement vide
     * @return liste d'identifiants nettoyés
     */
    private List<String> resolveIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(id -> !id.isEmpty())
            .toList();
    }
}
