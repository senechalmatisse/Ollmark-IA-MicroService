package com.penpot.ai.adapters.out.ai;

import com.penpot.ai.application.advisor.InspectionFirstAdvisor;
import com.penpot.ai.application.advisor.ToolErrorAdvisor;
import com.penpot.ai.application.advisor.ToolFailureRecoveryAdvisor;
import com.penpot.ai.application.advisor.ToolResultValidatorAdvisor;
import com.penpot.ai.application.advisor.ToolRetryLimiterAdvisor;
import com.penpot.ai.application.router.ToolCategoryResolver;
import com.penpot.ai.application.service.PromptsConfigService;
import com.penpot.ai.core.domain.*;
import com.penpot.ai.core.ports.out.ToolRouterPort;
import com.penpot.ai.infrastructure.config.OllamaConfig.ChatClientFactory;
import com.penpot.ai.shared.exception.ToolExecutionException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.*;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for {@link OllamaAiAdapter}.
 *
 * Uses Mockito to mock all collaborators (Spring AI, Ollama, RAG, router)
 * and validates the adapter's orchestration logic end-to-end.
 *
 * Naming convention: should[Description]_given[Context]_when[Action]
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OllamaAiAdapter — Integration Tests")
class OllamaAiAdapterTest {

    // ---- Collaborators ----
    @Mock private ChatClientFactory chatClientFactory;
    @Mock private RequestComplexityAnalyzer complexityAnalyzer;
    @Mock private ChatMemory chatMemory;
    @Mock private PromptsConfigService promptsConfigService;
    @Mock private RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;
    @Mock private ToolRouterPort toolRouter;
    @Mock private ToolCategoryResolver toolCategoryResolver;
    @Mock private InspectionFirstAdvisor inspectionFirstAdvisor;

    // ---- Spring AI fluent chain mocks ----
    @Mock private ChatClient chatClient;
    @Mock private ChatClientRequestSpec requestSpec;
    @Mock private StreamResponseSpec streamResponseSpec;
    @Mock private CallResponseSpec callResponseSpec;

    private OllamaAiAdapter adapter;
    ToolErrorAdvisor toolErrorAdvisor = new ToolErrorAdvisor();

    ToolFailureRecoveryAdvisor toolFailureRecoveryAdvisor = new ToolFailureRecoveryAdvisor();
    ToolRetryLimiterAdvisor toolRetryLimiterAdvisor = new ToolRetryLimiterAdvisor();
    ToolResultValidatorAdvisor toolResultValidatorAdvisor = new ToolResultValidatorAdvisor();

    @BeforeEach
    void setUp() {
        adapter = new OllamaAiAdapter(
            chatClientFactory,
            complexityAnalyzer,
            chatMemory,
            promptsConfigService,
            retrievalAugmentationAdvisor,
            toolRouter,
            toolCategoryResolver,
            inspectionFirstAdvisor,
            toolErrorAdvisor,
            toolFailureRecoveryAdvisor,
            toolRetryLimiterAdvisor,
            toolResultValidatorAdvisor
        );
    }

    // =========================================================================
    // chat()
    // =========================================================================

    @Nested
    @DisplayName("chat()")
    class ChatMethod {



