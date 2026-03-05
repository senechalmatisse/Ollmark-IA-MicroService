package com.penpot.ai.application.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PromptsConfigService — Integration")
class PromptsConfigServiceTest {

    @Autowired
    private PromptsConfigService promptsConfigService;

    @Nested
    @DisplayName("init (@PostConstruct)")
    class InitTests {

        @Test
        @DisplayName("init — service is successfully loaded in Spring context without throwing")
        void init_serviceIsSuccessfullyLoadedInSpringContextWithoutThrowing() {
            // GIVEN / WHEN

            // THEN
            assertThat(promptsConfigService).isNotNull();
        }
    }

    @Nested
    @DisplayName("getInitialInstructions — after @PostConstruct")
    class GetInitialInstructionsIntegrationTests {

        @Test
        @DisplayName("getInitialInstructions — returns non-null non-blank value after context load")
        void getInitialInstructions_returnsNonNullNonBlankValueAfterContextLoad() {
            // GIVEN / WHEN
            String instructions = promptsConfigService.getInitialInstructions();

            // THEN
            assertThat(instructions).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("getInitialInstructions — returns a String (not null even if YAML key absent)")
        void getInitialInstructions_returnsStringEvenIfYamlKeyIsAbsent() {
            // GIVEN / WHEN
            String instructions = promptsConfigService.getInitialInstructions();

            // THEN
            assertThat(instructions).isInstanceOf(String.class);
        }
    }

    @Nested
    @DisplayName("reloadConfiguration — live reload")
    class ReloadConfigurationIntegrationTests {

        @Test
        @DisplayName("reloadConfiguration — does not throw and service remains functional after reload")
        void reloadConfiguration_doesNotThrowAndServiceRemainsFunctionalAfterReload() {
            // GIVEN / WHEN
            promptsConfigService.reloadConfiguration();

            // THEN
            assertThat(promptsConfigService.getInitialInstructions())
                .isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("reloadConfiguration — getInitialInstructions returns consistent value before and after reload")
        void reloadConfiguration_getInitialInstructionsReturnsConsistentValueBeforeAndAfterReload() {
            // GIVEN
            String before = promptsConfigService.getInitialInstructions();

            // WHEN
            promptsConfigService.reloadConfiguration();
            String after = promptsConfigService.getInitialInstructions();

            // THEN
            assertThat(after).isEqualTo(before);
        }
    }
}