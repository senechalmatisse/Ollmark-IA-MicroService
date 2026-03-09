package com.penpot.ai.application.advisor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReReadingAdvisor — Unit")
class ReReadingAdvisorUnit {

    private ReReadingAdvisor advisor;

    @Mock
    private AdvisorChain advisorChain;

    @Mock
    private ChatClientRequest request;

    @Mock
    private ChatClientRequest.Builder requestBuilder;

    @BeforeEach
    void setUp() {
        advisor = new ReReadingAdvisor();
    }

    // ─────────────────────────────────────────────────────────────
    // getName / getOrder
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getName / getOrder")
    class MetadataTests {

        @Test
        @DisplayName("getName — returns 'ReReadingAdvisor'")
        void getName_returnsCorrectName() {
            assertThat(advisor.getName()).isEqualTo("ReReadingAdvisor");
        }

        @Test
        @DisplayName("getOrder — default constructor uses LOWEST_PRECEDENCE")
        void getOrder_defaultConstructorUsesLowestPrecedence() {
            assertThat(advisor.getOrder()).isEqualTo(Ordered.LOWEST_PRECEDENCE);
        }

        @Test
        @DisplayName("getOrder — custom order is correctly stored")
        void getOrder_customOrderIsCorrectlyStored() {
            ReReadingAdvisor customAdvisor = new ReReadingAdvisor(42);
            assertThat(customAdvisor.getOrder()).isEqualTo(42);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // before — transformation Re2
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("before — Re2 transformation")
    class BeforeTests {

        @Test
        @DisplayName("before — finds UserMessage at last position (single iteration)")
        void before_findsUserMessageAtLastPosition() {
            // GIVEN
            String userText = "question";
            String expectedText = "Read the question again: " + userText + "\n\n" + userText;

            // UserMessage en dernière position → la boucle entre directement dans le if dès
            // i = messages.size() - 1
            List<Message> messages = List.of(
                    new SystemMessage("system"),
                    new AssistantMessage("assistant"),
                    new UserMessage(userText) // Dernière position
            );
            Prompt prompt = new Prompt(messages);

            when(request.prompt()).thenReturn(prompt);
            when(request.mutate()).thenReturn(requestBuilder);
            when(requestBuilder.prompt(any(Prompt.class))).thenReturn(requestBuilder);
            when(requestBuilder.build()).thenReturn(request);

            // WHEN
            advisor.before(request, advisorChain);

            // THEN
            verify(requestBuilder).prompt(argThat(modifiedPrompt -> {
                List<Message> modifiedMessages = modifiedPrompt.getInstructions();

                // Le dernier message doit être modifié
                Message lastMessage = modifiedMessages.get(2);
                boolean isUserMessage = lastMessage instanceof UserMessage;
                boolean isModified = lastMessage.getText().equals(expectedText);

                return isUserMessage && isModified;
            }));
        }

        @Test
        @DisplayName("before — returns unchanged when no UserMessage in a non-empty list")
        void before_returnsUnchangedWhenNoUserMessageInNonEmptyList() {
            // GIVEN
            List<Message> messages = List.of(
                    new AssistantMessage("assistant"),
                    new SystemMessage("system"),
                    new AssistantMessage("assistant 2"));
            Prompt prompt = new Prompt(messages);

            when(request.prompt()).thenReturn(prompt);

            // WHEN
            ChatClientRequest result = advisor.before(request, advisorChain);

            // THEN
            assertThat(result).isSameAs(request);
            verify(request, never()).mutate();
        }

        @Test
        @DisplayName("before — handles multiple non-user messages before finding UserMessage")
        void before_handlesMultipleNonUserMessagesBeforeFindingUserMessage() {
            // GIVEN
            String userText = "question utilisateur";
            String expectedText = "Read the question again: " + userText + "\n\n" + userText;

            List<Message> messages = List.of(
                    new UserMessage(userText),
                    new AssistantMessage("réponse 1"),
                    new SystemMessage("instruction système"),
                    new AssistantMessage("réponse 2"));
            Prompt prompt = new Prompt(messages);

            when(request.prompt()).thenReturn(prompt);
            when(request.mutate()).thenReturn(requestBuilder);
            when(requestBuilder.prompt(any(Prompt.class))).thenReturn(requestBuilder);
            when(requestBuilder.build()).thenReturn(request);

            // WHEN
            advisor.before(request, advisorChain);

            // THEN
            verify(requestBuilder).prompt(argThat(modifiedPrompt -> {
                List<Message> modifiedMessages = modifiedPrompt.getInstructions();

                Message firstMessage = modifiedMessages.get(0);
                boolean isUserMessage = firstMessage instanceof UserMessage;
                boolean isModified = firstMessage.getText().equals(expectedText);

                boolean othersUnchanged = modifiedMessages.get(1).getText().equals("réponse 1")
                        && modifiedMessages.get(2).getText().equals("instruction système")
                        && modifiedMessages.get(3).getText().equals("réponse 2");

                return isUserMessage && isModified && othersUnchanged;
            }));
        }

        @Test
        @DisplayName("before — handles UserMessage at different positions in message list")
        void before_handlesUserMessageAtDifferentPositions() {
            // GIVEN
            String userText = "ma question";
            String expectedText = "Read the question again: " + userText + "\n\n" + userText;

            List<Message> messages = List.of(
                    new SystemMessage("système"),
                    new AssistantMessage("assistant 1"),
                    new UserMessage(userText),
                    new AssistantMessage("assistant 2"));
            Prompt prompt = new Prompt(messages);

            when(request.prompt()).thenReturn(prompt);
            when(request.mutate()).thenReturn(requestBuilder);
            when(requestBuilder.prompt(any(Prompt.class))).thenReturn(requestBuilder);
            when(requestBuilder.build()).thenReturn(request);

            // WHEN
            advisor.before(request, advisorChain);

            // THEN
            verify(requestBuilder).prompt(argThat(modifiedPrompt -> {
                List<Message> modifiedMessages = modifiedPrompt.getInstructions();

                boolean userMessageModified = modifiedMessages.get(2).getText().equals(expectedText);

                boolean othersUnchanged = modifiedMessages.get(0).getText().equals("système")
                        && modifiedMessages.get(1).getText().equals("assistant 1")
                        && modifiedMessages.get(3).getText().equals("assistant 2");

                return userMessageModified && othersUnchanged;
            }));
        }

        @Test
        @DisplayName("before — handles empty message list")
        void before_handlesEmptyMessageList() {
            // GIVEN
            List<Message> messages = List.of();
            Prompt prompt = new Prompt(messages);

            when(request.prompt()).thenReturn(prompt);

            // WHEN
            ChatClientRequest result = advisor.before(request, advisorChain);

            // THEN
            assertThat(result).isSameAs(request);
            verify(request, never()).mutate();
        }

        @Test
        @DisplayName("before — applies Re2 template to user message")
        void before_appliesRe2ToUserMessage() {
            // GIVEN
            String originalText = "mets du rouge sur ce rectangle";
            String expectedText = "Read the question again: " + originalText + "\n\n" + originalText;

            UserMessage userMessage = new UserMessage(originalText);
            Prompt prompt = new Prompt(List.of(userMessage));

            when(request.prompt()).thenReturn(prompt);
            when(request.mutate()).thenReturn(requestBuilder);
            when(requestBuilder.prompt(any(Prompt.class))).thenReturn(requestBuilder);
            when(requestBuilder.build()).thenReturn(request);

            // WHEN
            ChatClientRequest result = advisor.before(request, advisorChain);

            // THEN
            assertThat(result).isNotNull();

            verify(requestBuilder).prompt(argThat(modifiedPrompt -> {
                String modifiedText = modifiedPrompt.getInstructions().stream()
                        .filter(UserMessage.class::isInstance)
                        .map(Message::getText)
                        .findFirst()
                        .orElse("");
                return modifiedText.equals(expectedText);
            }));
        }

        @Test
        @DisplayName("before — returns request unchanged when no user message")
        void before_returnsUnchangedWhenNoUserMessage() {
            // GIVEN
            SystemMessage systemMessage = new SystemMessage("You are a helpful assistant.");
            Prompt prompt = new Prompt(List.of(systemMessage));

            when(request.prompt()).thenReturn(prompt);

            // WHEN
            ChatClientRequest result = advisor.before(request, advisorChain);

            // THEN
            assertThat(result).isSameAs(request);
            verify(request, never()).mutate();
        }

        @Test
        @DisplayName("before — returns request unchanged when user message is blank")
        void before_returnsUnchangedWhenUserMessageIsBlank() {
            // GIVEN
            UserMessage blankMessage = new UserMessage("   ");
            Prompt prompt = new Prompt(List.of(blankMessage));

            when(request.prompt()).thenReturn(prompt);

            // WHEN
            ChatClientRequest result = advisor.before(request, advisorChain);

            // THEN
            assertThat(result).isSameAs(request);
            verify(request, never()).mutate();
        }

        @Test
        @DisplayName("before — applies Re2 only to the last user message")
        void before_appliesRe2OnlyToLastUserMessage() {
            // GIVEN
            String firstUserText = "première question";
            String lastUserText = "dernière question";
            String expectedText = "Read the question again: " + lastUserText + "\n\n" + lastUserText;

            List<Message> messages = List.of(
                    new UserMessage(firstUserText),
                    new AssistantMessage("réponse intermédiaire"),
                    new UserMessage(lastUserText));
            Prompt prompt = new Prompt(messages);

            when(request.prompt()).thenReturn(prompt);
            when(request.mutate()).thenReturn(requestBuilder);
            when(requestBuilder.prompt(any(Prompt.class))).thenReturn(requestBuilder);
            when(requestBuilder.build()).thenReturn(request);

            // WHEN
            advisor.before(request, advisorChain);

            // THEN
            verify(requestBuilder).prompt(argThat(modifiedPrompt -> {
                List<Message> modifiedMessages = modifiedPrompt.getInstructions();
                String lastModified = modifiedMessages.stream()
                        .filter(UserMessage.class::isInstance)
                        .map(Message::getText)
                        .reduce((first, second) -> second)
                        .orElse("");
                return lastModified.equals(expectedText);
            }));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // after — aucune transformation
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("after — no transformation")
    class AfterTests {

        @Test
        @DisplayName("after — returns response unchanged")
        void after_returnsResponseUnchanged() {
            // GIVEN
            ChatClientResponse response = mock(ChatClientResponse.class);

            // WHEN
            ChatClientResponse result = advisor.after(response, advisorChain);

            // THEN
            assertThat(result).isSameAs(response);
        }
    }
}