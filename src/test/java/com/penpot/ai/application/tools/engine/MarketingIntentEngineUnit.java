package com.penpot.ai.application.tools.engine;

import com.penpot.ai.core.domain.spec.SectionSpec;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

public class MarketingIntentEngineUnit{

    private MarketingIntentEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MarketingIntentEngine();
    }

    @Test
    @DisplayName("analyze: Devrait retourner une intention valide")
    void analyze_ShouldReturnValidIntent() {
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Test Title");
        
        MarketingIntent intent = engine.analyze(spec);

        assertThat(intent).isNotNull();
        assertThat(intent.campaignType).isNotNull();
        assertThat(intent.ctaStrength).isNotNull();
    }
}