package com.penpot.ai.application.tools.engine;

import com.penpot.ai.core.domain.spec.SectionSpec;
import com.penpot.ai.shared.util.JsStringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Moteur de sélection de thème visuel pour une section.
 *
 * <p>Cette classe est responsable de déterminer le {@link Theme}
 * le plus approprié à appliquer lors du rendu d'une section.
 * La sélection s'appuie sur plusieurs sources d'information :</p>
 */
public class ThemeEngine {

    /** Ensemble de thèmes disponibles pouvant être utilisés */
    private static final List<Theme> THEME_POOL = Theme.all();

    /**
     * Sélectionne le thème visuel le plus adapté pour une section.
     *
     * <p>La sélection suit un ordre de priorité :</p>
     * <ol>
     *     <li>Utilisation du style explicitement défini dans {@link SectionSpec}</li>
     *     <li>Application de règles basées sur l'intention marketing</li>
     *     <li>Sélection pseudo-aléatoire mais déterministe dans un pool de thèmes</li>
     * </ol>
     *
     * @param spec spécification de la section contenant le contenu et les préférences visuelles
     * @param intent intention marketing détectée pour la section
     * @return thème visuel sélectionné
     */
    public Theme pickTheme(SectionSpec spec, MarketingIntent intent) {
        if (spec.getStyle() != null) {
            switch (spec.getStyle()) {
                case MODERN_GRADIENT -> { return Theme.STARTUP_PURPLE; }
                case DARK_SAAS -> { return Theme.DARK_SAAS; }
                case GLASSMORPHISM -> { return Theme.MINIMAL_LIGHT;  }
            }
        }

        if (intent.isHighPressureCampaign()) return Theme.ECOMMERCE_RED;
        if (intent.isPremiumBranding()) return Theme.LUXURY_BLACK;
        if (intent.isMinimalist) return Theme.MINIMAL_LIGHT;
        if (intent.containsSocialProof) return Theme.STARTUP_PURPLE;
        if (intent.isProductCentric) return Theme.MODERN_GREEN;

        long seed = stableSeed(spec);
        return THEME_POOL.get((int) Math.floorMod(seed, THEME_POOL.size()));
    }

    /**
     * Génère une graine déterministe à partir du contenu d'une section.
     *
     * <p>Cette graine est utilisée afin de sélectionner un thème
     * de manière stable lorsque plusieurs options sont possibles.
     * Ainsi, un même contenu produira toujours le même thème.</p>
     *
     * @param spec spécification de la section
     * @return graine déterministe dérivée du contenu
     */
    private long stableSeed(SectionSpec spec) {
        String base =
            JsStringUtils.safe(spec.getTitle()) + "|" +
            JsStringUtils.safe(spec.getSubtitle()) + "|" +
            JsStringUtils.safe(spec.getParagraph()) + "|" +
            JsStringUtils.safeEnum(spec.getTone()) + "|" +
            JsStringUtils.safeEnum(spec.getLayout()) + "|" +
            JsStringUtils.safeEnum(spec.getGenerationMode());

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            long seed = 0;

            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (hash[i] & 0xff);
            }

            return seed;
        } catch (Exception e) {
            return base.hashCode();
        }
    }
}