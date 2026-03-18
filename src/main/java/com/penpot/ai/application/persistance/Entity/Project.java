package com.penpot.ai.application.persistance.Entity;

import java.util.*;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "projects")
public class Project implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Conversation> conversations = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AiModelConfig> aiModelConfigs = new ArrayList<>();

    @Transient
    private boolean isNew = false;

    public Project() {}

    public Project(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public Project(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.isNew = true;
    }

    /**
     * Marque l'entité comme nouvelle pour forcer un INSERT plutôt qu'un merge.
     * Appelé dans MessagePersistenceService avant save().
     */
    public Project markAsNew() {
        this.isNew = true;
        return this;
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return isNew; }

    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Conversation> getConversations() { return conversations; }
    public List<AiModelConfig> getAiModelConfigs() { return aiModelConfigs; }
}