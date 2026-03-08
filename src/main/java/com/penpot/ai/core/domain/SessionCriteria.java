package com.penpot.ai.core.domain;

import lombok.*;
import java.util.Optional;

/**
 * Représente un ensemble de critères permettant de sélectionner
 * une session WebSocket dans le système.
 */
@Value
@Builder
public class SessionCriteria {

    /**
     * Token utilisateur permettant d’identifier la session associée
     * à un utilisateur spécifique.
     *
     * <p>Si ce champ est vide ({@link Optional#empty()}), la recherche
     * n’est pas limitée à un utilisateur particulier.</p>
     */
    @Builder.Default
    Optional<String> userToken = Optional.empty();

    /**
     * Indique si la session recherchée doit obligatoirement être active.
     *
     * <p>Lorsque cette valeur est {@code true}, seules les sessions
     * actuellement ouvertes et opérationnelles doivent être considérées
     * lors de la sélection.</p>
     *
     * <p>Lorsque cette valeur est {@code false}, les sessions inactives
     * peuvent également être prises en compte selon la logique
     * implémentée par le gestionnaire de sessions.</p>
     *
     * <p>Par défaut, cette valeur est initialisée à {@code true}
     * afin d’éviter l’utilisation accidentelle de sessions fermées
     * ou invalides.</p>
     */
    @Builder.Default
    boolean requireActive = true;

    /**
     * Crée un critère de recherche ciblant un utilisateur spécifique.
     *
     * <p>Le token utilisateur est encapsulé dans un {@link Optional}
     * afin de gérer proprement les valeurs nulles.</p>
     *
     * @param userToken le token identifiant l’utilisateur dont la session
     *                  doit être recherchée ; peut être {@code null}
     * @return une instance de {@link SessionCriteria} configurée
     *         pour rechercher une session associée à cet utilisateur
     */
    public static SessionCriteria forUser(String userToken) {
        return SessionCriteria.builder()
            .userToken(Optional.ofNullable(userToken))
            .build();
    }

    /**
     * Crée un critère de recherche générique ne filtrant
     * sur aucun utilisateur spécifique.
     *
     * <p>La recherche peut alors retourner n’importe quelle session
     * correspondant aux autres contraintes définies
     * (par exemple l’état actif).</p>
     *
     * @return une instance de {@link SessionCriteria} sans restriction
     *         sur l’utilisateur
     */
    public static SessionCriteria any() {
        return SessionCriteria.builder().build();
    }
}