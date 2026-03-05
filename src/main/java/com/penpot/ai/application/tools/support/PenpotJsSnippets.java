package com.penpot.ai.application.tools.support;

import lombok.experimental.UtilityClass;

import java.util.*;

/**
 * Utilitaire de génération des fragments JavaScript Penpot récurrents.
 */
@UtilityClass
public class PenpotJsSnippets {

    /**
     * Génère le code JS pour trouver une forme par ID, avec fallback sur la sélection.
     * <p>Variable résultante dans le scope JS : {@code shape}.</p>
     *
     * @param shapeId UUID de la forme Penpot
     */
    public static String findShapeOrFallback(String shapeId) {
        return JsScriptLoader.loadWith("snippets/find-shape.js", Map.of("shapeId", shapeId));
    }

    /**
     * Génère le code JS pour trouver la <em>première</em> forme d'une liste d'IDs,
     * avec fallback sur le premier élément sélectionné.
     * <p>Variable résultante : {@code shape}.</p>
     *
     * @param shapeIds liste d'IDs (utilise uniquement le premier)
     */
    public static String findFirstShapeOrFallback(List<String> shapeIds) {
        if (shapeIds == null || shapeIds.isEmpty()) {
            return JsScriptLoader.load("snippets/find-first-shape-selection.js");
        }
        return JsScriptLoader.loadWith("snippets/find-first-shape.js",
            Map.of("shapeId", shapeIds.get(0)));
    }

    /**
     * Génère le code JS pour collecter plusieurs formes avec fallback sur la sélection.
     * <p>Variable résultante : {@code shapes} (tableau).</p>
     *
     * <p>Si {@code ids} est null ou vide, utilise directement {@code penpot.selection}.</p>
     *
     * @param ids      liste des UUIDs (null ou vide = utiliser la sélection courante)
     * @param toolName nom du tool pour les messages de console
     */
    public static String collectShapesOrFallback(List<String> ids, String toolName) {
        if (ids == null || ids.isEmpty()) {
            return JsScriptLoader.loadWith("snippets/collect-shapes-selection.js",
                Map.of("toolName", toolName));
        }

        StringBuilder sb = new StringBuilder("const shapes = [];\n");
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            sb.append(String.format("""
                try {
                    const _s%d = penpot.currentPage.getShapeById('%s');
                    if (_s%d) shapes.push(_s%d);
                } catch (e) {
                    console.log('[%s] Invalid ID or not found: %s');
                }
                """, i, id, i, i, toolName, id));
        }
        sb.append(String.format("""
            if (shapes.length === 0) shapes.push(...penpot.selection);
            """, toolName));
        return sb.toString();
    }

    /**
     * Génère le code JS pour créer un élément texte Penpot.
     *
     * <p>Extrait ici car dupliqué dans {@code PenpotShapeTools} et {@code PenpotContentTools}.
     * Variable résultante : {@code text}.</p>
     *
     * @param content    contenu textuel
     * @param x          position X
     * @param y          position Y
     * @param fontSize   taille de police (null = ignoré)
     * @param fontWeight graisse (null = ignoré)
     * @param fillColor  couleur hex (null = ignoré)
     * @param name       nom de l'élément (null = ignoré)
     */
    public static String createText(
        String content, int x, int y,
        Integer fontSize, String fontWeight, String fillColor, String name
    ) {
        String escaped = escapeJsString(content);
        StringBuilder code = new StringBuilder();
        code.append(String.format("const text = penpot.createText('%s');\n", escaped));
        code.append(String.format("text.x = %d;\n", x));
        code.append(String.format("text.y = %d;\n", y));
        if (fontSize != null && fontSize > 0)
            code.append(String.format("text.fontSize = %d;\n", fontSize));
        if (fontWeight != null && !fontWeight.isBlank())
            code.append(String.format("text.fontWeight = '%s';\n", fontWeight));
        if (fillColor != null && !fillColor.isBlank())
            code.append(String.format("text.fills = [{ fillColor: '%s' }];\n", fillColor));
        if (name != null && !name.isBlank())
            code.append(String.format("text.name = '%s';\n", escapeJsString(name)));
        code.append("return text.id;\n");
        return code.toString();
    }

    /**
     * Échappe une chaîne pour inclusion dans du code JavaScript entre single quotes.
     */
    public static String escapeJsString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}