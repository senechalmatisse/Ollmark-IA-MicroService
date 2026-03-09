package com.penpot.ai.core.domain;

import java.util.Arrays;

/**
 * Énumération des types de tâches supportés par le moteur d'exécution.
 *
 * @see #fromString(String)
 * @see #getTaskName()
 */
public enum TaskType {

    /**
     * Exécution de code JavaScript dans le contexte du plugin Penpot.
     *
     * <p>Correspond à l'identifiant textuel {@code "executeCode"}.
     * Utilisé pour transmettre et exécuter des scripts générés par le moteur IA.</p>
     */
    EXECUTE_CODE("executeCode"),

    /**
     * Récupération de la structure d'un élément ou d'une page Penpot.
     *
     * <p>Correspond à l'identifiant textuel {@code "fetchStructure"}.
     * Permet d'inspecter l'arbre de composants avant de le modifier.</p>
     */
    FETCH_STRUCTURE("fetchStructure"),

    /**
     * Modification d'une forme existante sur le canevas Penpot.
     *
     * <p>Correspond à l'identifiant textuel {@code "modifyShape"}.
     * Couvre les opérations de repositionnement, redimensionnement et
     * changement de style appliquées à un composant déjà présent.</p>
     */
    MODIFY_SHAPE("modifyShape"),

    /**
     * Création d'un nouvel élément graphique sur le canevas Penpot.
     *
     * <p>Correspond à l'identifiant textuel {@code "createElement"}.
     * Déclenche la génération et l'insertion d'un composant depuis le moteur IA.</p>
     */
    CREATE_ELEMENT("createElement");

    /**
     * Identifiant textuel de la tâche, utilisé pour la sérialisation et
     * le routage. Immuable et non {@code null}.
     */
    private final String taskName;

    /**
     * Initialise la constante avec son identifiant textuel.
     *
     * @param taskName identifiant textuel de la tâche ; ne doit pas être {@code null}
     */
    TaskType(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Retourne l'identifiant textuel de cette tâche.
     *
     * @return l'identifiant textuel, par exemple {@code "executeCode"} ;
     *         jamais {@code null}
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Résout la constante {@code TaskType} correspondant à l'identifiant
     * textuel fourni.
     *
     * <p>La comparaison est sensible à la casse et s'effectue sur la valeur
     * de {@link #getTaskName()}, et non sur {@link #name()}. Par exemple,
     * {@code "executeCode"} est valide mais {@code "EXECUTE_CODE"} ne l'est pas.</p>
     *
     * @param taskName identifiant textuel à résoudre ; peut être {@code null},
     *                 auquel cas une {@link IllegalArgumentException} est levée
     * @return la constante {@code TaskType} dont {@link #getTaskName()} est
     *         égal à {@code taskName}
     * @throws IllegalArgumentException si aucune constante ne correspond à
     *         {@code taskName}, avec un message précisant la valeur rejetée
     */
    public static TaskType fromString(String taskName) {
        return Arrays.stream(values())
            .filter(t -> t.taskName.equals(taskName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown task type: " + taskName));
    }
}