package com.penpot.ai.adapters.in.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.application.service.TaskOrchestrator;
import com.penpot.ai.infrastructure.session.SessionManager;
import com.penpot.ai.model.PluginTaskResponse;
import com.penpot.ai.shared.exception.FormattingException;
import com.penpot.ai.shared.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Composant d'entrée agissant comme gestionnaire principal pour les communications bidirectionnelles via WebSocket avec le plugin Penpot.
 * <p>
 * Cette classe orchestre l'intégralité du cycle de vie des connexions réseau. Elle intercepte l'établissement et la fermeture des sessions 
 * afin de maintenir un registre actif via le {@link SessionManager}. De plus, elle assure la réception des messages textuels entrants, 
 * procède à leur désérialisation sécurisée, puis notifie le {@link TaskOrchestrator} pour résoudre les processus asynchrones en attente. 
 * Par conséquent, ce composant centralise également la gestion des erreurs de transport et les anomalies de formatage des données.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PluginWebSocketHandler extends TextWebSocketHandler {

    /** Constante définissant la clé du paramètre d'URL utilisé pour transmettre le jeton d'authentification de l'utilisateur. */
    private static final String USER_TOKEN_PARAM = "userToken=";

    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager;
    private final TaskOrchestrator taskOrchestrator;

    /**
     * Appelé lorsqu'une nouvelle connexion WebSocket est établie.
     * Extrait le userToken si présent et enregistre la session.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userToken = extractUserToken(session);
        sessionManager.registerSession(session, userToken);

        log.info("WebSocket connection established: {} (active connections: {})", 
            session.getId(), 
            sessionManager.getActiveSessionCount());
    }

    /**
     * Intercepte l'ouverture d'une nouvelle connexion WebSocket par le client.
     * Lors de l'initialisation, la méthode extrait l'éventuel jeton utilisateur depuis l'URI de connexion, 
     * puis enregistre formellement la session auprès du gestionnaire centralisé afin de la rendre éligible 
     * au routage des futures tâches.
     *
     * @param session L'objet représentant la nouvelle session WebSocket établie avec le plugin.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        log.debug("Received WebSocket message from session {}: {}", 
            session.getId(), 
            JsonUtils.truncateForLog(payload, 200)
        );

        try {
            PluginTaskResponse<?> response = parseTaskResponse(payload);
            if (response == null) {
                log.warn("Received non-task-response message from session {}: {}", 
                    session.getId(), 
                    JsonUtils.truncateForLog(payload, 100));
                return;
            }

            log.info(
                "Parsed task response: id={}, success={}", 
                response.getId(), 
                response.getSuccess()
            );

            boolean notified = taskOrchestrator.notifyResponse(response);

            if (!notified) {
                log.warn(
                    "Received response for unknown or expired task: {}", 
                    response.getId()
                );
            }
        } catch (Exception e) {
            log.error(
                "Failed to handle WebSocket message from session {}", 
                session.getId(), e
            );
            String taskId = tryExtractTaskId(payload);
            if (taskId != null) taskOrchestrator.notifyError(taskId, e);
        }
    }

    /**
     * Gère la clôture intentionnelle ou accidentelle d'une connexion WebSocket.
     * La méthode s'assure de désinscrire la session du gestionnaire pour éviter les envois dans le vide (dead letters). 
     * En outre, si cette fermeture entraîne l'absence totale de sessions actives, elle déclenche une procédure 
     * de sécurité annulant préventivement toutes les tâches actuellement en attente de résolution.
     *
     * @param session La session WebSocket venant d'être fermée.
     * @param status  Le code et le motif de fermeture de la connexion.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.unregisterSession(session);

        log.info("WebSocket connection closed: {} - {} (active connections: {})", 
            session.getId(),
            status, 
            sessionManager.getActiveSessionCount());

        if (!sessionManager.hasActiveSessions()) {
            log.warn("No active sessions remaining - cancelling all pending tasks");
            taskOrchestrator.cancelAllPendingTasks(
                "WebSocket connection closed: " + status.getReason()
            );
        }
    }

    /**
     * Intercepte les erreurs de bas niveau survenant sur la couche de transport réseau.
     * Face à une défaillance de la communication, le gestionnaire force la fermeture de la session en erreur 
     * avec un statut approprié et procède au nettoyage des ressources allouées en la retirant du registre actif.
     *
     * @param session   La session affectée par le problème de transport.
     * @param exception L'exception technique levée par le conteneur WebSocket.
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error for session {}", 
            session.getId(), exception);

        try {
            if (session.isOpen()) session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            log.error("Failed to close session after transport error", e);
        }

        sessionManager.unregisterSession(session);
    }

    /**
     * Analyse et convertit la charge utile JSON en un objet métier {@link PluginTaskResponse}.
     * Afin de garantir une interopérabilité maximale avec les différentes versions du plugin, cette méthode 
     * supporte deux formats de structuration : une enveloppe explicite spécifiant le type de message, 
     * ou une déclaration directe des propriétés à la racine du document JSON.
     *
     * @param payload La chaîne de caractères contenant les données JSON brutes.
     * @return        L'instance de réponse formatée, ou {@code null} si le message ne correspond pas au contrat attendu.
     * @throws FormattingException Si le processus de désérialisation échoue à cause d'une anomalie structurelle majeure.
     */
    private PluginTaskResponse<?> parseTaskResponse(String payload) {
        try {
            if (!JsonUtils.isValidJson(payload)) {
                log.warn("Received invalid JSON payload");
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);

            if ("task-response".equals(messageMap.get("type"))) {
                Map<String, Object> responseMap = (Map<String, Object>) messageMap.get("response");
                if (responseMap == null) {
                    log.warn("task-response envelope missing 'response' field");
                    return null;
                }

                return objectMapper.convertValue(responseMap, PluginTaskResponse.class);
            }

            if (messageMap.containsKey("id") && messageMap.containsKey("success")) {
                return objectMapper.convertValue(messageMap, PluginTaskResponse.class);
            }

            return null;
        } catch (Exception e) {
            log.error("Failed to parse task response", e);
            throw new FormattingException("Failed to parse task response: " + e.getMessage(), e);
        }
    }

    /**
     * Effectue une tentative de récupération d'urgence de l'identifiant de la tâche au sein d'un document JSON jugé invalide.
     *
     * @param payload La charge utile JSON potentiellement malformée ou incomplète.
     * @return        L'identifiant de la tâche s'il a pu être isolé, ou {@code null} si l'extraction s'avère impossible.
     */
    private String tryExtractTaskId(String payload) {
        try {
            Map<String, Object> map = objectMapper.readValue(payload, Map.class);
            Object id = map.get("id");
            if (id != null) return id.toString();

            Map<String, Object> response = (Map<String, Object>) map.get("response");
            if (response != null) {
                id = response.get("id");
                if (id != null) return id.toString();
            }

            return null;
        } catch (Exception e) {
            log.debug("Could not extract task ID from malformed payload", e);
            return null;
        }
    }

    /**
     * Inspecte l'URI de connexion initiale pour en extraire le jeton d'authentification ou d'identification de l'utilisateur.
     *
     * @param session La session WebSocket porteuse de l'URI de connexion.
     * @return        Le jeton utilisateur sous forme de chaîne de caractères, ou {@code null} si celui-ci est absent ou introuvable.
     */
    private String extractUserToken(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;

        String query = uri.getQuery();
        if (query != null && query.contains(USER_TOKEN_PARAM)) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith(USER_TOKEN_PARAM)) {
                    return param.substring(USER_TOKEN_PARAM.length());
                }
            }
        }
        return null;
    }
}