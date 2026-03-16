package com.penpot.ai.core.domain;

import lombok.*;

import java.util.*;

/**
 * Objet-valeur (<em>Value Object</em>) représentant une tâche à exécuter
 * dans le plugin Penpot.
 *
 * @see TaskType
 * @see TaskResult
 */
@Value
@Builder
public class Task {

    /** Identifiant unique de la tâche. */
    String id;

    /**
     * Type de la tâche, déterminant le comportement du moteur d'exécution.
     *
     * <p>Le type conditionne le traitement appliqué aux {@link #getParameters()}
     * et le format attendu du résultat retourné dans {@link TaskResult#getData()}.</p>
     *
     * @see TaskType
     */
    TaskType type;

    /**
     * Paramètres d'entrée de la tâche, indexés par nom.
     *
     * <p>La structure attendue dépend du {@link #getType()} : chaque
     * {@link TaskType} définit son propre schéma de paramètres. La map
     * est supposée non modifiable ; aucune copie défensive n'est effectuée
     * par cette classe.</p>
     *
     * <p>Exemples de clés selon le type :</p>
     * <ul>
     *   <li>{@code EXECUTE_CODE} → {@code "code"} (String)</li>
     *   <li>{@code MODIFY_SHAPE} → {@code "shapeId"} (String), {@code "properties"} (Map)</li>
     * </ul>
     */
    Map<String, Object> parameters;

    /**
     * Identifiant de session WebSocket pour le routage précis vers le bon plugin.
     * Remplace userToken qui n'est plus utilisé.
     */
    @Builder.Default
    Optional<String> sessionId = Optional.empty();
}