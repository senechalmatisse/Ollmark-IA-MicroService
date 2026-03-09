package com.penpot.ai.core.domain;

import lombok.*;
import java.util.Optional;

/**
 * Représente une commande d'exécution de code JavaScript dans le plugin Penpot.
 *
 * <p>
 * Cette commande contient :
 * </p>
 * <ul>
 *     <li>le code JavaScript à exécuter dans l'environnement Penpot</li>
 *     <li>un token utilisateur optionnel permettant de gérer les scénarios
 *     multi-utilisateurs</li>
 * </ul>
 */
@Value
@Builder
public class ExecuteCodeCommand {

    /** Code JavaScript à exécuter dans le plugin Penpot. */
    String code;

    /**
     * Token utilisateur optionnel utilisé dans les environnements
     * multi-utilisateurs.
     *
     * <p>
     * Lorsque présent, ce token permet d'associer l'exécution du code
     * à un utilisateur spécifique ou à une session donnée.
     * </p>
     *
     * <p>
     * Par défaut, la valeur est {@link Optional#empty()}.
     * </p>
     */
    @Builder.Default
    Optional<String> userToken = Optional.empty();

    /**
     * Méthode de fabrique permettant de créer une commande simple
     * d'exécution de code JavaScript.
     *
     * <p>
     * Cette méthode est utilisée lorsque le contexte utilisateur
     * n'est pas nécessaire.
     * </p>
     *
     * @param code le code JavaScript à exécuter
     * @return une instance de {@link ExecuteCodeCommand}
     */
    public static ExecuteCodeCommand of(String code) {
        return ExecuteCodeCommand.builder()
            .code(code)
            .build();
    }

    /**
     * Méthode de fabrique permettant de créer une commande d'exécution
     * avec un token utilisateur.
     *
     * <p>
     * Cette variante est utile dans les scénarios multi-utilisateurs
     * où l'exécution doit être associée à un utilisateur ou une session
     * spécifique.
     * </p>
     *
     * @param code le code JavaScript à exécuter
     * @param userToken token représentant l'utilisateur ou la session
     * @return une instance de {@link ExecuteCodeCommand}
     */
    public static ExecuteCodeCommand of(String code, String userToken) {
        return ExecuteCodeCommand.builder()
            .code(code)
            .userToken(Optional.ofNullable(userToken))
            .build();
    }

    /**
     * Vérifie la validité de la commande avant son exécution.
     *
     * <p>
     * Cette méthode s'assure que le code JavaScript à exécuter est
     * présent et non vide.
     * </p>
     *
     * @throws IllegalArgumentException si le code est {@code null}
     *                                  ou vide
     */
    public void validate() {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be null or empty");
        }
    }
}