package com.penpot.ai.application.tools;

import com.penpot.ai.application.tools.support.*;
import com.penpot.ai.shared.util.JsStringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Tools pour la gestion des assets et styles dans Penpot.
 *
 * <p>Cette classe centralise exclusivement les opérations relatives à l'application 
 * de styles visuels (remplissage, dégradés, contours, ombres et opacité) ainsi qu'aux interactions. 
 * Le repérage des formes s'appuie sur la méthode utilitaire {@link PenpotJsSnippets#findShapeOrFallback}, 
 * tandis que l'exécution effective des scripts JavaScript générés est déléguée 
 * au service {@link PenpotToolExecutor#applyStyle}.</p>
 * 
 * @version 1.3
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PenpotAssetTools {

    private final PenpotToolExecutor toolExecutor;

    /**
     * Structure de données immuable encapsulant l'état de validation des paramètres de rayon de courbure.
     *
     * <p>Ce composant centralise l'évaluation des arguments fournis par le modèle d'intelligence artificielle.
     * Il permet de pré-calculer la validité des paramètres afin d'éviter la redondance des opérations 
     * logiques lors du traitement de la requête et de la construction du script.</p>
     * @param hasUniform  Indique si un rayon global valide a été spécifié.
     * @param hasTopLeft  Indique si un rayon valide est défini pour le coin supérieur gauche.
     * @param hasTopRight Indique si un rayon valide est défini pour le coin supérieur droit.
     * @param hasBotRight Indique si un rayon valide est défini pour le coin inférieur droit.
     * @param hasBotLeft  Indique si un rayon valide est défini pour le coin inférieur gauche.
     * @param radius      La valeur brute du rayon global.
     * @param topLeft     La valeur brute du rayon supérieur gauche.
     * @param topRight    La valeur brute du rayon supérieur droit.
     * @param bottomRight La valeur brute du rayon inférieur droit.
     * @param bottomLeft  La valeur brute du rayon inférieur gauche.
     */
    private record BorderRadiusState(
        boolean hasUniform,
        boolean hasTopLeft, boolean hasTopRight,
        boolean hasBotRight, boolean hasBotLeft,
        Float radius,
        Float topLeft, Float topRight,
        Float bottomRight, Float bottomLeft
    ) {
        /**
         * Instancie et valide un nouvel état de configuration pour les rayons de courbure.
         *
         * @param radius      Le rayon global applicable.
         * @param topLeft     Le rayon du coin supérieur gauche.
         * @param topRight    Le rayon du coin supérieur droit.
         * @param bottomRight Le rayon du coin inférieur droit.
         * @param bottomLeft  Le rayon du coin inférieur gauche.
         * @return L'objet d'état initialisé avec les vérifications de validité.
         */
        static BorderRadiusState of(
            Float radius, Float topLeft, Float topRight,
            Float bottomRight, Float bottomLeft
        ) {
            return new BorderRadiusState(
                radius != null && radius >= 0,
                topLeft != null && topLeft >= 0,
                topRight != null && topRight >= 0,
                bottomRight != null && bottomRight >= 0,
                bottomLeft != null && bottomLeft >= 0,
                radius, topLeft, topRight, bottomRight, bottomLeft
            );
        }

        /**
         * Vérifie si au moins un paramètre de courbure est présent et valide.
         *
         * @return Vrai si l'état contient au moins une valeur exploitable.
         */
        boolean isValid() { return hasUniform || hasTopLeft || hasTopRight || hasBotRight || hasBotLeft; }

        /**
         * Vérifie si les quatre coins possèdent une configuration individuelle.
         *
         * @return Vrai si toutes les valeurs individuelles sont définies.
         */
        boolean allIndividual() { return hasTopLeft && hasTopRight && hasBotRight && hasBotLeft; }

        /**
         * Vérifie si au moins un des quatre coins possède une configuration individuelle.
         *
         * @return Vrai si une valeur de substitution locale existe.
         */
        boolean anyIndividual() { return hasTopLeft || hasTopRight || hasBotRight || hasBotLeft; }
    }

    /**
     * Applique une couleur de remplissage unie à une forme ciblée.
     *
     * @param shapeId   L'identifiant unique de la forme à modifier.
     * @param fillColor La couleur de remplissage au format hexadécimal.
     * @param opacity   Le niveau d'opacité souhaité.
     * @return Le résultat de l'exécution du script via le moteur Penpot.
     */
    @Tool(description = "Apply a solid color fill to a shape.")
    public String applyFillColor(
        @ToolParam(description = "ID of the shape to fill") String shapeId,
        @ToolParam(description = "Fill color in hex format (#RRGGBB)") String fillColor,
        @ToolParam(description = "Opacity absolute ratio from 0.0 to 1.0 (default: 1.0)", required = false) Float opacity
    ) {
        float o = (opacity != null && opacity >= 0f && opacity <= 1f) ? opacity : 1.0f;
        log.info("Tool called: applyFillColor (id={}, color={}, opacity={})", shapeId, fillColor, o);
        return toolExecutor.applyStyle(
            buildFillCode(shapeId, fillColor, o),
            "fillApplied", shapeId,
            String.format(Locale.US, "Applied %s with opacity %.2f", fillColor, o)
        );
    }

    /**
     * Applique un dégradé linéaire sur une forme en fonction d'un angle donné.
     *
     * @param shapeId    L'identifiant unique de la forme à modifier.
     * @param startColor La couleur initiale du dégradé.
     * @param endColor   La couleur finale du dégradé.
     * @param angle      L'angle d'application du dégradé en degrés.
     * @return Le résultat de l'exécution du script de dégradé.
     */
    @Tool(description = "Apply a linear gradient fill to a shape.")
    public String applyGradient(
        @ToolParam(description = "ID of the shape") String shapeId,
        @ToolParam(description = "Start color in hex format (#RRGGBB)") String startColor,
        @ToolParam(description = "End color in hex format (#RRGGBB)") String endColor,
        @ToolParam(description = "Gradient angle in degrees (0 = left to right, 90 = top to bottom, 45 = diagonal)", required = false) Float angle
    ) {
        float a = (angle != null) ? angle : 0f;
        log.info("Tool called: applyGradient (id={}, start={}, end={}, angle={})", shapeId, startColor, endColor, a);
        return toolExecutor.applyStyle(
            buildGradientCode(shapeId, startColor, endColor, a),
            "gradientApplied", shapeId,
            String.format(Locale.US, "Applied gradient %s → %s at %.0f°", startColor, endColor, a)
        );
    }

    /**
     * Applique un contour (stroke) à une forme spécifique.
     *
     * @param shapeId     L'identifiant unique de la forme concernée.
     * @param strokeColor La couleur du contour.
     * @param strokeWidth L'épaisseur du contour exprimée en pixels.
     * @return Le résultat de l'exécution de l'ajout de contour.
     */
    @Tool(description = "Apply a stroke (border/outline) to a shape.")
    public String applyStroke(
        @ToolParam(description = "ID of the shape") String shapeId,
        @ToolParam(description = "Stroke color in hex format (#RRGGBB)") String strokeColor,
        @ToolParam(description = "Stroke width in pixels (default: 1)", required = false) Float strokeWidth
    ) {
        float w = (strokeWidth != null && strokeWidth > 0) ? strokeWidth : 1f;
        log.info("Tool called: applyStroke (id={}, color={}, width={})", shapeId, strokeColor, w);
        return toolExecutor.applyStyle(
            buildStrokeCode(shapeId, strokeColor, w),
            "strokeApplied", shapeId,
            String.format(Locale.US, "Applied %.1fpx %s stroke", w, strokeColor)
        );
    }

    /**
     * Applique un effet d'ombre portée sur une forme.
     *
     * @param shapeId     L'identifiant de la forme cible.
     * @param offsetX     Le décalage horizontal de l'ombre en pixels.
     * @param offsetY     Le décalage vertical de l'ombre en pixels.
     * @param blur        Le rayon de floutage appliqué à l'ombre.
     * @param shadowColor La couleur de l'ombre incluant le canal alpha.
     * @return Le résultat de la modification stylistique.
     */
    @Tool(description = "Apply a drop shadow effect to a shape.")
    public String applyShadow(
        @ToolParam(description = "ID of the shape") String shapeId,
        @ToolParam(description = "Horizontal shadow offset in pixels") float offsetX,
        @ToolParam(description = "Vertical shadow offset in pixels") float offsetY,
        @ToolParam(description = "Shadow blur radius in pixels") float blur,
        @ToolParam(description = "Shadow color in hex format (with alpha: #RRGGBBAA)", required = false) String shadowColor
    ) {
        String color = (shadowColor != null && !shadowColor.isBlank()) ? shadowColor : "#00000066";
        log.info("Tool called: applyShadow (id={}, offset=({},{}), blur={}, color={})", shapeId, offsetX, offsetY, blur, color);
        return toolExecutor.applyStyle(
            buildShadowCode(shapeId, offsetX, offsetY, blur, color),
            "shadowApplied", shapeId,
            String.format(Locale.US, "Applied shadow offset(%.1f, %.1f) blur %.1f", offsetX, offsetY, blur)
        );
    }

    /**
     * Met à jour l'opacité globale d'une forme.
     *
     * @param shapeId L'identifiant de la forme à mettre à jour.
     * @param opacity La valeur de l'opacité.
     * @return Le résultat de la mise à jour ou un message d'erreur si la valeur est invalide.
     */
    @Tool(description = "Update the overall opacity of a shape.")
    public String updateOpacity(
        @ToolParam(description = "ID of the shape") String shapeId,
        @ToolParam(description = "Opacity absolute ratio between 0.0 and 1.0 (not a percentage)") Float opacity
    ) {
        if (opacity == null || opacity < 0f || opacity > 1f) {
            return ToolResponseBuilder.error("Opacity must be between 0.0 and 1.0");
        }
        log.info("Tool called: updateOpacity (id={}, opacity={})", shapeId, opacity);
        return toolExecutor.applyStyle(
            buildOpacityCode(shapeId, opacity),
            "opacityUpdated", shapeId,
            String.format(Locale.US, "Opacity set to %.2f", opacity)
        );
    }

    /**
     * Modifie les rayons de courbure (border radius) d'une forme, de manière uniforme ou individuelle.
     *
     * @param shapeId     L'identifiant de la forme.
     * @param radius      Le rayon global applicable à l'ensemble des coins.
     * @param topLeft     Le rayon spécifique pour le coin supérieur gauche.
     * @param topRight    Le rayon spécifique pour le coin supérieur droit.
     * @param bottomRight Le rayon spécifique pour le coin inférieur droit.
     * @param bottomLeft  Le rayon spécifique pour le coin inférieur gauche.
     * @return Le résultat de l'application des nouveaux rayons de courbure.
     */
    @Tool(description = "Update the corner rounding of a shape.")
    public String updateBorderRadius(
        @ToolParam(description = "ID of the shape") String shapeId,
        @ToolParam(description = "Uniform border radius for all corners in pixels. Used as default when individual corners are not specified.", required = false) Float radius,
        @ToolParam(description = "Border radius for the top-left corner in pixels", required = false) Float topLeft,
        @ToolParam(description = "Border radius for the top-right corner in pixels", required = false) Float topRight,
        @ToolParam(description = "Border radius for the bottom-right corner in pixels", required = false) Float bottomRight,
        @ToolParam(description = "Border radius for the bottom-left corner in pixels", required = false) Float bottomLeft
    ) {
        BorderRadiusState state = BorderRadiusState.of(radius, topLeft, topRight, bottomRight, bottomLeft);

        if (!state.isValid()) {
            return ToolResponseBuilder.error(
                "At least one border radius parameter must be provided and >= 0. "
                + "Use 'radius' for all corners, or topLeft/topRight/bottomRight/bottomLeft for individual corners."
            );
        }

        log.info("Tool called: updateBorderRadius (id={}, radius={}, tl={}, tr={}, br={}, bl={})",
            shapeId, radius, topLeft, topRight, bottomRight, bottomLeft);

        return toolExecutor.applyStyle(
            buildBorderRadiusCode(shapeId, state),
            "borderRadiusUpdated",
            shapeId,
            buildBorderRadiusDetails(state)
        );
    }

    /**
     * Ajoute une interaction (déclencheur et action) à une forme.
     *
     * @param shapeId       L'identifiant de la forme cible.
     * @param trigger       Le déclencheur de l'interaction (ex: "click", "hover", "after-delay").
     * @param actionType    Le type d'action à exécuter (ex: "navigate-to", "open-overlay").
     * @param destinationId L'identifiant de la board de destination (requis pour navigate-to).
     * @param delay         Le délai en millisecondes (uniquement pour le trigger "after-delay").
     * @return Le résultat de l'exécution du script.
     */
    @Tool(description = "Add an interaction to a shape.")
    public String addInteraction(
        @ToolParam(description = "ID of the shape") String shapeId,
        @ToolParam(description = "Trigger type: 'click', 'mouse-enter', 'mouse-leave', 'after-delay'") String trigger,
        @ToolParam(description = "Action type: 'navigate-to', 'open-overlay', 'close-overlay', 'toggle-overlay', 'prev-screen', 'open-url'") String actionType,
        @ToolParam(description = "ID of the destination board (required for navigate-to and overlay actions)", required = false) String destinationId,
        @ToolParam(description = "Delay in milliseconds, only used when trigger is 'after-delay' (default: 0)", required = false) Integer delay
    ) {
        if (trigger == null || trigger.isBlank()) {
            return ToolResponseBuilder.error("Trigger must be provided (e.g. 'click', 'mouse-enter', 'after-delay').");
        }
        if (actionType == null || actionType.isBlank()) {
            return ToolResponseBuilder.error("Action type must be provided (e.g. 'navigate-to', 'open-overlay').");
        }

        boolean needsDestination = actionType.equals("navigate-to")
            || actionType.equals("open-overlay")
            || actionType.equals("toggle-overlay");

        if (needsDestination && (destinationId == null || destinationId.isBlank())) {
            return ToolResponseBuilder.error(
                "Action '" + actionType + "' requires a destinationId (ID of the target board)."
            );
        }

        int delayMs = (delay != null && delay >= 0) ? delay : 0;
        log.info("Tool called: addInteraction (id={}, trigger={}, action={}, dest={}, delay={})",
            shapeId, trigger, actionType, destinationId, delayMs);

        return toolExecutor.applyStyle(
            buildAddInteractionCode(shapeId, trigger, actionType, destinationId, delayMs),
            "interactionAdded", shapeId,
            String.format("Added interaction: trigger=%s action=%s", trigger, actionType)
        );
    }

    /**
     * Supprime une interaction d'une forme par le biais de son index dans le tableau des interactions.
     *
     * @param shapeId          L'identifiant de la forme.
     * @param interactionIndex L'index (en base 0) de l'interaction à supprimer.
     * @return Le résultat de l'exécution du script.
     */
    @Tool(description = "Remove an interaction from a shape by its index.")
    public String removeInteraction(
        @ToolParam(description = "ID of the shape") String shapeId,
        @ToolParam(description = """
            Zero-based index of the interaction to remove
            Use index 0 for the first interaction, 1 for the second, etc.
            To find the index, inspect the shape's interactions list first.
        """) int interactionIndex
    ) {
        if (interactionIndex < 0) {
            return ToolResponseBuilder.error("Interaction index must be >= 0. Use 0 for the first interaction.");
        }

        log.info("Tool called: removeInteraction (id={}, index={})", shapeId, interactionIndex);
        return toolExecutor.applyStyle(
            buildRemoveInteractionCode(shapeId, interactionIndex),
            "interactionRemoved", shapeId,
            String.format("Removed interaction at index %d", interactionIndex)
        );
    }

    /**
     * Remplace l'image d'une forme existante par une nouvelle image chargée depuis une URL.
     *
     * @param shapeId         L'identifiant de la forme dont l'image doit être remplacée.
     * @param newImageUrl     L'URL de la nouvelle image à charger.
     * @param keepAspectRatio Un indicateur stipulant si l'image doit conserver son ratio lors du redimensionnement.
     * @return Le résultat de l'exécution du script.
     */
    @Tool(description = "Replace the image of an existing shape with a new image loaded from a URL.")
    public String replaceImage(
        @ToolParam(description = "ID of the shape whose image must be replaced") String shapeId,
        @ToolParam(description = "URL of the new image to upload and apply") String newImageUrl,
        @ToolParam(description = "If true, keeps the image aspect ratio (default: false)", required = false) Boolean keepAspectRatio
    ) {
        if (newImageUrl == null || newImageUrl.isBlank()) {
            return ToolResponseBuilder.error("newImageUrl must be provided.");
        }

        boolean keepRatio = Boolean.TRUE.equals(keepAspectRatio);
        log.info("Tool called: replaceImage (id={}, url={}, keepRatio={})", shapeId, newImageUrl, keepRatio);

        return toolExecutor.applyStyle(
            buildReplaceImageCode(shapeId, newImageUrl, keepRatio),
            "imageReplaced", shapeId,
            String.format("Image replaced with: %s", newImageUrl)
        );
    }

    /**
     * Construit le code JavaScript nécessaire pour appliquer une couleur de remplissage.
     *
     * @param shapeId   L'identifiant de la forme cible.
     * @param fillColor La couleur hexadécimale à appliquer.
     * @param opacity   Le niveau d'opacité du remplissage.
     * @return La chaîne de caractères contenant le script d'exécution.
     */
    private String buildFillCode(String shapeId, String fillColor, float opacity) {
        String opacityStr = String.format(Locale.US, "%.2f", opacity);
        return PenpotJsSnippets.findShapeOrFallback(shapeId)
            + JsScriptLoader.loadWith("tools/asset/apply-fill.js", Map.of(
                "fillColor", fillColor,
                "opacity", opacityStr));
    }

    /**
     * Construit le code JavaScript permettant d'appliquer un dégradé linéaire.
     *
     * <p>Les coordonnées de départ et d'arrivée sont calculées dynamiquement sous forme 
     * de valeurs normalisées relatives à la boîte englobante de la forme, en s'appuyant 
     * sur la trigonométrie pour respecter l'angle fourni.</p>
     *
     * @param shapeId    L'identifiant de la forme.
     * @param startColor La couleur de départ du dégradé.
     * @param endColor   La couleur de fin du dégradé.
     * @param angle      L'angle d'inclinaison.
     * @return Le script JavaScript formaté pour l'API Penpot.
     */
    private String buildGradientCode(String shapeId, String startColor, String endColor, float angle) {
        double radians = Math.toRadians(angle);
        float cosA = (float) Math.cos(radians);
        float sinA = (float) Math.sin(radians);

        return PenpotJsSnippets.findShapeOrFallback(shapeId)
            + JsScriptLoader.loadWith("tools/asset/apply-gradient.js", Map.of(
                "startX", String.format(Locale.US, "%.4f", 0.5f - 0.5f * cosA),
                "startY", String.format(Locale.US, "%.4f", 0.5f - 0.5f * sinA),
                "endX", String.format(Locale.US, "%.4f", 0.5f + 0.5f * cosA),
                "endY", String.format(Locale.US, "%.4f", 0.5f + 0.5f * sinA),
                "startColor", startColor,
                "endColor", endColor,
                "angle", String.format(Locale.US, "%.0f", angle)));
    }

    /**
     * Génère le script d'application d'un contour autour d'une forme.
     *
     * @param shapeId     L'identifiant de la forme concernée.
     * @param strokeColor La couleur hexadécimale du contour.
     * @param strokeWidth L'épaisseur désirée en pixels.
     * @return Le code JavaScript correspondant.
     */
    private String buildStrokeCode(String shapeId, String strokeColor, float strokeWidth) {
        return PenpotJsSnippets.findShapeOrFallback(shapeId)
            + JsScriptLoader.loadWith("tools/asset/apply-stroke.js", Map.of(
                "strokeColor", strokeColor,
                "strokeWidth", String.format(Locale.US, "%.1f", strokeWidth)));
    }

    /**
     * Prépare le code JavaScript pour l'intégration d'une ombre portée.
     *
     * @param shapeId L'identifiant de la forme.
     * @param offsetX Le décalage sur l'axe des abscisses.
     * @param offsetY Le décalage sur l'axe des ordonnées.
     * @param blur    La valeur d'estompage.
     * @param color   La couleur de l'ombre au format RGBA hexadécimal.
     * @return Le script à transmettre à l'exécuteur.
     */
    private String buildShadowCode(String shapeId, float offsetX, float offsetY, float blur, String color) {
        return PenpotJsSnippets.findShapeOrFallback(shapeId)
            + JsScriptLoader.loadWith("tools/asset/apply-shadow.js", Map.of(
                "offsetX", String.format(Locale.US, "%.1f", offsetX),
                "offsetY", String.format(Locale.US, "%.1f", offsetY),
                "blur", String.format(Locale.US, "%.1f", blur),
                "color", color));
    }

    /**
     * Formalise l'instruction JavaScript de modification d'opacité.
     *
     * @param shapeId L'identifiant de l'élément cible.
     * @param opacity La valeur de transparence à appliquer.
     * @return Le bloc de code exécutable.
     */
    private String buildOpacityCode(String shapeId, float opacity) {
        return PenpotJsSnippets.findShapeOrFallback(shapeId)
            + JsScriptLoader.loadWith("tools/asset/update-opacity.js",
                Map.of("opacity", String.format(Locale.US, "%.2f", opacity)));
    }

    /**
     * Construit le code JavaScript chargé de redéfinir les rayons de courbure.
     *
     * <p>L'algorithme arbitre entre l'application d'un rayon global et la surcharge 
     * par des rayons spécifiques à certains coins.</p>
     *
     * @param shapeId     L'identifiant de la forme.
     * @param radius      Le rayon de base applicable uniformément.
     * @param topLeft     La valeur de substitution pour le coin supérieur gauche.
     * @param topRight    La valeur de substitution pour le coin supérieur droit.
     * @param bottomRight La valeur de substitution pour le coin inférieur droit.
     * @param bottomLeft  La valeur de substitution pour le coin inférieur gauche.
     * @return Le script de configuration des bordures.
     */
    private String buildBorderRadiusCode(String shapeId, BorderRadiusState s) {
        StringBuilder code = new StringBuilder(PenpotJsSnippets.findShapeOrFallback(shapeId));
        code.append("if (!('borderRadius' in shape)) throw new Error('This shape does not support border radius');\n");

        if (s.allIndividual() && !s.hasUniform()) {
            code.append("shape.borderRadius = 0;\n");
            code.append(String.format(Locale.US, "shape.borderRadiusTopLeft = %.2f;\n", s.topLeft()));
            code.append(String.format(Locale.US, "shape.borderRadiusTopRight = %.2f;\n", s.topRight()));
            code.append(String.format(Locale.US, "shape.borderRadiusBottomRight = %.2f;\n", s.bottomRight()));
            code.append(String.format(Locale.US, "shape.borderRadiusBottomLeft = %.2f;\n", s.bottomLeft()));
        } else if (!s.anyIndividual()) {
            code.append(String.format(Locale.US, "shape.borderRadius = %.2f;\n", s.radius()));
        } else {
            float base = s.hasUniform() ? s.radius() : 0f;
            code.append(String.format(Locale.US, "shape.borderRadius = %.2f;\n", base));
            if (s.hasTopLeft()) code.append(String.format(Locale.US, "shape.borderRadiusTopLeft = %.2f;\n", s.topLeft()));
            if (s.hasTopRight()) code.append(String.format(Locale.US, "shape.borderRadiusTopRight = %.2f;\n", s.topRight()));
            if (s.hasBotRight()) code.append(String.format(Locale.US, "shape.borderRadiusBottomRight = %.2f;\n", s.bottomRight()));
            if (s.hasBotLeft()) code.append(String.format(Locale.US, "shape.borderRadiusBottomLeft = %.2f;\n", s.bottomLeft()));
        }

        code.append("""
            return {
                id: shape.id,
                borderRadius: shape.borderRadius,
                borderRadiusTopLeft: shape.borderRadiusTopLeft,
                borderRadiusTopRight: shape.borderRadiusTopRight,
                borderRadiusBottomRight: shape.borderRadiusBottomRight,
                borderRadiusBottomLeft: shape.borderRadiusBottomLeft
            };
            """);
        return code.toString();
    }

    /**
     * Génère une chaîne de caractères descriptive récapitulant la configuration des rayons de courbure.
     *
     * @param s L'objet d'état validé contenant l'ensemble des valeurs de bordure.
     * @return Le message de journalisation formaté.
     */
    private String buildBorderRadiusDetails(BorderRadiusState s) {
        if (!s.anyIndividual()) return String.format(Locale.US, "Border radius set uniformly to %.1fpx", s.radius());

        StringBuilder sb = new StringBuilder("Border radius set: ");
        if (s.radius() != null) sb.append(String.format(Locale.US, "base=%.1f ", s.radius()));
        if (s.topLeft() != null) sb.append(String.format(Locale.US, "TL=%.1f ", s.topLeft()));
        if (s.topRight() != null) sb.append(String.format(Locale.US, "TR=%.1f ", s.topRight()));
        if (s.bottomRight() != null) sb.append(String.format(Locale.US, "BR=%.1f ", s.bottomRight()));
        if (s.bottomLeft() != null) sb.append(String.format(Locale.US, "BL=%.1f ", s.bottomLeft()));
        sb.append("(px)");
        return sb.toString();
    }

    /**
     * Construit le script JavaScript permettant l'intégration d'une nouvelle interaction via l'API Penpot.
     *
     * <p>Le paramétrage de l'action s'adapte dynamiquement à la nature de la requête.
     * Dans le cadre d'actions de navigation, le système procède à une recherche contextuelle 
     * pour associer l'identifiant fourni à la zone de dessin correspondante au sein de la page.</p>
     *
     * @param shapeId       L'identifiant de la forme réceptrice.
     * @param trigger       Le type d'événement déclencheur.
     * @param actionType    La classification de l'action ciblée.
     * @param destinationId L'identifiant de la destination (requis en cas de navigation).
     * @param delay         Le délai d'exécution de l'action (en millisecondes).
     * @return La syntaxe JavaScript prête à l'exécution.
     */
    private String buildAddInteractionCode(
        String shapeId, String trigger, String actionType, String destinationId, int delay
    ) {
        String actionJs = buildActionJs(actionType, destinationId);
        String delayArg = "after-delay".equals(trigger) ? String.format(", %d", delay) : "";

        return PenpotJsSnippets.findShapeOrFallback(shapeId)
            + JsScriptLoader.loadWith("tools/asset/add-interaction.js", Map.of(
                "actionJs", actionJs,
                "trigger", trigger,
                "delayArg", delayArg,
                "actionType", actionType));
    }

    /**
     * Structure et formate l'objet JavaScript modélisant l'action d'une interaction.
     *
     * <p>Cette méthode gère spécifiquement les contraintes liées aux événements de 
     * navigation en injectant la logique de récupération de la zone de dessin (board) 
     * directement dans le contexte de la page courante.</p>
     *
     * @param actionType    Le type d'action à instancier.
     * @param destinationId La référence de destination le cas échéant.
     * @return L'objet JavaScript représentant l'action sous format texte.
     */
    private String buildActionJs(String actionType, String destinationId) {

    String safeAction = JsStringUtils.jsSafe(actionType);
    String safeDest = destinationId != null ? JsStringUtils.jsSafe(destinationId) : "";

    return switch (actionType) {

        case "navigate-to", "open-overlay", "toggle-overlay" ->
            String.format("""
                (() => {
                    const dest = penpot.currentPage.getShapeById('%s');
                    if (!dest) throw new Error('Destination board not found: %s');
                    return { type: '%s', destination: dest };
                })()
                """,
                safeDest,
                safeDest,
                safeAction
            );

        case "close-overlay" -> "{ type: 'close-overlay' }";

        case "prev-screen" -> "{ type: 'prev-screen' }";

        case "open-url" ->
            String.format(
                "{ type: 'open-url', url: '%s' }",
                safeDest
            );

        default ->
            String.format("{ type: '%s' }", safeAction);
    };
}

    /**
     * Formalise le code JavaScript destiné à la suppression d'une interaction ciblée.
     *
     * <p>Compte tenu du fait que l'API Penpot manipule directement des objets d'interaction 
     * et non des indices de position, ce générateur inclut l'extraction préalable de l'objet 
     * depuis le tableau des interactions attaché à la forme.</p>
     *
     * @param shapeId          L'identifiant de la forme hébergeant l'interaction.
     * @param interactionIndex La position de l'interaction dans le tableau (base 0).
     * @return Le script de suppression formaté.
     */
    private String buildRemoveInteractionCode(String shapeId, int interactionIndex) {
        String idx = String.valueOf(interactionIndex);
        return PenpotJsSnippets.findShapeOrFallback(shapeId)
            + JsScriptLoader.loadWith("tools/asset/remove-interaction.js", Map.of("index", idx));
    }

    /**
     * Construit le code JavaScript destiné au remplacement de l'image associée à une forme.
     *
     * <p>Cette méthode exploite la fonction native du moteur Penpot pour télécharger une nouvelle ressource 
     * depuis une URL distante. Ensuite, elle procède à l'écrasement de la propriété de remplissage visuel 
     * par cette nouvelle image. Par ailleurs, les dimensions originelles de l'élément graphique sont 
     * intentionnellement préservées afin de ne pas altérer la mise en page existante.</p>
     *
     * @param shapeId         L'identifiant de la forme à modifier.
     * @param newImageUrl     L'adresse URL pointant vers la nouvelle ressource graphique.
     * @param keepAspectRatio Un indicateur stipulant si les proportions d'origine doivent être conservées lors du redimensionnement.
     * @return Le script JavaScript formaté pour l'opération de substitution d'image.
     */
    private String buildReplaceImageCode(String shapeId, String newImageUrl, boolean keepAspectRatio) {
        return PenpotJsSnippets.findShapeOrFallback(shapeId)
            + JsScriptLoader.loadWith("tools/asset/replace-image.js", Map.of(
                "imageUrl", JsStringUtils.jsSafe(newImageUrl),
                "keepAspectRatio", String.valueOf(keepAspectRatio)));
    }
}