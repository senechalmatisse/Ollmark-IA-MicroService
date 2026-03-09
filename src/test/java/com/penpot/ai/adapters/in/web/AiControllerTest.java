package com.penpot.ai.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.core.ports.in.ConversationChatUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour {@link AiController}.
 * <p>
 * Utilise {@link WebMvcTest} pour tester la configuration Web de Spring, 
 * les mappings d'URL, la sérialisation JSON et le comportement des en-têtes HTTP.
 */
@WebMvcTest(AiController.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConversationChatUseCase conversationChatUseCase;

    /**
     * Test d'intégration de l'endpoint /ai/chat.
     * Vérifie que l'endpoint accepte les requêtes et produit un flux d'événements (SSE).
     */
    @Test
    @DisplayName("POST /ai/chat should return a text stream")
    void chatIntegration() throws Exception {
        // Given
        ChatRequest request = new ChatRequest();
        request.setConversationId("123");
        request.setMessage("hello");

        when(conversationChatUseCase.chat(any(), any(), any()))
                .thenReturn(Flux.just("stream-data"));

        // When & Then
        mockMvc.perform(post("/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    /**
     * Test d'intégration de l'endpoint /ai/chat/new.
     * Vérifie le bon fonctionnement du mapping POST pour la création de conversation.
     */
    @Test
    @DisplayName("POST /ai/chat/new should return a new conversation ID")
    void startNewConversationIntegration() throws Exception {
        // Given
        NewConversationRequest request = new NewConversationRequest();
        request.setUserId("user1");
        
        when(conversationChatUseCase.startNewConversation("user1")).thenReturn("conv-xyz");

        // When & Then
        mockMvc.perform(post("/ai/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value("conv-xyz"))
                .andExpect(jsonPath("$.userId").value("user1"));
    }

    /**
     * Test d'intégration de l'endpoint DELETE /ai/chat/{id}.
     * Vérifie la résolution de la variable de chemin (PathVariable).
     */
    @Test
    @DisplayName("DELETE /ai/chat/{id} should return success")
    void clearConversationIntegration() throws Exception {
        // When & Then
        mockMvc.perform(delete("/ai/chat/conv-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.conversationId").value("conv-123"));
    }
}