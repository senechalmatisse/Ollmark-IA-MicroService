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

import static org.mockito.Mockito.*;
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
        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Mon projet"));

        // Vérifier l'appel du service
        verify(projectService, times(1)).getProjectById(projectId);
    }
}