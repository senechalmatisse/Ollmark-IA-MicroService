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

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ToolRetryLimiterAdvisorUnit {

    private ToolRetryLimiterAdvisor advisor;

    @Mock private CallAdvisorChain chain;
    @Mock private ChatClientRequest request;
    @Mock private ChatClientResponse response;
    @Mock private Prompt prompt;
    @Mock private Prompt augmentedPrompt;

    @BeforeEach
    void setUp() {
        advisor = new ToolRetryLimiterAdvisor();
    }

    // ─────────────────────────────────────────
    // Metadata
    // ─────────────────────────────────────────

    @Test
    void getName_returnsCorrectName() {
        assertThat(advisor.getName()).isEqualTo("ToolRetryLimiterAdvisor");
    }

    @Test
    void getOrder_returnsCorrectOrder() {
        assertThat(advisor.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 160);
    }

    // ─────────────────────────────────────────
    // Sans erreur détectée : le compteur ne bouge pas
    // ─────────────────────────────────────────

    @Test
    void adviseCall_noError_shouldPassThroughWithoutIncrementingCounter() {
        // Pas de toolErrorDetected dans le contexte
        when(request.context()).thenReturn(Map.of());
        when(request.prompt()).thenReturn(prompt);
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);

        // Le compteur doit rester absent (non incrémenté)
        verify(chain).nextCall(argThat(req ->
            !req.context().containsKey("toolRetryCount")
        ));
    }

    // ─────────────────────────────────────────
    // Premier retry avec erreur (0 → 1)
    // ─────────────────────────────────────────

    @Test
    void adviseCall_firstErrorRetry_shouldIncrementCounterToOne() {
        Map<String, Object> context = new HashMap<>();
        context.put("toolErrorDetected", true);
        // toolRetryCount absent → 0 par défaut

        when(request.context()).thenReturn(context);
        when(request.prompt()).thenReturn(prompt);
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);

        verify(chain).nextCall(argThat(req ->
            (int) req.context().get("toolRetryCount") == 1
        ));
    }

    // ─────────────────────────────────────────
    // Second retry avec erreur (1 → 2)
    // ─────────────────────────────────────────

    @Test
    void adviseCall_secondErrorRetry_shouldIncrementCounterToTwo() {
        Map<String, Object> context = new HashMap<>();
        context.put("toolErrorDetected", true);
        context.put("toolRetryCount", 1);

        when(request.context()).thenReturn(context);
        when(request.prompt()).thenReturn(prompt);
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

        advisor.adviseCall(request, chain);

        verify(chain).nextCall(argThat(req ->
            (int) req.context().get("toolRetryCount") == 2
        ));
    }

    // ─────────────────────────────────────────
    // Limite atteinte : injection du message stop
    // ─────────────────────────────────────────

    @Test
    void adviseCall_shouldStopRetryWhenLimitReached() {
        Map<String, Object> context = new HashMap<>();
        context.put("toolErrorDetected", true);
        context.put("toolRetryCount", 2); // déjà à MAX_RETRIES

        when(request.context()).thenReturn(context);
        when(request.prompt()).thenReturn(prompt);
        when(prompt.augmentSystemMessage(anyString())).thenReturn(augmentedPrompt);
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);

        verify(prompt).augmentSystemMessage(contains("TOOL RETRY LIMIT REACHED"));
        verify(chain).nextCall(any(ChatClientRequest.class));
    }

    // ─────────────────────────────────────────
    // Limite atteinte : le compteur ne doit pas être modifié
    // ─────────────────────────────────────────

    @Test
    void adviseCall_shouldPreserveCounterWhenLimitReached() {
        Map<String, Object> context = new HashMap<>();
        context.put("toolErrorDetected", true);
        context.put("toolRetryCount", 2);

        when(request.context()).thenReturn(context);
        when(request.prompt()).thenReturn(prompt);
        when(prompt.augmentSystemMessage(anyString())).thenReturn(augmentedPrompt);
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

        advisor.adviseCall(request, chain);

        verify(chain).nextCall(argThat(req ->
            (int) req.context().get("toolRetryCount") == 2
        ));
    }
}