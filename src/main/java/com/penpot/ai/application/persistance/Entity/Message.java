package com.penpot.ai.application.persistance.Entity;


import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Historique des échanges d'une conversation.
 * Chaque Message stocke le tour complet : message utilisateur + réponse IA.
 * penpotSnapshotId permet de retrouver le snapshot associé à une génération.
 */
@Entity
@Table(
    name = "messages",
    indexes = @Index(
        name = "idx_messages_conversation_id",
        columnList = "conversation_id"
    )
)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "content_user", columnDefinition = "text")
    private String contentUser;

    @Column(name = "content_assistant", columnDefinition = "text")
    private String contentAssistant;

    @Column(name = "penpot_snapshot_id", length = 255)
    private String penpotSnapshotId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Message() {}

    public Message(Conversation conversation, String contentUser, String contentAssistant) {
        this.conversation = conversation;
        this.contentUser = contentUser;
        this.contentAssistant = contentAssistant;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getId() { return id; }

    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }

    public String getContentUser() { return contentUser; }
    public void setContentUser(String contentUser) { this.contentUser = contentUser; }

    public String getContentAssistant() { return contentAssistant; }
    public void setContentAssistant(String contentAssistant) { this.contentAssistant = contentAssistant; }

    public String getPenpotSnapshotId() { return penpotSnapshotId; }
    public void setPenpotSnapshotId(String penpotSnapshotId) { this.penpotSnapshotId = penpotSnapshotId; }

    public Instant getCreatedAt() { return createdAt; }
}