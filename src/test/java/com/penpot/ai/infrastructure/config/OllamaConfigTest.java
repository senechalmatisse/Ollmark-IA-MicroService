package com.penpot.ai.infrastructure.config;

import com.penpot.ai.core.domain.TaskComplexity;
import com.penpot.ai.infrastructure.config.OllamaConfig.ChatClientFactory;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de {@link OllamaConfig}.
 *
 * Aucun contexte Spring — on instancie OllamaConfig directement
 * et on injecte les @Value via les setters du test.
 * Cela évite toute connexion vers Ollama, PostgreSQL ou Flyway.
 */
@DisplayName("OllamaConfig — Unit")
class OllamaConfigTest {

    private OllamaConfig config;

    @BeforeEach
    void setUp() {
        config = new OllamaConfig();
        // Injection manuelle des @Value (reflète application-test.yml)
        setField(config, "modelName",           "test-executor-model");
        setField(config, "defaultTemperature",  0.7);
        setField(config, "maxTokens",           1024);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Injecte une valeur dans un champ privé (remplace l'injection @Value Spring).
     */
    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Cannot inject field: " + fieldName, e);
        }
    }

    // ── simpleOptions ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("simpleOptions bean")
    class SimpleOptionsBeanTests {

        private OllamaChatOptions options;

        @BeforeEach
        void build() {
            options = config.simpleOptions();
        }

        @Test
        @DisplayName("bean non-null")
        void simpleOptions_isNotNull() {
            assertThat(options).isNotNull();
        }

        @Test
        @DisplayName("température = 0.1 (déterministe)")
        void simpleOptions_temperatureIs01() {
            assertThat(options.getTemperature()).isEqualTo(0.1);
        }

        @Test
        @DisplayName("topK = 3 (faible diversité)")
        void simpleOptions_topKIs3() {
            assertThat(options.getTopK()).isEqualTo(3);
        }
    }

    // ── creativeOptions ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("creativeOptions bean")
    class CreativeOptionsBeanTests {

        private OllamaChatOptions options;

        @BeforeEach
        void build() {
            options = config.creativeOptions();
        }

        @Test
        @DisplayName("bean non-null")
        void creativeOptions_isNotNull() {
            assertThat(options).isNotNull();
        }

        @Test
        @DisplayName("température = 0.8 (créativité élevée)")
        void creativeOptions_temperatureIs08() {
            assertThat(options.getTemperature()).isEqualTo(0.8);
        }

        @Test
        @DisplayName("topK = 5 (diversité élevée)")
        void creativeOptions_topKIs5() {
            assertThat(options.getTopK()).isEqualTo(5);
        }

        @Test
        @DisplayName("topP = 0.9")
        void creativeOptions_topPIs09() {
            assertThat(options.getTopP()).isEqualTo(0.9);
        }
    }

    // ── complexOptions ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("complexOptions bean")
    class ComplexOptionsBeanTests {

        private OllamaChatOptions options;

        @BeforeEach
        void build() {
            options = config.complexOptions();
        }

        @Test
        @DisplayName("bean non-null")
        void complexOptions_isNotNull() {
            assertThat(options).isNotNull();
        }

        @Test
        @DisplayName("température = valeur injectée depuis penpot.ai.executor.temperature")
        void complexOptions_temperatureMatchesExecutorProperty() {
            assertThat(options.getTemperature()).isEqualTo(0.7);
        }

        @Test
        @DisplayName("numCtx = 32000 (grand contexte pour l'orchestration)")
        void complexOptions_numCtxIs32000() {
            assertThat(options.getNumCtx()).isEqualTo(32000);
        }

        @Test
        @DisplayName("numPredict = valeur injectée depuis penpot.ai.executor.max-tokens")
        void complexOptions_numPredictMatchesMaxTokensProperty() {
            assertThat(options.getNumPredict()).isEqualTo(1024);
        }
    }

    // ── chatClientFactory ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("chatClientFactory")
    class ChatClientFactoryTests {

        private ChatClientFactory factory;

        @BeforeEach
        void build() {
            OllamaChatOptions simple   = config.simpleOptions();
            OllamaChatOptions creative = config.creativeOptions();
            OllamaChatOptions complex  = config.complexOptions();

            // Mock OllamaChatModel — ChatClient.builder() en a besoin
            OllamaChatModel chatModel = mock(OllamaChatModel.class);
            when(chatModel.getDefaultOptions()).thenReturn(simple);

            MessageChatMemoryAdvisor memoryAdvisor = mock(MessageChatMemoryAdvisor.class);

            factory = config.chatClientFactory(chatModel, memoryAdvisor, simple, creative, complex);
        }

        @Test
        @DisplayName("buildForComplexity retourne des instances distinctes à chaque appel")
        void factory_returnsDistinctInstancesPerCall() {
            ChatClient client1 = factory.buildForComplexity(TaskComplexity.SIMPLE);
            ChatClient client2 = factory.buildForComplexity(TaskComplexity.SIMPLE);

            assertThat(client1).isNotSameAs(client2);
        }

        @Test
        @DisplayName("buildForComplexity supporte tous les niveaux de complexité")
        void factory_supportsAllComplexityLevels() {
            for (TaskComplexity complexity : TaskComplexity.values()) {
                ChatClient client = factory.buildForComplexity(complexity);
                assertThat(client)
                        .as("buildForComplexity(%s) ne doit pas retourner null", complexity)
                        .isNotNull();
            }
        }
    }
}