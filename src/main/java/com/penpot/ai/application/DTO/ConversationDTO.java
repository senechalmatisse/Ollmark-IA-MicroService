package com.penpot.ai.application.DTO;

import java.util.List;

public class ConversationDTO {

    private String id;
    private String conversationId;
    private String userId;
    private String projectId;
    private List<MessageDTO> messages;

    public ConversationDTO() {}

    public ConversationDTO(String id, String conversationId, String userId, String projectId, List<MessageDTO> messages) {
        this.id = id;
        this.conversationId = conversationId;
        this.userId = userId;
        this.projectId = projectId;
        this.messages = messages;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public List<MessageDTO> getMessages() { return messages; }
    public void setMessages(List<MessageDTO> messages) { this.messages = messages; }
}
