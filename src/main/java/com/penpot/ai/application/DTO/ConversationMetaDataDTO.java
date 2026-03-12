package com.penpot.ai.application.DTO;

import java.time.Instant;
import java.util.UUID;

public class ConversationMetaDataDTO {

    private UUID id;
    private UUID conversationId;
    private UUID projectId;
    private UUID userId;
    private Instant createdAt;

    public ConversationMetaDataDTO() {}

    public ConversationMetaDataDTO(UUID id, UUID conversationId, UUID projectId, UUID userId, Instant createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.projectId = projectId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
