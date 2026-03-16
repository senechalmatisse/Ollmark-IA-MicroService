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
     * Identifiant de session WebSocket pour un routage précis.
     * Absent = n'importe quelle session disponible.
     */
    @Builder.Default
    Optional<String> sessionId = Optional.empty();

    /**
     * Crée un critère ciblant une session WebSocket précise.
     */
    public static SessionCriteria forSession(String sessionId) {
        return SessionCriteria.builder()
            .sessionId(Optional.ofNullable(sessionId))
            .requireActive(true)
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