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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationChatUseCaseImpl — Unit")
class ConversationChatUseCaseImplUnit {

    @Mock
    private AiServicePort aiService;

    @InjectMocks
    private ConversationChatUseCaseImpl useCase;

    private static final String PROJECT_ID = "proj-123";
    private static final String MESSAGE = "Crée un post Instagram pour ma boulangerie";
    private static final String SESSION_ID = "ses-123";

    // ─────────────────────────────────────────────────────────────
    // chat
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("chat — validation et délégation")
    class ChatTests {

        @Test
        @DisplayName("chat — delegates to aiService and returns mono")
        void chat_delegatesToAiServiceAndReturnsMono() {
            // GIVEN
            Flux<String> aiResponse = Flux.just("réponse ", "IA");
            when(aiService.chat(PROJECT_ID, MESSAGE)).thenReturn(aiResponse);

            // WHEN
            Mono<String> result = useCase.chat(PROJECT_ID, MESSAGE, SESSION_ID);

            // THEN
            StepVerifier.create(result)
                    .expectNext("réponse IA")
                    .verifyComplete();

            verify(aiService).chat(PROJECT_ID, MESSAGE);
        }

        @Test
        @DisplayName("chat — throws when projectId is null")
        void chat_throwsWhenProjectIdIsNull() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.chat(null, MESSAGE, SESSION_ID))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).chat(any(), any());
        }

        @Test
        @DisplayName("chat — throws when projectId is blank")
        void chat_throwsWhenProjectIdIsBlank() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.chat("  ", MESSAGE, SESSION_ID))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).chat(any(), any());
        }

        @Test
        @DisplayName("chat — throws when message is null")
        void chat_throwsWhenMessageIsNull() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.chat(PROJECT_ID, null, SESSION_ID))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).chat(any(), any());
        }

        @Test
        @DisplayName("chat — throws when message is blank")
        void chat_throwsWhenMessageIsBlank() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.chat(PROJECT_ID, "   ", SESSION_ID))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).chat(any(), any());
        }

        @Test
        @DisplayName("chat — throws when sessionId is null")
        void chat_throwsWhenSessionIdIsNull() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.chat(PROJECT_ID, MESSAGE, null))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).chat(any(), any());
        }

        @Test
        @DisplayName("chat — throws when sessionId is blank")
        void chat_throwsWhenSessionIdIsBlank() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.chat(PROJECT_ID, MESSAGE, "   "))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).chat(any(), any());
        }

        @Test
        @DisplayName("chat — throws when message exceeds 10000 characters")
        void chat_throwsWhenMessageExceedsMaxLength() {
            // GIVEN
            String tooLongMessage = "a".repeat(10001);

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.chat(PROJECT_ID, tooLongMessage, SESSION_ID))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).chat(any(), any());
        }

        @Test
        @DisplayName("chat — accepts message with exactly 10000 characters")
        void chat_acceptsMessageWithExactlyMaxLength() {
            // GIVEN
            String maxLengthMessage = "a".repeat(10000);
            Flux<String> aiResponse = Flux.just("réponse IA");
            when(aiService.chat(PROJECT_ID, maxLengthMessage)).thenReturn(aiResponse);

            // WHEN
            Mono<String> result = useCase.chat(PROJECT_ID, maxLengthMessage, SESSION_ID);

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
        @DisplayName("startNewConversation — returns projectId")
        void startNewConversation_returnsProjectId() {
            // WHEN
            String result = useCase.startNewConversation(PROJECT_ID);

            // THEN
            assertThat(result).isEqualTo(PROJECT_ID);
        }

        @Test
        @DisplayName("startNewConversation — works for any projectId")
        void startNewConversation_worksForAnyProjectId() {
            // WHEN
            String id1 = useCase.startNewConversation("proj-A");
            String id2 = useCase.startNewConversation("proj-B");

            // THEN
            assertThat(id1).isEqualTo("proj-A");
            assertThat(id2).isEqualTo("proj-B");
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
            doNothing().when(aiService).clearConversation(PROJECT_ID);

            // WHEN
            useCase.clearConversation(PROJECT_ID);

            // THEN
            verify(aiService).clearConversation(PROJECT_ID);
        }

        @Test
        @DisplayName("clearConversation — throws when projectId is null")
        void clearConversation_throwsWhenProjectIdIsNull() {
            // WHEN / THEN
            assertThatThrownBy(() -> useCase.clearConversation(null))
                    .isInstanceOf(ValidationException.class);

            verify(aiService, never()).clearConversation(any());
        }

        @Test
        @DisplayName("clearConversation — throws when projectId is blank")
        void clearConversation_throwsWhenProjectIdIsBlank() {
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
                    .when(aiService).clearConversation(PROJECT_ID);

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.clearConversation(PROJECT_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("invalid id");
        }

        @Test
        @DisplayName("clearConversation — wraps unexpected exception in ToolExecutionException")
        void clearConversation_wrapsUnexpectedExceptionInToolExecutionException() {
            // GIVEN
            doThrow(new RuntimeException("unexpected error"))
                    .when(aiService).clearConversation(PROJECT_ID);

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.clearConversation(PROJECT_ID))
                    .isInstanceOf(ToolExecutionException.class)
                    .hasMessageContaining("Failed to clear conversation");
        }
    }
}