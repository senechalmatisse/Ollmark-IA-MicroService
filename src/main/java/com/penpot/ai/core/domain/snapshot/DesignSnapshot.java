package com.penpot.ai.core.domain.snapshot;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Snapshot d'une génération IA.
 *
 * Distingue deux types d'événements :
 *   - createdShapeIds  : shapes créées → undo = suppression
 *   - modifications    : shapes modifiées → undo = restauration état "before"
 */
@Entity
@Table(name = "design_snapshots", indexes = {
    @Index(name = "idx_snapshot_conversation", columnList = "conversation_id"),
    @Index(name = "idx_snapshot_created_at",   columnList = "created_at")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    @Column(name = "user_message", length = 2000)
    private String userMessage;

    /**
     * UUIDs des shapes créées par l'IA.
     * Undo → shape.remove() pour chacun.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "created_shape_ids", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> createdShapeIds = List.of();

    /**
     * Modifications de shapes existantes avec leur état avant.
     * Undo → restauration de l'état "before" pour chacune.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "modifications", columnDefinition = "jsonb")
    @Builder.Default
    private List<ShapeModification> modifications = List.of();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "undone")
    @Builder.Default
    private boolean undone = false;

    public void markAsUndone() {
        this.undone = true;
    }

    /** Indique si ce snapshot contient des événements à annuler */
    public boolean hasEvents() {
        return (createdShapeIds != null && !createdShapeIds.isEmpty())
            || (modifications != null && !modifications.isEmpty());
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}

