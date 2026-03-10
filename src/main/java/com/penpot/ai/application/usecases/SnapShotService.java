package com.penpot.ai.application.usecases;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.penpot.ai.core.domain.ExecuteCodeCommand;
import com.penpot.ai.core.domain.snapshot.DesignSnapshot;
import com.penpot.ai.core.domain.snapshot.ShapeModification;
import com.penpot.ai.core.ports.in.ExecuteCodeUseCase;
import com.penpot.ai.core.ports.out.SnapshotRepositoryPort;
import com.penpot.ai.shared.exception.SnapshotNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service de gestion des snapshots de design.
 *
 * Undo complet :
 *   1. Supprime les shapes créées par l'IA (createdShapeIds)
 *   2. Restaure l'état "before" des shapes modifiées (modifications)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SnapShotService {

    private final SnapshotRepositoryPort snapshotRepository;
    private final ExecuteCodeUseCase executeCodeUseCase;

    // =========================================================================
    // Sauvegarde
    // =========================================================================

    @Transactional
    public DesignSnapshot saveSnapshot(String conversationId, String userMessage,
                                       List<String> createdShapeIds,
                                       List<ShapeModification> modifications) {
        boolean hasCreations    = createdShapeIds != null && !createdShapeIds.isEmpty();
        boolean hasModifications = modifications  != null && !modifications.isEmpty();

        if (!hasCreations && !hasModifications) {
            log.debug("[Snapshot] Nothing to snapshot — skipped");
            return null;
        }

        DesignSnapshot snapshot = DesignSnapshot.builder()
                .conversationId(conversationId)
                .userMessage(userMessage)
                .createdShapeIds(hasCreations ? createdShapeIds : List.of())
                .modifications(hasModifications ? modifications : List.of())
                .build();

        DesignSnapshot saved = snapshotRepository.save(snapshot);
        log.info("[Snapshot] Saved: id={}, created={}, modified={}",
                saved.getId(), createdShapeIds.size(), modifications.size());
        return saved;
    }

    // =========================================================================
    // Undo
    // =========================================================================

    @Transactional
    public void undoLastGeneration(String conversationId) {
        DesignSnapshot snapshot = snapshotRepository
                .findLatestActive(conversationId)
                .orElseThrow(() -> new SnapshotNotFoundException(
                        "No active snapshot for conversation: " + conversationId));

        executeUndo(snapshot);
    }

    @Transactional
    public void undoSnapshot(String snapshotId) {
        DesignSnapshot snapshot = snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new SnapshotNotFoundException(
                        "Snapshot not found: " + snapshotId));

        if (snapshot.isUndone()) {
            log.warn("[Snapshot] {} already undone — skipped", snapshotId);
            return;
        }

        executeUndo(snapshot);
    }

    // =========================================================================
    // Exécution de l'undo
    // =========================================================================

    private void executeUndo(DesignSnapshot snapshot) {
        log.info("[Snapshot] Undoing id={} (created={}, modified={})",
                snapshot.getId(),
                snapshot.getCreatedShapeIds().size(),
                snapshot.getModifications().size());

        // Étape 1 : supprimer les shapes créées
        if (!snapshot.getCreatedShapeIds().isEmpty()) {
            String deleteJs = buildDeleteJs(snapshot.getCreatedShapeIds());
            executeCodeUseCase.execute(ExecuteCodeCommand.of(deleteJs));
            log.info("[Snapshot] Deleted {} created shapes", snapshot.getCreatedShapeIds().size());
        }

        // Étape 2 : restaurer les shapes modifiées
        for (ShapeModification mod : snapshot.getModifications()) {
            String restoreJs = mod.before().buildRestoreJs();
            executeCodeUseCase.execute(ExecuteCodeCommand.of(restoreJs));
            log.info("[Snapshot] Restored shape {} (was modified by {})",
                    mod.shapeId(), mod.toolName());
        }

        snapshot.markAsUndone();
        snapshotRepository.save(snapshot);
        log.info("[Snapshot] Undo complete for id={}", snapshot.getId());
    }

    // =========================================================================
    // JS de suppression des shapes créées
    // =========================================================================

    private String buildDeleteJs(List<String> shapeIds) {
        String idsJson = shapeIds.stream()
                .map(id -> "\"" + id + "\"")
                .collect(Collectors.joining(", ", "[", "]"));

        return """
            const ids = %s;
            let removed = 0;
            for (const id of ids) {
                const shape = penpot.context.currentPage.getShapeById(id);
                if (shape) { shape.remove(); removed++; }
            }
            return `Deleted ${removed}/%d shapes`;
            """.formatted(idsJson, shapeIds.size());
    }

    // =========================================================================
    // Historique
    // =========================================================================

    public List<DesignSnapshot> getHistory(String conversationId) {
        return snapshotRepository.findByConversationIdOrderByCreatedAtDesc(conversationId);
    }
}

