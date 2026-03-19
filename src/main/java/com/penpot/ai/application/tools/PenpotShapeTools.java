package com.penpot.ai.application.tools;

import java.util.Locale;

import org.springframework.ai.tool.annotation.*;
import org.springframework.stereotype.Component;

import com.penpot.ai.application.tools.support.*;
import com.penpot.ai.shared.util.JsStringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tools pour la création de formes graphiques dans Penpot.
 *
 * <p>L'exécution et le formatage sont délégués à {@link PenpotToolExecutor}.</p>
 * <p>La génération de texte réutilise {@link PenpotJsSnippets#createText}.</p>
 *
 * @see PenpotLayoutTools pour l'alignement
 * @see PenpotTransformTools pour les transformations
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PenpotShapeTools {

    private final PenpotToolExecutor toolExecutor;

    /**
     * Crée un rectangle sur la page courante de Penpot.
     *
     * <p>Retourne l'UUID du shape créé — il est CRUCIAL de conserver cet ID
     * pour toute opération ultérieure (déplacement, transformation, boolean, ...).</p>
     *
     * @param x abscisse (pixels) de la position
     * @param y ordonnée (pixels) de la position
     * @param width largeur en pixels
     * @param height hauteur en pixels
     * @param fillColor couleur de remplissage au format hex (#RRGGBB), facultatif
     * @param name nom optionnel du rectangle
     * @return l'UUID du rectangle créé
     */
    @Tool(description = "Create a rectangle shape in Penpot.")
    public String createRectangle(
        @ToolParam(description = "X position in pixels") Integer x,
        @ToolParam(description = "Y position in pixels") Integer y,
        @ToolParam(description = "Width in pixels") Integer width,
        @ToolParam(description = "Height in pixels") Integer height,
        @ToolParam(description = "Fill color in hex format (#RRGGBB)", required = false) String fillColor,
        @ToolParam(description = "Optional name for the rectangle", required = false) String name
    ) {
        log.info("Tool called: createRectangle (x={}, y={}, w={}, h={}, color={})", x, y, width, height, fillColor);
        return toolExecutor.createShape(buildRectangleCode(x, y, width, height, fillColor, name), "rectangle");
    }

    /**
     * Crée une ellipse (ou un cercle si width==height) sur la page courante.
     *
     * <p>Retourne l'UUID de l'ellipse — conserver cet ID pour les opérations
     * suivantes.</p>
     *
     * @param x abscisse (pixels) du centre
     * @param y ordonnée (pixels) du centre
     * @param width largeur en pixels
     * @param height hauteur en pixels
     * @param fillColor couleur de remplissage en hex (facultatif)
     * @param name nom optionnel de l'ellipse
     * @return l'UUID de l'ellipse créée
     */
    @Tool(description = "Create an ellipse (circle or oval) shape in Penpot.")
    public String createEllipse(
        @ToolParam(description = "X position of center in pixels") Integer x,
        @ToolParam(description = "Y position of center in pixels") Integer y,
        @ToolParam(description = "Width in pixels") Integer width,
        @ToolParam(description = "Height in pixels (same as width for circle)") Integer height,
        @ToolParam(description = "Fill color in hex format", required = false) String fillColor,
        @ToolParam(description = "Optional name for the ellipse", required = false) String name
    ) {
        log.info("Tool called: createEllipse (x={}, y={}, w={}, h={})", x, y, width, height);
        return toolExecutor.createShape(buildEllipseCode(x, y, width, height, fillColor, name), "ellipse");
    }

    /**
     * Crée un élément de texte sur la page courante.
     *
     * <p>Retourne l'UUID du texte créé — conservez-le pour référence future.</p>
     *
     * @param content contenu du texte
     * @param x position X en pixels
     * @param y position Y en pixels
     * @param fontSize taille de police en pixels (peut être null pour la valeur par défaut)
     * @param fontWeight poids de la police ("normal" ou "bold"), facultatif
     * @param fillColor couleur du texte en hex (facultatif)
     * @param name nom optionnel de l'élément texte
     * @return l'UUID de l'élément texte créé
     */
    @Tool(description = "Create a text element in Penpot.")
    public String createText(
        @ToolParam(description = "Text content to display") String content,
        @ToolParam(description = "X position in pixels") Integer x,
        @ToolParam(description = "Y position in pixels") Integer y,
        @ToolParam(description = "Font size in pixels (default: 16) small=14, medium=18, large=24, xlarge=36, xxlarge=48", required = false) Integer fontSize,
        @ToolParam(description = "Font weight: normal or bold (default: normal)", required = false) String fontWeight,
        @ToolParam(description = "Text color in hex format (default: #000000)", required = false) String fillColor,
        @ToolParam(description = "Optional name for the text element", required = false) String name
    ) {
        log.info("Tool called: createText (content='{}', x={}, y={})", content, x, y);
        return toolExecutor.createShape(
            PenpotJsSnippets.createText(content, x, y, fontSize, fontWeight, fillColor, name),
            "text"
        );
    }

    /**
     * Crée un board (artboard/canvas) et retourne son UUID.
     *
     * @param width largeur du board en pixels
     * @param height hauteur du board en pixels
     * @param name nom du board
     * @param backgroundColor couleur de fond en hex (facultatif)
     * @return l'UUID du board créé
     */
    @Tool(description = """
        Create a board (artboard/canvas) in Penpot.
        Use this as a container for design elements.
    """)
    public String createBoard(
        @ToolParam(description = "Board width in pixels") Integer width,
        @ToolParam(description = "Board height in pixels") Integer height,
        @ToolParam(description = "Name of the board") String name,
        @ToolParam(description = "Background color in hex format", required = false) String backgroundColor
    ) {
        log.info("Tool called: createBoard (w={}, h={}, name='{}')", width, height, name);
        return toolExecutor.createShape(buildBoardCode(width, height, name, backgroundColor), "board");
    }

    /**
     * Crée une étoile (SVG) et la place à la position donnée.
     *
     * <p>Si le nombre de points n'est pas renseigné, la valeur par défaut est 5.
     * L'innerRadius est exprimé en pourcentage (0-100) et contrôle le rayon
     * intérieur de l'étoile.</p>
     *
     * @param x position X en pixels
     * @param y position Y en pixels
     * @param width largeur de l'étoile (et du SVG) en pixels
     * @param height hauteur de l'étoile (et du SVG) en pixels
     * @param points nombre de branches (facultatif)
     * @param innerRadius rayon intérieur en pourcentage 0-100 (facultatif)
     * @param fillColor couleur de remplissage en hex (facultatif)
     * @param name nom optionnel de l'étoile
     * @return l'UUID de l'étoile créée
     */
    @Tool(description = "Create a star shape in Penpot.")
    public String createStar(
        @ToolParam(description = "X position in pixels") Integer x,
        @ToolParam(description = "Y position in pixels") Integer y,
        @ToolParam(description = "Width in pixels") Integer width,
        @ToolParam(description = "Height in pixels") Integer height,
        @ToolParam(description = "Number of points (default: 5)", required = false) Integer points,
        @ToolParam(description = "Inner radius percentage 0-100 (default: 38)", required = false) Integer innerRadius,
        @ToolParam(description = "Fill color in hex format", required = false) String fillColor,
        @ToolParam(description = "Optional name for the star", required = false) String name
    ) {
        log.info("Tool called: createStar (x={}, y={}, w={}, h={}, points={})", x, y, width, height, points);
        return toolExecutor.createShape(buildStarCode(x, y, width, height, points, innerRadius, fillColor, name), "star");
    }

    /**
     * Crée un triangle (différents types supportés) et retourne son UUID.
     *
     * <p>Types supportés: "equilateral" (par défaut), "right", "isosceles".</p>
     *
     * @param x position X en pixels
     * @param y position Y en pixels
     * @param width largeur en pixels
     * @param height hauteur en pixels
     * @param type type de triangle (facultatif)
     * @param fillColor couleur de remplissage en hex (facultatif)
     * @param name nom optionnel du triangle
     * @return l'UUID du triangle créé
     */
    @Tool(description = "Create a triangle shape in Penpot.")
    public String createTriangle(
        @ToolParam(description = "X position in pixels") Integer x,
        @ToolParam(description = "Y position in pixels") Integer y,
        @ToolParam(description = "Width in pixels") Integer width,
        @ToolParam(description = "Height in pixels") Integer height,
        @ToolParam(description = "Triangle type: equilateral, right, isosceles (default: equilateral)", required = false) String type,
        @ToolParam(description = "Fill color in hex format (#RRGGBB)", required = false) String fillColor,
        @ToolParam(description = "Optional name for the triangle", required = false) String name
    ) {
        log.info("Tool called: createTriangle (x={}, y={}, w={}, h={}, type={})",
                x, y, width, height, type);
        return toolExecutor.createShape(
            buildTriangleCode(x, y, width, height, type, fillColor, name),
            "triangle"
        );
    }

    /**
     * Combine plusieurs formes existantes en appliquant une opération booléenne
     * et retourne l'UUID du résultat.
     *
     * <p>Ne pas fournir d'IDs factices : les UUIDs doivent être ceux retournés
     * par les appels de création précédents. Requiert au moins deux shapes.</p>
     *
     * @param boolType type d'opération: "union", "subtract", "intersect", "exclude"
     * @param shapeIds liste d'UUIDs séparés par des virgules (minimum 2)
     * @param name nom optionnel du résultat
     * @return l'UUID du shape résultant
     */
    @Tool(description = "Combine multiple existing shapes using a boolean operation in Penpot.")
    public String createBoolean(
        @ToolParam(description = "Boolean operation type: union, subtract, intersect, or exclude") String boolType,
        @ToolParam(description = "Comma-separated UUIDs of the shapes to combine (minimum 2), e.g: 'uuid1,uuid2,uuid3'") String shapeIds,
        @ToolParam(description = "Optional name for the resulting boolean shape", required = false) String name
    ) {
        log.info("Tool called: createBoolean (type={}, shapeIds={})", boolType, shapeIds);
        return toolExecutor.createShape(buildBooleanCode(boolType, shapeIds, name), "boolean");
    }

    /**
     * Construit le code JavaScript exécuté par Penpot pour créer un rectangle.
     *
     * <p>Cette méthode assemble une chaîne JS qui initialise un rectangle,
     * positionne et redimensionne l'élément, applique la couleur et le nom
     * si fournis, puis retourne l'ID du shape.</p>
     *
     * @return code JS à exécuter par l'exécuteur de tools
     */
    private String buildRectangleCode(Integer x, Integer y, Integer width, Integer height, String fillColor, String name) {
        StringBuilder code = new StringBuilder();
        code.append("const rect = penpot.createRectangle();\n");
        code.append(String.format("rect.x = %d;\n", x));
        code.append(String.format("rect.y = %d;\n", y));
        code.append(String.format("rect.resize(%d, %d);\n", width, height));
        if (fillColor != null && !fillColor.isBlank())
            code.append(String.format("rect.fills = [{ fillColor: '%s' }];\n", JsStringUtils.sanitizeColor(fillColor)));
        if (name != null && !name.isBlank())
            code.append(String.format("rect.name = '%s';\n", JsStringUtils.jsSafe(name)));
        code.append("return rect.id;\n");
        return code.toString();
    }

    /**
     * Construit le code JavaScript pour créer une ellipse/cercle dans Penpot.
     *
     * @return code JS prêt à être passé à l'exécuteur
     */
    private String buildEllipseCode(Integer x, Integer y, Integer width, Integer height, String fillColor, String name) {
        StringBuilder code = new StringBuilder();
        code.append("const ellipse = penpot.createEllipse();\n");
        code.append(String.format("ellipse.x = %d;\n", x));
        code.append(String.format("ellipse.y = %d;\n", y));
        code.append(String.format("ellipse.resize(%d, %d);\n", width, height));
        if (fillColor != null && !fillColor.isBlank())
            code.append(String.format("ellipse.fills = [{ fillColor: '%s' }];\n",
                JsStringUtils.sanitizeColor(fillColor)));
        if (name != null && !name.isBlank())
            code.append(String.format("ellipse.name = '%s';\n", JsStringUtils.jsSafe(name)));
        code.append("return ellipse.id;\n");
        return code.toString();
    }

    /**
     * Construit le code JavaScript pour créer un board (artboard/canvas).
     *
     * @return code JS qui crée et configure un board et retourne son UUID
     */
    private String buildBoardCode(Integer width, Integer height, String name, String backgroundColor) {
        StringBuilder code = new StringBuilder();
        code.append("const board = penpot.createBoard();\n");
        code.append(String.format("board.resize(%d, %d);\n", width, height));
        if (name != null && !name.isBlank())
            code.append(String.format("board.name = '%s';\n", JsStringUtils.jsSafe(name)));
        if (backgroundColor != null && !backgroundColor.isBlank())
            code.append(String.format("board.fills = [{ fillColor: '%s' }];\n",
                JsStringUtils.sanitizeColor(backgroundColor)));
        code.append("return board.id;\n");
        return code.toString();
    }

    /**
     * Génère le code JS pour créer une étoile à partir d'un SVG inline.
     *
     * <p>Calcule le path, génère le SVG et produit le JS qui crée le groupe
     * correspondant dans Penpot puis positionne et nomme le groupe.</p>
     *
     * @return code JS qui crée l'étoile et retourne son UUID
     */
    private String buildStarCode(
        Integer x, Integer y, Integer width, Integer height,
        Integer points, Integer innerRadius, String fillColor, String name
    ) {
        int actualPoints = (points != null && points > 2) ? points : 5;
        double ratio = (innerRadius != null && innerRadius > 0 && innerRadius < 100)
            ? innerRadius / 100.0 : 0.382;

        String pathData = generateStarPath(width, height, actualPoints, ratio);
        String color = (fillColor != null && !fillColor.isBlank()) ? fillColor : "#CCCCCC";
        String svg = String.format(
            "<svg width='%d' height='%d' viewBox='0 0 %d %d' xmlns='http://www.w3.org/2000/svg'>"
            + "<path d='%s' fill='%s'/></svg>",
            width, height, width, height, pathData, color
        );

        StringBuilder code = new StringBuilder();
        code.append(String.format("const svg = `%s`;\n", svg));
        code.append("const group = penpot.createShapeFromSvg(svg);\n");
        code.append("if (!group) throw new Error('Failed to create star from SVG');\n");
        code.append(String.format("group.x = %d;\n", x));
        code.append(String.format("group.y = %d;\n", y));
        if (name != null && !name.isBlank())
            code.append(String.format("group.name = '%s';\n", JsStringUtils.jsSafe(name)));
        code.append("return group.id;\n");
        return code.toString();
    }

    /**
     * Génère la chaîne 'd' pour le path d'une étoile SVG.
     *
     * @param width largeur du SVG
     * @param height hauteur du SVG
     * @param points nombre de branches
     * @param innerRadiusRatio ratio du rayon intérieur (0-1)
     * @return la valeur du path 'd' utilisable dans un élément <path>
     */
    private String generateStarPath(int width, int height, int points, double innerRadiusRatio) {
        double cx = width / 2.0;
        double cy = height / 2.0;
        double rx = width / 2.0;
        double ry = height / 2.0;

        StringBuilder sb = new StringBuilder();
        double step = Math.PI / points;
        double angle = -Math.PI / 2;

        for (int i = 0; i < 2 * points; i++) {
            double r = (i % 2 == 0) ? 1.0 : innerRadiusRatio;
            double currX = cx + Math.cos(angle) * rx * r;
            double currY = cy + Math.sin(angle) * ry * r;
            if (i == 0) sb.append("M").append(currX).append(" ").append(currY);
            else        sb.append(" L").append(currX).append(" ").append(currY);
            angle += step;
        }
        sb.append(" Z");
        return sb.toString();
    }

    /**
     * Construit le JS pour créer un triangle (SVG) selon le type demandé.
     *
     * @return code JS qui crée le triangle et retourne son UUID
     */
    private String buildTriangleCode(
        Integer x, Integer y, Integer width, Integer height,
        String type, String fillColor, String name
    ) {
        String pathData = generateTrianglePath(width, height, type);
        String color = (fillColor != null && !fillColor.isBlank()) ? fillColor : "#CCCCCC";
        String svg = String.format(
                "<svg width='%d' height='%d' viewBox='0 0 %d %d' xmlns='http://www.w3.org/2000/svg'>"
                + "<path d='%s' fill='%s'/></svg>",
                width, height, width, height, pathData, color
        );

        StringBuilder code = new StringBuilder();
        code.append(String.format("const svg = `%s`;\n", svg));
        code.append("const group = penpot.createShapeFromSvg(svg);\n");
        code.append("if (!group) throw new Error('Failed to create triangle from SVG');\n");
        code.append(String.format("group.x = %d;\n", x));
        code.append(String.format("group.y = %d;\n", y));
        if (name != null && !name.isBlank()) {
            code.append(String.format("group.name = '%s';\n", JsStringUtils.jsSafe(name)));
        }
        code.append("return group.id;\n");
        return code.toString();
    }

    /**
     * Génère le chemin SVG pour un triangle selon le type demandé.
     *
     * <p>Supporte les types: "equilateral", "right", "isosceles".</p>
     *
     * @param width largeur souhaitée
     * @param height hauteur souhaitée
     * @param type type de triangle (peut être null -> "equilateral")
     * @return la chaîne path SVG correspondante
     */
    private String generateTrianglePath(int width, int height, String type) {
        double cx = width / 2.0;
        if (type == null) type = "equilateral";
        return switch (type.toLowerCase().trim()) {
            case "right" ->
                String.format("M 0,0 L %d,%d L 0,%d Z", width, height, height);
            case "isosceles" ->
                String.format(Locale.US, "M %.1f,0 L %d,%d L 0,%d Z", cx, width, height, height);
            default -> // equilateral
                String.format(Locale.US, "M %.1f,0 L %d,%d L 0,%d Z", cx, width, height, height);
        };
    }

    /**
    * Génère le JS pour une opération booléenne Penpot.
    *
    */
    private String buildBooleanCode(String boolType, String shapeIds, String name) {
        String resolvedType = resolveBooleanType(boolType);
        String[] ids = shapeIds.split(",");
        StringBuilder code = new StringBuilder();

        code.append("const shapes = [];\n");
        for (String id : ids) {
            String trimmedId = id.trim();
            code.append(String.format(
                "const shape_%s = penpot.currentPage.getShapeById('%s');\n",
                trimmedId.replace("-", "_"), trimmedId
            ));
            code.append(String.format(
                "if (!shape_%s) throw new Error('Shape not found: %s');\n",
                trimmedId.replace("-", "_"), trimmedId
            ));
            code.append(String.format("shapes.push(shape_%s);\n",
                trimmedId.replace("-", "_")));
        }

        code.append("if (shapes.length < 2) throw new Error('createBoolean requires at least 2 shapes');\n");
        code.append(String.format(
            "const result = penpot.createBoolean('%s', shapes);\n", resolvedType
        ));
        code.append("if (!result) throw new Error('Boolean operation failed');\n");

        if (name != null && !name.isBlank()) {
            code.append(String.format("result.name = '%s';\n",
                JsStringUtils.jsSafe(name)));
        }
        code.append("return result.id;\n");
        return code.toString();
    }

    /**
     * Mappe le type booléen vers la valeur attendue par l'API Penpot.
     *
     * Penpot API accepte : 'union' | 'difference' | 'exclude' | 'intersection'
     * On accepte en plus : 'subtract' → 'difference', 'intersect' → 'intersection'
     */
    private String resolveBooleanType(String boolType) {
        if (boolType == null) return "union";
        return switch (boolType.toLowerCase().trim()) {
            case "subtract", "difference" -> "difference";
            case "intersect", "intersection" -> "intersection";
            case "exclude" -> "exclude";
            case "union" -> "union";
            default -> "union"; 
        };
    }

}