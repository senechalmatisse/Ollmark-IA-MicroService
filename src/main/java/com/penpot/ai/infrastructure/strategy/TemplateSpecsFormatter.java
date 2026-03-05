package com.penpot.ai.infrastructure.strategy;

import java.util.*;
import org.springframework.stereotype.Component;
import com.penpot.ai.core.domain.TemplateSpecs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Composant responsable du formatage des spécifications d'un modèle ({@link TemplateSpecs}) 
 * en un plan d'exécution textuel (blueprint) destiné au LLM.
 * <p>
 * Le texte généré guide ainsi le modèle, étape par étape, afin qu'il invoque les outils de l'API Penpot dans 
 * l'ordre approprié. De plus, il indique clairement comment substituer les variables textuelles 
 * dynamiques (placeholders) par les valeurs contextuelles extraites de la requête de l'utilisateur.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateSpecsFormatter {

    /**
     * Dictionnaire de correspondance statique permettant de transposer les clés issues du format JSON 
     * vers les noms de paramètres formels attendus par les méthodes Java de l'API. Par exemple, 
     * la clé "fill" est traduite en "fillColor" pour garantir la compatibilité des appels.
     */
    private static final Map<String, String> PARAM_KEY_REMAPPING = Map.of(
        "fill",   "fillColor",
        "stroke", "strokeColor"
    );

    /**
     * Orchestre la compilation des spécifications fournies afin de générer un plan d'exécution complet et ordonné.
     *
     * @param specs Les spécifications structurelles et sémantiques du modèle à formater.
     * @return Une chaîne de caractères représentant le plan d'exécution, lisible par la machine et par l'humain.
     */
    public String format(TemplateSpecs specs) {
        List<Map<String, Object>> elements = specs.getElements();
        int elementCount = elements != null ? elements.size() : 0;

        log.debug("Formatting template: {} ({} elements)", specs.getTemplateId(), elementCount);

        StringBuilder sb = new StringBuilder(512);
        appendHeader(sb, specs);
        appendPlaceholders(sb, specs.getTextPlaceholders());
        appendElements(sb, elements);
        appendRules(sb, elementCount);

        return sb.toString();
    }

    /**
     * Construit et intègre l'en-tête du plan d'exécution au sein du constructeur de chaîne.
     *
     * @param sb Le constructeur de chaîne de caractères ({@link StringBuilder}) en cours d'assemblage.
     * @param specs L'objet contenant les métadonnées globales du modèle, notamment ses dimensions.
     */
    private void appendHeader(StringBuilder sb, TemplateSpecs specs) {
        sb.append("=== DESIGN BLUEPRINT: ").append(specs.getTemplateId()).append(" ===\n")
          .append("Canvas: ")
          .append(specs.getDimensions().getWidth()).append("x")
          .append(specs.getDimensions().getHeight()).append("px\n\n")
          .append("Execute each tool below ONE BY ONE in order.\n")
          .append("Replace text placeholders with values from the user request:\n");
    }

    /**
     * Ajoute la définition des variables textuelles dynamiques (placeholders) au document.
     *
     * @param sb Le constructeur de chaîne de caractères ({@link StringBuilder}) recevant les instructions.
     * @param placeholders Le dictionnaire associant les clés des variables textuelles à leurs descriptions ou valeurs de substitution.
     */
    private void appendPlaceholders(StringBuilder sb, Map<String, String> placeholders) {
        if (placeholders != null && !placeholders.isEmpty()) {
            placeholders.forEach((key, hint) ->
                sb.append("  - ").append(key).append(" → ").append(hint).append("\n"));
        }
        sb.append("\n");
    }

    /**
     * Intègre la séquence ordonnée des outils graphiques à appeler au sein du plan d'exécution.
     *
     * @param sb Le constructeur de chaîne de caractères ({@link StringBuilder}) en cours d'assemblage.
     * @param elements La liste structurée des éléments graphiques à transposer en instructions d'outils.
     */
    private void appendElements(StringBuilder sb, List<Map<String, Object>> elements) {
        if (elements == null || elements.isEmpty()) {
            sb.append("(no elements defined)\n");
            return;
        }

        for (int i = 0; i < elements.size(); i++) {
            Map<String, Object> el = elements.get(i);
            sb.append(i + 1).append(". ").append(el.get("tool")).append("\n")
              .append("   ").append(normalizeParams(el.get("params"))).append("\n");
        }
    }

    /**
     * Conclut le plan d'exécution en définissant les règles de contrainte strictes pour le LLM.
     *
     * @param sb Le constructeur de chaîne de caractères ({@link StringBuilder}) destiné à recevoir les directives finales.
     * @param elementCount Le nombre total d'outils à exécuter, utilisé pour garantir l'exhaustivité du traitement par le LLM.
     */
    private void appendRules(StringBuilder sb, int elementCount) {
        sb.append("\nRULES:\n")
          .append("- Call tools ONE BY ONE. Do NOT return JSON.\n")
          .append("- Execute ALL ").append(elementCount).append(" tools listed above. Do not stop early.\n")
          .append("- Never use commas as decimal separator (95.0 not 95,0).\n");
    }

    /**
     * Normalise les paramètres d'un élément graphique en adaptant leur nomenclature.
     *
     * @param rawParams L'objet représentant les paramètres bruts extraits de l'élément JSON.
     * @return Un dictionnaire ({@link Map}) contenant les paramètres normalisés et ordonnés, prêt à être injecté dans le blueprint.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> normalizeParams(Object rawParams) {
        if (!(rawParams instanceof Map<?, ?> raw)) return new LinkedHashMap<>();

        Map<String, Object> params = new LinkedHashMap<>((Map<String, Object>) raw);
        PARAM_KEY_REMAPPING.forEach((from, to) -> {
            if (params.containsKey(from)) params.put(to, params.remove(from));
        });
        return params;
    }
}