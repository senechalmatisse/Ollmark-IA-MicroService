package com.penpot.ai.adapters.out.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.core.domain.*;
import com.penpot.ai.core.ports.out.PluginCommunicationPort;
import com.penpot.ai.infrastructure.session.SessionManager;
import com.penpot.ai.application.service.TaskOrchestrator;
import com.penpot.ai.model.*;
import com.penpot.ai.shared.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Adaptateur d'infrastructure responsable de la communication bidirectionnelle avec le plugin Penpot via le protocole WebSocket.
 * <p>
 * L'architecture repose sur un processus séquentiel :
 * <ul>
 *  <li>Vérification d'abord la disponibilité d'une session WebSocket adéquate,</li>
 *  <li>enregistre la tâche auprès du {@link TaskOrchestrator} pour le suivi,</li>
 *  <li>
 *    transmet la charge utile sérialisée, puis bloque l'exécution$
 *    de manière asynchrone (via un {@link CompletableFuture}) dans l'attente de la réponse du client.
 *  </li>
 * </ul>
 * </p>
 * <p>
 * En matière de concurrence, ce service est conçu pour résister à de fortes charges : les opérations d'envoi WebSocket 
 * sont natives et sécurisées, le gestionnaire de tâches s'appuie sur des structures concurrentes (telles que {@code ConcurrentHashMap}), 
 * et la complétion des promesses garantit une isolation parfaite entre les différents fils d'exécution.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PluginBridgeAdapter implements PluginCommunicationPort {

    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final TaskOrchestrator responseOrchestrator;

    /** Délai d'attente maximal alloué pour la résolution d'une tâche par le plugin. */
    @Value("${penpot.ai.plugin.timeout-ms:30000}")
    private long pluginTimeoutMs;

    /**
     * Transmet une tâche opérationnelle au plugin Penpot et suspend le traitement jusqu'à la réception de son résultat ou l'expiration du délai imparti.
     * <p>
     * Dans un premier temps, la méthode identifie la session WebSocket appropriée en fonction des critères de la tâche. 
     * Ensuite, elle convertit le modèle du domaine en une requête compatible avec l'interface du plugin et l'enregistre auprès de l'orchestrateur. 
     * Finalement, elle expédie le message et attend la résolution du {@link CompletableFuture}. En cas d'anomalie (dépassement de délai, 
     * interruption ou erreur d'exécution), des exceptions spécifiques sont levées pour assurer une gestion d'erreur robuste en amont, 
     * tout en garantissant systématiquement le nettoyage des registres de l'orchestrateur via un bloc {@code finally}.
     * </p>
     *
     * @param <T>  Le type générique attendu pour les données de retour encapsulées dans la réponse.
     * @param task L'entité métier représentant la commande ou l'action à déléguer au plugin.
     * @return     L'objet de réponse typé renvoyé par le plugin en cas de succès de l'opération.
     * @throws PluginConnectionException Si aucune session WebSocket active ne correspond aux critères de la tâche.
     * @throws TaskExecutionException    Si l'envoi échoue, si le délai d'attente est dépassé (Timeout), ou si une erreur survient lors du traitement.
     */
    @Override
    public <T> PluginTaskResponse<T> sendTask(Task task) {
        log.info("Sending task {} to plugin (type: {})", task.getId(), task.getType());

        SessionCriteria criteria = buildCriteria(task);
        WebSocketSession session = sessionManager.findSession(criteria)
            .orElseThrow(() -> new PluginConnectionException(
                "No active plugin session found for criteria: " + criteria
            ));

        PluginTaskRequest request = buildRequest(task);
        CompletableFuture<PluginTaskResponse<?>> future = responseOrchestrator.registerTask(task.getId());

        try {
            sendWebSocketMessage(session, request);
            log.debug("Task {} sent successfully, waiting for response...", task.getId());

            PluginTaskResponse<?> response = future.get(pluginTimeoutMs, TimeUnit.MILLISECONDS);
            log.info(
                "Received response for task {}: success={}, hasData={}", 
                task.getId(), 
                response.getSuccess(),
                response.getData() != null
            );

            return (PluginTaskResponse<T>) response;
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new TaskExecutionException(
                String.format("Task %s timed out after %d ms", task.getId(), pluginTimeoutMs), e
            );
        } catch (InterruptedException e) {
            log.error("Task {} was interrupted", task.getId());
            Thread.currentThread().interrupt();
            throw new TaskExecutionException(
                "Task " + task.getId() + " was interrupted", e
            );
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            log.error("Task {} execution failed: {}", 
                task.getId(), 
                cause != null ? cause.getMessage() : "Unknown error");
            throw new TaskExecutionException(
                "Task " + task.getId() + " execution failed: " +
                    (cause != null ? cause.getMessage() : "Unknown error"),
                cause != null ? cause : e
            );
        } catch (Exception e) {
            log.error("Unexpected error sending task {}", task.getId(), e);
            throw new TaskExecutionException(
                "Failed to send task " + task.getId() + ": " + e.getMessage(), e
            );
        } finally {
            responseOrchestrator.unregisterTask(task.getId());
        }
    }

    /**
     * Assure la sérialisation JSON et l'expédition sécurisée d'une requête au travers d'un canal WebSocket existant.
     * Préalablement à l'envoi, une validation de l'état de la connexion est effectuée. Toute défaillance lors de la 
     * conversion ou de la transmission réseau est interceptée et rediffusée sous forme d'erreur d'exécution.
     *
     * @param session La session WebSocket établie avec le client cible.
     * @param request Le modèle de données représentant la requête à expédier.
     * @throws PluginConnectionException Si le canal de communication WebSocket a été fermé prématurément.
     * @throws TaskExecutionException    Si le processus de sérialisation ou d'émission rencontre une anomalie technique.
     */
    private void sendWebSocketMessage(WebSocketSession session, PluginTaskRequest request) {
        try {
            if (!session.isOpen()) {
                throw new PluginConnectionException(
                    "WebSocket session " + session.getId() + " is not open"
                );
            }

            String jsonRequest = objectMapper.writeValueAsString(request);
            log.debug("Sending WebSocket message: {}", jsonRequest);
            session.sendMessage(new TextMessage(jsonRequest));
        } catch (Exception e) {
            log.error("Failed to send WebSocket message for task {}", request.getId(), e);
            throw new TaskExecutionException(
                "Failed to send WebSocket message: " + e.getMessage(),
                e
            );
        }
    }

    @Override
    public boolean hasActiveConnection() {
        boolean hasActive = sessionManager.hasActiveSessions();
        log.debug("Has active connection: {}", hasActive);
        return hasActive;
    }

    @Override
    public Optional<WebSocketSession> findSession(SessionCriteria criteria) {
        return sessionManager.findSession(criteria);
    }

    /**
     * Construit les critères de sélection de la session cible en se basant sur le contexte de la tâche à exécuter.
     * Si la tâche spécifie un jeton utilisateur, le filtre sera strict. À l'inverse, si aucun utilisateur n'est désigné, 
     * le critère tolérera l'utilisation de n'importe quelle session disponible.
     *
     * @param task L'entité métier dont proviennent les informations de ciblage.
     * @return     Les critères de recherche normalisés pour le gestionnaire de sessions.
     */
    private SessionCriteria buildCriteria(Task task) {
        return task.getSessionId()
            .map(SessionCriteria::forSession)
            .orElseGet(SessionCriteria::any);
    }

    /**
     * Réalise le mappage des données entre l'objet du domaine interne et le contrat d'interface exposé au plugin externe.
     *
     * @param task L'instruction métier source.
     * @return     La structure de transport (DTO) formatée pour le canal WebSocket.
     */
    private PluginTaskRequest buildRequest(Task task) {
        return PluginTaskRequest.builder()
            .id(task.getId())
            .task(task.getType().getTaskName())
            .params(task.getParameters())
            .build();
    }
}