package com.penpot.ai.application.DTO;

import java.time.Instant;

public class MessageDTO {

    private String id;
    private String conversationId;
    private String projectId;
    private String contentUser;
    private String contentAssistant;
    private Instant createdAt;

    public MessageDTO() {}

    public MessageDTO(String id, String conversationId, String projectId, String contentUser, String contentAssistant, Instant createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.projectId = projectId;
        this.contentUser = contentUser;
        this.contentAssistant = contentAssistant;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getContentUser() { return contentUser; }
    public void setContentUser(String contentUser) { this.contentUser = contentUser; }

    public String getContentAssistant() { return contentAssistant; }
    public void setContentAssistant(String contentAssistant) { this.contentAssistant = contentAssistant; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}