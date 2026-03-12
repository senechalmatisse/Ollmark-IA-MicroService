package com.penpot.ai.application.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.penpot.ai.application.DTO.ConversationDTO;
import com.penpot.ai.application.DTO.ConversationMetaDataDTO;
import com.penpot.ai.application.controller.ConversationController;
import com.penpot.ai.application.service.ConversationService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationController - Tests")
class ConversationControllerTest {

    private static final String BASE_URL = "/api/conversations";

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private ConversationController conversationController;

    private MockMvc mockMvc;

    private String projectId;
    private String userId;
    private String conversationId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(conversationController).build();
        projectId = UUID.randomUUID().toString();
        userId = "user-" + UUID.randomUUID().toString().substring(0, 8);
        conversationId = UUID.randomUUID().toString();
    }

    // =========================================================
    // GET /api/conversations/project/{projectId}
    // =========================================================

    @Test
    @DisplayName("getProjectConversations - retourne une page de conversations pour un projet")
    void getProjectConversations_returnsPage() throws Exception {
        ConversationDTO dto = buildConversationDTO(conversationId, projectId, userId);
        Page<ConversationDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(conversationService.getAllProjectConversations(projectId)).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/project/{projectId}", projectId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(conversationId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(conversationService, times(1)).getAllProjectConversations(projectId);
    }

    @Test
    @DisplayName("getProjectConversations - retourne une page vide si aucune conversation")
    void getProjectConversations_emptyPage() throws Exception {
        Page<ConversationDTO> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(conversationService.getAllProjectConversations(projectId)).thenReturn(emptyPage);

        mockMvc.perform(get(BASE_URL + "/project/{projectId}", projectId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(conversationService, times(1)).getAllProjectConversations(projectId);
    }

    @Test
    @DisplayName("getProjectConversations - appelle le service avec le bon projectId")
    void getProjectConversations_callsServiceWithCorrectId() throws Exception {
        when(conversationService.getAllProjectConversations(projectId))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(BASE_URL + "/project/{projectId}", projectId));

        verify(conversationService).getAllProjectConversations(projectId);
        verifyNoMoreInteractions(conversationService);
    }

    // =========================================================
    // GET /api/conversations/project/{projectId}/user/{userId}
    // =========================================================

    @Test
    @DisplayName("getUserProjectConversations - retourne les conversations d'un user dans un projet")
    void getUserProjectConversations_returnsPage() throws Exception {
        ConversationDTO dto = buildConversationDTO(conversationId, projectId, userId);
        Page<ConversationDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(conversationService.getAllConversationsByUserIdAndProjectId(userId, projectId))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/project/{projectId}/user/{userId}", projectId, userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(conversationId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(conversationService, times(1))
                .getAllConversationsByUserIdAndProjectId(userId, projectId);
    }

    // @Test
    // @DisplayName("getUserProjectConversations - retourne page vide si aucune conversation")
    // void getUserProjectConversations_emptyPage() throws Exception {
    //     when(conversationService.getAllConversationsByUserIdAndProjectId(userId, projectId))
    //             .thenReturn(new PageImpl<>(List.of()));

    //     mockMvc.perform(get(BASE_URL + "/project/{projectId}/user/{userId}", projectId, userId)
    //                     .accept(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.content").isEmpty());

    //     verify(conversationService, times(1))
    //             .getAllConversationsByUserIdAndProjectId(userId, projectId);
    // }

    @Test
    @DisplayName("getUserProjectConversations - appelle le service avec userId et projectId corrects")
    void getUserProjectConversations_callsServiceWithCorrectIds() throws Exception {
        when(conversationService.getAllConversationsByUserIdAndProjectId(userId, projectId))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(BASE_URL + "/project/{projectId}/user/{userId}", projectId, userId));

        verify(conversationService).getAllConversationsByUserIdAndProjectId(userId, projectId);
        verifyNoMoreInteractions(conversationService);
    }

    // =========================================================
    // GET /api/conversations/{conversationId}/metadata
    // =========================================================

    @Test
    @DisplayName("getConversationMetaData - retourne les métadonnées d'une conversation")
    void getConversationMetaData_returnsMetaData() throws Exception {
        ConversationMetaDataDTO metaDataDTO = buildMetaDataDTO(conversationId, projectId, userId);

        when(conversationService.getConversationMetaData(conversationId)).thenReturn(metaDataDTO);

        mockMvc.perform(get(BASE_URL + "/{conversationId}/metadata", conversationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(conversationId.toString()));

        verify(conversationService, times(1)).getConversationMetaData(conversationId);
    }

    @Test
    @DisplayName("getConversationMetaData - appelle le service avec le bon conversationId")
    void getConversationMetaData_callsServiceWithCorrectId() throws Exception {
        when(conversationService.getConversationMetaData(conversationId))
                .thenReturn(buildMetaDataDTO(conversationId, projectId, userId));

        mockMvc.perform(get(BASE_URL + "/{conversationId}/metadata", conversationId));

        verify(conversationService).getConversationMetaData(conversationId);
        verifyNoMoreInteractions(conversationService);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private ConversationDTO buildConversationDTO(String id, String projectId, String userId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(id);
        dto.setConversationId(id); 
        dto.setProjectId(projectId);
        dto.setUserId(userId);
        return dto;
    }

    private ConversationMetaDataDTO buildMetaDataDTO(String conversationId, String projectId, String userId) {
        ConversationMetaDataDTO dto = new ConversationMetaDataDTO();
        dto.setId(UUID.fromString(conversationId));
        dto.setConversationId(conversationId);
        dto.setProjectId(UUID.fromString(projectId));
        dto.setUserId(userId);
        dto.setCreatedAt(Instant.now());
        return dto;
    }
}