        @Test
        @DisplayName("shouldIncludeRagAdvisor_givenCategoryIsTemplateSearch_whenChatIsCalled")
        void shouldIncludeRagAdvisor_givenCategoryIsTemplateSearch_whenChatIsCalled() {
            // GIVEN a message that routes to TEMPLATE_SEARCH category
            String conversationId = "conv-002";
            String userMessage = "Find a newsletter template";
            String userToken = "token-xyz";

            when(complexityAnalyzer.analyze(userMessage)).thenReturn(TaskComplexity.CREATIVE);
            when(toolRouter.route(userMessage)).thenReturn(Set.of(ToolCategory.TEMPLATE_SEARCH));
            when(toolCategoryResolver.resolveTools(any())).thenReturn(new Object[]{});
            when(chatClientFactory.buildForComplexity(TaskComplexity.CREATIVE)).thenReturn(chatClient);
            when(promptsConfigService.getInitialInstructions()).thenReturn("system prompt");
            mockFluentChainForStream(Flux.just("Template found"));

            // WHEN chat() is called
            Flux<String> result = adapter.chat(conversationId, userMessage, userToken);

            // THEN the stream should complete and the client was built with CREATIVE complexity
            StepVerifier.create(result)
                .expectNext("Template found")
                .verifyComplete();

            verify(chatClientFactory).buildForComplexity(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldIncludeRagAdvisor_givenCategoryIsShapeCreation_whenChatIsCalled")
        void shouldIncludeRagAdvisor_givenCategoryIsShapeCreation_whenChatIsCalled() {
            // GIVEN a message that routes to SHAPE_CREATION category
            String conversationId = "conv-003";
            String userMessage = "Create a rectangle";
            String userToken = "token-123";

            when(complexityAnalyzer.analyze(userMessage)).thenReturn(TaskComplexity.COMPLEX);
            when(toolRouter.route(userMessage)).thenReturn(Set.of(ToolCategory.SHAPE_CREATION));
            when(toolCategoryResolver.resolveTools(any())).thenReturn(new Object[]{});
            when(chatClientFactory.buildForComplexity(TaskComplexity.COMPLEX)).thenReturn(chatClient);
            when(promptsConfigService.getInitialInstructions()).thenReturn("system prompt");
            mockFluentChainForStream(Flux.just("Rectangle created"));

            // WHEN chat() is called
            Flux<String> result = adapter.chat(conversationId, userMessage, userToken);

            // THEN a COMPLEX client is built and the stream emits the response
            StepVerifier.create(result)
                .expectNext("Rectangle created")
                .verifyComplete();

            verify(chatClientFactory).buildForComplexity(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldPropagateError_givenStreamEmitsError_whenChatIsCalled")
        void shouldPropagateError_givenStreamEmitsError_whenChatIsCalled() {
            // GIVEN the stream emits an error mid-flight
            String conversationId = "conv-004";
            String userMessage = "Move element";
            String userToken = "token-err";
            RuntimeException streamError = new RuntimeException("Ollama connection lost");

            when(complexityAnalyzer.analyze(userMessage)).thenReturn(TaskComplexity.SIMPLE);
            when(toolRouter.route(userMessage)).thenReturn(Set.of(ToolCategory.INSPECTION));
            when(toolCategoryResolver.resolveTools(any())).thenReturn(new Object[]{});
            when(chatClientFactory.buildForComplexity(TaskComplexity.SIMPLE)).thenReturn(chatClient);
            when(promptsConfigService.getInitialInstructions()).thenReturn("system");
            mockFluentChainForStream(Flux.error(streamError));

            // WHEN chat() is called
            Flux<String> result = adapter.chat(conversationId, userMessage, userToken);

            // THEN the stream should propagate the error
            StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
        }

        @Test
        @DisplayName("shouldReturnErrorFlux_givenComplexityAnalyzerThrows_whenChatIsCalled")
        void shouldReturnErrorFlux_givenComplexityAnalyzerThrows_whenChatIsCalled() {
            // GIVEN the complexity analyzer throws an unexpected exception
            String conversationId = "conv-005";
            String userMessage = "Some message";

            when(complexityAnalyzer.analyze(userMessage))
                .thenThrow(new RuntimeException("analyzer failure"));

            // WHEN chat() is called
            Flux<String> result = adapter.chat(conversationId, userMessage, "token");

            // THEN a ToolExecutionException should be emitted
            StepVerifier.create(result)
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ToolExecutionException.class);
                    assertThat(e.getMessage()).contains("Stream init failed");
                })
                .verify();
        }

        @Test
        @DisplayName("shouldPassNullTokenAsEmpty_givenNullUserToken_whenChatIsCalled")
        void shouldPassNullTokenAsEmpty_givenNullUserToken_whenChatIsCalled() {
            // GIVEN a null user token
            String conversationId = "conv-006";
            String userMessage = "Delete element";

            when(complexityAnalyzer.analyze(userMessage)).thenReturn(TaskComplexity.SIMPLE);
            when(toolRouter.route(userMessage)).thenReturn(Set.of());
            when(toolCategoryResolver.resolveTools(any())).thenReturn(new Object[]{});
            when(chatClientFactory.buildForComplexity(TaskComplexity.SIMPLE)).thenReturn(chatClient);
            when(promptsConfigService.getInitialInstructions()).thenReturn("system");
            mockFluentChainForStream(Flux.just("Deleted"));

            // WHEN chat() is called with null token
            Flux<String> result = adapter.chat(conversationId, userMessage, null);

            // THEN it should complete without error (null token replaced by empty string)
            StepVerifier.create(result)
                .expectNext("Deleted")
                .verifyComplete();
        }

        @Test
        @DisplayName("shouldCallRouterOnce_givenAnyMessage_whenChatIsCalled")
        void shouldCallRouterOnce_givenAnyMessage_whenChatIsCalled() {
            // GIVEN a standard message
            String conversationId = "conv-007";
            String userMessage = "Rename this layer";

            when(complexityAnalyzer.analyze(userMessage)).thenReturn(TaskComplexity.SIMPLE);
            when(toolRouter.route(userMessage)).thenReturn(Set.of(ToolCategory.INSPECTION));
            when(toolCategoryResolver.resolveTools(any())).thenReturn(new Object[]{});
            when(chatClientFactory.buildForComplexity(any())).thenReturn(chatClient);
            when(promptsConfigService.getInitialInstructions()).thenReturn("system");
            mockFluentChainForStream(Flux.just("Renamed"));

            // WHEN chat() is called
            adapter.chat(conversationId, userMessage, "token").blockLast();

            // THEN the router should have been called exactly once
            verify(toolRouter, times(1)).route(userMessage);
        }
    }

