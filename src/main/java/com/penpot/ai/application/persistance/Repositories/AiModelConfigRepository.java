package com.penpot.ai.application.persistance.Repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.penpot.ai.application.persistance.Entity.AiModelConfig;

@Repository
public interface AiModelConfigRepository extends JpaRepository<AiModelConfig, UUID> {
    Optional<AiModelConfig> findByProjectId(UUID projectId);
}