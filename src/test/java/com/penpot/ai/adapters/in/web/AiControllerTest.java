package com.penpot.ai.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.application.service.MessageService;
import com.penpot.ai.core.ports.in.AiConfigUseCase;
import com.penpot.ai.core.ports.in.ConversationChatUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;
import com.penpot.ai.infrastructure.config.SecurityConfig; 
import org.springframework.context.annotation.Import;     
import java.util.UUID;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

/**
 * Tests d'intégration pour {@link AiController}.
 * <p>
 * Utilise {@link WebMvcTest} pour tester la configuration Web de Spring, 
 * les mappings d'URL, la sérialisation JSON et le comportement des en-têtes HTTP.
 */
@WebMvcTest(value = AiController.class, properties = {
    "app.security.swagger.user=admin_audit",
    "app.security.swagger.password=admin_audit_secret",
    "SWAGGER_USER=admin_audit",
    "SWAGGER_PASSWORD=admin_audit_secret"
})
@Import(SecurityConfig.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConversationChatUseCase conversationChatUseCase;

    @MockBean
    private AiConfigUseCase aiConfigUseCase;

    @MockitoBean
    private MessageService messageService;

    /**
     * Test d'intégration de l'endpoint /api/ai/chat.
     * Vérifie que l'endpoint accepte les requêtes et produit un flux d'événements (SSE).
     */
    @Test
    @DisplayName("POST /api/ai/chat should return a JSON response")
    void chatIntegration() throws Exception {
        // Given
        String projectId = UUID.randomUUID().toString();
        ChatRequest request = new ChatRequest();
        request.setProjectId(projectId);
        request.setMessage("hello");

        when(conversationChatUseCase.chat(any(), any(), any()))
                .thenReturn(Mono.just("ai-response"));

        // When & Then
        // For reactive endpoints returning Mono, we must handle the async result
        org.springframework.test.web.servlet.MvcResult mvcResult = mockMvc.perform(post("/api/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.response").value("ai-response"));
    }

    /**
     * Test d'intégration de l'endpoint /api/ai/chat/new.
     * Vérifie le bon fonctionnement du mapping POST pour la création de conversation.
     */
    @Test
    @DisplayName("POST /ai/chat/new should return a success with projectId")
    void startNewConversationIntegration() throws Exception {
        // Given
        String projectId = UUID.randomUUID().toString();
        NewConversationRequest request = new NewConversationRequest();
        request.setProjectId(projectId);
        
        when(conversationChatUseCase.startNewConversation(any())).thenReturn(projectId);

        // When & Then
        mockMvc.perform(post("/api/ai/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.projectId").value(projectId));
    }

    /**
     * Test d'intégration de l'endpoint DELETE /ai/chat/{id}.
     * Vérifie la résolution de la variable de chemin (PathVariable).
     */
    @Test
    @DisplayName("DELETE /api/ai/chat/{projectId} should return success")
    void clearConversationIntegration() throws Exception {
        // Given
        String projectId = UUID.randomUUID().toString();

        // When & Then
        mockMvc.perform(delete("/api/ai/chat/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.projectId").value(projectId));
    }
}