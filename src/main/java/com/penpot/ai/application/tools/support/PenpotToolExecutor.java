package com.penpot.ai.application.tools.support;

import com.penpot.ai.infrastructure.session.SessionContextHolder;
import com.penpot.ai.core.domain.*;
import com.penpot.ai.core.ports.in.ExecuteCodeUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

/**
 * Service centralisé pilotant l'exécution technique de l'ensemble des outils Penpot.
 * Ce composant agit comme un chef d'orchestre : il réceptionne les scripts générés,
 * délègue leur exécution physique au noyau applicatif via le port d'entrée approprié,
 * puis uniformise les retours d'information.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PenpotToolExecutor {

    private final ExecuteCodeUseCase executeCodeUseCase;

    /** Constante définissant la valeur par défaut lorsqu'un identifiant ne peut être extrait. */
    private static final String UNKNOWN_ID = "unknown";

    /**
     * Pilote l'exécution d'un script JavaScript Penpot et applique une logique de conversion personnalisée sur le résultat brut.
     *
     * @param code          Le code JavaScript ciblant l'API Penpot à interpréter.
     * @param operationName L'intitulé descriptif de l'opération, utilisé principalement pour assurer la traçabilité dans les journaux d'application (logs).
     * @param resultMapper  La fonction de transformation chargée de convertir l'objet {@link TaskResult} de domaine en une chaîne JSON finalisée.
     * @return              La réponse JSON formatée, indiquant soit la réussite et les données associées, soit l'échec et son motif.
     */
    public String execute(
        String code,
        String operationName,
        Function<TaskResult, String> resultMapper
    ) {
        try {
            String sessionId = SessionContextHolder.getSessionId().orElse(null);
            ExecuteCodeCommand command = ExecuteCodeCommand.of(code, sessionId);

            log.info("[TOOL EXEC] operation='{}' | thread={} | sessionId={}",
                operationName, Thread.currentThread().getId(),
                sessionId != null ? sessionId : "⚠ NULL — no session context !");

            TaskResult result = executeCodeUseCase.execute(command);
            if (!result.isSuccess()) {
                return ToolResponseBuilder.error(result.getError().orElse("Unknown error"));
            }

            return resultMapper.apply(result);
        } catch (Exception e) {
            log.error("Failed to execute Penpot operation: {}", operationName, e);
            return ToolResponseBuilder.error(e.getMessage());
        }
    }

    /**
     * Orchestre la génération d'une nouvelle forme géométrique.
     *
     * @param code      Le script d'instanciation de la forme renvoyant son identifiant technique.
     * @param shapeType La catégorisation sémantique de l'élément (par exemple : rectangle, ellipse, board).
     * @return          Un objet JSON confirmant la création et exposant l'identifiant de la nouvelle forme.
     */
    public String createShape(String code, String shapeType) {
        log.info("Creating shape: {}", shapeType);
        return execute(code, "create " + shapeType, result -> {
            String shapeId = extractRawId(result);
            log.debug("Created {} → ID: {}", shapeType, shapeId);
            return ToolResponseBuilder.shapeCreated(shapeType, shapeId);
        });
    }

    /**
     * Supervise l'application d'une modification spatiale ou géométrique sur un élément existant, 
     * telle qu'une rotation, une mise à l'échelle ou un redimensionnement. 
     *
     * @param code            Le code d'altération spatiale renvoyant un objet contenant l'identifiant traité.
     * @param operation       La désignation technique de la transformation effectuée (ex. rotated, scaled).
     * @param expectedShapeId L'identifiant de la forme cible, servant de filet de sécurité en cas de retour silencieux.
     * @return                Une réponse JSON certifiant l'aboutissement de la manipulation sur la forme concernée.
     */
    public String transformShape(String code, String operation, String expectedShapeId) {
        log.info("Transforming shape: {} ({})", expectedShapeId, operation);
        return execute(code, operation, result -> {
            String actualId = extractSingleId(result, "id", expectedShapeId);
            log.info("Successfully {} shape: {}", operation, actualId);
            return ToolResponseBuilder.shapeOperation(operation, actualId,
                String.format("Shape %s successfully", operation));
        });
    }

    /**
     * Concrétise l'application d'un style visuel ou d'une propriété esthétique sur une entité graphique.
     *
     * @param code      Le script appliquant les nouvelles propriétés de rendu visuel.
     * @param operation La nature de l'altération esthétique (ex. fillApplied, strokeApplied).
     * @param shapeId   L'identifiant unique de l'élément subissant la stylisation.
     * @param details   La description textuelle des paramètres spécifiques déployés lors de l'opération.
     * @return          Le format JSON attestant de la bonne prise en compte du nouveau style.
     */
    public String applyStyle(String code, String operation, String shapeId, String details) {
        log.info("Applying style {} to shape: {}", operation, shapeId);
        return execute(code, operation, result -> {
            log.info("Successfully applied {} to: {}", operation, shapeId);
            return ToolResponseBuilder.shapeOperation(operation, shapeId, details);
        });
    }

    /**
     * Finalise la constitution d'un groupe en liant hiérarchiquement plusieurs éléments au sein du canevas.
     *
     * @param code Le script ordonnant le regroupement logique des cibles.
     * @return     Le document JSON comportant l'identifiant exclusif du groupe créé.
     */
    public String createGroup(String code) {
        return execute(code, "group shapes", result -> {
            String groupId = extractSingleId(result, "groupId", UNKNOWN_ID);
            log.info("Created group: {}", groupId);
            return ToolResponseBuilder.groupCreated(groupId);
        });
    }

    /**
     * Pilote le processus de duplication d'une forme existante afin de générer une copie conforme.
     *
     * @param code Le script déclenchant l'action de clonage sur l'API cible.
     * @return     Une structure JSON officialisant la création de la copie et révélant son identifiant.
     */
    public String cloneShape(String code) {
        return execute(code, "clone shape", result -> {
            String cloneId = extractSingleId(result, "cloneId", UNKNOWN_ID);
            return ToolResponseBuilder.shapeCloned(cloneId);
        });
    }

    /**
     * Commande la suppression définitive d'un ou plusieurs éléments du document.
     *
     * @param code           Le script chargé de l'effacement de l'élément cible.
     * @param operationLabel Le nom descriptif de l'action de suppression pour le suivi technique.
     * @return               Une confirmation JSON basique certifiant l'exécution de l'opération.
     */
    public String executeDelete(String code, String operationLabel) {
        return execute(code, operationLabel, result ->
            ToolResponseBuilder.success(
                result.getData().map(Object::toString).orElse("Operation completed")
            )
        );
    }

    /**
     * Gère l'instanciation des contenus informatifs spécialisés, tels que les blocs textuels ou les conteneurs d'images.
     *
     * @param code        Le script d'intégration du contenu textuel ou média.
     * @param contentType La nature sémantique du contenu injecté (ex. paragraph, image).
     * @return            La validation JSON rattachant le type de contenu à son nouvel identifiant.
     */
    public String createContent(String code, String contentType) {
        return execute(code, "create " + contentType, result -> {
            String shapeId = result.getData().map(Object::toString).orElse(UNKNOWN_ID);
            return ToolResponseBuilder.contentCreated(contentType, shapeId);
        });
    }

    /**
     * Tente d'extraire la valeur brute de l'identifiant contenu dans la réponse du moteur d'exécution.
     *
     * @param result L'objet encapsulant le résultat de la tâche exécutée.
     * @return       La chaîne de caractères représentant l'identifiant, ou une valeur par défaut si l'extraction échoue.
     */
    private String extractRawId(TaskResult result) {
        return result.getData()
            .map(data -> {
                if (data instanceof Map<?, ?> map) {
                    Object inner = map.get("result");
                    if (inner != null) return inner.toString();
                }
                return data.toString();
            })
            .orElse(UNKNOWN_ID);
    }

    /**
     * Inspecte les données contenues dans le résultat d'exécution pour isoler un identifiant unique associé à une clé spécifique.
     *
     * @param result   L'objet contenant les données de retour du script.
     * @param key      La clé d'accès ciblée au sein du dictionnaire de données.
     * @param fallback La valeur de substitution à retourner si la clé est absente ou si le format est inattendu.
     * @return         La valeur de l'identifiant sous forme de chaîne de caractères.
     */
    private String extractSingleId(TaskResult result, String key, String fallback) {
        return result.getData()
            .filter(d -> d instanceof Map<?, ?>)
            .map(d -> {
                Object id = ((Map<?, ?>) d).get(key);
                return id != null ? id.toString() : fallback;
            })
            .orElse(fallback);
    }
}