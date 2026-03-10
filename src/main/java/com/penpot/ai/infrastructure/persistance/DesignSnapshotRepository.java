package com.penpot.ai.infrastructure.persistence;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.penpot.ai.core.domain.snapshot.DesignSnapshot;
import com.penpot.ai.core.ports.out.SnapshotRepositoryPort;

import lombok.RequiredArgsConstructor;

/**
 * Implémentation JPA de SnapshotRepositoryPort.
 *
 * Architecture hexagonale :
 *   SnapshotRepositoryPort    → core/ports/out       (interface pure, pas de JPA)
 *   DesignSnapshotRepository  → infrastructure/persistence (implémentation JPA)
 *
 * Le core ne connaît que le port — jamais cette classe directement.
 */
@Component
@RequiredArgsConstructor
public class DesignSnapshotRepository implements SnapshotRepositoryPort {

    private final DesignSnapshotJpaRepository jpaRepository;

    @Override
    public DesignSnapshot save(DesignSnapshot snapshot) {
        return jpaRepository.save(snapshot);
    }

    @Override
    public Optional<DesignSnapshot> findLatestActive(String conversationId) {
        return jpaRepository.findLatestActiveByConversationId(conversationId);
    }

    @Override
    public Optional<DesignSnapshot> findById(String snapshotId) {
        return jpaRepository.findById(snapshotId);
    }

    @Override
    public List<DesignSnapshot> findByConversationIdOrderByCreatedAtDesc(String conversationId) {
        return jpaRepository.findByConversationIdOrderByCreatedAtDesc(conversationId);
    }
}

/**
 * Interface Spring Data JPA — détail d'implémentation interne.
 * Non exposée hors du package infrastructure.
 */
@Repository
interface DesignSnapshotJpaRepository extends JpaRepository<DesignSnapshot, String> {

    List<DesignSnapshot> findByConversationIdOrderByCreatedAtDesc(String conversationId);

    @Query("""
        SELECT s FROM DesignSnapshot s
        WHERE s.conversationId = :conversationId
          AND s.undone = false
        ORDER BY s.createdAt DESC
        LIMIT 1
    """)
    Optional<DesignSnapshot> findLatestActiveByConversationId(String conversationId);
}
