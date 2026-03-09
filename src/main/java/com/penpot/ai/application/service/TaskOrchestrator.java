package com.penpot.ai.application.service;

import com.penpot.ai.model.PluginTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Composant centralisé agissant comme un coordinateur pour la gestion des réponses asynchrones liées aux tâches déléguées au plugin.
 * <p>
 * Son architecture repose sur un cycle de vie en quatre étapes : premièrement, les cas d'usage métiers enregistrent une tâche 
 * avant son expédition sur le réseau ; deuxièmement, un objet {@link CompletableFuture} est instancié et conservé en mémoire pour assurer le suivi ; 
 * troisièmement, le gestionnaire de connexions (WebSocketHandler) notifie ce coordinateur dès la réception d'une réponse asynchrone ; 
 * enfin, la promesse (Future) est complétée, permettant au cas d'usage initial de reprendre son exécution avec les données reçues.
 * </p>
 */
@Slf4j
@Component
public class TaskOrchestrator {

    /**
     * Dictionnaire en mémoire conservant l'état des tâches en cours de traitement.
     * L'association lie l'identifiant unique de la tâche (clé) à la promesse asynchrone correspondante (valeur).
     */
    private final Map<String, CompletableFuture<PluginTaskResponse<?>>> pendingTasks = new ConcurrentHashMap<>();

    /**
     * Initialise et consigne une nouvelle tâche dans le registre des attentes de réponse.
     *
     * @param taskId L'identifiant unique assigné à la tâche lors de sa création.
     * @return       L'objet {@link CompletableFuture} qui sera complété à la réception de la réponse du plugin.
     * @throws IllegalArgumentException Si l'identifiant fourni est nul ou ne contient que des espaces blancs.
     */
    public CompletableFuture<PluginTaskResponse<?>> registerTask(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }

        CompletableFuture<PluginTaskResponse<?>> future = new CompletableFuture<>();
        CompletableFuture<PluginTaskResponse<?>> existing = pendingTasks.putIfAbsent(taskId, future);
        if (existing != null) {
            log.warn("Task {} was already registered - this may indicate a duplicate task ID", taskId);
            return existing;
        }

        log.debug(
            "Registered task {} for response tracking (total pending: {})", 
            taskId, pendingTasks.size()
        );

        return future;
    }

    /**
     * Retire une tâche du registre de suivi, qu'elle ait abouti naturellement ou expiré suite à un délai dépassé.
     *
     * @param taskId L'identifiant unique de la tâche à désinscrire.
     * @return       {@code true} si la tâche était effectivement enregistrée et a été supprimée, {@code false} dans le cas contraire.
     */
    public boolean unregisterTask(String taskId) {
        if (taskId == null || taskId.isBlank()) return false;
        CompletableFuture<?> removed = pendingTasks.remove(taskId);

        if (removed != null) {
            log.debug(
                "Unregistered task {} (total pending: {})", 
                taskId, pendingTasks.size()
            );

            if (!removed.isDone()) {
                log.debug("Cancelling incomplete future for task {}", taskId);
                removed.cancel(false);
            }

            return true;
        }

        log.debug("Attempted to unregister unknown task: {}", taskId);
        return false;
    }

    /**
     * Signale au coordinateur la réception effective d'une réponse émanant du plugin distant.
     *
     * @param response L'objet de réponse structuré reçu depuis le réseau.
     * @return         {@code true} si la promesse correspondante a pu être complétée avec succès, {@code false} sinon.
     */
    public boolean notifyResponse(PluginTaskResponse<?> response) {
        if (response == null) {
            log.warn("Received null response");
            return false;
        }

        String taskId = response.getId();
        if (taskId == null || taskId.isBlank()) {
            log.warn("Received response with null or empty task ID");
            return false;
        }

        CompletableFuture<PluginTaskResponse<?>> future = pendingTasks.get(taskId);
        if (future == null) {
            log.warn("Received response for unknown or expired task: {}", taskId);
            return false;
        }

        log.debug(
            "Completing future for task {} (success: {})", 
            taskId, response.getSuccess()
        );

        try {
            boolean completed = future.complete(response);
            if (!completed) log.warn("Future for task {} was already completed", taskId);
            return completed;
        } catch (Exception e) {
            log.error("Error completing future for task {}", taskId, e);
            return false;
        }
    }

    /**
     * Propage une erreur technique ou métier vers le processus appelant qui attend la résolution de la tâche.
     *
     * @param taskId L'identifiant de la tâche affectée par l'anomalie.
     * @param error  L'instance de l'exception ou de l'erreur survenue lors du traitement.
     * @return       {@code true} si la promesse a été rompue avec succès, {@code false} si la tâche est introuvable.
     */
    public boolean notifyError(String taskId, Throwable error) {
        if (taskId == null || taskId.isBlank()) {
            log.warn("Attempted to notify error with null/empty task ID");
            return false;
        }

        CompletableFuture<PluginTaskResponse<?>> future = pendingTasks.get(taskId);
        if (future == null) {
            log.warn("Received error for unknown task {}: {}", taskId, error.getMessage());
            return false;
        }

        log.debug(
            "Completing future with error for task {}: {}", 
            taskId, error.getMessage()
        );

        try {
            boolean completed = future.completeExceptionally(error);
            if (!completed) log.warn("Future for task {} was already completed when trying to set error", taskId);
            return completed;
        } catch (Exception e) {
            log.error("Error completing future exceptionally for task {}", taskId, e);
            return false;
        }
    }

    /**
     * Interrompt de manière abrupte l'ensemble des tâches actuellement répertoriées dans le registre d'attente.
     *
     * @param reason La justification textuelle motivant cette annulation massive, transmise aux appelants.
     */
    public void cancelAllPendingTasks(String reason) {
        int count = pendingTasks.size();
        if (count == 0) {
            log.debug("No pending tasks to cancel");
            return;
        }

        log.info("Cancelling {} pending tasks: {}", count, reason);
        pendingTasks.forEach((taskId, future) -> {
            if (!future.isDone()) {
                log.debug("Cancelling task: {}", taskId);
                future.completeExceptionally(new RuntimeException("Task cancelled: " + reason));
            } else {
                log.debug("Task {} was already completed", taskId);
            }
        });

        pendingTasks.clear();
        log.info("All pending tasks cancelled and cleared");
    }
}