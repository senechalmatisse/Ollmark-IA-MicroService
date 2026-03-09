package com.penpot.ai.application.tools.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.application.tools.a4engine.*;
import com.penpot.ai.application.tools.a4engine.SpecNormalizer;
import com.penpot.ai.application.tools.engine.*;
import com.penpot.ai.application.tools.support.*;
import com.penpot.ai.core.domain.spec.SectionSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Pipeline de création de section A4 marketing.
 *
 * <p>Implémente les quatre étapes variables du patron de méthode :</p>
 * <ul>
 *   <li>{@link #contentType()} → {@code "a4-section"}</li>
 *   <li>{@link #validate(A4SectionRequest)} → vérifie qu'au moins un champ texte est fourni</li>
 *   <li>{@link #buildJsCode(A4SectionRequest)} → pipeline complet de génération A4</li>
 *   <li>{@link #parseResult(String)} → extrait le {@code sectionId} de la réponse JSON</li>
 * </ul>
 *
 * <p>La méthode socle {@link AbstractContentPipeline#execute} reste inchangée ;
 * cette classe ne fait que personnaliser les étapes qui lui sont propres.</p>
 */
@Slf4j
@Component
public class A4SectionPipeline extends AbstractContentPipeline<A4SectionPipeline.A4SectionRequest> {

    /** Mapper JSON utilisé pour analyser la réponse retournée par l'exécuteur d'outils Penpot. */
    private final ObjectMapper mapper;

    /**
     * Composants du pipeline responsables des différentes étapes
     * de transformation et de génération.
     */
    private final SpecNormalizer specNormalizer = new SpecNormalizer();
    private final A4MarketingIntentEngine intentEngine = new A4MarketingIntentEngine();
    private final A4ComponentAutoEngine componentAutoEngine = new A4ComponentAutoEngine();
    private final ThemeEngine themeEngine = new ThemeEngine();
    private final A4ThemeRenderer themeRenderer = new A4ThemeRenderer();
    private final A4TemplateResolver templateResolver = new A4TemplateResolver();
    private final A4LayoutRenderer layoutRenderer = new A4LayoutRenderer();
    private final A4ComponentRenderer componentRenderer = new A4ComponentRenderer();
    private final A4CtaRenderer ctaRenderer = new A4CtaRenderer();

    /**
     * Construit un pipeline de génération de section A4.
     *
     * @param toolExecutor exécuteur chargé d'envoyer le code JavaScript
     *                     généré vers Penpot
     * @param mapper objet permettant de parser la réponse JSON du moteur
     */
    public A4SectionPipeline(PenpotToolExecutor toolExecutor, ObjectMapper mapper) {
        super(toolExecutor);
        this.mapper = mapper;
    }

    @Override
    public String contentType() {
        return "a4-section";
    }

    /**
     * Vérifie la validité minimale de la requête de génération.
     *
     * <p>Au moins un des champs textuels de la section doit être fourni :
     * titre, sous-titre ou paragraphe. Une section entièrement vide
     * n'a pas de sens fonctionnel et serait rejetée par Penpot.</p>
     *
     * @param request requête contenant la spécification de section
     * @return un message d'erreur si la requête est invalide,
     *         sinon {@link Optional#empty()}
     */
    @Override
    protected Optional<String> validate(A4SectionRequest request) {
        SectionSpec spec = request.spec();
        if (spec == null) return Optional.of("Section spec must not be null.");
        if (isBlank(spec.getTitle()) && isBlank(spec.getSubtitle()) && isBlank(spec.getParagraph())) {
            return Optional.of("Title, subtitle or paragraph must be provided for the A4 section.");
        }
        return Optional.empty();
    }

    /**
     * Construit le code JavaScript permettant de générer la section A4.
     *
     * <p>Cette méthode implémente l'intégralité du pipeline de génération :</p>
     * <ol>
     *     <li>normalisation de la spécification</li>
     *     <li>analyse de l'intention marketing</li>
     *     <li>enrichissement automatique des composants</li>
     *     <li>sélection du thème</li>
     *     <li>résolution du template de layout</li>
     *     <li>assemblage du code JavaScript final</li>
     * </ol>
     *
     * @param request requête de génération contenant la spécification
     *                et les coordonnées de placement
     * @return code JavaScript complet à exécuter
     */
    @Override
    protected String buildJsCode(A4SectionRequest request) {
        SectionSpec spec = request.spec();
        int posX = request.x();
        int posY = request.y();

        specNormalizer.normalize(spec);

        MarketingIntent intent = intentEngine.analyze(spec);
        componentAutoEngine.enrich(spec, intent);
        Theme theme = themeEngine.pickTheme(spec, intent);
        A4LayoutTemplate template = templateResolver.resolveTemplate(spec, intent);

        StringBuilder code = new StringBuilder();
        code.append(JsScriptLoader.loadWith("tools/pipeline/a4-section-init.js", Map.of(
            "posX", String.valueOf(posX),
            "posY", String.valueOf(posY)
        )));
        code.append(themeRenderer.render(theme));
        code.append(layoutRenderer.render(template, spec));
        code.append(componentRenderer.render(spec, theme.textOnDark));
        code.append(ctaRenderer.render(spec, theme));
        code.append(JsScriptLoader.load("tools/pipeline/a4-section-finalize.js"));

        return code.toString();
    }

    /**
     * Analyse la réponse JSON retournée par l'exécuteur d'outils
     * afin d'en extraire l'identifiant de la section créée.
     *
     * <p>Deux formats de réponse sont supportés :</p>
     * <ul>
     *     <li>{@code {"id": "..."}}</li>
     *     <li>{@code {"result": {"sectionId": "..."}}}</li>
     * </ul>
     *
     * <p>Si aucun identifiant n'est trouvé ou si le JSON est invalide,
     * une réponse d'erreur est retournée sans propager d'exception.</p>
     *
     * @param rawJson réponse brute retournée par l'exécuteur
     * @return identifiant de la section créée ou message d'erreur JSON
     */
    @Override
    protected String parseResult(String rawJson) {
        try {
            JsonNode root = mapper.readTree(rawJson);

            JsonNode id = root.path("id");
            if (!id.isMissingNode() && !id.asText().isBlank()) return id.asText();

            JsonNode nested = root.path("result").path("sectionId");
            if (!nested.isMissingNode() && !nested.asText().isBlank()) {
                return nested.asText();
            }

            log.error("No sectionId found in A4 tool response: {}", rawJson);
            return ToolResponseBuilder.error("No sectionId found in tool response");
        } catch (Exception e) {
            log.error("Error parsing A4 tool response: {}", rawJson);
            return ToolResponseBuilder.error("Invalid A4 tool response: " + e.getMessage());
        }
    }

    /**
     * Vérifie si une chaîne de caractères est vide ou composée
     * uniquement d'espaces.
     *
     * @param s chaîne à vérifier
     * @return {@code true} si la chaîne est nulle ou vide
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Objet de requête immuable représentant les paramètres
     * nécessaires à la génération d'une section A4.
     *
     * <p>Cette structure regroupe :</p>
     * <ul>
     *     <li>la spécification métier de la section ({@link SectionSpec})</li>
     *     <li>les coordonnées de placement dans le document</li>
     * </ul>
     *
     * @param spec spécification de la section à générer
     * @param x position horizontale dans le document
     * @param y position verticale dans le document
     */
    public record A4SectionRequest(SectionSpec spec, int x, int y) {}
}