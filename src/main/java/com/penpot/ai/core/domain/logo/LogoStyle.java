package com.penpot.ai.core.domain.logo;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Définit l'orientation esthétique et conceptuelle globale appliquée lors de la création d'un logo.
 */
public enum LogoStyle {

    /** Approche basée sur des formes organiques ou des compositions géométriques libres. */
    @JsonAlias({"ABSTRACT", "ABSTRAIT"})
    ABSTRAIT,

    /** Conception structurée reposant sur des formes mathématiques pures. */
    @JsonAlias({"GEOMETRIC", "GEOMETRIQUE"})
    GEOMETRIQUE,

    /** Identité visuelle centrée sur l'exploitation typographique des initiales. */
    @JsonAlias({"MONOGRAM", "MONOGRAMME"})
    MONOGRAMME,

    /** Design épuré éliminant tout élément superflu pour se concentrer sur l'essentiel. */
    @JsonAlias({"MINIMALIST", "MINIMALISTE"})
    MINIMALISTE,

    /** Structure unifiée prenant la forme d'un insigne ou d'un badge. */
    @JsonAlias({"EMBLEM", "EMBLEME"})
    EMBLEME
}