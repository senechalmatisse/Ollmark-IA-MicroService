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
 * Tools de layout Penpot.
 *
 * <p>Cette version garde le Java simple :
 * <ul>
 *   <li>le Java prépare le contexte d'exécution (page, shapes, paramètres),</li>
 *   <li>les scripts JS métier sont chargés via {@link JsScriptLoader},</li>
 *   <li>les réponses sont mappées directement en JSON standardisé.</li>
 * </ul>
 *
 * <p>Aucune convention fragile du type {@code OK_*}, aucun parsing regex maison,
 * et pas de dépendance à {@code PenpotJsSnippets} ici.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PenpotLayoutTools {

    private static final String UNKNOWN_ERROR = "Unknown error";
    private static final String UNKNOWN_ID = "unknown";
    private final ObjectMapper objectMapper;

    private final PenpotToolExecutor toolExecutor;

    @Tool(description = """
        Move one or more shapes into an existing target board.
        The board must already exist on the current page.
    """)
    public String addShapeToBoard(
        @ToolParam(description = "One or more shape IDs to move, separated by commas.") String shapeIds,
        @ToolParam(description = "UUID of the target board.") String boardId
    ) {
        log.info("Tool called: addShapeToBoard({}, {})", shapeIds, boardId);

        String code = buildResolvedShapesPrelude(resolveIds(shapeIds))
            + jsString("boardId", boardId)
            + JsScriptLoader.load("tools/layout/add-shapes-to-board.js");

        return executeMultiShape(code, "moved to board");
    }

    @Tool(description = """
        Extract one or more shapes from their current parent without deleting them.
        The shapes are moved back to the page root.
    """)
    public String removeShapeFromParent(
        @ToolParam(description = "One or more shape IDs to extract, separated by commas.") String shapeIds
    ) {
        log.info("Tool called: removeShapeFromParent({})", shapeIds);

        String code = buildResolvedShapesPrelude(resolveIds(shapeIds))
            + JsScriptLoader.load("tools/layout/remove-shapes.js");

        return executeMultiShape(code, "extracted");
    }

    @Tool(description = """
        Duplicate a shape and offset the clone by a given number of pixels.
    """)
    public String cloneShape(
        @ToolParam(description = "UUID of the shape to clone.") String shapeId,
        @ToolParam(required = false, description = "Horizontal offset in pixels (default: 20).") Integer offsetX,
        @ToolParam(required = false, description = "Vertical offset in pixels (default: 20).") Integer offsetY
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

    @Tool(description = "Align two or more shapes along a common axis or edge.")
    public String alignShapes(
        @ToolParam(description = "Two or more shape IDs to align, separated by commas.") String shapeIds,
        @ToolParam(description = "Alignment mode: left, center, right, top, middle, bottom.") String alignment
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

    @Tool(description = "Distribute three or more shapes evenly along a horizontal or vertical axis.")
    public String distributeShapes(
        @ToolParam(description = "Three or more shape IDs to distribute, separated by commas.") String shapeIds,
        @ToolParam(description = "Axis: horizontal or vertical.") String axis
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

    @Tool(description = """
        Group two or more shapes into a single Penpot group.
    """)
    public String groupShapes(
        @ToolParam(description = "Two or more shape IDs to group together, separated by commas.") String shapeIds,
        @ToolParam(description = "Optional name for the resulting group.", required = false) String groupName
    ) {
        log.info("Tool called: groupShapes({}, {})", shapeIds, groupName);

        String code = buildResolvedShapesPrelude(resolveIds(shapeIds))
            + jsNullableString("groupName", groupName)
            + JsScriptLoader.load("tools/layout/group-shapes.js");

        return executeGroup(code);
    }

    @Tool(description = "Ungroup one or more Penpot groups, releasing their children back to the parent layer.")
    public String ungroupShapes(
        @ToolParam(description = "One or more group IDs to ungroup, separated by commas.") String groupIds
    ) {
        log.info("Tool called: ungroupShapes({})", groupIds);

        String code = buildResolvedShapesPrelude(resolveIds(groupIds))
            + JsScriptLoader.load("tools/layout/ungroup-shapes.js");

        return executeMultiShape(code, "ungrouped");
    }

    @Tool(description = "Move one or more shapes one step backward in the layer stack (z-order).")
    public String sendShapeBackward(
        @ToolParam(description = "One or more shape IDs to send backward, separated by commas.") String shapeIds
    ) {
        log.info("Tool called: sendShapeBackward({})", shapeIds);
        return executeZOrder(shapeIds, "sendBackward", "sent backward");
    }

    @Tool(description = "Move one or more shapes one step forward in the layer stack (z-order).")
    public String sendShapeFrontward(
        @ToolParam(description = "One or more shape IDs to bring forward, separated by commas.") String shapeIds
    ) {
        log.info("Tool called: sendShapeFrontward({})", shapeIds);
        return executeZOrder(shapeIds, "bringForward", "brought forward");
    }

    @Tool(description = "Send one or more shapes to the very back of the layer stack (z-order).")
    public String sendShapeToTheBack(
        @ToolParam(description = "One or more shape IDs to send to the back, separated by commas.") String shapeIds
    ) {
        log.info("Tool called: sendShapeToTheBack({})", shapeIds);
        return executeZOrder(shapeIds, "sendToBack", "sent to back");
    }

    @Tool(description = "Bring one or more shapes to the very front of the layer stack (z-order).")
    public String sendShapeToTheFront(
        @ToolParam(description = "One or more shape IDs to bring to the front, separated by commas.") String shapeIds
    ) {
        log.info("Tool called: sendShapeToTheFront({})", shapeIds);
        return executeZOrder(shapeIds, "bringToFront", "brought to front");
    }

    private String executeZOrder(String shapeIds, String action, String operationLabel) {
        String code = buildResolvedShapesPrelude(resolveIds(shapeIds))
            + jsString("action", action)
            + JsScriptLoader.load("tools/layout/z-order.js");

        return executeMultiShape(code, operationLabel);
    }

    private String executeMultiShape(String code, String operationLabel) {
    return toolExecutor.execute(code, operationLabel, result -> {
        Object rawData = result.getData().orElse(null);
        log.info("[PenpotLayoutTools] Raw task result data for '{}': {}", operationLabel, rawData);

        return ToolResponseBuilder.multiShapeOperation(operationLabel, readIds(result));
    });
}

    private String executeGroup(String code) {
        return toolExecutor.execute(code, "group shapes", result ->
            ToolResponseBuilder.groupCreated(readString(result, "groupId", UNKNOWN_ID))
        );
    }

    private String executeClone(String code) {
        return toolExecutor.execute(code, "clone shape", result ->
            ToolResponseBuilder.shapeCloned(readString(result, "cloneId", UNKNOWN_ID))
        );
    }

    private String buildResolvedShapesPrelude(List<String> ids) {
        return """
            const page = penpot.currentPage;
            if (!page) throw new Error('No current page');
            const shapeIds = %s;
            const resolvedShapes = shapeIds.length > 0
                ? shapeIds.map(id => page.getShapeById(id)).filter(Boolean)
                : (penpot.selection || []).filter(Boolean);
            """.formatted(toJsArray(ids));
    }

    private String buildSingleShapePrelude(String shapeId) {
        return """
            const page = penpot.currentPage;
            if (!page) throw new Error('No current page');
            const shapeId = '%s';
            const shape = page.getShapeById(shapeId);
            """.formatted(JsStringUtils.jsSafe(shapeId));
    }

    private String jsString(String variableName, String value) {
        return "const %s = '%s';\n".formatted(variableName, JsStringUtils.jsSafe(value));
    }

    private String jsNullableString(String variableName, String value) {
        if (value == null || value.isBlank()) {
            return "const %s = null;\n".formatted(variableName);
        }
        return jsString(variableName, value);
    }

    private String jsNumber(String variableName, Number value) {
        return "const %s = %s;\n".formatted(variableName, value);
    }

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

private List<String> readIds(TaskResult result) {
    Object raw = unwrapResultData(result);
    log.info("[PenpotLayoutTools] Unwrapped result data: {}", raw);

    if (!(raw instanceof Map<?, ?> map)) {
        log.warn("[PenpotLayoutTools] Result data is not a Map: {}", raw);
        return Collections.emptyList();
    }

    Object ids = map.get("ids");
    log.info("[PenpotLayoutTools] Raw ids field: {}", ids);

    if (!(ids instanceof List<?> list)) {
        log.warn("[PenpotLayoutTools] ids field is not a List: {}", ids);
        return Collections.emptyList();
    }

    return list.stream()
        .filter(Objects::nonNull)
        .map(Object::toString)
        .filter(s -> !s.isBlank())
        .toList();
}

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

    private List<String> resolveIds(String raw) {
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("selection")) {
            return List.of();
        }

        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(id -> !id.isEmpty())
            .toList();
    }
}