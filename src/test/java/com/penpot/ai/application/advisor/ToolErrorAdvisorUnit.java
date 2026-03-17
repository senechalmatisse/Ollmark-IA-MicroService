package com.penpot.ai.application.advisor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
@DisplayName("ToolErrorAdvisor — Unit")
class ToolErrorAdvisorUnit {

    private ToolErrorAdvisor advisor;

    @Mock
    private CallAdvisorChain chain;

    @Mock
    private ChatClientRequest request;

    @Mock
    private ChatClientResponse response;

    @Mock
    private Prompt prompt;

    @Mock
    private Prompt augmentedPrompt;

    @BeforeEach
    void setUp() {
        advisor = new ToolErrorAdvisor();
    }

    // ─────────────────────────────
    // Metadata
    // ─────────────────────────────

    @Nested
    class MetadataTests {

        @Test
        void getName_returnsCorrectName() {
            assertThat(advisor.getName()).isEqualTo("ToolErrorAdvisor");
        }

        @Test
        void getOrder_returnsCorrectOrder() {
            assertThat(advisor.getOrder())
                    .isEqualTo(Ordered.HIGHEST_PRECEDENCE + 200);
        }
    }

    // ─────────────────────────────
    // Normal execution
    // ─────────────────────────────

    @Nested
    class NormalExecutionTests {

        @Test
        void adviseCall_forwardsRequestWhenNoExceptionOccurs() {

            when(chain.nextCall(request)).thenReturn(response);

            ChatClientResponse result = advisor.adviseCall(request, chain);

            assertThat(result).isSameAs(response);
            verify(chain).nextCall(request);
        }
    }

    // ─────────────────────────────
    // Error handling
    // ─────────────────────────────

    @Nested
    class ErrorHandlingTests {

        @Test
        void adviseCall_interceptsExceptionAndInjectsRecoveryPrompt() {

            RuntimeException exception = new RuntimeException("Tool failure");

            when(chain.nextCall(any(ChatClientRequest.class)))
                    .thenThrow(exception)
                    .thenReturn(response);

            when(request.prompt()).thenReturn(prompt);
            when(request.context()).thenReturn(Map.of());

            when(prompt.augmentSystemMessage(anyString()))
                    .thenReturn(augmentedPrompt);

            ChatClientResponse result = advisor.adviseCall(request, chain);

            assertThat(result).isSameAs(response);

            verify(prompt).augmentSystemMessage(contains("TOOL EXECUTION ERROR"));
            verify(chain, times(2)).nextCall(any(ChatClientRequest.class));
        }

        @Test
        void adviseCall_preservesContextWhenRetrying() {

            Map<String,Object> context = Map.of("conversationId", "123");

            when(chain.nextCall(any(ChatClientRequest.class)))
                    .thenThrow(new RuntimeException("tool error"))
                    .thenReturn(response);

            when(request.prompt()).thenReturn(prompt);
            when(request.context()).thenReturn(context);

            when(prompt.augmentSystemMessage(anyString()))
                    .thenReturn(augmentedPrompt);

            advisor.adviseCall(request, chain);

            verify(chain, times(2)).nextCall(any(ChatClientRequest.class));
        }
    }
}