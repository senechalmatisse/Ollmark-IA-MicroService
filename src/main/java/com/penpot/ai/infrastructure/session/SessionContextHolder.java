package com.penpot.ai.infrastructure.session;

import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

/**
 * Gestionnaire du contexte de session via ThreadLocal.
 * 
 * <p>
 * Stocke le sessionId et le userToken pour le thread courant.
 * Permet à n'importe quel composant d'accéder au contexte de session
 * sans avoir à le passer comme paramètre.
 * </p>
 * 
 * <p>
 * <b>Important :</b> À utiliser avec prudence, car un ThreadLocal persiste
 * pendant toute la durée de vie du thread. En environnement multi-threadé
 * (notamment avec les pools de threads), toujours appeler {@link #clear()}
 * ou {@link #clearAll()} après utilisation pour éviter les fuites mémoire.
 * </p>
 */
@Slf4j
public class SessionContextHolder {

    /** ThreadLocal stockant l'ID de la session pour le thread courant. */
    private static final ThreadLocal<String> SESSION_ID_HOLDER = new ThreadLocal<>();

    /** ThreadLocal stockant le token utilisateur pour le thread courant. */
    private static final ThreadLocal<String> USER_TOKEN_HOLDER = new ThreadLocal<>();

    /**
     * Définit le sessionId pour le thread courant.
     * 
     * @param sessionId l'ID de la session
     */
    public static void setSessionId(String sessionId) {
        log.debug("Setting session context for thread {}: sessionId={}", 
            Thread.currentThread().getId(), 
            sessionId);
        SESSION_ID_HOLDER.set(sessionId);
    }

    /**
     * Récupère le sessionId du thread courant.
     * 
     * @return un Optional contenant le sessionId s'il a été défini
     */
    public static Optional<String> getSessionId() {
        String sessionId = SESSION_ID_HOLDER.get();
        if (sessionId == null) {
            log.debug("No session context found for thread {}", Thread.currentThread().getId());
        }
        return Optional.ofNullable(sessionId);
    }

    /**
     * Récupère le sessionId du thread courant ou lève une exception si absent.
     * 
     * @return le sessionId
     * @throws IllegalStateException si aucun sessionId n'est défini
     */
    public static String getSessionIdRequired() {
        return getSessionId()
            .orElseThrow(() -> new IllegalStateException(
                "No session context found for thread " + Thread.currentThread().getId()
            ));
    }

    /**
     * Définit le userToken pour le thread courant.
     * 
     * @param userToken le token utilisateur
     */
    public static void setUserToken(String userToken) {
        log.debug("Setting user token context for thread {}", Thread.currentThread().getId());
        USER_TOKEN_HOLDER.set(userToken);
    }

    /**
     * Récupère le userToken du thread courant.
     * 
     * @return un Optional contenant le userToken s'il a été défini
     */
    public static Optional<String> getUserToken() {
        return Optional.ofNullable(USER_TOKEN_HOLDER.get());
    }

    /**
     * Récupère le userToken du thread courant ou lève une exception si absent.
     * 
     * @return le userToken
     * @throws IllegalStateException si aucun userToken n'est défini
     */
    public static String getUserTokenRequired() {
        return getUserToken()
            .orElseThrow(() -> new IllegalStateException(
                "No user token context found for thread " + Thread.currentThread().getId()
            ));
    }

    /**
     * Nettoie le sessionId du thread courant.
     * À appeler systématiquement après utilisation pour éviter les fuites mémoire.
     */
    public static void clearSessionId() {
        log.debug("Clearing session context for thread {}", Thread.currentThread().getId());
        SESSION_ID_HOLDER.remove();
    }

    /**
     * Nettoie le userToken du thread courant.
     * À appeler systématiquement après utilisation pour éviter les fuites mémoire.
     */
    public static void clearUserToken() {
        log.debug("Clearing user token context for thread {}", Thread.currentThread().getId());
        USER_TOKEN_HOLDER.remove();
    }

    /**
     * Nettoie tous les contextes (sessionId et userToken) du thread courant.
     * À appeler systématiquement après utilisation pour éviter les fuites mémoire.
     */
    public static void clearAll() {
        log.debug("Clearing all context for thread {}", Thread.currentThread().getId());
        SESSION_ID_HOLDER.remove();
        USER_TOKEN_HOLDER.remove();
    }

    /**
     * Vérifie si un contexte de session est défini pour le thread courant.
     * 
     * @return true si un sessionId est défini
     */
    public static boolean hasSessionContext() {
        return SESSION_ID_HOLDER.get() != null;
    }

    /**
     * Vérifie si un contexte utilisateur est défini pour le thread courant.
     * 
     * @return true si un userToken est défini
     */
    public static boolean hasUserContext() {
        return USER_TOKEN_HOLDER.get() != null;
    }

    private SessionContextHolder() {
        // Classe utilitaire, ne pas instancier
    }
}
