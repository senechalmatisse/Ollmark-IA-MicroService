package com.penpot.ai.application.service;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.penpot.ai.application.DTO.ProjectDTO;
import com.penpot.ai.application.persistance.Entity.Project;
import com.penpot.ai.application.persistance.Repositories.ProjectRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectDTO getProjectById(UUID projectId) {
        Project p = projectRepository.findById(projectId).get();
        // TODO ajouter convesation
        
        return new ProjectDTO(
            p.getId(),
            p.getName(),
            null
        );
    }

    // Supprimer un projet et toutes ses données (CascadeType.ALL sur conversations et aiModelConfigs)
    @Transactional
    public void deleteProject(UUID projectId) {
        log.debug("Suppression du projet {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + projectId));
        projectRepository.delete(project);
        log.debug("Projet {} supprimé avec toutes ses données en cascade", projectId);
    }
}