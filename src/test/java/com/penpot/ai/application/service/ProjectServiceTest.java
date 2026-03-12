package com.penpot.ai.application.service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.penpot.ai.application.persistance.Entity.Project;
import com.penpot.ai.application.persistance.Repositories.ProjectRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService - Tests DELETE")
class ProjectServiceDeleteTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    // =========================================================
    // deleteProject
    // =========================================================

    @Test
    @DisplayName("deleteProject - supprime le projet existant")
    void deleteProject_deletesProject() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project("Mon Projet");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(projectRepository).delete(project);

        projectService.deleteProject(projectId);

        verify(projectRepository, times(1)).findById(projectId);
        verify(projectRepository, times(1)).delete(project);
    }

    @Test
    @DisplayName("deleteProject - lève NoSuchElementException si projet inexistant")
    void deleteProject_notFound_throwsException() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(projectId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(projectId.toString());

        verify(projectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteProject - ne supprime pas si projet inexistant")
    void deleteProject_notFound_neverCallsDelete() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        try { projectService.deleteProject(projectId); } catch (NoSuchElementException ignored) {}

        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    @DisplayName("deleteProject - supprime exactement le bon projet")
    void deleteProject_deletesCorrectEntity() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project("Projet Cible");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        projectService.deleteProject(projectId);

        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("deleteProject - n'appelle le repo qu'une seule fois pour findById")
    void deleteProject_callsFindByIdOnlyOnce() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project("Test");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        projectService.deleteProject(projectId);

        verify(projectRepository, times(1)).findById(projectId);
    }
}