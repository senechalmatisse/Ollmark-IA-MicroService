package com.penpot.ai.application.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.penpot.ai.application.DTO.ProjectDTO;
import com.penpot.ai.application.controller.ProjectController;
import com.penpot.ai.application.service.ProjectService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectController - Tests")
@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private MockMvc mockMvc;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController).build();
        projectId = UUID.randomUUID();
    }

    @Test
    void getProject_returnsProject() throws Exception {
        // Préparer le DTO factice
        ProjectDTO dto = new ProjectDTO();
        dto.setId(projectId);
        dto.setName("Mon projet");

        // Mocker le service
        when(projectService.getProjectById(projectId)).thenReturn(dto);

        // Appel MockMvc
        mockMvc.perform(get("/api/ai/projects/{projectId}", projectId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Mon projet"));

        // Vérifier l'appel du service
        verify(projectService, times(1)).getProjectById(projectId);
    }

        @Test
    @DisplayName("deleteProject - retourne 204 et supprime le projet")
    void deleteProject_returns204() throws Exception {
        doNothing().when(projectService).deleteProject(projectId);

        mockMvc.perform(delete("/api/ai/projects/{projectId}", projectId))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).deleteProject(projectId);
    }

    @Test
    @DisplayName("deleteProject - appelle le service avec le bon projectId")
    void deleteProject_callsServiceWithCorrectId() throws Exception {
        doNothing().when(projectService).deleteProject(any(UUID.class));

        mockMvc.perform(delete("/api/ai/projects/{projectId}", projectId));

        verify(projectService).deleteProject(projectId);
        verifyNoMoreInteractions(projectService);
    }

    @Test
    @DisplayName("deleteProject - n'appelle le service qu'une seule fois")
    void deleteProject_callsServiceOnlyOnce() throws Exception {
        doNothing().when(projectService).deleteProject(any());

        mockMvc.perform(delete("/api/ai/projects/{projectId}", projectId));

        verify(projectService, times(1)).deleteProject(projectId);
    }
}