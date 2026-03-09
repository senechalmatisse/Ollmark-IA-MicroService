package com.penpot.ai.application.tools.engine;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

public class MarketingIntentUnit {

    @Test
    @DisplayName("Logic Helpers: Vérification des conditions de campagnes haute pression et premium")
    void testLogicHelpers() {
        // Cas Haute Pression (Conversion + Urgence + Persuasion > 70)
        MarketingIntent highPressure = new MarketingIntent(
                true, true, false, false, false, false,
                50, 90, 
                MarketingIntent.CtaStrength.AGGRESSIVE,
                MarketingIntent.HierarchyLevel.BALANCED,
                MarketingIntent.CampaignType.FLASH_SALE
        );
        assertThat(highPressure.isHighPressureCampaign()).isTrue();

        // Cas Premium Branding (Luxury + Pas d'urgence)
        MarketingIntent premium = new MarketingIntent(
                false, false, false, true, false, false,
                50, 50,
                MarketingIntent.CtaStrength.SOFT,
                MarketingIntent.HierarchyLevel.SOFT_BRANDING,
                MarketingIntent.CampaignType.PREMIUM_SHOWCASE
        );
        assertThat(premium.isPremiumBranding()).isTrue();

        // Cas Layout Centré (Branding ou Minimaliste)
        MarketingIntent branding = new MarketingIntent(
                false, false, false, false, false, true, 
                50, 50,
                null, null, MarketingIntent.CampaignType.BRANDING
        );
        assertThat(branding.shouldCenterLayout()).isTrue();
    }
}