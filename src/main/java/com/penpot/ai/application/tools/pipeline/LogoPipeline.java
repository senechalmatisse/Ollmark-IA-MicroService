package com.penpot.ai.application.tools.pipeline;

import com.penpot.ai.application.tools.logo.*;
import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import com.penpot.ai.core.domain.logo.*;
import org.springframework.stereotype.Component;

/**
 * Pipeline de création de logo.
 *
 * <p>Implémente les deux étapes variables du patron de méthode :</p>
 * <ul>
 *   <li>{@link #contentType()} → {@code "logo"}</li>
 *   <li>{@link #buildJsCode(LogoSpec)} → pipeline
 *       {@code LogoIntentEngine → LogoThemeEngine → LogoRenderer}</li>
 * </ul>
 *
 * <p>La validation et le parsing du résultat utilisent les comportements
 * par défaut hérités de {@link AbstractContentPipeline} : pas de précondition
 * spécifique, résultat renvoyé tel quel.</p>
 */
@Component
public class LogoPipeline extends AbstractContentPipeline<LogoSpec> {

    /** Moteur chargé de l'analyse sémantique des besoins de l'utilisateur. */
    private final LogoIntentEngine intentEngine;

    /** Moteur responsable de l'application des contraintes graphiques et stylistiques. */
    private final LogoThemeEngine themeEngine;

    /** Moteur de génération du code JavaScript final destiné au moteur de rendu Penpot. */
    private final LogoRenderer logoRenderer;

    /**
     * Constructeur principal permettant l'injection des dépendances nécessaires au pipeline.
     *
     * @param toolExecutor L'exécuteur de services Penpot pour la gestion du contexte.
     * @param intentEngine Le moteur d'analyse de l'intention.
     * @param themeEngine  Le moteur d'application des thèmes visuels.
     * @param logoRenderer Le moteur de production du rendu technique.
     */
    public LogoPipeline(
        PenpotToolExecutor toolExecutor,
        LogoIntentEngine intentEngine,
        LogoThemeEngine themeEngine,
        LogoRenderer logoRenderer
    ) {
        super(toolExecutor);
        this.intentEngine = intentEngine;
        this.themeEngine = themeEngine;
        this.logoRenderer = logoRenderer;
    }

    @Override
    public String contentType() {
        return "logo";
    }

    /**
     * Orchestre la transformation d'une spécification de logo en code exécutable.
     * <p>Le traitement suit une séquence linéaire rigoureuse afin de raffiner progressivement 
     * l'objet métier :</p>
     * <ul>
     *   <li>D'abord, le {@link LogoIntentEngine} extrait l'intention conceptuelle à partir de la {@code spec}.</li>
     *   <li>Ensuite, le {@link LogoThemeEngine} enrichit cette intention en y intégrant les attributs 
     * du thème choisi pour produire une intention finale cohérente.</li>
     *   <li>Enfin, le {@link LogoRenderer} convertit ces données structurées en une chaîne de 
     * caractères contenant le code JavaScript nécessaire à la création visuelle.</li>
     * </ul>
     *
     * @param spec Les données sources contenant les préférences de l'utilisateur.
     * @return Une chaîne de caractères représentant le code JavaScript généré pour le logo.
     */
    @Override
    protected String buildJsCode(LogoSpec spec) {
        LogoIntent intent = intentEngine.analyze(spec);
        LogoIntent finalIntent = themeEngine.applyTheme(spec, intent);
        return logoRenderer.render(spec, finalIntent);
    }
}