    // =========================================================================
    // clearConversation()
    // =========================================================================

    @Nested
    @DisplayName("clearConversation()")
    class ClearConversationMethod {

        @Test
        @DisplayName("shouldClearChatMemory_givenValidConversationId_whenClearConversationIsCalled")
        void shouldClearChatMemory_givenValidConversationId_whenClearConversationIsCalled() {
            // GIVEN a valid conversation ID
            String conversationId = "clear-001";

            // WHEN clearConversation() is called
            adapter.clearConversation(conversationId);

            // THEN chatMemory.clear() must be invoked with that ID
            verify(chatMemory, times(1)).clear(conversationId);
        }

        @Test
        @DisplayName("shouldThrowIllegalArgumentException_givenNullConversationId_whenClearConversationIsCalled")
        void shouldThrowIllegalArgumentException_givenNullConversationId_whenClearConversationIsCalled() {
            // GIVEN a null conversation ID

            // WHEN / THEN clearConversation() should throw IllegalArgumentException
            assertThatThrownBy(() -> adapter.clearConversation(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Conversation ID cannot be null or empty");
        }

        @Test
        @DisplayName("shouldThrowIllegalArgumentException_givenBlankConversationId_whenClearConversationIsCalled")
        void shouldThrowIllegalArgumentException_givenBlankConversationId_whenClearConversationIsCalled() {
            // GIVEN a blank conversation ID
            String blankId = "   ";

            // WHEN / THEN clearConversation() should throw IllegalArgumentException
            assertThatThrownBy(() -> adapter.clearConversation(blankId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Conversation ID cannot be null or empty");
        }

        @Test
        @DisplayName("shouldThrowToolExecutionException_givenChatMemoryThrows_whenClearConversationIsCalled")
        void shouldThrowToolExecutionException_givenChatMemoryThrows_whenClearConversationIsCalled() {
            // GIVEN chatMemory.clear() throws a runtime exception
            String conversationId = "clear-002";
            doThrow(new RuntimeException("DB connection lost")).when(chatMemory).clear(conversationId);

            // WHEN / THEN a ToolExecutionException wrapping the original error should be thrown
            assertThatThrownBy(() -> adapter.clearConversation(conversationId))
                .isInstanceOf(ToolExecutionException.class)
                .hasMessageContaining("Failed to clear conversation");
        }

        @Test
        @DisplayName("shouldNotInteractWithRouter_givenValidConversationId_whenClearConversationIsCalled")
        void shouldNotInteractWithRouter_givenValidConversationId_whenClearConversationIsCalled() {
            // GIVEN a valid conversation ID
            String conversationId = "clear-003";

            // WHEN clearConversation() is called
            adapter.clearConversation(conversationId);

            // THEN no other collaborators should be touched
            verifyNoInteractions(toolRouter, complexityAnalyzer, chatClientFactory);
        }
    }

    // =========================================================================
    // Helpers — Spring AI fluent chain stubbing
    // =========================================================================

    /**
     * Stubs the full Spring AI fluent chain for streaming responses:
     * chatClient.prompt() → .system() → .user() → .advisors() → .advisors() → .tools()
     * → .toolContext() → .stream() → .content() → Flux
     */
    private void mockFluentChainForStream(Flux<String> content) {

        when(chatClient.prompt()).thenReturn(requestSpec);

        lenient().when(requestSpec.system(anyString())).thenReturn(requestSpec);
        lenient().when(requestSpec.user(anyString())).thenReturn(requestSpec);

        lenient().when(requestSpec.advisors(any(java.util.List.class))).thenReturn(requestSpec);
        lenient().when(requestSpec.advisors(any(java.util.function.Consumer.class))).thenReturn(requestSpec);

        lenient().when(requestSpec.tools(any(Object[].class))).thenReturn(requestSpec);
        lenient().when(requestSpec.toolContext(any())).thenReturn(requestSpec);

        // STREAM
        lenient().when(requestSpec.stream()).thenReturn(streamResponseSpec);
        lenient().when(streamResponseSpec.content()).thenReturn(content);

        // CALL
        lenient().when(requestSpec.call()).thenReturn(callResponseSpec);

        lenient().when(callResponseSpec.content()).thenAnswer(invocation -> {
            try {
                return content.blockFirst();
            } catch (Exception e) {
                return null;
            }
        });
    }
}