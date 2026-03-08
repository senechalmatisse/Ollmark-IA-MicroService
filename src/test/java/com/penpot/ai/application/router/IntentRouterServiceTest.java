package com.penpot.ai.application.router;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration Spring pour {@link IntentRouterService}.
 *
 * <p>Ce test vérifie uniquement que le bean est correctement chargé
 * dans le contexte Spring. La logique métier du routage est testée
 * dans les tests unitaires.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("IntentRouterService — Integration tests")
public class IntentRouterServiceTest {

    @Autowired
    private IntentRouterService intentRouterService;

    @Test
    @DisplayName("context — loads IntentRouterService bean")
    void context_loadsIntentRouterServiceBean() {
        assertThat(intentRouterService).isNotNull();
    }
}