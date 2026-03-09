package com.penpot.ai.application.service;

import java.util.*;

import org.springframework.stereotype.Service;

import com.penpot.ai.core.domain.TemplateSpecs;
import com.penpot.ai.core.domain.spec.DimensionsSpec;
import com.penpot.ai.model.MarketingTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Service dédié à l'extraction et à la structuration des spécifications depuis les modèles JSON.
 * <p>
 * Avec le nouveau format simplifié (reposant sur 24 fichiers JSON directs), l'extraction
 * s'effectue en transformant les métadonnées brutes en objets de domaine fortement typés.
 * </p>
 */
@Slf4j
@Service
public class TemplateSpecsExtractor {

    /**
     * Analyse un modèle marketing brut afin d'en extraire l'ensemble des spécifications structurées.
     * 
     * @param template Le modèle source ({@link MarketingTemplate}) contenant les données brutes.
     * @return Un objet {@link TemplateSpecs} regroupant les spécifications validées et structurées.
     */
    public TemplateSpecs extractSpecs(MarketingTemplate template) {
        log.info("Extracting specs for template: {} (type: {})",
                template.getId(), template.getType());

        Map<String, Object> metadata = template.getMetadata() != null
                ? template.getMetadata() : new HashMap<>();

        List<Map<String, Object>> elements = template.getLayoutStructure() != null
                ? template.getLayoutStructure()
                : new ArrayList<>();

        if (elements.isEmpty()) {
            log.warn("Template '{}' has no layout_structure elements", template.getId());
        }

        return TemplateSpecs.builder()
            .templateId(template.getId())
            .type(template.getType())
            .description(template.getDescription())
            .tags(template.getTags())
            .dimensions(extractDimensions(metadata))
            .elements(elements)
            .textPlaceholders(template.getTextPlaceholders())
            .build();
    }

    /**
     * Extrait les dimensions de l'image à partir des métadonnées du modèle de façon sécurisée.
     * 
     * @param metadata Le dictionnaire des métadonnées extraites du modèle.
     * @return Un objet {@link DimensionsSpec} représentant la largeur et la hauteur calculées.
     */
    private DimensionsSpec extractDimensions(Map<String, Object> metadata) {
        Object formatObj = metadata.get("format");
        int width = 1080;
        int height = 1080;

        if (formatObj instanceof Map<?, ?> format) {
            Object w = format.get("width");
            Object h = format.get("height");
            if (w instanceof Number widthNum) width = widthNum.intValue();
            if (h instanceof Number heightNum) height = heightNum.intValue();
        } else {
            log.warn("metadata.format absent pour ce template — dimensions par défaut (1080x1080)");
        }

        return DimensionsSpec.builder()
            .width(width)
            .height(height)
            .build();
    }
}