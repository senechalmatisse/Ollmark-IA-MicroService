package com.penpot.ai.application.DTO;

import java.time.Instant;

public class ConversationMetaDataDTO {

    private String id;
    private String conversationId;
    private String projectId;
    private String userId;
    private Instant createdAt;

    public ConversationMetaDataDTO() {}

    public ConversationMetaDataDTO(String id, String conversationId, String projectId, String userId, Instant createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.projectId = projectId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
