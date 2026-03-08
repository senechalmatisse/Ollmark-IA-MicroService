package com.penpot.ai.application.tools;

import com.penpot.ai.application.tools.support.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Tools pour les transformations géométriques dans Penpot.
* <p>
 * Se concentre exclusivement sur l'altération spatiale des éléments existants, à savoir la rotation, 
 * la mise à l'échelle, le déplacement et le redimensionnement absolu. Dans un souci de cohérence et de robustesse, 
 * il s'appuie systématiquement sur la méthode {@link PenpotJsSnippets#findShapeOrFallback()} pour garantir 
 * la récupération de la forme cible avant toute manipulation. En outre, l'exécution finale des scripts générés 
 * est systématiquement déléguée au composant central {@link PenpotToolExecutor#transformShape()}, assurant ainsi 
 * un traitement standardisé des retours et des potentielles erreurs.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PenpotTransformTools {

    private final PenpotToolExecutor toolExecutor;

    /**
     * Applique une rotation angulaire à une forme géométrique spécifique.
     *
     * @param shapeId L'identifiant unique de la forme à pivoter.
     * @param angle   La valeur de l'angle de rotation exprimée en degrés.
     * @return        Le statut d'exécution renvoyé par le coordinateur d'outils, confirmant l'application de la rotation.
     */
    @Tool(description = """
        Rotates a specific shape or element on the canvas by a given angle. 
        Use this when you need to tilt, spin, or change the orientation of an object.
    """)
    public String rotateShape(
        @ToolParam(description = "The unique identifier (UUID) of the target shape to be rotated.") String shapeId,
        @ToolParam(description = """
            The rotation angle in degrees. Positive values rotate clockwise, negative values rotate counter-clockwise.
        """) int angle
    ) {
        log.info("Tool called: rotateShape (id={}, angle={}°)", shapeId, angle);
        return toolExecutor.transformShape(buildRotateCode(shapeId, angle), "rotated", shapeId);
    }

    /**
     * Opère un redimensionnement proportionnel ou anamorphique en appliquant des facteurs d'échelle.
     *
     * @param shapeId L'identifiant de la forme cible.
     * @param scaleX  Le coefficient multiplicateur applicable à la largeur de l'objet (1.0 équivaut à la taille originale).
     * @param scaleY  Le coefficient multiplicateur applicable à la hauteur de l'objet (1.0 équivaut à la taille originale).
     * @return        Le résultat JSON de l'opération actant le changement d'échelle.
     */
    @Tool(description = """
        Scales a shape proportionally or disproportionally using multipliers. 
        Use this to make an object twice as big or half the size without knowing its exact pixel dimensions.
    """)
    public String scaleShape(
        @ToolParam(description = "The unique identifier (UUID) of the target shape to be scaled.") String shapeId,
        @ToolParam(description = """
            The horizontal scaling factor multiplier (e.g., 1.5 to increase width by 50%, 1.0 for no change).
        """) float scaleX,
        @ToolParam(description = """
            The vertical scaling factor multiplier (e.g., 0.5 to reduce height by half, 1.0 for no change).
        """) float scaleY
    ) {
        log.info("Tool called: scaleShape (id={}, scaleX={}, scaleY={})", shapeId, scaleX, scaleY);
        return toolExecutor.transformShape(buildScaleCode(shapeId, scaleX, scaleY), "scaled", shapeId);
    }

    /**
     * Orchestre le déplacement spatial d'un élément selon un mode de positionnement dynamique.
     * 
     * @param shapeId  L'identifiant unique de la forme à déplacer.
     * @param newX     La nouvelle coordonnée horizontale ou la valeur de décalage sur l'axe des abscisses.
     * @param newY     La nouvelle coordonnée verticale ou la valeur de décalage sur l'axe des ordonnées.
     * @param relative Un indicateur booléen déterminant si le mouvement doit être relatif à la position courante (optionnel).
     * @return         La confirmation d'exécution incluant les nouvelles coordonnées de l'entité manipulée.
     */
    @Tool(description = """
        Translates or moves a shape across the canvas. 
        Can be used to set exact absolute coordinates (X/Y) or to nudge the shape relative to its current position.
    """)
    public String moveShape(
        @ToolParam(description = "The unique identifier (UUID) of the target shape to be moved.") String shapeId,
        @ToolParam(description = """
            The new absolute X coordinate, or the horizontal offset distance if 'relative' is true.
        """) float newX,
        @ToolParam(description = """
            The new absolute Y coordinate, or the vertical offset distance if 'relative' is true.
        """) float newY,
        @ToolParam(description = """
            Set to true to move the shape by an offset relative to its current position. Set to false to move it to exact absolute coordinates.
        """, required = false) Boolean relative
    ) {
        boolean isRelative = relative != null && relative;
        log.info("Tool called: moveShape (id={}, x={}, y={}, relative={})", shapeId, newX, newY, isRelative);
        return toolExecutor.transformShape(buildMoveCode(shapeId, newX, newY, isRelative), "moved", shapeId);
    }

    /**
     * Force l'attribution de dimensions absolues à une forme graphique spécifiée.
     *
     * @param shapeId   L'identifiant de la forme à redimensionner.
     * @param newWidth  La nouvelle largeur cible exprimée en pixels.
     * @param newHeight La nouvelle hauteur cible exprimée en pixels.
     * @return          La réponse du moteur confirmant l'application stricte des nouvelles mensurations.
     */
    @Tool(description = """
        Forces a shape to specific, absolute pixel dimensions. 
        Use this when you need an element to be an exact width and height, overriding its current proportions.
    """)
    public String resizeShape(
        @ToolParam(description = "The unique identifier (UUID) of the target shape to be resized.") String shapeId,
        @ToolParam(description = "The exact new target width in pixels.") float newWidth,
        @ToolParam(description = "The exact new target height in pixels.") float newHeight
    ) {
        log.info("Tool called: resizeShape (id={}, w={}, h={})", shapeId, newWidth, newHeight);
        return toolExecutor.transformShape(buildResizeCode(shapeId, newWidth, newHeight), "resized", shapeId);
    }

    /**
     * Construit dynamiquement la chaîne d'instructions JavaScript requise pour pivoter l'élément.
     *
     * @param shapeId L'identifiant de la forme cible.
     * @param angle   L'angle de rotation à appliquer.
     * @return        Le script JavaScript exécutable.
     */
    private String buildRotateCode(String shapeId, int angle) {
        return PenpotJsSnippets.findShapeOrFallback(shapeId)
            + String.format("shape.rotation = (shape.rotation || 0) + %d;%n", angle)
            + "return { id: shape.id, rotation: shape.rotation };\n";
    }

    /**
     * Élabore le script JavaScript responsable de la mise à l'échelle d'une forme.
     *
     * @param shapeId L'identifiant de l'objet graphique.
     * @param scaleX  Le coefficient de mise à l'échelle horizontale.
     * @param scaleY  Le coefficient de mise à l'échelle verticale.
     * @return        Le fragment de code JavaScript correspondant.
     */
    private String buildScaleCode(String shapeId, float scaleX, float scaleY) {
        String logic = String.format(Locale.US, """
            const currentWidth = shape.width || 1;
            const currentHeight = shape.height || 1;
            shape.resize(currentWidth * %.2f, currentHeight * %.2f);
            """, scaleX, scaleY);

        return PenpotJsSnippets.findShapeOrFallback(shapeId)
            + logic
            + "return { id: shape.id, width: shape.width, height: shape.height };\n";
    }

    /**
     * Assemble le code exécutable destiné à transposer la position d'une entité sur le canevas.
     *
     * @param shapeId  L'identifiant de la forme concernée.
     * @param newX     La valeur cible pour l'axe horizontal.
     * @param newY     La valeur cible pour l'axe vertical.
     * @param relative Le drapeau activant le calcul de positionnement relatif.
     * @return         Les instructions JavaScript de déplacement spatial.
     */
    private String buildMoveCode(String shapeId, float newX, float newY, boolean relative) {
        String movement = relative
            ? String.format(Locale.US, """
                shape.x = (shape.x || 0) + %.2f;
                shape.y = (shape.y || 0) + %.2f;
                """, newX, newY)
            : String.format(Locale.US, """
                shape.x = %.2f;
                shape.y = %.2f;
                """, newX, newY);

        return PenpotJsSnippets.findShapeOrFallback(shapeId)
            + movement
            + "return { id: shape.id, x: shape.x, y: shape.y };\n";
    }

    /**
     * Formule le script d'assignation autoritaire des dimensions d'une forme géométrique.
     *
     * @param shapeId   L'identifiant de l'élément à modifier.
     * @param newWidth  La largeur finale exigée.
     * @param newHeight La hauteur finale exigée.
     * @return          Le script JavaScript configurant les nouvelles dimensions.
     */
    private String buildResizeCode(String shapeId, float newWidth, float newHeight) {
        return PenpotJsSnippets.findShapeOrFallback(shapeId)
            + String.format(Locale.US, "shape.resize(%.2f, %.2f);%n", newWidth, newHeight)
            + "return { id: shape.id, width: shape.width, height: shape.height };\n";
    }
}