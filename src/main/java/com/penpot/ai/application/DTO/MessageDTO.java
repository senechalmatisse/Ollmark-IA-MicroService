package com.penpot.ai.application.DTO;

import java.time.Instant;
import java.util.UUID;

public class MessageDTO {

    private UUID id;
    private UUID conversationId;
    private UUID projectId;
    private String contentUser;
    private String contentAssistant;
    private Instant createdAt;

    public MessageDTO() {}

    public MessageDTO(UUID id, UUID conversationId, UUID projectId, String contentUser, String contentAssistant, Instant createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.projectId = projectId;
        this.contentUser = contentUser;
        this.contentAssistant = contentAssistant;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getContentUser() { return contentUser; }
    public void setContentUser(String contentUser) { this.contentUser = contentUser; }

    public String getContentAssistant() { return contentAssistant; }
    public void setContentAssistant(String contentAssistant) { this.contentAssistant = contentAssistant; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}