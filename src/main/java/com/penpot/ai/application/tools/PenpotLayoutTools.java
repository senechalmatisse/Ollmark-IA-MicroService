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

@Slf4j
@Component
@RequiredArgsConstructor
public class PenpotLayoutTools {

    private static final String UNKNOWN_ID = "unknown";

    private final ObjectMapper objectMapper;
    private final PenpotToolExecutor toolExecutor;

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
            const resolvedShapes = shapeIds.map(id => page.getShapeById(id)).filter(Boolean);
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
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(id -> !id.isEmpty())
            .toList();
    }
}