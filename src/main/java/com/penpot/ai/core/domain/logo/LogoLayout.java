package com.penpot.ai.core.domain.logo;

/**
 * Définit les différentes structures spatiales disponibles pour l'agencement d'un logo.
 */
public enum LogoLayout {

    /**
     * Disposition en ligne privilégiant la largeur.
     * <p>L'icône est positionnée à gauche du texte (Nom | Slogan). Ce format est 
     * idéal pour les barres de navigation ou les documents où l'espace vertical 
     * est restreint.</p>
     */
    HORIZONTAL,

    /**
     * Disposition centrée privilégiant la hauteur.
     * <p>L'icône est placée au-dessus du texte. Cette configuration renforce 
     * l'impact visuel du symbole graphique et convient particulièrement aux 
     * supports de communication de type affiche ou carte de visite.</p>
     */
    VERTICAL,

    /**
     * Disposition hiérarchique à trois niveaux.
     * <p>Empile verticalement l'icône, le nom de la marque, puis le slogan. 
     * Cette structure permet une lecture complète de l'identité de marque 
     * tout en conservant un alignement central rigoureux.</p>
     */
    STACKED,

    /**
     * Disposition fusionnée de type "Badge".
     * <p>Le texte est directement intégré ou encapsulé à l'intérieur de l'élément 
     * graphique. Ce style compact est souvent utilisé pour des sceaux, des icônes 
     * d'application ou des identités visuelles de type institutionnel.</p>
     */
    EMBLEM
}