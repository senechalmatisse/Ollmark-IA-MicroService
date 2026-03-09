package com.penpot.ai.core.domain;

import lombok.*;

import java.util.*;

/**
 * Objet-valeur (<em>Value Object</em>) représentant le résultat d'une
 * exécution de tâche.
 *
 * <h2>Utilisation recommandée</h2>
 * <p>Préférer systématiquement les fabriques statiques aux constructeurs
 * Lombok générés afin de garantir les invariants (champs {@code Optional}
 * non {@code null}, listes défensives) :</p>
 * <pre>{@code
 * // Succès simple
 * TaskResult result = TaskResult.success(myData);
 *
 * // Échec avec journaux
 * TaskResult result = TaskResult.failure("Erreur réseau", logs);
 * }</pre>
 *
 * <h2>Discrimination succès / échec</h2>
 * <p>{@link #isSuccess()} est le discriminant principal. Par convention :</p>
 * <ul>
 *   <li>si {@code success == true}, {@link #getData()} est présent et
 *       {@link #getError()} est vide ;</li>
 *   <li>si {@code success == false}, {@link #getError()} est présent et
 *       {@link #getData()} est vide.</li>
 * </ul>
 *
 * @see #success(Object)
 * @see #success(Object, List)
 * @see #failure(String)
 * @see #failure(String, List)
 */
@Value
@Builder
public class TaskResult {

    /**
     * Indique si la tâche s'est terminée avec succès.
     *
     * <p>{@code true} signifie que {@link #getData()} contient le résultat
     * attendu ; {@code false} signifie que {@link #getError()} contient
     * la description de l'échec.</p>
     */
    boolean success;

    /**
     * Résultat produit par la tâche en cas de succès.
     *
     * <p>Vide ({@link Optional#empty()}) lorsque {@link #isSuccess()} est
     * {@code false}. Le type est {@link Object} pour rester générique vis-à-vis
     * des différents moteurs d'exécution ; l'appelant est responsable du cast.</p>
     */
    @Builder.Default
    Optional<Object> data = Optional.empty();

    /**
     * Message d'erreur en cas d'échec de la tâche.
     *
     * <p>Vide ({@link Optional#empty()}) lorsque {@link #isSuccess()} est
     * {@code true}. Contient un libellé lisible destiné à la journalisation
     * ou à l'affichage utilisateur.</p>
     */
    @Builder.Default
    Optional<String> error = Optional.empty();

    /**
     * Journaux d'exécution associés à la tâche, présents aussi bien en cas
     * de succès que d'échec.
     *
     * <p>La liste est non modifiable ; toute tentative de mutation lève une
     * {@link UnsupportedOperationException}. Vaut une liste vide par défaut.</p>
     */
    @Builder.Default
    List<String> logs = Collections.emptyList();

    /**
     * Crée un résultat de succès sans journaux.
     *
     * <p>{@link #getError()} sera vide et {@link #getLogs()} sera une liste
     * vide dans le résultat retourné.</p>
     *
     * @param data résultat produit par la tâche ; peut être {@code null},
     *             auquel cas {@link #getData()} retournera {@link Optional#empty()}
     * @return un {@code TaskResult} avec {@link #isSuccess()} à {@code true}
     */
    public static TaskResult success(Object data) {
        return TaskResult.builder()
            .success(true)
            .data(Optional.ofNullable(data))
            .build();
    }

    /**
     * Crée un résultat de succès accompagné de journaux d'exécution.
     *
     * <p>La liste {@code logs} est copiée dans une vue non modifiable afin
     * de préserver l'immuabilité de l'objet. Si {@code logs} est {@code null},
     * une liste vide est utilisée.</p>
     *
     * @param data résultat produit par la tâche ; peut être {@code null},
     *             auquel cas {@link #getData()} retournera {@link Optional#empty()}
     * @param logs journaux d'exécution à associer au résultat ;
     *             {@code null} est toléré et traité comme une liste vide
     * @return un {@code TaskResult} avec {@link #isSuccess()} à {@code true}
     *         et {@link #getLogs()} non modifiable
     */
    public static TaskResult success(Object data, List<String> logs) {
        return TaskResult.builder()
            .success(true)
            .data(Optional.ofNullable(data))
            .logs(logs != null ? Collections.unmodifiableList(logs) : Collections.emptyList())
            .build();
    }

    /**
     * Crée un résultat d'échec sans journaux.
     *
     * <p>{@link #getData()} sera vide et {@link #getLogs()} sera une liste
     * vide dans le résultat retourné.</p>
     *
     * @param error message décrivant la cause de l'échec ; ne doit pas être
     *              {@code null} (un {@link Optional#of(Object)} lui est appliqué)
     * @return un {@code TaskResult} avec {@link #isSuccess()} à {@code false}
     * @throws NullPointerException si {@code error} est {@code null}
     */
    public static TaskResult failure(String error) {
        return TaskResult.builder()
            .success(false)
            .error(Optional.of(error))
            .build();
    }

    /**
     * Crée un résultat d'échec accompagné de journaux d'exécution.
     *
     * <p>La liste {@code logs} est copiée dans une vue non modifiable afin
     * de préserver l'immuabilité de l'objet. Si {@code logs} est {@code null},
     * une liste vide est utilisée.</p>
     *
     * @param error message décrivant la cause de l'échec ; ne doit pas être
     *              {@code null} (un {@link Optional#of(Object)} lui est appliqué)
     * @param logs  journaux d'exécution à associer au résultat ;
     *              {@code null} est toléré et traité comme une liste vide
     * @return un {@code TaskResult} avec {@link #isSuccess()} à {@code false}
     *         et {@link #getLogs()} non modifiable
     * @throws NullPointerException si {@code error} est {@code null}
     */
    public static TaskResult failure(String error, List<String> logs) {
        return TaskResult.builder()
            .success(false)
            .error(Optional.of(error))
            .logs(logs != null ? Collections.unmodifiableList(logs) : Collections.emptyList())
            .build();
    }
}