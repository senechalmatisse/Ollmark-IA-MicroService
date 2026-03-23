package com.penpot.ai.application.tools.support;

import lombok.experimental.UtilityClass;

import java.util.*;

import com.penpot.ai.shared.util.JsStringUtils;

/**
 * Classe utilitaire responsable de la génération dynamique et centralisée des fragments de code JavaScript destinés à l'API Penpot. 
 */
@UtilityClass
public class PenpotJsSnippets {

    /**
     * Construit le script JavaScript permettant de récupérer une forme géométrique spécifique à partir de son identifiant unique. 
     *
     * @param shapeId L'identifiant unique (UUID) de la forme cible au sein de l'environnement Penpot.
     * @return        Le fragment de code JavaScript prêt à être exécuté par le moteur de rendu.
     */
    public static String findShapeOrFallback(String shapeId) {
        return JsScriptLoader.loadWith("snippets/find-shape.js", Map.of("shapeId", shapeId));
    }

    /**
     * Produit un fragment de code visant à isoler le premier élément d'une collection d'identifiants. 
     *
     * @param shapeIds La liste des identifiants disponibles, dont seul le premier élément sera exploité par le script généré.
     * @return         Le code JavaScript effectuant la sélection prioritaire ou son repli.
     */
    public static String findFirstShapeOrFallback(List<String> shapeIds) {
        if (shapeIds == null || shapeIds.isEmpty()) {
            return JsScriptLoader.load("snippets/find-first-shape-selection.js");
        }
        return JsScriptLoader.loadWith("snippets/find-first-shape.js",
            Map.of("shapeId", shapeIds.get(0)));
    }

    /**
     * Génère dynamiquement un script chargé d'agréger un ensemble de formes géométriques en itérant sur une liste d'identifiants. 
     *
     * @param ids      La collection des UUIDs à localiser ; une valeur nulle ou vide déclenche le comportement de repli sur la sélection courante.
     * @param toolName La nomenclature de l'outil appelant, injectée dans les messages de journalisation pour faciliter le débogage côté client.
     * @return         Le bloc d'instructions JavaScript accomplissant l'agrégation robuste des entités visuelles.
     */
    public static String collectShapesOrFallback(List<String> ids, String toolName) {
        if (ids == null || ids.isEmpty()) {
            return JsScriptLoader.loadWith("snippets/collect-shapes-selection.js",
                Map.of("toolName", toolName));
        }

        StringBuilder sb = new StringBuilder("const shapes = [];\n");
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            sb.append(String.format("""
                try {
                    const _s%d = penpot.currentPage.getShapeById('%s');
                    if (_s%d) shapes.push(_s%d);
                } catch (e) {
                    console.log('[%s] Invalid ID or not found: %s');
                }
                """, i, id, i, i, toolName, id));
        }
        sb.append(String.format("""
            if (shapes.length === 0) shapes.push(...penpot.selection);
            """, toolName));
        return sb.toString();
    }

    /**
     * Construit de bout en bout le code JavaScript nécessaire à l'instanciation et au paramétrage d'un nouvel élément textuel 
     * au sein du canevas Penpot.
     *
     * @param content    Le contenu sémantique brut à afficher, soumis automatiquement à un processus d'échappement.
     * @param x          La coordonnée horizontale de positionnement sur l'espace de travail.
     * @param y          La coordonnée verticale de positionnement sur l'espace de travail.
     * @param fontSize   La dimension de la police d'écriture (paramètre ignoré si nul ou inférieur à zéro).
     * @param fontWeight L'épaisseur typographique souhaitée (paramètre ignoré si nul ou vide).
     * @param fillColor  Le code couleur hexadécimal à appliquer en remplissage (paramètre ignoré si nul ou vide).
     * @param name       L'intitulé technique de l'élément dans l'arborescence des calques (paramètre ignoré si nul ou vide).
     * @return           Le script JavaScript complet orchestrant la création de la zone de texte.
     */
    public static String createText(
        String content, int x, int y,
        Integer fontSize, String fontWeight, String fillColor, String name
    ) {
        String escaped = JsStringUtils.jsSafe(content);
        StringBuilder code = new StringBuilder();
        code.append(String.format("const text = penpot.createText('%s');%n", escaped));
        code.append(String.format("text.x = %d;%n", x));
        code.append(String.format("text.y = %d;%n", y));
        if (fontSize != null && fontSize > 0)
            code.append(String.format("text.fontSize = %d;%n", fontSize));
        if (fontWeight != null && !fontWeight.isBlank())
            code.append(String.format("text.fontWeight = '%s';%n",
                PenpotStyleValues.resolveFontWeight(fontWeight)));
        if (fillColor != null && !fillColor.isBlank())
            code.append(String.format("text.fills = [{ fillColor: '%s' }];%n", fillColor));
        if (name != null && !name.isBlank())
            code.append(String.format("text.name = '%s';%n", JsStringUtils.jsSafe(name)));
        code.append("return text.id;\n");
        return code.toString();
    }
}