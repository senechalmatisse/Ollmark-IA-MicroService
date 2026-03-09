package com.penpot.ai.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.penpot.ai.core.ports.out.AiServicePort;
import com.penpot.ai.shared.exception.ToolExecutionException;
import com.penpot.ai.shared.exception.ValidationException;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationChatUseCaseImpl — Unit")
class ConversationChatUseCaseImplUnit {

    @Mock
    private AiServicePort aiService;

    @InjectMocks
    private ConversationChatUseCaseImpl useCase;

    private static final String CONVERSATION_ID = "conv-123";
    private static final String MESSAGE = "Crée un post Instagram pour ma boulangerie";
    private static final String USER_TOKEN = "token-user-A";

    // ─────────────────────────────────────────────────────────────
    // chat
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("chat — validation et délégation")
    class ChatTests {

        @Test
        @DisplayName("chat — delegates to aiService and returns flux")
        void chat_delegatesToAiServiceAndReturnsFlux() {
            // GIVEN
            Flux<String> expected = Flux.just("réponse IA");
            when(aiService.chat(CONVERSATION_ID, MESSAGE, USER_TOKEN)).thenReturn(expected);

            // WHEN
            Flux<String> result = useCase.chat(CONVERSATION_ID, MESSAGE, USER_TOKEN);

            // THEN
            StepVerifier.create(result)
                    .expectNext("réponse IA")
                    .verifyComplete();

            verify(aiService).chat(CONVERSATION_ID, MESSAGE, USER_TOKEN);
        }

        @Test
        @DisplayName("chat — throws when conversationId is null")
        void chat_throwsWhenConversationIdIsNull() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.chat(null, MESSAGE, USER_TOKEN))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).chat(any(), any(), any());
        }

        @Test
        @DisplayName("chat — throws when conversationId is blank")
        void chat_throwsWhenConversationIdIsBlank() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.chat("  ", MESSAGE, USER_TOKEN))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).chat(any(), any(), any());
        }

        @Test
        @DisplayName("chat — throws when message is null")
        void chat_throwsWhenMessageIsNull() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.chat(CONVERSATION_ID, null, USER_TOKEN))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).chat(any(), any(), any());
        }

        @Test
        @DisplayName("chat — throws when message is blank")
        void chat_throwsWhenMessageIsBlank() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.chat(CONVERSATION_ID, "   ", USER_TOKEN))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).chat(any(), any(), any());
        }

        @Test
        @DisplayName("chat — throws when message exceeds 10000 characters")
        void chat_throwsWhenMessageExceedsMaxLength() {
            // GIVEN
            String tooLongMessage = "a".repeat(10001);

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.chat(CONVERSATION_ID, tooLongMessage, USER_TOKEN))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).chat(any(), any(), any());
        }

        @Test
        @DisplayName("chat — accepts message with exactly 10000 characters")
        void chat_acceptsMessageWithExactlyMaxLength() {
            // GIVEN
            String maxLengthMessage = "a".repeat(10000);
            Flux<String> expected = Flux.just("réponse IA");
            when(aiService.chat(CONVERSATION_ID, maxLengthMessage, USER_TOKEN)).thenReturn(expected);

            // WHEN
            Flux<String> result = useCase.chat(CONVERSATION_ID, maxLengthMessage, USER_TOKEN);

            // THEN
            StepVerifier.create(result)
                    .expectNext("réponse IA")
                    .verifyComplete();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // startNewConversation
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("startNewConversation — génération d'ID")
    class StartNewConversationTests {

        @Test
        @DisplayName("startNewConversation — generates ID with userId prefix")
        void startNewConversation_generatesIdWithUserIdPrefix() {
            // WHEN
            String conversationId = useCase.startNewConversation("alice");

            // THEN
            assertThat(conversationId)
                    .startsWith("user-alice-")
                    .hasSize("user-alice-".length() + 8);
        }

        @Test
        @DisplayName("startNewConversation — generates anonymous ID when userId is null")
        void startNewConversation_generatesAnonymousIdWhenUserIdIsNull() {
            // WHEN
            String conversationId = useCase.startNewConversation(null);

            // THEN
            assertThat(conversationId)
                    .startsWith("anonymous-")
                    .hasSize("anonymous-".length() + 8);
        }

        @Test
        @DisplayName("startNewConversation — generates anonymous ID when userId is blank")
        void startNewConversation_generatesAnonymousIdWhenUserIdIsBlank() {
            // WHEN
            String conversationId = useCase.startNewConversation("   ");

            // THEN
            assertThat(conversationId)
                    .startsWith("anonymous-")
                    .hasSize("anonymous-".length() + 8);
        }

        @Test
        @DisplayName("startNewConversation — generates unique IDs for each call")
        void startNewConversation_generatesUniqueIds() {
            // WHEN
            String id1 = useCase.startNewConversation("alice");
            String id2 = useCase.startNewConversation("alice");

            // THEN
            assertThat(id1).isNotEqualTo(id2);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // clearConversation
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("clearConversation — validation et délégation")
    class ClearConversationTests {

        @Test
        @DisplayName("clearConversation — delegates to aiService")
        void clearConversation_delegatesToAiService() {
            // GIVEN
            doNothing().when(aiService).clearConversation(CONVERSATION_ID);

            // WHEN
            useCase.clearConversation(CONVERSATION_ID);

            // THEN
            verify(aiService).clearConversation(CONVERSATION_ID);
        }

        @Test
        @DisplayName("clearConversation — throws when conversationId is null")
        void clearConversation_throwsWhenConversationIdIsNull() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.clearConversation(null))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).clearConversation(any());
        }

        @Test
        @DisplayName("clearConversation — throws when conversationId is blank")
        void clearConversation_throwsWhenConversationIdIsBlank() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.clearConversation("  "))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).clearConversation(any());
        }

        @Test
        @DisplayName("clearConversation — rethrows IllegalArgumentException directly")
        void clearConversation_rethrowsIllegalArgumentExceptionDirectly() {
            // GIVEN
            doThrow(new IllegalArgumentException("invalid id"))
                    .when(aiService).clearConversation(CONVERSATION_ID);

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.clearConversation(CONVERSATION_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("invalid id");
        }

        @Test
        @DisplayName("clearConversation — wraps unexpected exception in ToolExecutionException")
        void clearConversation_wrapsUnexpectedExceptionInToolExecutionException() {
            // GIVEN
            doThrow(new RuntimeException("unexpected error"))
                    .when(aiService).clearConversation(CONVERSATION_ID);

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.clearConversation(CONVERSATION_ID))
                    .isInstanceOf(ToolExecutionException.class)
                    .hasMessageContaining("Failed to clear conversation");
        }
    }
}