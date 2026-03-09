package com.penpot.ai.application.tools.pipeline;

import com.penpot.ai.application.tools.support.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Template Method pour les pipelines de création de contenu Penpot.
 *
 * <p>Le squelette de l'algorithme est fixé par {@link #execute} et se décompose en
 * trois étapes ordonnées :</p>
 * <ol>
 *   <li>{@link #validate} — vérification des préconditions métier (hook facultatif).</li>
 *   <li>{@link #buildJsCode} — génération du code JavaScript Penpot (abstraite).</li>
 *   <li>{@link #parseResult} — extraction du résultat exploitable depuis la réponse
 *       brute de l'exécuteur (hook facultatif, identité par défaut).</li>
 * </ol>
 *
 * <p>Les sous-classes concrètes ({@code LogoPipeline}, {@code A4SectionPipeline})
 * n'ont qu'à implémenter les étapes qui varient ; elles ne touchent jamais à
 * {@link #execute} ni à l'orchestration globale.</p>
 *
 * @param <S> type de la spec d'entrée (ex. {@code LogoSpec}, {@code A4SectionRequest})
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractContentPipeline<S> {

    protected final PenpotToolExecutor toolExecutor;

    /**
     * Orchestre le pipeline complet : validation → génération JS → exécution → parsing.
     *
     * <p>Cette méthode est intentionnellement {@code final} pour garantir que
     * l'ordre des étapes reste immuable, conformément au patron de méthode.</p>
     *
     * @param spec spécification métier fournie par le tool Spring AI
     * @return résultat JSON à renvoyer à l'agent
     */
    public final String execute(S spec) {
        log.info("Pipeline started: {}", contentType());

        Optional<String> validationError = validate(spec);
        if (validationError.isPresent()) {
            log.warn("Pipeline {} validation failed: {}", contentType(), validationError.get());
            return ToolResponseBuilder.error(validationError.get());
        }

        try {
            String jsCode = buildJsCode(spec);
            String rawResult = toolExecutor.createContent(jsCode, contentType());
            return parseResult(rawResult);
        } catch (Exception e) {
            log.error("Pipeline {} failed", contentType(), e);
            return ToolResponseBuilder.error(e.getMessage());
        }
    }

    /**
     * Identifiant stable du type de contenu (ex. {@code "logo"}, {@code "a4-section"}).
     * Transmis à {@link PenpotToolExecutor#createContent} et utilisé dans les logs.
     */
    public abstract String contentType();

    /**
     * Génère le code JavaScript Penpot pour la spec fournie.
     *
     * <p>Cette étape encapsule le pipeline métier complet (intention, thème, rendu)
     * propre à chaque type de contenu.</p>
     *
     * @param spec spécification validée
     * @return code JavaScript prêt à être injecté dans Penpot
     */
    protected abstract String buildJsCode(S spec);

    /**
     * Vérifie les préconditions métier avant la génération.
     *
     * <p>Par défaut, aucune validation n'est effectuée ({@link Optional#empty()}).
     * Les sous-classes qui ont des règles de validation redéfinissent cette méthode
     * et retournent un {@link Optional} contenant le message d'erreur si la spec est invalide.</p>
     *
     * @param spec spécification à valider
     * @return {@link Optional#empty()} si valide, message d'erreur sinon
     */
    protected Optional<String> validate(S spec) {
        return Optional.empty();
    }

    /**
     * Extrait le résultat exploitable depuis la réponse brute de l'exécuteur.
     *
     * <p>Par défaut, retourne {@code rawResult} tel quel (identité).
     * Les sous-classes dont le résultat nécessite un post-traitement (ex. extraction
     * d'un {@code sectionId} dans du JSON) redéfinissent cette méthode.</p>
     *
     * @param rawResult réponse brute retournée par {@link PenpotToolExecutor}
     * @return résultat final à renvoyer à l'agent
     */
    protected String parseResult(String rawResult) {
        return rawResult;
    }
}