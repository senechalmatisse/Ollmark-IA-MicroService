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
class ToolRetryLimiterAdvisorUnit {

    private ToolRetryLimiterAdvisor advisor;

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
        advisor = new ToolRetryLimiterAdvisor();
    }

    // ─────────────────────────────────────────
    // Metadata
    // ─────────────────────────────────────────

    @Test
    void getName_returnsCorrectName() {

        assertThat(advisor.getName())
                .isEqualTo("ToolRetryLimiterAdvisor");
    }

    @Test
    void getOrder_returnsCorrectOrder() {

        assertThat(advisor.getOrder())
                .isEqualTo(Ordered.HIGHEST_PRECEDENCE + 160);
    }

    // ─────────────────────────────────────────
    // First retry (retryCount absent → 0 → 1)
    // ─────────────────────────────────────────

    @Test
    void adviseCall_initialRetryCount_shouldAllowRetry() {

        when(request.context()).thenReturn(Map.of());
        when(request.prompt()).thenReturn(prompt);

        when(chain.nextCall(any(ChatClientRequest.class)))
                .thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);

        verify(chain).nextCall(argThat(req ->
                (int) req.context().get("toolRetryCount") == 1
        ));
    }

    // ─────────────────────────────────────────
    // Second retry (1 → 2)
    // ─────────────────────────────────────────

    @Test
    void adviseCall_secondRetry_shouldIncrementCounter() {

        when(request.context()).thenReturn(Map.of("toolRetryCount", 1));
        when(request.prompt()).thenReturn(prompt);

        when(chain.nextCall(any(ChatClientRequest.class)))
                .thenReturn(response);

        advisor.adviseCall(request, chain);

        verify(chain).nextCall(argThat(req ->
                (int) req.context().get("toolRetryCount") == 2
        ));
    }

    // ─────────────────────────────────────────
    // Retry limit reached
    // ─────────────────────────────────────────

    @Test
    void adviseCall_shouldStopRetryWhenLimitReached() {

        when(request.context()).thenReturn(Map.of("toolRetryCount", 2));
        when(request.prompt()).thenReturn(prompt);

        when(prompt.augmentSystemMessage(anyString()))
                .thenReturn(augmentedPrompt);

        when(chain.nextCall(any(ChatClientRequest.class)))
                .thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);

        verify(prompt).augmentSystemMessage(
                contains("TOOL RETRY LIMIT REACHED")
        );

        verify(chain).nextCall(any(ChatClientRequest.class));
    }

    // ─────────────────────────────────────────
    // Context must not change when limit reached
    // ─────────────────────────────────────────

    @Test
    void adviseCall_shouldPreserveContextWhenLimitReached() {

        Map<String,Object> context = Map.of("toolRetryCount", 2);

        when(request.context()).thenReturn(context);
        when(request.prompt()).thenReturn(prompt);

        when(prompt.augmentSystemMessage(anyString()))
                .thenReturn(augmentedPrompt);

        when(chain.nextCall(any(ChatClientRequest.class)))
                .thenReturn(response);

        advisor.adviseCall(request, chain);

        verify(chain).nextCall(argThat(req ->
                (int) req.context().get("toolRetryCount") == 2
        ));
    }
}