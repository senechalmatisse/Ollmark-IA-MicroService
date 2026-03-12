package com.penpot.ai.application.persistance.Entity;



import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Lie une conversation à un projet et un utilisateur.
 * conversationId est l'identifiant métier (ex: "anonymous-bb24b241"),
 * distinct de id qui est la clé technique UUID.
 */
@Entity
@Table(
    name = "conversations",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_conversations_cid",
        columnNames = "conversation_id"
    ),
    indexes = @Index(
        name = "idx_conversations_project_id",
        columnList = "project_id"
    )
)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "conversation_id", nullable = false, length = 255)
    private String conversationId;

    @Column(name = "user_id", length = 255)
    private String userId;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Conversation() {}

    public Conversation(Project project, String conversationId, String userId) {
        this.project = project;
        this.conversationId = conversationId;
        this.userId = userId;
    }

    public java.util.UUID getId() { return id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<Message> getMessages() { return messages; }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
