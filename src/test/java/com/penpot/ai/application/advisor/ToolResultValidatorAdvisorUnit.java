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
class ToolResultValidatorAdvisorUnit {

    private ToolResultValidatorAdvisor advisor;

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
        advisor = new ToolResultValidatorAdvisor();
    }

    // ─────────────────────────────
    // Metadata
    // ─────────────────────────────

    @Test
    void getName_returnsCorrectName() {

        assertThat(advisor.getName())
                .isEqualTo("ToolResultValidatorAdvisor");
    }

    @Test
    void getOrder_returnsCorrectOrder() {

        assertThat(advisor.getOrder())
                .isEqualTo(Ordered.HIGHEST_PRECEDENCE + 170);
    }

    // ─────────────────────────────
    // Null handling
    // ─────────────────────────────

    @Test
    void adviseCall_returnsNull_whenResponseIsNull() {

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

    // ─────────────────────────────
    // Text validation
    // ─────────────────────────────

    @Test
    void adviseCall_returnsResponse_whenTextIsNull() {

        when(chain.nextCall(request)).thenReturn(response);

        when(response.chatResponse()).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText()).thenReturn(null);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
    }

    @Test
    void adviseCall_returnsResponse_whenTextIsBlank() {

        when(chain.nextCall(request)).thenReturn(response);

        when(response.chatResponse()).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText()).thenReturn("");

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
    }

    // ─────────────────────────────
    // Valid response
    // ─────────────────────────────

    @Test
    void adviseCall_returnsResponse_whenResultIsValid() {

        when(chain.nextCall(request)).thenReturn(response);

        when(response.chatResponse()).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText())
                .thenReturn("Rectangle created successfully");

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);

        verify(chain, times(1)).nextCall(request);
    }

    // ─────────────────────────────
    // Invalid UUID hallucination
    // ─────────────────────────────

    @Test
    void adviseCall_retriesWhenRect1Detected() {

        simulateInvalidResult("Move shape rect1 to x=200");
    }

    @Test
    void adviseCall_retriesWhenShape1Detected() {

        simulateInvalidResult("Move shape shape1");
    }

    @Test
    void adviseCall_retriesWhenMyRectangleDetected() {

        simulateInvalidResult("Resize myRectangle");
    }

    // ─────────────────────────────
    // Too short response
    // ─────────────────────────────

    @Test
    void adviseCall_retriesWhenResponseTooShort() {

        simulateInvalidResult("ok");
    }

    // ─────────────────────────────
    // Tool JSON error
    // ─────────────────────────────

    @Test
    void adviseCall_retriesWhenJsonErrorDetected() {

        simulateInvalidResult("{\"error\": true}");
    }

    // ─────────────────────────────
    // Helper method
    // ─────────────────────────────

    private void simulateInvalidResult(String text) {

        when(chain.nextCall(any(ChatClientRequest.class)))
                .thenReturn(response);

        when(response.chatResponse()).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText()).thenReturn(text);

        when(request.prompt()).thenReturn(prompt);
        when(request.context()).thenReturn(Map.of());

        when(prompt.augmentSystemMessage(anyString()))
                .thenReturn(augmentedPrompt);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);

        verify(prompt).augmentSystemMessage(contains("INVALID TOOL RESULT"));

        verify(chain, times(2))
                .nextCall(any(ChatClientRequest.class));
    }
}