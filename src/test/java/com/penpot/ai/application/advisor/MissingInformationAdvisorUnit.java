package com.penpot.ai.application.advisor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;

@ExtendWith(MockitoExtension.class)
@DisplayName("MissingInformationAdvisor — Unit")
class MissingInformationAdvisorUnit {

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

    private MissingInformationAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new MissingInformationAdvisor();
    }

    @Nested
    @DisplayName("getName / getOrder")
    class MetadataTests {

        @Test
        @DisplayName("getName — returns 'MissingInformationAdvisor'")
        void getName_returnsCorrectName() {
            assertThat(advisor.getName()).isEqualTo("MissingInformationAdvisor");
        }

        @Test
        @DisplayName("getOrder — returns HIGHEST_PRECEDENCE + 40")
        void getOrder_returnsExpectedOrder() {
            assertThat(advisor.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 40);
        }
    }

    @Test
    @DisplayName("adviseCall — augments prompt and delegates to chain")
    void adviseCall_augmentsPromptAndDelegates() {
        Map<String, Object> context = new HashMap<>();
        context.put("conversationId", "conv-1");

        when(request.prompt()).thenReturn(prompt);
        when(request.context()).thenReturn(context);
        when(prompt.augmentSystemMessage(any(String.class))).thenReturn(augmentedPrompt);
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(prompt).augmentSystemMessage(any(String.class));
        verify(chain).nextCall(any(ChatClientRequest.class));
    }
}
