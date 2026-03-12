package com.penpot.ai.application.DTO;

import java.time.Instant;
import java.util.UUID;

public class ConversationMetaDataDTO {

    private UUID id;
    private String conversationId;
    private UUID projectId;
    private String userId;
    private Instant createdAt;

    public ConversationMetaDataDTO() {}

    public ConversationMetaDataDTO(UUID id, String conversationId, UUID projectId, String userId, Instant createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.projectId = projectId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
