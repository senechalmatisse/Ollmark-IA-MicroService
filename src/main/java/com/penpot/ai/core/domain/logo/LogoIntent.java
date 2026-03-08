package com.penpot.ai.core.domain.logo;

import lombok.Value;

/**
 * Représente l'intention sémantique et graphique d'un logo au sein du domaine.
 * @see LogoSpec
 */
@Value
public class LogoIntent {

    /**
     * Indicateur de style "Startup". 
     * <p>Ce booléen détermine si le design doit adopter des codes visuels modernes, 
     * dynamiques et minimalistes, influençant ainsi la disposition des éléments 
     * et l'équilibre général du logo.</p>
     */
    boolean isStartupVibe; 

    /**
     * Rayon de l'arrondi des angles. 
     * <p>Définit la courbure appliquée aux formes géométriques du logo. Une valeur 
     * élevée favorise un aspect ludique et accessible, tandis qu'une valeur faible 
     * ou nulle renforce un aspect institutionnel et rigoureux.</p>
     */
    int borderRadius;      

    /**
     * Facteur d'échelle global du logo. 
     * <p>Ce coefficient multiplicateur ajuste la taille relative du logo par rapport 
     * à son conteneur d'origine, permettant une adaptation fluide selon le support 
     * de destination.</p>
     */
    double scalingFactor;  

    /**
     * Couleur principale du logo. 
     * <p>Représente la teinte dominante (généralement au format hexadécimal) utilisée 
     * pour l'élément central ou l'identité primaire de la marque.</p>
     */
    String primaryColor;

    /**
     * Couleur secondaire du logo. 
     * <p>Utilisée pour les éléments d'accentuation ou les fonds, elle vient compléter 
     * la couleur primaire pour créer un contraste ou une harmonie visuelle.</p>
     */
    String secondaryColor;

    /**
     * Couleur de la typographie associée. 
     * <p>Définit la teinte du texte du logo afin d'assurer une lisibilité optimale 
     * sur les différents arrière-plans générés.</p>
     */
    String textColor;
    
    /**
     * Activation de la typographie grasse.
     * <p>Ce paramètre est essentiel pour le moteur de rendu car il oriente le choix 
     * des polices de caractères vers des graisses plus lourdes, souvent privilégiées 
     * pour renforcer l'impact visuel dans un contexte moderne ou ludique.</p>
     */
    boolean useBoldTypography;

    /**
     * Application de dégradés chromatiques.
     * <p>Lorsqu'il est activé, ce paramètre permet au moteur de rendu d'utiliser des 
     * transitions de couleurs vives à la place de teintes plates, répondant ainsi 
     * aux tendances actuelles du design de logo pour les entreprises technologiques.</p>
     */
    boolean useGradient;
}