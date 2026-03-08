package com.penpot.ai.application.tools.support;

import com.penpot.ai.shared.util.JsonUtils;
import lombok.experimental.UtilityClass;

import java.util.*;

/**
 * Fournit une interface centralisée pour la construction des réponses au format JSON 
 * destinées à l'ensemble des tools.
 */
@UtilityClass
public class ToolResponseBuilder {

    /**
     * Génère une réponse d'erreur standardisée.
     *
     * @param message La description explicite de l'erreur rencontrée lors de l'exécution.
     * @return Une chaîne de caractères représentant l'objet JSON contenant le statut d'échec et le message d'erreur.
     */
    public static String error(String message) {
        return String.format("{\"success\": false, \"error\": %s}", 
            JsonUtils.escapeJson(message));
    }

    /**
     * Construit une réponse de succès générique intégrant un message textuel libre.
     *
     * @param message Le texte informatif confirmant la bonne exécution de la tâche.
     * @return Une chaîne JSON validant le succès de l'opération, accompagnée du message échappé.
     */
    public static String success(String message) {
        return String.format("{\"success\": true, \"message\": %s}", 
            JsonUtils.escapeJson(message));
    }

    /**
     * Formate la réponse consécutive à la création d'une nouvelle forme géométrique.
     *
     * @param shapeType La catégorie ou le type de la forme générée (ex. rectangle, ellipse).
     * @param shapeId L'identifiant unique assigné à la forme nouvellement créée.
     * @return Un objet JSON structuré contenant le type, l'identifiant, ainsi que des instructions explicites de mémorisation.
     */
    public static String shapeCreated(String shapeType, String shapeId) {
        String instructions = shapeType + " created successfully! SHAPE_ID: " + shapeId + ". SAVE THIS ID!";
        return String.format(
            "{\"success\": true, \"type\": %s, \"id\": %s, \"instructions\": %s}",
            JsonUtils.escapeJson(shapeType),
            JsonUtils.escapeJson(shapeId),
            JsonUtils.escapeJson(instructions)
        );
    }

    /**
     * Élabore une réponse validant la génération d'un contenu spécifique, 
     * tel qu'un élément textuel ou une ressource image.
     *
     * @param contentType La nature du contenu produit au sein de l'application.
     * @param shapeId L'identifiant unique rattaché à ce nouveau contenu.
     * @return Une structure JSON confirmant le succès de l'action et liant le type de contenu à son identifiant.
     */
    public static String contentCreated(String contentType, String shapeId) {
        return String.format(
            "{\"success\": true, \"type\": %s, \"id\": %s}",
            JsonUtils.escapeJson(contentType),
            JsonUtils.escapeJson(shapeId)
        );
    }

    /**
     * Formalise le retour d'information suite à l'application d'une transformation 
     * ou d'un style sur une forme existante.
     *
     * @param operation La désignation de l'action de transformation ou de stylisation effectuée.
     * @param shapeId L'identifiant de la forme cible ayant subi la modification.
     * @param details Les paramètres ou valeurs spécifiques appliqués lors de cette opération technique.
     * @return Un document JSON synthétisant la nature de l'opération, l'identifiant concerné et les détails associés.
     */
    public static String shapeOperation(String operation, String shapeId, String details) {
        return String.format(Locale.US,
            "{\"success\": true, \"operation\": %s, \"shapeId\": %s, \"details\": %s}",
            JsonUtils.escapeJson(operation),
            JsonUtils.escapeJson(shapeId),
            JsonUtils.escapeJson(details)
        );
    }

    /**
     * Restitue le résultat d'une opération de masse affectant simultanément plusieurs éléments.
     *
     * @param operation La nature de l'action collective réalisée sur le groupe d'éléments.
     * @param ids La collection des identifiants des formes impliquées dans la transformation.
     * @return Une chaîne JSON détaillant le type d'opération, le nombre d'éléments traités, la liste des identifiants et des conseils de manipulation.
     */
    public static String multiShapeOperation(String operation, List<String> ids) {
        String idList = String.join(",", ids);
        String instructions = String.format(
            "Successfully %s %d shapes. TIP: These shapes have been %s. You can continue working with them.",
            operation, ids.size(), operation
        );

        return String.format(
            "{\"success\": true, \"operation\": %s, \"count\": %d, \"ids\": %s, \"instructions\": %s}",
            JsonUtils.escapeJson(operation),
            ids.size(),
            JsonUtils.escapeJson(idList),
            JsonUtils.escapeJson(instructions)
        );
    }

    /**
     * Valide la constitution d'un nouveau groupe d'éléments au sein de l'interface. 
     *
     * @param groupId L'identifiant unique attribué au groupe fraîchement constitué.
     * @return Un format JSON confirmant la création, renvoyant l'identifiant ainsi que des instructions d'usage.
     */
    public static String groupCreated(String groupId) {
        String instructions = "Successfully created group. TIP: You can now work with the entire group as a single shape.";
        return String.format(
            "{\"success\": true, \"id\": %s, \"instructions\": %s}",
            JsonUtils.escapeJson(groupId),
            JsonUtils.escapeJson(instructions)
        );
    }

    /**
     * Confirme le clonage réussi d'une forme existante. 
     *
     * @param cloneId L'identifiant unique nouvellement généré pour l'élément dupliqué.
     * @return Une réponse JSON attestant du succès du clonage, incluant le nouvel identifiant et des recommandations de manipulation.
     */
    public static String shapeCloned(String cloneId) {
        String instructions = "Successfully cloned shape. TIP: Save this ID to manipulate the clone.";
        return String.format(
            "{\"success\": true, \"id\": %s, \"instructions\": %s}",
            JsonUtils.escapeJson(cloneId),
            JsonUtils.escapeJson(instructions)
        );
    }
}