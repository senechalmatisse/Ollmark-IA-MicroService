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
    @DisplayName("should return a stream of strings when chat is successful")
    void chatSuccess() {
        // Given
        ChatRequest request = new ChatRequest();
        request.setConversationId("conv-123");
        request.setMessage("Hello AI");
        
        when(conversationChatUseCase.chat("conv-123", "Hello AI", null))
                .thenReturn(Flux.just("Part 1", " Part 2"));

        // When
        Flux<String> result = aiController.chat(request);

        // Then
        StepVerifier.create(result)
                .expectNext("Part 1")
                .expectNext(" Part 2")
                .verifyComplete();
    }

    @Test
    @DisplayName("should return an error message in the flux when chat fails")
    void chatError() {
        // Given
        ChatRequest request = new ChatRequest();
        request.setConversationId("conv-123");
        when(conversationChatUseCase.chat(any(), any(), any()))
                .thenReturn(Flux.error(new RuntimeException("AI unreachable")));

        // When
        Flux<String> result = aiController.chat(request);

        // Then
        StepVerifier.create(result)
                .expectNext("[ERROR] AI unreachable")
                .verifyComplete();
    }

    // --- Tests pour startNewConversation ---

    @Test
    @DisplayName("should start a new conversation and return 200 OK")
    void startNewConversationSuccess() {
        // Given
        NewConversationRequest request = new NewConversationRequest();
        request.setUserId("user-456");
        when(conversationChatUseCase.startNewConversation("user-456")).thenReturn("new-conv-id");

        // When
        ResponseEntity<Map<String, Object>> response = aiController.startNewConversation(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("conversationId", "new-conv-id");
        assertThat(response.getBody()).containsEntry("userId", "user-456");
    }

    @Test
    @DisplayName("should handle failure when starting a new conversation")
    void startNewConversationFailure() {
        // Given
        when(conversationChatUseCase.startNewConversation(any())).thenThrow(new RuntimeException("DB Error"));

        // When
        ResponseEntity<Map<String, Object>> response = aiController.startNewConversation(null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("success", false);
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
    @DisplayName("should return 400 when clearing an invalid conversation ID")
    void clearConversationInvalidId() {
        // Given
        doThrow(new IllegalArgumentException("Invalid ID")).when(conversationChatUseCase).clearConversation(anyString());

        // When
        ResponseEntity<Map<String, Object>> response = aiController.clearConversation("wrong-id");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Invalid ID");
    }

    /**
     * Teste la création d'une conversation sans identifiant utilisateur (anonyme).
     * Couvre les branches ternaires 'ANONYMOUS_USER' dans startNewConversation.
     */
    @Test
    @DisplayName("should start an anonymous conversation when userId is null")
    void startNewConversationAnonymous() {
        // Given
        NewConversationRequest request = new NewConversationRequest();
        request.setUserId(null); 
        when(conversationChatUseCase.startNewConversation(null)).thenReturn("anon-conv-id");

        // When
        ResponseEntity<Map<String, Object>> response = aiController.startNewConversation(request);

        // Then
        assertThat(response.getBody()).containsEntry("userId", "anonymous");
        assertThat(response.getBody()).containsEntry("conversationId", "anon-conv-id");
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