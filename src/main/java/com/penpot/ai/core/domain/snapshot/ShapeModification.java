package com.penpot.ai.core.domain.snapshot;


/**
 * Capture le delta d'une modification IA sur une shape existante.
 *
 * before → état Penpot capturé AVANT l'appel au tool de modification
 * after  → état Penpot capturé APRÈS l'appel (optionnel, pour audit)
 *
 * Undo : restaure l'état "before" via ShapeState.buildRestoreJs()
 */
public record ShapeModification(
    String shapeId,
    String toolName,      
    ShapeState before,
    ShapeState after      
) {
    public static ShapeModification of(String shapeId, String toolName, ShapeState before) {
        return new ShapeModification(shapeId, toolName, before, null);
    }
}
