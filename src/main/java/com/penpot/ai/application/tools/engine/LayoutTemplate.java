package com.penpot.ai.application.tools.engine;

/**
 * Définit les modèles de mise en page (templates) disponibles pour le rendu des sections "Hero" au sein de l'application.
 */
public enum LayoutTemplate {

    /* =========================
            HERO CLASSIQUES
       ========================= */

    /**
     * Disposition classique avec une image positionnée à gauche et le bloc textuel à droite.
     */
    SPLIT_LEFT_IMAGE,

    /**
     * Disposition classique avec une image positionnée à droite et le bloc textuel à gauche.
     */
    SPLIT_RIGHT_IMAGE,

    /**
     * Structure centrée verticalement, idéale pour une mise en avant équilibrée du contenu.
     */
    CENTERED_STACK,

    /**
     * Empilement vertical plaçant l'image en haut de section, suivie par le texte en dessous.
     */
    IMAGE_TOP_STACK,

    /**
     * Modèle optimisé pour le contenu rédactionnel dense, où le texte occupe la place prépondérante.
     */
    TEXT_HEAVY,

    /* =========================
            HERO CONVERSION
       ========================= */

    /**
     * Mise en page asymétrique (60% gauche, 40% droite) mettant l'accent sur un produit à gauche.
     */
    SPLIT_60_40_LEFT,

    /**
     * Mise en page asymétrique (60% droite, 40% gauche) mettant l'accent sur un produit à droite.
     */
    SPLIT_60_40_RIGHT,

    /**
     * Configuration focalisée sur un produit large occupant la partie gauche de l'écran.
     */
    PRODUCT_FOCUS_LEFT,

    /**
     * Configuration focalisée sur un produit large occupant la partie droite de l'écran.
     */
    PRODUCT_FOCUS_RIGHT,

    /* =========================
            HERO VISUEL
       ========================= */

    /**
     * Modèle visuel utilisant une image de fond (background) avec un texte superposé (overlay) au centre.
     */
    BACKGROUND_OVERLAY_CENTER,

    /* =========================
            HERO BRANDING
       ========================= */

    /**
     * Disposition luxueuse et centrée, conçue pour renforcer l'image de marque et le positionnement premium.
     */
    PREMIUM_LUXURY_CENTER,

    /* =========================
            HERO MINIMAL
       ========================= */

    /**
     * Design épuré et minimaliste, privilégiant l'espace blanc et une clarté visuelle absolue.
     */
    MINIMAL_ULTRA_CLEAN
}