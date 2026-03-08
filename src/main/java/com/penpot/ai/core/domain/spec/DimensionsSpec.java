package com.penpot.ai.core.domain.spec;

import lombok.*;

/**
 * Encapsule les spécifications dimensionnelles d'un espace de travail ou d'un composant généré.
 */
@Value
@Builder
public class DimensionsSpec {

    /** Largeur absolue de l'espace de travail. */
    int width;

    /** Hauteur absolue de l'espace de travail. */
    int height;

    /** Désignation sémantique du gabarit de la zone de dessin. */
    String canvasSize;

    /** Ratio d'aspect ou format d'exportation cible. */
    String format;

    /** Traçabilité technique de la définition dimensionnelle. */
    String source;
}