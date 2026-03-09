package com.penpot.ai.application.tools.pipeline;

import com.fasterxml.jackson.databind.*;
import com.penpot.ai.application.tools.engine.*;
import com.penpot.ai.application.tools.support.*;
import com.penpot.ai.core.domain.spec.SectionSpec;
import com.penpot.ai.shared.exception.TaskExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Pipeline responsable de la génération d'une section marketing dans Penpot
 * à partir d'une {@link SectionSpec}.
 *
 * <p>
 * Cette classe implémente une chaîne de traitement permettant :
 * </p>
 * <ul>
 *     <li>la normalisation de la spécification de section</li>
 *     <li>l'analyse de l'intention marketing</li>
 *     <li>la sélection d'un thème visuel</li>
 *     <li>la résolution du template de layout</li>
 *     <li>le rendu des composants graphiques</li>
 *     <li>la génération du code JavaScript exécuté dans Penpot</li>
 * </ul>
 */
@Slf4j
@Component
public class SectionPipeline extends AbstractContentPipeline<SectionPipeline.SectionRequest> {

    /** Mapper Jackson utilisé pour parser les réponses JSON retournées par l'outil. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Composant chargé de normaliser les spécifications de section. */
    private static final SpecNormalizer SPEC_NORMALIZER = new SpecNormalizer();

    /** Moteur d'analyse de l'intention marketing de la section. */
    private static final MarketingIntentEngine INTENT_ENGINE = new MarketingIntentEngine();

    /** Moteur responsable de la sélection du thème visuel. */
    private static final ThemeEngine THEME_ENGINE = new ThemeEngine();

    /** Résolveur de template de layout. */
    private static final TemplateResolver TEMPLATE_RESOLVER = new TemplateResolver();

    /** Renderer responsable du rendu du layout global. */
    private static final LayoutRenderer LAYOUT_RENDERER = new LayoutRenderer();

    /** Renderer chargé de générer les composants internes de la section. */
    private static final ComponentRenderer COMPONENT_RENDERER = new ComponentRenderer();

    /** Renderer responsable du rendu des call-to-action (CTA). */
    private static final CtaRenderer CTA_RENDERER = new CtaRenderer();

    /**
     * Constructeur du pipeline.
     *
     * @param toolExecutor exécuteur d'outils Penpot permettant d'exécuter
     *                     le script JavaScript généré.
     */
    public SectionPipeline(PenpotToolExecutor toolExecutor) {
        super(toolExecutor);
    }

    /**
     * Retourne le type de contenu traité par ce pipeline.
     *
     * @return la chaîne identifiant le type de contenu ("section")
     */
    @Override
    public String contentType() {
        return "section";
    }

    /**
     * Valide la requête de génération de section.
     *
     * <p>
     * Une section doit contenir au minimum un titre, un sous-titre
     * ou un paragraphe.
     * </p>
     *
     * @param request requête contenant la spécification de section
     * @return un {@link Optional} contenant un message d'erreur si la requête
     *         est invalide, sinon {@link Optional#empty()}
     */
    @Override
    protected Optional<String> validate(SectionRequest request) {
        SectionSpec spec = request.spec();
        if (spec == null) return Optional.of("SectionSpec cannot be null");
        if (isBlank(spec.getTitle()) &&
            isBlank(spec.getSubtitle()) &&
            isBlank(spec.getParagraph())) {
            return Optional.of("SectionSpec must contain title, subtitle or paragraph");
        }
        return Optional.empty();
    }

    /**
     * Construit le script JavaScript permettant de générer la section
     * dans l'environnement Penpot.
     *
     * <p>
     * Les étapes du pipeline sont les suivantes :
     * </p>
     * <ol>
     *     <li>normalisation de la spécification</li>
     *     <li>analyse de l'intention marketing</li>
     *     <li>sélection du thème visuel</li>
     *     <li>résolution du template de layout</li>
     *     <li>génération du layout et des composants</li>
     *     <li>ajout des CTA</li>
     * </ol>
     *
     * @param request requête contenant la spécification et la position
     * @return script JavaScript à exécuter dans Penpot
     */
    @Override
    protected String buildJsCode(SectionRequest request) {
        SectionSpec spec = request.spec();
        int posX = request.x();
        int posY = request.y();

        SPEC_NORMALIZER.normalize(spec);

        MarketingIntent intent = INTENT_ENGINE.analyze(spec);
        Theme theme = THEME_ENGINE.pickTheme(spec, intent);
        LayoutTemplate template = TEMPLATE_RESOLVER.resolveTemplate(spec, intent);

        StringBuilder code = new StringBuilder();
        code.append(JsScriptLoader.load("tools/pipeline/section-init.js"));
        code.append(buildHeroBase(posX, posY, spec));
        code.append(applyTheme(theme));
        code.append(wrapJsBlock(LAYOUT_RENDERER.render(template, spec, theme, intent)));
        code.append(wrapJsBlock(COMPONENT_RENDERER.render(spec, theme, intent)));
        code.append(wrapJsBlock(CTA_RENDERER.render(spec, theme)));
        code.append(JsScriptLoader.load("tools/pipeline/section-finalize.js"));

        return code.toString();
    }

