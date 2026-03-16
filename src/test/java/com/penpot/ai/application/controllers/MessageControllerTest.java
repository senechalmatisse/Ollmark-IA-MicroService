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

import com.penpot.ai.application.DTO.MessageDTO;
import com.penpot.ai.application.controller.MessageController;
import com.penpot.ai.application.service.MessageService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageController - Tests")
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    private MockMvc mockMvc;
    private UUID conversationId;
    private UUID messageId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();
        conversationId = UUID.randomUUID();
        messageId = UUID.randomUUID();
    }

    // =========================================================
    // GET /api/ai/messages/conversation/{conversationId}
    // =========================================================

    @Test
    @DisplayName("getLastMessages - retourne 20 messages par défaut si nMessages non fourni")
    void getLastMessages_defaultNMessages_returns20() throws Exception {
        List<MessageDTO> messages = buildMessages(conversationId, 20);
        when(messageService.getLastMessages(conversationId, 20)).thenReturn(messages);

        mockMvc.perform(get("/api/ai/messages/conversation/{conversationId}", conversationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(20));

        verify(messageService).getLastMessages(conversationId, 20);
    }

    @Test
    @DisplayName("getLastMessages - retourne N messages si nMessages fourni")
    void getLastMessages_customNMessages_returnsN() throws Exception {
        List<MessageDTO> messages = buildMessages(conversationId, 5);
        when(messageService.getLastMessages(conversationId, 5)).thenReturn(messages);

        mockMvc.perform(get("/api/ai/messages/conversation/{conversationId}", conversationId)
                        .param("nMessages", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));

        verify(messageService).getLastMessages(conversationId, 5);
    }

    @Test
    @DisplayName("getLastMessages - retourne liste vide si aucun message")
    void getLastMessages_noMessages_returnsEmptyList() throws Exception {
        when(messageService.getLastMessages(conversationId, 20)).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/messages/conversation/{conversationId}", conversationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("getLastMessages - les messages retournés ont le bon conversationId et contenu")
    void getLastMessages_returnsMessagesWithCorrectFields() throws Exception {
        MessageDTO msg = buildMessage(conversationId, "Hello world");
        when(messageService.getLastMessages(conversationId, 20)).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/ai/messages/conversation/{conversationId}", conversationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conversationId").value(conversationId.toString()));
    }

    @Test
    @DisplayName("getLastMessages - appelle le service avec le bon conversationId et nMessages")
    void getLastMessages_callsServiceWithCorrectParams() throws Exception {
        when(messageService.getLastMessages(any(UUID.class), anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/messages/conversation/{conversationId}", conversationId)
                .param("nMessages", "10"));

        verify(messageService, times(1)).getLastMessages(conversationId, 10);
        verifyNoMoreInteractions(messageService);
    }

    @Test
    @DisplayName("getLastMessages - nMessages=1 retourne un seul message")
    void getLastMessages_nMessagesOne_returnsSingleMessage() throws Exception {
        MessageDTO msg = buildMessage(conversationId, "Dernier message");
        when(messageService.getLastMessages(conversationId, 1)).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/ai/messages/conversation/{conversationId}", conversationId)
                        .param("nMessages", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // =========================================================
    // GET /api/ai/messages/conversation/{conversationId}/last
    // =========================================================

    @Test
    @DisplayName("getLastMessage - retourne le dernier message de la conversation")
    void getLastMessage_returnsLastMessage() throws Exception {
        MessageDTO msg = buildMessage(conversationId, "Dernier message");
        when(messageService.getLastMessage(conversationId)).thenReturn(msg);

        mockMvc.perform(get("/api/ai/messages/conversation/{conversationId}/last", conversationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(conversationId.toString()));

        verify(messageService, times(1)).getLastMessage(conversationId);
    }

    @Test
    @DisplayName("getLastMessage - appelle le service avec le bon conversationId")
    void getLastMessage_callsServiceWithCorrectId() throws Exception {
        when(messageService.getLastMessage(any(UUID.class)))
                .thenReturn(buildMessage(conversationId, "msg"));

        mockMvc.perform(get("/api/ai/messages/conversation/{conversationId}/last", conversationId));

        verify(messageService).getLastMessage(conversationId);
        verifyNoMoreInteractions(messageService);
    }

    @Test
    @DisplayName("getLastMessage - retourne null si aucun message dans la conversation")
    void getLastMessage_noMessage_returnsNull() throws Exception {
        when(messageService.getLastMessage(conversationId)).thenReturn(null);

        mockMvc.perform(get("/api/ai/messages/conversation/{conversationId}/last", conversationId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(messageService, times(1)).getLastMessage(conversationId);
    }

    @Test
    @DisplayName("getLastMessage - appelle le service une seule fois")
    void getLastMessage_callsServiceOnlyOnce() throws Exception {
        when(messageService.getLastMessage(conversationId))
                .thenReturn(buildMessage(conversationId, "msg"));

        mockMvc.perform(get("/api/ai/messages/conversation/{conversationId}/last", conversationId));

        verify(messageService, times(1)).getLastMessage(conversationId);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private List<MessageDTO> buildMessages(UUID conversationId, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> buildMessage(conversationId, "Message " + i))
                .toList();
    }

    private MessageDTO buildMessage(UUID conversationId, String content) {
        MessageDTO dto = new MessageDTO();
        dto.setId(UUID.randomUUID());
        dto.setConversationId(conversationId);
        dto.setContentUser(content);
        return dto;
    }

    @Test
    @DisplayName("clearConversationMessages - retourne 204 et vide les messages")
    void clearConversationMessages_returns204() throws Exception {
        doNothing().when(messageService).deleteAllByConversationId(conversationId);

        mockMvc.perform(delete("/api/ai/messages/conversation/{conversationId}", conversationId))
                .andExpect(status().isNoContent());

        verify(messageService, times(1)).deleteAllByConversationId(conversationId);
    }

    @Test
    @DisplayName("clearConversationMessages - appelle le service avec le bon conversationId")
    void clearConversationMessages_callsServiceWithCorrectId() throws Exception {
        doNothing().when(messageService).deleteAllByConversationId(any(UUID.class));

        mockMvc.perform(delete("/api/ai/messages/conversation/{conversationId}", conversationId));

        verify(messageService).deleteAllByConversationId(conversationId);
        verifyNoMoreInteractions(messageService);
    }

    // =========================================================
    // DELETE /api/ai/messages/{messageId}
    // =========================================================

    @Test
    @DisplayName("deleteMessage - retourne 204 et supprime le message")
    void deleteMessage_returns204() throws Exception {
        doNothing().when(messageService).deleteById(messageId);

        mockMvc.perform(delete("/api/ai/messages/{messageId}", messageId))
                .andExpect(status().isNoContent());

        verify(messageService, times(1)).deleteById(messageId);
    }

    @Test
    @DisplayName("deleteMessage - appelle le service avec le bon messageId")
    void deleteMessage_callsServiceWithCorrectId() throws Exception {
        doNothing().when(messageService).deleteById(any(UUID.class));

        mockMvc.perform(delete("/api/ai/messages/{messageId}", messageId));

        verify(messageService).deleteById(messageId);
        verifyNoMoreInteractions(messageService);
    }
}