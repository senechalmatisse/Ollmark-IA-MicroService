package com.penpot.ai.application.usecases;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.penpot.ai.application.persistance.Entity.AiModelConfig;
import com.penpot.ai.application.persistance.Entity.Project;
import com.penpot.ai.application.persistance.Repositories.AiModelConfigRepository;
import com.penpot.ai.application.persistance.Repositories.ProjectRepository;
import com.penpot.ai.application.service.PromptsConfigService;
import com.penpot.ai.core.ports.in.AiConfigUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiConfigUseCaseImpl implements AiConfigUseCase {

    private final AiModelConfigRepository aiModelConfigRepository;
    private final ProjectRepository projectRepository;
    private final PromptsConfigService promptsConfigService;

    @Override
    public Map<String, Object> getConfig(String projectId) {
        return aiModelConfigRepository.findByProjectId(UUID.fromString(projectId))
            .map(config -> {
                Map<String, Object> result = new HashMap<>();
                result.put("engine", config.getProvider());
                result.put("version", config.getModelName());
                if (config.getParameters() != null) result.putAll(config.getParameters());
                return result;
            })
            .orElseGet(() -> Map.of(
                "engine", "gemini",
                "version", "3.1-Pro",
                "temp", 1.5,
                "topK", 3
            ));
    }

    @Override
    @Transactional
    public void updateConfig(String projectId, Map<String, Object> configUpdates) {
        UUID projectUuid = UUID.fromString(projectId);
        Project project = projectRepository.findById(projectUuid)
                .orElseGet(() -> projectRepository.save(new Project(projectUuid, "Project " + projectId)));

        AiModelConfig config = aiModelConfigRepository.findByProjectId(projectUuid)
                .orElseGet(() -> {
                    AiModelConfig newConfig = new AiModelConfig();
                    newConfig.setProject(project);
                    return newConfig;
                });

        if (configUpdates.containsKey("apiKey")) {
            config.setModelApiKey((String) configUpdates.get("apiKey"));
        }
        if (configUpdates.containsKey("engine")) {
            config.setProvider((String) configUpdates.get("engine"));
        }
        if (configUpdates.containsKey("version")) {
            config.setModelName((String) configUpdates.get("version"));
        }

        final Map<String, Object> params = config.getParameters() != null ? 
                new HashMap<>(config.getParameters()) : new HashMap<>();

        configUpdates.forEach((key, value) -> {
            if (!key.equals("apiKey") && !key.equals("engine") && !key.equals("version")) {
                params.put(key, value);
            }
        });

        config.setParameters(params);
        aiModelConfigRepository.save(config);
    }

    @Override
    public String getPrompt(String projectId) {
        return aiModelConfigRepository.findByProjectId(UUID.fromString(projectId))
                .map(AiModelConfig::getPromptContent)
                .orElseGet(promptsConfigService::getInitialInstructions);
    }

    @Override
    @Transactional
    public void updatePrompt(String projectId, String prompt) {
        UUID projectUuid = UUID.fromString(projectId);
        Project project = projectRepository.findById(projectUuid)
            .orElseGet(() -> {
                Project newProject = new Project();
                newProject.setId(projectUuid); 
                newProject.setName("Project " + projectId);
                return projectRepository.save(newProject);
            });

        AiModelConfig config = aiModelConfigRepository.findByProjectId(projectUuid)
            .orElseGet(() -> {
                AiModelConfig newConfig = new AiModelConfig();
                newConfig.setProject(project);
                return newConfig;
            });

        config.setPromptContent(prompt);
        aiModelConfigRepository.save(config);
    }
}