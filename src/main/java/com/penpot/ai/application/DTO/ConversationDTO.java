package com.penpot.ai.application.DTO;

import java.util.List;
import java.util.UUID;

public class ConversationDTO {

    private UUID id;
    private UUID conversationId;
    private UUID userId;
    private UUID projectId;
    private List<MessageDTO> messages;

    public ConversationDTO() {}

    public ConversationDTO(UUID id, UUID conversationId, UUID userId, UUID projectId, List<MessageDTO> messages) {
        this.id = id;
        this.conversationId = conversationId;
        this.userId = userId;
        this.projectId = projectId;
        this.messages = messages;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public List<MessageDTO> getMessages() { return messages; }
    public void setMessages(List<MessageDTO> messages) { this.messages = messages; }
}