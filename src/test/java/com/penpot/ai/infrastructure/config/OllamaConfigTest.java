package com.penpot.ai.infrastructure.config;

import com.penpot.ai.core.domain.TaskComplexity;
import com.penpot.ai.infrastructure.config.OllamaConfig.ChatClientFactory;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OllamaConfig — Integration")
public class OllamaConfigTest {

    @Autowired
    @Qualifier("simpleOptions")
    private OllamaChatOptions simpleOptions;

    @Autowired
    @Qualifier("creativeOptions")
    private OllamaChatOptions creativeOptions;

    @Autowired
    @Qualifier("complexOptions")
    private OllamaChatOptions complexOptions;

    @Autowired
    @Qualifier("executorChatClientBuilder")
    private ChatClient.Builder executorChatClientBuilder;

    @Autowired
    @Qualifier("executorChatClient")
    private ChatClient executorChatClient;

    @Autowired
    private ChatClientFactory chatClientFactory;

    @Nested
    @DisplayName("simpleOptions bean")
    class SimpleOptionsBeanTests {

        @Test
        @DisplayName("simpleOptions — bean is loaded in Spring context and is not null")
        void simpleOptions_beanIsLoadedInSpringContextAndIsNotNull() {
            // GIVEN / WHEN / THEN
            assertThat(simpleOptions).isNotNull();
        }

        @Test
        @DisplayName("simpleOptions — temperature is 0.1 (deterministic)")
        void simpleOptions_temperatureIs01() {
            // GIVEN / WHEN / THEN
            assertThat(simpleOptions.getTemperature()).isEqualTo(0.1);
        }

        @Test
        @DisplayName("simpleOptions — topK is 3 (low diversity)")
        void simpleOptions_topKIs3() {
            // GIVEN / WHEN / THEN
            assertThat(simpleOptions.getTopK()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("creativeOptions bean")
    class CreativeOptionsBeanTests {

        @Test
        @DisplayName("creativeOptions — bean is loaded in Spring context and is not null")
        void creativeOptions_beanIsLoadedInSpringContextAndIsNotNull() {
            // GIVEN / WHEN / THEN
            assertThat(creativeOptions).isNotNull();
        }

        @Test
        @DisplayName("creativeOptions — temperature is 0.8 (high creativity)")
        void creativeOptions_temperatureIs08() {
            // GIVEN / WHEN / THEN
            assertThat(creativeOptions.getTemperature()).isEqualTo(0.8);
        }

        @Test
        @DisplayName("creativeOptions — topK is 5 (high diversity)")
        void creativeOptions_topKIs5() {
            // GIVEN / WHEN / THEN
            assertThat(creativeOptions.getTopK()).isEqualTo(5);
        }

        @Test
        @DisplayName("creativeOptions — topP is 0.9")
        void creativeOptions_topPIs09() {
            // GIVEN / WHEN / THEN
            assertThat(creativeOptions.getTopP()).isEqualTo(0.9);
        }
    }

    @Nested
    @DisplayName("complexOptions bean")
    class ComplexOptionsBeanTests {

        @Test
        @DisplayName("complexOptions — bean is loaded in Spring context and is not null")
        void complexOptions_beanIsLoadedInSpringContextAndIsNotNull() {
            // GIVEN / WHEN / THEN
            assertThat(complexOptions).isNotNull();
        }

        @Test
        @DisplayName("complexOptions — temperature is 0.0 (balanced reasoning)")
        void complexOptions_temperatureIs00() {
            // GIVEN / WHEN / THEN
            assertThat(complexOptions.getTemperature()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("chatClientFactory bean")
    class ChatClientFactoryBeanTests {

        @Test
        @DisplayName("chatClientFactory — buildForComplexity returns distinct ChatClient instances per call")
        void chatClientFactory_buildForComplexityReturnsDistinctInstancesPerCall() {
            // GIVEN / WHEN
            ChatClient client1 = chatClientFactory.buildForComplexity(TaskComplexity.SIMPLE);
            ChatClient client2 = chatClientFactory.buildForComplexity(TaskComplexity.SIMPLE);

            // THEN
            assertThat(client1).isNotSameAs(client2);
        }
    }
}