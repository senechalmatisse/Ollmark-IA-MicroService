package com.penpot.ai.application.persistance.Entity;


import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Configuration LLM par projet.
 * Permet de surcharger le modèle, le prompt système et les paramètres
 * d'inférence (température, top_p, etc.) pour un projet donné.
 */
@Entity
@Table(name = "ai_model_config")
public class AiModelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "prompt_content", columnDefinition = "text")
    private String promptContent;

    @Column(name = "model_name", length = 255)
    private String modelName;

    @Column(name = "provider", length = 255)
    private String provider;

    /**
     * Paramètres d'inférence LLM (ex: temperature, top_p, max_tokens).
     * Stockés en JSONB pour flexibilité selon les providers.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parameters", columnDefinition = "jsonb")
    private Map<String, Object> parameters;

    @Column(name = "model_api_key", columnDefinition = "text")
    private String modelApiKey;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AiModelConfig() {}

    public AiModelConfig(Project project, String modelName, String provider) {
        this.project = project;
        this.modelName = modelName;
        this.provider = provider;
    }

    @PrePersist
    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() { return id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getPromptContent() { return promptContent; }
    public void setPromptContent(String promptContent) { this.promptContent = promptContent; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public String getModelApiKey() { return modelApiKey; }
    public void setModelApiKey(String modelApiKey) { this.modelApiKey = modelApiKey; }

    public Instant getUpdatedAt() { return updatedAt; }
}
