package com.penpot.ai.application.DTO;

import java.util.List;
import java.util.UUID;

public class ProjectDTO {

    private UUID id;
    private String name;
    private List<ConversationDTO> conversations;

    public ProjectDTO() {}

    public ProjectDTO(UUID id, String name, List<ConversationDTO> conversations) {
        this.id = id;
        this.name = name;
        this.conversations = conversations;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<ConversationDTO> getConversations() { return conversations; }
    public void setConversations(List<ConversationDTO> conversations) { this.conversations = conversations; }
}
