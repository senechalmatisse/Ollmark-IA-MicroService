package com.penpot.ai.application.advisor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ToolFailureRecoveryAdvisorUnit {

    private ToolFailureRecoveryAdvisor advisor;

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

    @Mock
    private org.springframework.ai.chat.model.ChatResponse chatResponse;

    @Mock
    private org.springframework.ai.chat.model.Generation generation;

    @Mock
    private org.springframework.ai.chat.messages.AssistantMessage assistantMessage;

    @BeforeEach
    void setUp() {
        advisor = new ToolFailureRecoveryAdvisor();
    }

    // ─────────────────────────────────────
    // Metadata
    // ─────────────────────────────────────

    @Test
    void getName_returnsCorrectName() {

        assertThat(advisor.getName())
                .isEqualTo("ToolFailureRecoveryAdvisor");
    }

    @Test
    void getOrder_returnsCorrectOrder() {

        assertThat(advisor.getOrder())
                .isEqualTo(Ordered.HIGHEST_PRECEDENCE + 150);
    }

    // ─────────────────────────────────────
    // Null response handling
    // ─────────────────────────────────────

    @Test
    void adviseCall_returnsNullResponse_whenResponseIsNull() {

        when(chain.nextCall(request)).thenReturn(null);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isNull();
    }

    @Test
    void adviseCall_returnsResponse_whenChatResponseIsNull() {

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse()).thenReturn(null);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
    }

    // ─────────────────────────────────────
    // Normal tool result (no error)
    // ─────────────────────────────────────

    @Test
    void adviseCall_returnsResponse_whenNoToolErrorDetected() {

        when(chain.nextCall(request)).thenReturn(response);

        when(response.chatResponse()).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText()).thenReturn("{\"result\": \"ok\"}");

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);

        verify(chain, times(1)).nextCall(request);
    }

    // ─────────────────────────────────────
    // Tool functional error detected
    // ─────────────────────────────────────

    @Test
    void adviseCall_retriesWhenToolReturnsError() {

        when(chain.nextCall(any(ChatClientRequest.class)))
                .thenReturn(response);

        when(response.chatResponse()).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText())
                .thenReturn("{\"error\": true, \"message\": \"Shape not found\"}");

        when(request.prompt()).thenReturn(prompt);
        when(request.context()).thenReturn(Map.of());

        when(prompt.augmentSystemMessage(anyString()))
                .thenReturn(augmentedPrompt);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);

        verify(prompt).augmentSystemMessage(contains("TOOL EXECUTION FAILED"));

        verify(chain, times(2))
                .nextCall(any(ChatClientRequest.class));
    }

    // ─────────────────────────────────────
    // Context preservation during retry
    // ─────────────────────────────────────

    @Test
    void adviseCall_preservesContextWhenRetrying() {

        Map<String,Object> context = Map.of("conversationId","42");

        when(chain.nextCall(any(ChatClientRequest.class)))
                .thenReturn(response);

        when(response.chatResponse()).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText())
                .thenReturn("{\"error\": true}");

        when(request.prompt()).thenReturn(prompt);
        when(request.context()).thenReturn(context);

        when(prompt.augmentSystemMessage(anyString()))
                .thenReturn(augmentedPrompt);

        advisor.adviseCall(request, chain);

        verify(chain, times(2))
                .nextCall(any(ChatClientRequest.class));
    }

}