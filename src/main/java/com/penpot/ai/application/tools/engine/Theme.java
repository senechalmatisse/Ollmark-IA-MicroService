package com.penpot.ai.application.tools.engine;

import java.util.List;

/**
 * Définit l'identité visuelle et la charte graphique appliquée aux composants générés.
 */
public class Theme {

    /** Le nom unique identifiant le thème. */
    public final String name;

    /** Indique si le thème utilise un dégradé de couleurs pour son arrière-plan. */
    public final boolean gradient;

    /** Première couleur du dégradé (hexadécimal). Ignorée si {@code gradient} est faux. */
    public final String g1;

    /** Seconde couleur du dégradé (hexadécimal). Ignorée si {@code gradient} est faux. */
    public final String g2;

    /** Couleur d'arrière-plan unie (hexadécimal). Utilisée si {@code gradient} est faux. */
    public final String bgSolid;

    /** Couleur de texte recommandée lorsque le fond est sombre. */
    public final String textOnDark;

    /** Couleur de texte recommandée lorsque le fond est clair. */
    public final String textOnLight;

    /** Couleur principale appliquée aux boutons et éléments d'action (CTA). */
    public final String ctaPrimary;

    /** Couleur du texte à l'intérieur des boutons d'action. */
    public final String ctaText;

    /** Couleur d'accentuation utilisée pour mettre en relief des éléments spécifiques. */
    public final String accent;

    /** Couleur subtile utilisée pour les bordures, les séparateurs ou les fonds secondaires. */
    public final String subtle;

    /**
     * Constructeur privé assurant l'initialisation complète des propriétés du thème.
     * <p>
     * L'instanciation doit passer par les méthodes de fabrique statiques {@link #gradient} 
     * ou {@link #solid} afin de garantir la cohérence des paramètres.
     * </p>
     */
    private Theme(
        String name, boolean gradient,
        String g1, String g2, String bgSolid,
        String textOnDark, String textOnLight,
        String ctaPrimary, String ctaText,
        String accent, String subtle
    ) {
        this.name = name; this.gradient = gradient;
        this.g1 = g1; this.g2 = g2; this.bgSolid = bgSolid;
        this.textOnDark = textOnDark; this.textOnLight = textOnLight;
        this.ctaPrimary = ctaPrimary; this.ctaText = ctaText;
        this.accent = accent; this.subtle = subtle;
    }

    /**
     * Crée une instance de thème configurée avec un arrière-plan en dégradé.
     *
     * @param name         Nom du thème.
     * @param g1           Couleur de départ du dégradé.
     * @param g2           Couleur de fin du dégradé.
     * @param textOnDark   Texte sur fond sombre.
     * @param textOnLight  Texte sur fond clair.
     * @param ctaPrimary   Couleur du CTA.
     * @param ctaText      Couleur du texte du CTA.
     * @param accent       Couleur d'accent.
     * @param subtle       Couleur subtile.
     * @return Une nouvelle instance de {@link Theme} de type gradient.
     */
    public static Theme gradient(
        String name, String g1, String g2,
        String textOnDark, String textOnLight,
        String ctaPrimary, String ctaText,
        String accent, String subtle
    ) {
        return new Theme(
            name, true, g1, g2,
            "", textOnDark, textOnLight,
            ctaPrimary, ctaText, accent, subtle
        );
    }

    /**
     * Crée une instance de thème configurée avec un arrière-plan de couleur unie.
     *
     * @param name         Nom du thème.
     * @param bg           Couleur de fond unie.
     * @param textOnDark   Texte sur fond sombre.
     * @param textOnLight  Texte sur fond clair.
     * @param ctaPrimary   Couleur du CTA.
     * @param ctaText      Couleur du texte du CTA.
     * @param accent       Couleur d'accent.
     * @param subtle       Couleur subtile.
     * @return Une nouvelle instance de {@link Theme} de type solide.
     */
    public static Theme solid(
        String name, String bg,
        String textOnDark, String textOnLight,
        String ctaPrimary, String ctaText,
        String accent, String subtle
    ) {
        return new Theme(
            name, false, "", "",
            bg, textOnDark, textOnLight,
            ctaPrimary, ctaText, accent, subtle
        );
    }

