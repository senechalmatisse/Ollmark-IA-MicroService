package com.penpot.ai.core.domain.spec;

import com.penpot.ai.core.domain.marketing.*;
import lombok.Data;

import java.util.List;

/**
 * Représente la spécification d'une section marketing utilisée pour générer
 * dynamiquement une section de design ou de contenu marketing.
 *
 * <p>
 * Les propriétés de cette spécification permettent notamment de définir :
 * </p>
 * <ul>
 *     <li>le contenu textuel principal (titre, sous-titre, paragraphe)</li>
 *     <li>les appels à l'action (boutons principaux et secondaires)</li>
 *     <li>la présence de certains blocs marketing (features, statistiques, témoignages)</li>
 *     <li>le layout et le style marketing de la section</li>
 *     <li>le ton marketing utilisé</li>
 *     <li>la liste explicite de composants marketing à inclure</li>
 *     <li>le mode de génération utilisé par l'IA</li>
 * </ul>
 */
@Data
public class SectionSpec {

    /** Titre principal de la section. */
    private String title;

    /** Sous-titre de la section. */
    private String subtitle;

    /** Paragraphe descriptif de la section. */
    private String paragraph;

    /** Texte du bouton principal (CTA principal). */
    private String primaryButton;

    /** Texte du bouton secondaire (CTA secondaire). */
    private String secondaryButton;

    /**
     * Indique si un bloc de témoignage client doit être inclus dans la section.
     */
    private boolean withTestimonial;

    /**
     * Indique si un aperçu visuel (image) doit être affiché dans la section.
     */
    private boolean withPreview;

    /**
     * Indique si une liste ou grille de fonctionnalités doit être incluse.
     */
    private boolean withFeatures;

    /**
     * Indique si un bloc de statistiques ou de métriques doit être affiché.
     */
    private boolean withStats;

    /**
     * Type de layout marketing utilisé pour organiser
     * les éléments visuels de la section.
     *
     * <p>
     * Définit la structure globale de la section
     * (ex : hero centré, layout split, section promotionnelle).
     * </p>
     */
    private MarketingLayoutType layout;

    /**
     * Style marketing global de la section.
     *
     * <p>
     * Permet de définir l'identité visuelle dominante
     * (par exemple moderne, minimaliste, coloré, professionnel).
     * </p>
     */
    private MarketingStyle style;

    /**
     * Ton marketing utilisé dans la communication.
     *
     * <p>
     * Le ton influence la manière dont le message
     * est formulé (ex : promotionnel, informatif, premium).
     * </p>
     *
     * <p>
     * La valeur par défaut est {@link MarketingTone#UNKNOWN}
     * afin d'éviter les valeurs nulles.
     * </p>
     */
    private MarketingTone tone = MarketingTone.UNKNOWN;

    /**
     * Liste explicite des composants marketing à inclure
     * dans la section.
     *
     * <p>
     * Cette liste peut être utilisée pour forcer l'inclusion
     * de certains éléments visuels spécifiques.
     * </p>
     */
    private List<MarketingComponent> components;

    /**
     * Mode de génération utilisé pour produire la section.
     *
     * <p>
     * Ce paramètre contrôle le niveau d'autonomie de l'intelligence
     * artificielle dans la génération du contenu et de la structure
     * de la section.
     * </p>
     */
    private GenerationMode generationMode = GenerationMode.SMART_ASSISTED;

    /**
     * Définit le mode de génération du contenu de la section.
     *
     * <p>
     * Ce mode permet d'ajuster le niveau de contrôle de l'utilisateur
     * par rapport aux décisions prises par l'IA lors de la génération.
     * </p>
     */
    public enum GenerationMode {

        /**
         * Mode strict.
         *
         * <p>
         * L'IA suit strictement les instructions fournies par l'utilisateur
         * sans ajouter d'éléments supplémentaires.
         * </p>
         */
        USER_STRICT,

        /**
         * Mode assisté intelligent.
         *
         * <p>
         * L'IA complète la demande utilisateur en ajoutant des éléments
         * pertinents tout en respectant l'intention initiale.
         * </p>
         */
        SMART_ASSISTED,

        /**
         * Mode créatif automatique.
         *
         * <p>
         * L'IA prend la majorité des décisions concernant la structure
         * et les composants de la section.
         * </p>
         */
        AUTO_CREATIVE
    }
}