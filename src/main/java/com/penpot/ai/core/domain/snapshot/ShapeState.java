package com.penpot.ai.core.domain.snapshot;
import java.util.List;
import java.util.Map;

import lombok.Builder;

/**
 * Capture l'état complet d'une shape à un instant T.
 * Utilisé pour stocker l'état AVANT une modification — permet le restore.
 *
 * Toutes les propriétés sont nullable — seules celles
 * effectivement modifiées par l'IA sont capturées.
 */
@Builder
public record ShapeState(
    String id,
    Double x,
    Double y,
    Double width,
    Double height,
    Double rotation,
    Double opacity,
    List<Map<String, Object>> fills,   
    String content,                   
    Integer fontSize,                  
    String fontWeight                  
) {
    /**
     * JS Penpot pour lire l'état complet d'une shape.
     * Retourne un objet JSON avec toutes les propriétés capturables.
     */
    public static String buildReadJs(String shapeId) {
        return """
            const shape = penpot.context.currentPage.getShapeById('%s');
            if (!shape) return null;
            const state = {
                id:       shape.id,
                x:        shape.x,
                y:        shape.y,
                width:    shape.width,
                height:   shape.height,
                rotation: shape.rotation,
                opacity:  shape.opacity,
                fills:    shape.fills
            };
            if (shape.type === 'text') {
                state.content    = shape.content;
                state.fontSize   = shape.fontSize;
                state.fontWeight = shape.fontWeight;
            }
            return JSON.stringify(state);
            """.formatted(shapeId);
    }

    /**
     * JS Penpot pour restaurer une shape à cet état (undo d'une modification).
     */
    public String buildRestoreJs() {
        StringBuilder js = new StringBuilder();
        js.append("""
            const shape = penpot.context.currentPage.getShapeById('%s');
            if (!shape) throw new Error('Shape not found for restore: %s');
            """.formatted(id, id));

        if (x != null)        js.append("shape.x = %s;\n".formatted(x));
        if (y != null)        js.append("shape.y = %s;\n".formatted(y));
        if (rotation != null) js.append("shape.rotate(%s);\n".formatted(rotation));
        if (opacity != null)  js.append("shape.opacity = %s;\n".formatted(opacity));

        if (width != null && height != null)
            js.append("shape.resize(%s, %s);\n".formatted(width, height));

        if (fills != null && !fills.isEmpty())
            js.append("shape.fills = %s;\n".formatted(fillsToJs()));

        if (content != null)
            js.append("shape.content = '%s';\n".formatted(content.replace("'", "\\'")));

        js.append("return shape.id;\n");
        return js.toString();
    }

    private String fillsToJs() {
        if (fills == null || fills.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < fills.size(); i++) {
            Map<String, Object> fill = fills.get(i);
            sb.append("{");
            fill.forEach((k, v) -> sb.append("'%s':'%s',".formatted(k, v)));
            if (sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
            sb.append("}");
            if (i < fills.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