    /* =========================
            PREDEFINED THEMES
       ========================= */

    /** Thème sombre optimisé pour les interfaces logicielles (SaaS). */
    public static final Theme DARK_SAAS = gradient(
        "Dark SaaS", "#0F172A", "#1E293B", "#FFFFFF", "#0F172A", "#6366F1", "#FFFFFF", "#8B5CF6", "#334155"
    );

    /** Thème dynamique utilisant des nuances de violet, typique de l'écosystème startup. */
    public static final Theme STARTUP_PURPLE = gradient(
        "Startup Purple", "#4F46E5", "#9333EA", "#FFFFFF", "#1E1B4B", "#1E1B4B", "#FFFFFF", "#A78BFA", "#EDE9FE"
    );

    /** Thème frais et moderne basé sur des teintes vertes et écologiques. */
    public static final Theme MODERN_GREEN = gradient(
        "Modern Green", "#10B981", "#A3E635", "#064E3B", "#064E3B", "#065F46", "#FFFFFF", "#34D399", "#D1FAE5"
    );

    /** Thème énergique aux couleurs chaudes, idéal pour les plateformes de vente en ligne. */
    public static final Theme ECOMMERCE_RED = gradient(
        "E-commerce Red", "#DC2626", "#F97316", "#FFFFFF", "#7F1D1D", "#7F1D1D", "#FFFFFF", "#F87171", "#FEE2E2"
    );

    /** Thème futuriste à haut contraste utilisant des couleurs néon sur fond sombre. */
    public static final Theme NEON_FUTURE = gradient(
        "Neon Future", "#111827", "#1F2937", "#FFFFFF", "#111827", "#00F5FF", "#000000", "#00F5FF", "#1E40AF"
    );

    /** Thème apaisant évoquant l'océan avec des nuances de bleu-vert (teal). */
    public static final Theme OCEAN_TEAL = gradient(
        "Ocean Teal", "#0F766E", "#14B8A6", "#FFFFFF", "#083344", "#083344", "#FFFFFF", "#5EEAD4", "#CCFBF1"
    );

    /** Thème sobre et prestigieux utilisant un noir profond et des accents or. */
    public static final Theme LUXURY_BLACK = solid(
        "Luxury Black", "#0A0A0A", "#FFFFFF", "#1C1C1C", "#D4AF37", "#000000", "#D4AF37", "#2A2A2A"
    );

    /** Déclinaison luxueuse avec un dégradé sombre et des reflets dorés. */
    public static final Theme PREMIUM_GOLD = gradient(
        "Premium Gold", "#1C1C1C", "#3A3A3A", "#FFFFFF", "#111111", "#D4AF37", "#000000", "#FBBF24", "#2C2C2C"
    );

    /** Thème institutionnel et professionnel basé sur des bleus profonds. */
    public static final Theme CORPORATE_BLUE = solid(
        "Corporate Blue", "#0C4A6E", "#FFFFFF", "#E0F2FE", "#0284C7", "#FFFFFF", "#38BDF8", "#075985"
    );

    /** Thème épuré et clair, privilégiant la lisibilité et la simplicité. */
    public static final Theme MINIMAL_LIGHT = solid(
        "Minimal Light", "#FFFFFF", "#111827", "#F9FAFB", "#111827", "#FFFFFF", "#6B7280", "#E5E7EB"
    );

    /* =========================
            THEME REGISTRY
       ========================= */

    /** Registre immuable contenant l'intégralité des thèmes disponibles. */
    private static final List<Theme> REGISTRY = List.of(
        DARK_SAAS, STARTUP_PURPLE, MODERN_GREEN, ECOMMERCE_RED, NEON_FUTURE,
        OCEAN_TEAL, LUXURY_BLACK, PREMIUM_GOLD, CORPORATE_BLUE, MINIMAL_LIGHT
    );

    /**
     * Fournit la liste complète des thèmes enregistrés dans le système.
     *
     * @return Une liste immuable de thèmes.
     */
    public static List<Theme> all() {
        return REGISTRY;
    }

    /**
     * Vérifie si le thème courant est configuré avec un dégradé.
     *
     * @return {@code true} si le thème est un dégradé, sinon {@code false}.
     */
    public boolean isGradient() {
        return gradient;
    }
}