package com.penpot.ai.application.tools.engine;

import com.penpot.ai.core.domain.spec.SectionSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LayoutRendererUnit {

    private final LayoutRenderer renderer = new LayoutRenderer();

    @Test
    @DisplayName("render: Devrait générer du code JS valide avec un thème réel")
    void render_ShouldGenerateJsCode() {
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Hello World");
        
        Theme theme = Theme.DARK_SAAS;
        
        MarketingIntent intent = new MarketingIntent(
                true, true, false, false, true, false,
                80, 80,
                MarketingIntent.CtaStrength.STRONG,
                MarketingIntent.HierarchyLevel.STRONG_HERO,
                MarketingIntent.CampaignType.PRODUCT_LAUNCH
        );

        String js = renderer.render(LayoutTemplate.SPLIT_RIGHT_IMAGE, spec, theme, intent);

        assertThat(js).contains("penpot");
        assertThat(js).contains("Hello World");
    }
}