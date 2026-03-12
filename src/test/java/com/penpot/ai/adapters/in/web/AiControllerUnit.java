package com.penpot.ai.adapters.in.web;

import com.penpot.ai.core.ports.in.ConversationChatUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link AiController}.
 * Couvre les endpoints d'exécution de code, de chat et de gestion de conversation.
 */
@ExtendWith(MockitoExtension.class)
class AiControllerUnit {

    @Mock
    private ConversationChatUseCase conversationChatUseCase;

    @InjectMocks
    private AiController aiController;

    // --- Tests pour chat (Flux) ---

    @Test
    @DisplayName("should return a Mono with success details when chat is successful")
    void chatSuccess() {
        // Given
        ChatRequest request = new ChatRequest();
        request.setProjectId("proj-123");
        request.setMessage("Hello AI");
        
        when(conversationChatUseCase.chat("proj-123", "Hello AI"))
                .thenReturn(Mono.just("AI response message"));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = aiController.chat(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).containsEntry("success", true);
                    assertThat(body).containsEntry("projectId", "proj-123");
                    assertThat(body).containsEntry("response", "AI response message");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should return an error response when chat fails")
    void chatError() {
        // Given
        ChatRequest request = new ChatRequest();
        request.setProjectId("proj-123");
        when(conversationChatUseCase.chat(any(), any()))
                .thenReturn(Mono.error(new RuntimeException("AI unreachable")));

        // When
        Mono<ResponseEntity<Map<String, Object>>> result = aiController.chat(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    Map<String, Object> body = response.getBody();
                    assertThat(body).containsEntry("success", false);
                    assertThat(body).containsEntry("error", "AI unreachable");
                })
                .verifyComplete();
    }

    // --- Tests pour startNewConversation ---

    @Test
    @DisplayName("should start a new conversation and return 200 OK")
    void startNewConversationSuccess() {
        // Given
        NewConversationRequest request = new NewConversationRequest();
        request.setProjectId("proj-123");
        when(conversationChatUseCase.startNewConversation("proj-123")).thenReturn("proj-123");

        // When
        ResponseEntity<Map<String, Object>> response = aiController.startNewConversation(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
        assertThat(response.getBody()).containsEntry("projectId", "proj-123");
    }

    @Test
    @DisplayName("should handle failure when request is null")
    void startNewConversationNullRequest() {
        // When
        ResponseEntity<Map<String, Object>> response = aiController.startNewConversation(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("success", false);
    }

    @Test
    @DisplayName("should handle service failure when starting a new conversation")
    void startNewConversationServiceFailure() {
        // Given
        NewConversationRequest request = new NewConversationRequest();
        request.setProjectId("proj-123");
        when(conversationChatUseCase.startNewConversation("proj-123")).thenThrow(new RuntimeException("DB Error"));

        // When
        ResponseEntity<Map<String, Object>> response = aiController.startNewConversation(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("success", false);
        assertThat(response.getBody()).containsEntry("error", "DB Error");
    }

    // --- Tests pour clearConversation ---

    @Test
    @DisplayName("should clear conversation and return 200 OK")
    void clearConversationSuccess() {
        // Given
        String conversationId = "conv-to-delete";

        // When
        ResponseEntity<Map<String, Object>> response = aiController.clearConversation(conversationId);

        // Then
        verify(conversationChatUseCase, times(1)).clearConversation(conversationId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
    }

    @Test
    @DisplayName("should return 500 when clearing fails")
    void clearConversationFailure() {
        // Given
        doThrow(new RuntimeException("Error")).when(conversationChatUseCase).clearConversation(anyString());

        // When
        ResponseEntity<Map<String, Object>> response = aiController.clearConversation("proj-123");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("success", false);
    }

    /**
     * Teste la création d'une conversation sans identifiant de projet.
     */
    @Test
    @DisplayName("should start a conversation with null projectId")
    void startNewConversationWithNullProject() {
        // Given
        NewConversationRequest request = new NewConversationRequest();
        request.setProjectId(null); 
        when(conversationChatUseCase.startNewConversation(null)).thenReturn(null);

        // When
        ResponseEntity<Map<String, Object>> response = aiController.startNewConversation(request);

        // Then
        assertThat(response.getBody()).containsEntry("projectId", null);
    }

    /**
     * Teste la génération d'une réponse d'erreur avec un message null.
     * Couvre la branche ternaire 'Unknown error' dans buildErrorResponse.
     */
    @Test
    @DisplayName("should return 'Unknown error' when exception message is null")
    void clearConversationWithNullExceptionMessage() {
        // Given
        // On simule une exception qui n'a pas de message (ex: NullPointerException)
        doThrow(new RuntimeException((String) null)).when(conversationChatUseCase).clearConversation(anyString());

        // When
        ResponseEntity<Map<String, Object>> response = aiController.clearConversation("conv-id");

        // Then
        assertThat(response.getBody()).containsEntry("error", "Unknown error");
    }
}