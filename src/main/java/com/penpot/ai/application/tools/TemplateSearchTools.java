package com.penpot.ai.application.tools;

import com.penpot.ai.application.service.*;
import com.penpot.ai.application.tools.support.ToolResponseBuilder;
import com.penpot.ai.core.domain.TemplateSpecs;
import com.penpot.ai.infrastructure.strategy.TemplateSpecsFormatter;
import com.penpot.ai.model.MarketingTemplate;
import com.penpot.ai.shared.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Ensemble d'outils permettant la recherche et l'exploration de templates marketing
 * à l'aide d'un système de recherche sémantique basé sur le principe de
 * <strong>Retrieval-Augmented Generation (RAG)</strong>.
 *
 * <p>
 * Cette classe expose plusieurs outils utilisables par un agent IA afin de :
 * </p>
 * <ul>
 *     <li>rechercher des templates marketing par description en langage naturel</li>
 *     <li>récupérer les spécifications complètes de design d’un template</li>
 *     <li>explorer les catégories de templates disponibles</li>
 *     <li>filtrer les templates par type ou par tag</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateSearchTools {

    /** Service responsable de la recherche sémantique des templates via un index RAG. */
    private final RagTemplateService ragTemplateService;

    /** Composant chargé d'extraire les spécifications de design depuis un template marketing. */
    private final TemplateSpecsExtractor specsExtractor;

    /**
     * Composant responsable du formatage des spécifications de design
     * en une représentation exploitable par les outils de génération.
     */
    private final TemplateSpecsFormatter specsFormatter;

    /**
     * Recherche des templates marketing à partir d'une description
     * en langage naturel.
     *
     * <p>
     * Cette méthode utilise un moteur de recherche sémantique afin de
     * trouver les templates les plus pertinents correspondant à la requête.
     * </p>
     *
     * <p>
     * Les résultats retournés contiennent uniquement les informations
     * principales du template :
     * </p>
     * <ul>
     *     <li>identifiant</li>
     *     <li>type de template</li>
     *     <li>description</li>
     *     <li>tags associés</li>
     * </ul>
     *
     * @param query description en langage naturel du contenu marketing recherché
     * @return une chaîne JSON contenant la liste des templates correspondants
     */
    @Tool(description = """
        Search for marketing design templates using semantic search (RAG).
        After finding templates, use getTemplateDesignSpecs() to get full design specifications.
        Categories: social_media_post, email, poster_a3, poster_a4, flyer_a5.
        Returns: list of matching templates with id, type, description, tags.
    """)
    public String searchTemplates(
        @ToolParam(description = "Natural language description of the desired marketing material.")
        String query
    ) {
        log.info("Tool called: searchTemplates (query='{}')", query);
        if (query == null || query.isBlank()) return ToolResponseBuilder.error("Query cannot be empty");

        try {
            List<MarketingTemplate> templates = ragTemplateService.searchTemplates(query);
            if (templates.isEmpty()) return formatNoResults(query);
            log.info("Found {} templates for query: '{}'", templates.size(), query);
            return formatTemplateList(templates);
        } catch (Exception e) {
            log.error("Error searching templates for query: '{}'", query, e);
            return ToolResponseBuilder.error("Search failed: " + e.getMessage());
        }
    }

    /**
     * Récupère les spécifications complètes de design d'un template.
     *
     * <p>
     * Les spécifications retournées incluent notamment :
     * </p>
     * <ul>
     *     <li>les dimensions (largeur, hauteur, format)</li>
     *     <li>la palette de couleurs (primaire, secondaire, fond, texte)</li>
     *     <li>les informations de layout (mode, direction, positionnement)</li>
     *     <li>la typographie (polices, tailles, styles)</li>
     *     <li>les paramètres d’arrière-plan (couleur, gradient, texture)</li>
     *     <li>les éléments décoratifs (badges, icônes, éléments graphiques)</li>
     *     <li>les recommandations d’utilisation et d’ordre de création</li>
     * </ul>
     *
     * @param templateId identifiant unique du template obtenu via {@link #searchTemplates(String)}
     * @return une chaîne JSON contenant les spécifications de design
     */
    @Tool(description = """
        Get detailed design specifications from a template.

        Returns complete design specifications including:
        - Dimensions (width, height, format)
        - Colors (primary, secondary, background, text)
        - Layout (mode, direction, positioning hints)
        - Typography (fonts, sizes, styles)
        - Background (color, texture, gradient, pattern)
        - Elements (badges, icons, decorative elements)
        - Usage hints (creation order, type-specific guidance)

        Use these specs as parameters for creation tools.
    """)
    public String getTemplateDesignSpecs(
        @ToolParam(description = "The template ID obtained from searchTemplates().")
        String templateId
    ) {
        log.info("Tool called: getTemplateDesignSpecs (templateId='{}')", templateId);
        if (templateId == null || templateId.isBlank()) return ToolResponseBuilder.error("Template ID cannot be empty");

        try {
            MarketingTemplate template = ragTemplateService.getTemplateById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));
            TemplateSpecs specs = specsExtractor.extractSpecs(template);
            return specsFormatter.format(specs);
        } catch (IllegalArgumentException e) {
            log.warn("Template not found: {}", templateId);
            return ToolResponseBuilder.error("Template not found: " + templateId);
        } catch (Exception e) {
            log.error("Error extracting design specs from template: {}", templateId, e);
            return ToolResponseBuilder.error("Failed to extract specs: " + e.getMessage());
        }
    }

    /**
     * Liste l'ensemble des types de templates et des tags disponibles
     * dans le système.
     *
     * @return une chaîne JSON contenant les types de templates,
     * les tags disponibles et les statistiques globales
     */
    @Tool(description = """
        List all available template types, categories, and tags.
        Call this when user asks "what can you create?" or wants to browse options.
    """)
    public String listTemplateTypes() {
        log.info("Tool called: listTemplateTypes");
        try {
            Set<String> types = ragTemplateService.getAvailableTypes();
            Set<String> tags  = ragTemplateService.getAvailableTags();
            int total         = ragTemplateService.getTemplateCount();
            return formatTypesAndTags(types, tags, total);
        } catch (Exception e) {
            log.error("Error listing template types", e);
            return ToolResponseBuilder.error("Failed to list types: " + e.getMessage());
        }
    }

    /**
     * Récupère les templates appartenant à un type spécifique.
     *
     * <p>
     * Les types correspondent aux catégories de supports marketing,
     * par exemple :
     * </p>
     * <ul>
     *     <li>social_media_post</li>
     *     <li>social_media_story</li>
     *     <li>email</li>
     *     <li>poster_a3</li>
     *     <li>poster_a4</li>
     *     <li>flyer_a5</li>
     * </ul>
     *
     * @param type type de template recherché
     * @return une chaîne JSON contenant les templates correspondants
     */
    @Tool(description = """
        Get all templates of a specific type/category.
        Type mapping: "post" = social_media_post, "email" = email, "poster" = poster_a3/a4, "flyer" = flyer_a5.
    """)
    public String getTemplatesByType(
        @ToolParam(description = "Template type: social_media_post, social_media_story, email, poster_a3, poster_a4, flyer_a5")
        String type
    ) {
        log.info("Tool called: getTemplatesByType (type='{}')", type);
        if (type == null || type.isBlank()) return ToolResponseBuilder.error("Type cannot be empty");

        try {
            List<MarketingTemplate> templates = ragTemplateService.getTemplatesByType(type);
            if (templates.isEmpty()) return formatNoTypeResults(type, ragTemplateService.getAvailableTypes());
            return formatTemplatesByType(type, templates);
        } catch (Exception e) {
            log.error("Error getting templates by type: '{}'", type, e);
            return ToolResponseBuilder.error("Failed to get templates: " + e.getMessage());
        }
    }

    /**
     * Récupère les templates associés à un tag spécifique.
     *
     * <p>
     * Les tags peuvent représenter différents aspects :
     * </p>
     * <ul>
     *     <li>un style visuel (modern, classic, minimal, colorful)</li>
     *     <li>un contexte métier (bakery, tech, fashion, food)</li>
     *     <li>un objectif marketing (promotion, announcement, seasonal)</li>
     * </ul>
     *
     * @param tag tag utilisé pour filtrer les templates
     * @return une chaîne JSON contenant les templates correspondants
     */
    @Tool(description = """
        Get templates filtered by a specific style tag.

        Tags represent design styles, contexts, or purposes:
        - Style tags: modern, classic, minimal, colorful, professional
        - Context tags: bakery, tech, fashion, food, product
        - Purpose tags: promotion, announcement, seasonal, deal

        Use this to narrow down templates based on aesthetic or context.
    """)
    public String getTemplatesByTag(
        @ToolParam(description = "Style or context tag to filter by.")
        String tag
    ) {
        log.info("Tool called: getTemplatesByTag (tag='{}')", tag);
        if (tag == null || tag.isBlank()) return ToolResponseBuilder.error("Tag cannot be empty");

        try {
            List<MarketingTemplate> templates = ragTemplateService.getTemplatesByTag(tag);
            if (templates.isEmpty()) return formatNoTagResults(tag, ragTemplateService.getAvailableTags());
            return formatTemplatesByTag(tag, templates);
        } catch (Exception e) {
            log.error("Error getting templates by tag: '{}'", tag, e);
            return ToolResponseBuilder.error("Failed to get templates: " + e.getMessage());
        }
    }

    /**
     * Formate la liste des templates retournés par une recherche.
     *
     * @param templates liste des templates
     * @return représentation JSON des templates
     */
    private String formatTemplateList(List<MarketingTemplate> templates) {
        return String.format(
            "{\"success\": true, \"count\": %d, \"templates\": %s, "
            + "\"message\": \"Use getTemplateDesignSpecs(templateId) to get full design specifications\"}",
            templates.size(), formatTemplatesArray(templates)
        );
    }

    /**
     * Convertit une liste de templates en tableau JSON.
     *
     * @param templates liste des templates
     * @return tableau JSON
     */
    private String formatTemplatesArray(List<MarketingTemplate> templates) {
        return templates.stream()
            .map(t -> String.format("{\"id\": %s, \"type\": %s, \"description\": %s, \"tags\": %s}",
                JsonUtils.escapeJson(t.getId()), JsonUtils.escapeJson(t.getType()),
                JsonUtils.escapeJson(t.getDescription()), formatTags(t.getTags())))
            .collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * Formate les résultats filtrés par type.
     *
     * @param type type de template
     * @param templates liste des templates
     * @return représentation JSON des résultats
     */
    private String formatTemplatesByType(String type, List<MarketingTemplate> templates) {
        return String.format("{\"success\": true, \"type\": %s, \"count\": %d, \"templates\": %s}",
            JsonUtils.escapeJson(type), templates.size(), formatTemplatesArray(templates));
    }

    /**
     * Formate les résultats filtrés par tag.
     *
     * @param tag tag utilisé pour le filtrage
     * @param templates liste des templates
     * @return représentation JSON des résultats
     */
    private String formatTemplatesByTag(String tag, List<MarketingTemplate> templates) {
        return String.format("{\"success\": true, \"tag\": %s, \"count\": %d, \"templates\": %s}",
            JsonUtils.escapeJson(tag), templates.size(), formatTemplatesArray(templates));
    }

    /**
     * Formate la liste des types et tags disponibles.
     *
     * @param types types de templates disponibles
     * @param tags tags disponibles
     * @param total nombre total de templates
     * @return représentation JSON des métadonnées
     */
    private String formatTypesAndTags(Set<String> types, Set<String> tags, int total) {
        String typesJson = types.stream().map(JsonUtils::escapeJson).collect(Collectors.joining(", ", "[", "]"));
        String tagsJson  = tags.stream().map(JsonUtils::escapeJson).collect(Collectors.joining(", ", "[", "]"));
        return String.format(
            "{\"success\": true, \"types\": %s, \"tags\": %s, "
            + "\"count\": {\"types\": %d, \"tags\": %d, \"total_templates\": %d}}",
            typesJson, tagsJson, types.size(), tags.size(), total);
    }

    /**
     * Formate la réponse lorsqu'aucun résultat n'est trouvé pour une requête.
     *
     * @param query requête initiale
     * @return réponse JSON indiquant l'absence de résultats
     */
    private String formatNoResults(String query) {
        return String.format(
            "{\"success\": true, \"templates\": [], \"count\": 0, \"query\": %s, "
            + "\"suggestion\": \"Try listTemplateTypes() to see available categories\"}",
            JsonUtils.escapeJson(query));
    }

    /**
     * Formate la réponse lorsqu'aucun template ne correspond à un type donné.
     *
     * @param type type demandé
     * @param available types disponibles
     * @return réponse JSON indiquant les types disponibles
     */
    private String formatNoTypeResults(String type, Set<String> available) {
        return String.format("{\"success\": false, \"requestedType\": %s, \"availableTypes\": %s}",
            JsonUtils.escapeJson(type),
            available.stream().map(JsonUtils::escapeJson).collect(Collectors.joining(", ", "[", "]")));
    }

    /**
     * Formate la réponse lorsqu'aucun template ne correspond à un tag donné.
     *
     * @param tag tag demandé
     * @param available tags disponibles
     * @return réponse JSON indiquant les tags disponibles
     */
    private String formatNoTagResults(String tag, Set<String> available) {
        return String.format("{\"success\": false, \"requestedTag\": %s, \"availableTags\": %s}",
            JsonUtils.escapeJson(tag),
            available.stream().map(JsonUtils::escapeJson).collect(Collectors.joining(", ", "[", "]")));
    }

    /**
     * Convertit une liste de tags en tableau JSON.
     *
     * @param tags liste des tags
     * @return tableau JSON
     */
    private String formatTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "[]";
        return tags.stream().map(JsonUtils::escapeJson).collect(Collectors.joining(", ", "[", "]"));
    }
}