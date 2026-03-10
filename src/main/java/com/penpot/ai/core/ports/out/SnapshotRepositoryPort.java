package com.penpot.ai.core.ports.out;



import java.util.List;
import java.util.Optional;

import com.penpot.ai.core.domain.snapshot.DesignSnapshot;

/**
 * Port out — abstraction du repository de snapshots.
 *
 * Le core ne dépend pas de JPA ou Spring Data.
 * L'implémentation JPA est dans infrastructure/persistence/.
 */
public interface SnapshotRepositoryPort {

    /** Sauvegarde un snapshot */
    DesignSnapshot save(DesignSnapshot snapshot);

    /** Retourne le snapshot actif le plus récent d'une conversation */
    Optional<DesignSnapshot> findLatestActive(String conversationId);

    /** Retourne un snapshot par son ID */
    Optional<DesignSnapshot> findById(String snapshotId);

    /** Retourne tous les snapshots d'une conversation, du plus récent au plus ancien */
    List<DesignSnapshot> findByConversationIdOrderByCreatedAtDesc(String conversationId);
}
