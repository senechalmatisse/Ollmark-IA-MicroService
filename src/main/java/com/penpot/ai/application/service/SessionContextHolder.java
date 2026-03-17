package com.penpot.ai.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Détenteur du sessionId courant pour le thread en cours d'exécution.
 *
 * SRP : ce composant a une seule responsabilité — stocker et restituer
 * le sessionId pour la durée d'un appel LLM, sans connaître ni l'adaptateur
 * IA ni les tools qui le consomment.
 *
 * Le ThreadLocal garantit l'isolation entre requêtes concurrentes sur des
 * threads Tomcat distincts. Le bloc finally dans OllamaAiAdapter assure
 * le nettoyage systématique après chaque appel.
 */
@Slf4j
@Component
public class SessionContextHolder {

    private final ThreadLocal<String> sessionId = new ThreadLocal<>();

    public void set(String id) {
        sessionId.set(id);
        log.debug("[SessionContext] sessionId set: {}", id);
    }

    public String get() {
        return sessionId.get();
    }

    public void clear() {
        sessionId.remove();
        log.debug("[SessionContext] sessionId cleared");
    }
}