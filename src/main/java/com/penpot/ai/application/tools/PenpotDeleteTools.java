package com.penpot.ai.application.tools;

import com.penpot.ai.application.tools.support.*;
import com.penpot.ai.shared.util.JsStringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Tools pour la suppression d'éléments dans Penpot.
 *
 * <p>L'exécution et le formatage sont délégués à {@link PenpotToolExecutor#executeDelete}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PenpotDeleteTools {

    private final PenpotToolExecutor toolExecutor;

    @Tool(description = "Delete all currently selected shapes or elements in Penpot.")
    public String deleteSelection() {
        log.info("Tool called: deleteSelection");
        return executeDeleteWithStructuredResponse("""
            const selection = penpot.selection;
            if (selection.length === 0) return "ERR:No items selected";
            const count = selection.length;
            selection.forEach(shape => shape.remove());
            return "OK:Deleted " + count + " items";
            """, "delete selection");
    }

    @Tool(description = "Delete a specific shape by its ID.")
    public String deleteShapeById(
        @ToolParam(description = "The UUID of the shape to delete") String shapeId
    ) {
        if (shapeId == null || !JsStringUtils.UUID_PATTERN.matcher(shapeId.trim()).matches()) {
            throw new IllegalArgumentException("UUID invalide : " + shapeId);
        }
        log.info("Tool called: deleteShapeById (id={})", shapeId);
        return executeDeleteWithStructuredResponse(
            String.format("""
                const shape = penpot.currentPage.getShapeById('%s');
                if (shape) { shape.remove(); return "OK:Shape deleted"; }
                return "ERR:Shape not found";
                """, shapeId.trim()),
            "delete shape " + shapeId
        );
    }

    @Tool(description = "Delete only the selected shapes that are on the board and were created manually.")
    public String deleteManualShapesOnBoard() {
        log.info("Tool called: deleteManualShapesOnBoard");
        return executeDeleteWithStructuredResponse("""
            const selection = penpot.selection;
            if (selection.length === 0) return "ERR:No items selected";
            let deletedCount = 0;
            selection.forEach(shape => {
                const isManual = !shape.componentId && !shape.componentFile && !shape.mainInstance;
                const isOnBoard = shape.parent && (shape.parent.type === 'page' || shape.parent.type === 'frame');
                if (isManual && isOnBoard) { shape.remove(); deletedCount++; }
            });
            return deletedCount === 0
                ? "ERR:No manual shapes on board found"
                : "OK:Deleted " + deletedCount + " manual shape(s) from board";
            """, "delete manual shapes on board");
    }

    @Tool(description = "Delete ALL manually created shapes from the current board.")
    public String deleteAllManualShapesFromBoard() {
        log.info("Tool called: deleteAllManualShapesFromBoard");
        return executeDeleteWithStructuredResponse("""
            const currentPage = penpot.currentPage;
            if (!currentPage) return "ERR:No active page found";
            let deletedCount = 0;
            function deleteManualShapes(element) {
                if (!element.children) return;
                [...element.children].forEach(child => {
                    const isManual = !child.componentId && !child.componentFile && !child.mainInstance;
                    if (isManual && child.type !== 'page') { child.remove(); deletedCount++; }
                    else deleteManualShapes(child);
                });
            }
            deleteManualShapes(currentPage);
            return deletedCount === 0
                ? "ERR:No manual shapes found"
                : "OK:Deleted " + deletedCount + " manual shape(s)";
            """, "delete all manual shapes");
    }

    /**
     * Exécute un script de suppression et traduit le marqueur "OK:" / "ERR:" retourné
     * par le script JS en réponse JSON standardisée via {@link ToolResponseBuilder}.
     *
     * <p>Les scripts JS retournent des chaînes préfixées :
     * <ul>
     *   <li>{@code "OK:..."} → {@link ToolResponseBuilder#success}</li>
     *   <li>{@code "ERR:..."} → {@link ToolResponseBuilder#error}</li>
     * </ul>
     *
     * @param code           script JavaScript à exécuter
     * @param operationLabel libellé de l'opération pour les logs
     * @return réponse JSON standardisée
     */
    private String executeDeleteWithStructuredResponse(String code, String operationLabel) {
        return toolExecutor.execute(code, operationLabel, result -> {
            if (!result.isSuccess()) {
                return ToolResponseBuilder.error(result.getError().orElse("Unknown error"));
            }
            String raw = result.getData().map(Object::toString).orElse("");
            if (raw.startsWith("ERR:")) {
                return ToolResponseBuilder.error(raw.substring(4));
            }
            String message = raw.startsWith("OK:") ? raw.substring(3) : raw;
            return ToolResponseBuilder.success(message);
        });
    }
}