    /**
     * Analyse la réponse brute retournée par l'outil Penpot et extrait
     * l'identifiant de la section créée.
     *
     * @param rawJson réponse JSON brute
     * @return réponse formatée indiquant la création de la section
     */
    @Override
    protected String parseResult(String rawJson) {
        String sectionId = extractSectionId(rawJson);
        return ToolResponseBuilder.shapeCreated("section", sectionId);
    }

    /**
     * Extrait l'identifiant de section depuis la réponse JSON retournée
     * par l'outil.
     *
     * @param rawJson réponse JSON brute
     * @return identifiant de la section créée
     * @throws TaskExecutionException si aucun identifiant n'est trouvé
     */
    private String extractSectionId(String rawJson) {
        try {
            JsonNode root = MAPPER.readTree(rawJson);

            JsonNode direct = root.path("result").path("sectionId");
            if (!direct.isMissingNode() && !direct.asText().isBlank()) return direct.asText();

            JsonNode idNode = root.path("id");
            if (!idNode.isMissingNode() && !idNode.asText().isBlank()) {
                String idText = idNode.asText().trim();
                if (looksLikeUuid(idText)) return idText;

                try {
                    JsonNode inner = MAPPER.readTree(idText);
                    JsonNode sectionId = inner.path("result").path("sectionId");
                    if (!sectionId.isMissingNode() && !sectionId.asText().isBlank()) {
                        return sectionId.asText();
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.error("Invalid tool response: {}", rawJson);
        }
        throw new TaskExecutionException("No sectionId found in tool response");
    }

    /**
     * Génère le bloc JavaScript responsable de la création de la base
     * de la section (zone hero).
     *
     * @param x position horizontale de la section
     * @param y position verticale de la section
     * @param spec spécification de la section
     * @return script JavaScript correspondant
     */
    private String buildHeroBase(int x, int y, SectionSpec spec) {
        int width = 1120;
        int height = 480;
        if (spec.getLayout() != null) {
            height = switch (spec.getLayout()) {
                case HERO_CENTERED -> 440;
                case HERO_WITH_STATS -> 540;
                case PROMO_SECTION -> 420;
                case HERO_SPLIT -> 480;
                default -> 480;
            };
        }
        return JsScriptLoader.loadWith("tools/pipeline/section-hero-base.js", Map.of(
            "x", String.valueOf(x),
            "y", String.valueOf(y),
            "width", String.valueOf(width),
            "height", String.valueOf(height)
        ));
    }

    /**
     * Applique le thème visuel à la section.
     *
     * @param theme thème sélectionné
     * @return code JavaScript appliquant le thème
     */
    private String applyTheme(Theme theme) {
        if (theme.isGradient()) {
            return JsScriptLoader.loadWith("tools/pipeline/section-theme-gradient.js", Map.of(
                "g1", theme.g1, "g2", theme.g2));
        }
        return JsScriptLoader.loadWith("tools/pipeline/section-theme-solid.js", Map.of(
            "bgSolid", theme.bgSolid));
    }

    /**
     * Encapsule un bloc JavaScript dans un scope isolé.
     *
     * @param js code JavaScript à encapsuler
     * @return bloc JavaScript encapsulé
     */
    private String wrapJsBlock(String js) {
        if (js == null || js.isBlank()) return "";
        return "{ " + js + " }";
    }

    /**
     * Vérifie si une chaîne correspond au format UUID.
     *
     * @param s chaîne à tester
     * @return {@code true} si la chaîne ressemble à un UUID
     */
    private boolean looksLikeUuid(String s) {
        return s != null && s.matches("^[0-9a-fA-F\\-]{36}$");
    }

    /**
     * Vérifie si une chaîne est nulle ou vide après suppression des espaces.
     *
     * @param s chaîne à tester
     * @return {@code true} si la chaîne est vide
     */
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Requête utilisée pour déclencher la génération d'une section.
     *
     * @param spec spécification de la section
     * @param x position horizontale dans le canvas
     * @param y position verticale dans le canvas
     */
    public record SectionRequest(SectionSpec spec, int x, int y) {}
}