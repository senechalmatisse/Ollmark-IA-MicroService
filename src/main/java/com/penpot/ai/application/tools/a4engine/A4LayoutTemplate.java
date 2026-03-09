package com.penpot.ai.application.tools.a4engine;

/**
 * Énumération centralisant l'ensemble des gabarits de mise en page (layouts) structurant les documents au format A4.
 */
public enum A4LayoutTemplate {

    /* ===== GABARITS EXISTANTS ET FONDAMENTAUX ===== */

    /** Configuration classique occupant la totalité de l'en-tête, favorisant ainsi une introduction visuelle percutante et immersive. */
    HEADER_HERO_FULL,

    /** Agencement asymétrique structuré autour d'une colonne latérale gauche, particulièrement optimisé pour intégrer une navigation ou des métadonnées. */
    SIDEBAR_LEFT_COL,

    /** Disposition équilibrée scindant l'espace en deux colonnes égales, idéale pour la comparaison ou la présentation parallèle de contenus. */
    TWO_COLUMN_GRID,

    /** Organisation séquentielle alignant les éléments sous forme de lignes empilées, facilitant de fait la lecture descendante de listes denses. */
    ROW_BASED_LIST,

    /** Approche épurée concentrant le contenu au cœur de la page, réduisant drastiquement les distractions pour un rendu minimaliste. */
    CENTERED_MINIMAL,

    /** Mise en page complexe inspirée de la presse écrite, permettant au texte d'épouser organiquement les contours des éléments graphiques. */
    MAGAZINE_TEXT_WRAP,

    /** Empilement logique et hiérarchisé de champs de saisie, conçu spécifiquement pour clarifier les interfaces de formulaires. */
    FORM_FIELDS_STACK,

    /** Structure visuelle dense valorisant les données chiffrées et les statistiques, adaptée aux infographies ou aux rapports d'impact. */
    STATS_HEAVY_POSTER,

    /** Alternance dynamique en damier des blocs de texte et d'image, générant par conséquent un rythme visuel soutenu tout au long de la page. */
    CHECKERBOARD_LAYOUT,

    /* ===== GABARITS ORIENTÉS MARKETING ET CONVERSION ===== */

    /** Composition stratégique positionnant l'accroche textuelle à gauche et l'élément visuel principal sur la partie droite. */
    HERO_IMAGE_RIGHT,

    /** Composition symétrique inversée plaçant l'illustration phare à gauche afin de guider naturellement l'œil vers le texte d'appel à l'action. */
    HERO_IMAGE_LEFT,

    /** Agencement promotionnel massif empilant verticalement et au centre l'accroche, l'image et les appels à l'action. */
    HERO_CENTER_STACK,

    /** Grille structurée divisant l'espace en trois colonnes, couramment utilisée pour exposer synthétiquement les avantages clés d'une offre. */
    FEATURE_GRID_3,

    /** Grille densifiée à quatre colonnes, pertinente pour les présentations exhaustives de caractéristiques ou de services. */
    FEATURE_GRID_4,

    /** Mise en exergue d'un article spécifique, organisant l'espace autour des visuels et des détails techniques du produit. */
    PRODUCT_SHOWCASE,

    /** Modèle de réassurance focalisant l'attention sur les retours clients et la preuve sociale afin de consolider la confiance. */
    TESTIMONIAL_FOCUS,

    /** Disposition tabulaire claire et comparative, indispensable pour exposer lisiblement les différents paliers tarifaires d'une offre. */
    PRICING_TABLE,

    /** Design ultra-directif réduisant les éléments textuels au strict minimum pour maximiser le taux de clics sur le bouton d'action principal. */
    CTA_FOCUSED,

    /* ===== GABARITS DÉDIÉS À LA NARRATION ET AU CONTENU ===== */

    /** Représentation chronologique linéaire s'écoulant verticalement, parfaite pour illustrer un historique ou les étapes d'un projet. */
    TIMELINE_VERTICAL,

    /** Agencement analytique mêlant données qualitatives et quantitatives, spécifiquement formaté pour les retours d'expérience et les études de cas. */
    CASE_STUDY_LAYOUT,

    /** Format aéré et structuré en paragraphes distincts, respectant les standards typographiques des articles de blog pour un confort de lecture optimal. */
    BLOG_EDITORIAL,

    /* ===== GABARITS POUR ÉVÉNEMENTS PROMOTIONNELS ===== */

    /** Composition visuelle agressive et contrastée, destinée à capter immédiatement l'attention pour annoncer une période de soldes. */
    SALE_POSTER,

    /** Page d'atterrissage événementielle articulée autour d'un code promotionnel ou d'une offre tarifaire exceptionnelle. */
    DISCOUNT_LANDING,

    /** Agencement intégrant un compte à rebours central, exploitant ainsi le principe d'urgence pour accélérer la prise de décision de l'utilisateur. */
    COUNTDOWN_PROMO
}