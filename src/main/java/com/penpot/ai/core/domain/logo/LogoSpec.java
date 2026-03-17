package com.penpot.ai.core.domain.logo;

import lombok.*;

/**
 * Spécification définissant les paramètres de création d'un logo.
 *
 * @see LogoStyle
 * @see LogoLayout
 */
@Data
@Builder
public class LogoSpec {

    /**
     * Nom de la marque à afficher.
     * <p>Représente l'élément textuel principal du logo. Cette donnée est 
     * centrale pour le calcul de l'emprise visuelle et le choix de la typographie.</p>
     */
    private String brandName;

    /**
     * Slogan ou texte secondaire.
     * <p>Optionnel, ce texte vient compléter le nom de la marque. Sa présence 
     * influence directement le rendu final, notamment pour les dispositions 
     * de type {@link LogoLayout#STACKED}.</p>
     */
    private String tagline;

    /**
     * Style graphique global.
     * <p>Définit l'orientation esthétique (moderne, classique, minimaliste, etc.) 
     * qui sera ensuite interprétée par les moteurs d'intention et de thème.</p>
     */
    private LogoStyle style;

    /**
     * Structure spatiale du logo.
     * <p>Détermine l'organisation relative entre l'icône et les différents 
     * blocs de texte au sein de la composition.</p>
     */
    private LogoLayout layout;
    
    /**
     * Coordonnée horizontale d'origine.
     * <p>Positionne le point d'ancrage du logo sur l'axe X dans l'espace de 
     * travail Penpot. La valeur par défaut est fixée à {@code 0}.</p>
     */
    @Builder.Default
    private int x = 0;

    /**
     * Coordonnée verticale d'origine.
     * <p>Positionne le point d'ancrage du logo sur l'axe Y dans l'espace de 
     * travail Penpot. La valeur par défaut est fixée à {@code 0}.</p>
     */
    @Builder.Default
    private int y = 0;

    /**
     * Taille de référence pour le rendu.
     * <p>Sert de base de calcul pour les dimensions globales du logo.
     * La valeur par défaut est de {@code 200} unités.</p>
     */
    @Builder.Default
    private int baseSize = 200; 
}