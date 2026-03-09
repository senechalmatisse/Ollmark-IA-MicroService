package com.penpot.ai.application.router;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.penpot.ai.core.domain.ToolCategory;
import com.penpot.ai.core.ports.out.ToolRouterPort;
import com.penpot.ai.shared.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de routing d'intention basé sur un modèle LLM léger.
 *
 * <h2>Rôle dans l'architecture</h2>
 * <p>Ce service est la première étape du pipeline de traitement dans
 * {@link com.penpot.ai.adapters.out.ai.OllamaAiAdapter}. Il analyse le message
 * utilisateur et retourne l'ensemble des {@link ToolCategory} pertinentes, permettant
 * au registry ({@link ToolCategoryResolver}) de ne charger que les tools nécessaires
 * avant d'appeler le modèle exécuteur.</p>
 *
 * <h2>Position dans le pipeline</h2>
 * <pre>
 * userMessage
 *     │
 *     ├─ (1) RequestComplexityAnalyzer  →  TaskComplexity
 *     │
 *     ├─ (2) IntentRouterService        →  Set&lt;ToolCategory&gt;   ← ICI
 *     │       llama3.1
 *     │
 *     ├─ (3) ToolCategoryResolver       →  Object[] tools
 *     │
 *     └─ (4) ChatClientFactory          →  réponse finale
 *             qwen3:8b
 * </pre>
 *
 * <h2>Stratégie de classification en 3 niveaux</h2>
 * <ol>
 *   <li><b>Niveau 1 — JSON structuré</b> : appel au modèle + parsing Jackson.
 *       Le modèle est instruit de répondre exclusivement avec un objet JSON
 *       {@code {"categories": ["CAT1", "CAT2"]}}. Jackson extrait la liste
 *       via {@code readTree()} + {@code convertValue()}.</li>
 *   <li><b>Niveau 2 — Keyword scan</b> : si le niveau 1 lève une exception
 *       (JSON malformé, réponse vide, etc.), une seconde requête est effectuée
 *       et la réponse brute est scannée à la recherche des noms d'enum
 *       {@link ToolCategory} en majuscules.</li>
 *   <li><b>Fallback final</b> : si les deux niveaux échouent, retourne
 *       {@code EnumSet.of(ToolCategory.INSPECTION)} — valeur minimale sûre
 *       permettant à l'exécuteur de lire l'état de la page.</li>
 * </ol>
 *
 * <h2>Contrat du port</h2>
 * <p>Cette classe implémente {@link ToolRouterPort} dont le contrat interdit
 * toute propagation d'exception. Toutes les erreurs sont attrapées et dégradées
 * vers le niveau suivant. La méthode {@link #route(String)} ne lève donc
 * jamais d'exception.</p>
 *
 * <h2>Catégories disponibles</h2>
 * <table border="1">
 *   <tr><th>Catégorie</th><th>Déclencheurs typiques</th></tr>
 *   <tr><td>{@code SHAPE_CREATION}</td><td>créer rectangle, cercle, board, frame</td></tr>
 *   <tr><td>{@code SHAPE_MODIFICATION}</td><td>déplacer, redimensionner, rotation, dupliquer</td></tr>
 *   <tr><td>{@code COLOR_AND_STYLE}</td><td>couleur, opacité, ombre, dégradé, contour</td></tr>
 *   <tr><td>{@code LAYOUT_AND_ALIGNMENT}</td><td>aligner, distribuer, grouper, ordonner</td></tr>
 *   <tr><td>{@code CONTENT_AND_TEXT}</td><td>texte, titre, paragraphe, image, média</td></tr>
 *   <tr><td>{@code ASSET_MANAGEMENT}</td><td>composant, style partagé, police, librairie</td></tr>
 *   <tr><td>{@code INSPECTION}</td><td>lister, trouver, propriétés, état de la page</td></tr>
 *   <tr><td>{@code DELETION}</td><td>supprimer, effacer, retirer un élément</td></tr>
 *   <tr><td>{@code TEMPLATE_SEARCH}</td><td>template, affiche, post réseaux sociaux, flyer</td></tr>
 * </table>
 *
 * @see ToolRouterPort          Port de sortie implémenté
 * @see ToolCategory            Enum des catégories disponibles
 * @see com.penpot.ai.infrastructure.config.RouterConfig  Configuration du bean router
 * @see com.penpot.ai.adapters.out.ai.OllamaAiAdapter     Consommateur de ce service
 */
@Slf4j
@Service
public class IntentRouterService implements ToolRouterPort {

    /**
     * Prompt système pour les petits modèles de classification.
     *
     * <p>Structure du prompt :</p>
     * <ul>
     *   <li><b>Rôle</b> : définit le modèle comme classifieur exclusif</li>
     *   <li><b>Catégories</b> : liste exhaustive avec mots-clés déclencheurs par catégorie</li>
     *   <li><b>Règles</b> : contraintes strictes (JSON uniquement, minimalisme)</li>
     *   <li><b>Exemples</b> : 5 cas concrets input/output pour le few-shot learning</li>
     * </ul>
     */
    private static final String ROUTER_SYSTEM_PROMPT = """
        You are a tool classifier for Penpot, a graphic design application.
        Your ONLY job is to return a JSON object with the relevant tool categories.

        AVAILABLE CATEGORIES (choose only from these exact values):
        - SHAPE_CREATION    : create rectangle, circle, ellipse, board, frame
        - SHAPE_MODIFICATION: move, resize, rotate, scale, duplicate, clone existing shape
        - COLOR_AND_STYLE   : fill color, stroke, gradient, shadow, opacity, blur
        - LAYOUT_AND_ALIGNMENT: align, distribute, group, ungroup, arrange, order
        - CONTENT_AND_TEXT  : text, title, paragraph, subtitle, image from URL
        - ASSET_MANAGEMENT  : component, shared style, font
        - INSPECTION        : list elements, find shape, get properties, what is on the page
        - DELETION          : delete, remove, clear element
        - TEMPLATE_SEARCH : find existing template, search template library, get template example
        - CONTENT_GENERATION : create marketing section, hero section, landing section, premium section, structured block

        RULES:
        - Return ONLY valid JSON, nothing else.
        - Include INSPECTION when the request is ambiguous or requires reading page state.
        - Be minimal: only include categories truly needed.

        EXAMPLES:
        User: "put a red fill on the rectangle"
        Response: {"categories": ["COLOR_AND_STYLE", "INSPECTION"]}

        User: "create a 400x200 blue rectangle"
        Response: {"categories": ["SHAPE_CREATION"]}

        User: "align all elements to the left then group them"
        Response: {"categories": ["LAYOUT_AND_ALIGNMENT", "INSPECTION"]}

        User: "find me a social media template"
        Response: {"categories": ["TEMPLATE_SEARCH"]}

        User: "delete the header"
        Response: {"categories": ["DELETION", "INSPECTION"]}

        User: "create a marketing hero section"
        Response: {"categories": ["CONTENT_GENERATION"]}

        Return ONLY JSON. No explanation. No markdown.
    """;

    private final ChatClient routerChatClient;

    public IntentRouterService(
        @Qualifier("routerChatClient") ChatClient routerChatClient
    ) {
        this.routerChatClient = routerChatClient;
    }

    /**
     * Analyse le message utilisateur, classifie l'intention et retourne
     * les catégories de tools pertinentes.
     *
     * <p>Orchestre les trois niveaux de classification dans l'ordre suivant :</p>
     * <ol>
     *   <li>{@link #classifyWithStructuredOutput(String)} — parsing JSON strict</li>
     *   <li>{@link #classifyWithKeywordScan(String)} — scan textuel (si niveau 1 échoue)</li>
     *   <li>{@code EnumSet.of(INSPECTION)} — fallback ultime (si niveau 2 échoue)</li>
     * </ol>
     *
     * @param userMessage message brut de l'utilisateur, ne doit pas être {@code null}
     * @return ensemble non-null et non-vide des {@link ToolCategory} pertinentes ;
     *         contient au minimum {@link ToolCategory#INSPECTION}
     */
    @Override
    public Set<ToolCategory> route(String userMessage) {
        log.debug("[Router] Classifying intent for message: '{}'", truncate(userMessage, 80));
        long start = System.currentTimeMillis();

        try {
            Set<ToolCategory> categories = classifyWithStructuredOutput(userMessage);
            log.info("[Router] Classified in {}ms → categories: {}",
                System.currentTimeMillis() - start, categories);
            return categories;
        } catch (PenpotAiException e) {
            log.warn("[Router] Structured output failed, falling back to keyword scan. Cause: {}", e.getMessage());

            try {
                Set<ToolCategory> categories = classifyWithKeywordScan(userMessage);
                log.info("[Router] Keyword scan result in {}ms → categories: {}",
                    System.currentTimeMillis() - start, categories);
                return categories;
            } catch (Exception ex) {
                log.error("[Router] All classification strategies failed. Using INSPECTION fallback.", ex);
                return EnumSet.of(ToolCategory.INSPECTION);
            }
        } catch (Exception e) {
            log.error("[Router] Unexpected error during classification. Using INSPECTION fallback.", e);
            return EnumSet.of(ToolCategory.INSPECTION);
        }
    }

    /**
     * Appelle le modèle LLM et parse sa réponse JSON via Jackson.
     *
     * @param userMessage message brut de l'utilisateur
     * @return ensemble non-null et non-vide des catégories parsées
     * @throws RuntimeException si la réponse du modèle n'est pas un JSON valide
     */
    private Set<ToolCategory> classifyWithStructuredOutput(String userMessage) {
        try {
            String raw = routerChatClient.prompt()
                .system(ROUTER_SYSTEM_PROMPT)
                .user(userMessage)
                .call()
                .content();

            ObjectMapper mapper = new ObjectMapper();
            var node = mapper.readTree(raw);
            List<String> categories = mapper.convertValue(
                node.get("categories"), new TypeReference<List<String>>() {}
            );
            return parseCategories(categories != null ? categories : List.of());
        } catch (JsonProcessingException e) {
            throw new FormattingException("JSON parsing failed while classifying intent: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FormattingException("Unexpected error during structured output classification", e);
        }
    }

    /**
     * Stratégie de fallback : scan des noms d'enum dans la réponse texte brute.
     *
     * <p>Utilisée quand {@link #classifyWithStructuredOutput(String)} échoue,
     * typiquement quand le modèle produit du texte autour du JSON ou une réponse
     * non structurée.</p>
     *
     * @param userMessage message brut de l'utilisateur
     * @return ensemble non-null et non-vide des catégories détectées,
     *         ou {@code EnumSet.of(INSPECTION)} si aucune correspondance
     */
    private Set<ToolCategory> classifyWithKeywordScan(String userMessage) {
        String rawResponse = routerChatClient.prompt()
            .system(ROUTER_SYSTEM_PROMPT)
            .user(userMessage)
            .call()
            .content();

        log.debug("[Router] Raw text response for keyword scan: {}", rawResponse);

        Set<ToolCategory> found = Arrays.stream(ToolCategory.values())
            .filter(category -> rawResponse != null
                && rawResponse.toUpperCase().contains(category.name()))
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(ToolCategory.class)));

        return found.isEmpty() ? EnumSet.of(ToolCategory.INSPECTION) : found;
    }

    /**
     * Convertit une liste de chaînes (noms d'enum) en {@code Set<ToolCategory>}.
     *
     * @param rawCategories liste brute des noms de catégories retournée par le modèle
     * @return ensemble non-null et non-vide des catégories valides ;
     *         contient au minimum {@link ToolCategory#INSPECTION} en cas d'échec total
     */
    private Set<ToolCategory> parseCategories(List<String> rawCategories) {
        if (rawCategories.isEmpty()) {
            log.warn("[Router] Model returned empty categories list, using INSPECTION fallback");
            return EnumSet.of(ToolCategory.INSPECTION);
        }

        Set<ToolCategory> result = rawCategories.stream()
            .map(String::trim)
            .map(String::toUpperCase)
            .flatMap(name -> {
                try {
                    return java.util.stream.Stream.of(ToolCategory.valueOf(name));
                } catch (IllegalArgumentException e) {
                    log.warn("[Router] Unknown category name ignored: '{}'. "
                        + "Valid values: {}", name, Arrays.toString(ToolCategory.values()));
                    return java.util.stream.Stream.empty();
                }
            })
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(ToolCategory.class)));

        return result.isEmpty() ? EnumSet.of(ToolCategory.INSPECTION) : result;
    }

    /**
     * Tronque une chaîne à {@code max} caractères pour les logs.
     *
     * @param str chaîne à tronquer, peut être {@code null}
     * @param max nombre maximum de caractères à conserver
     * @return la chaîne tronquée avec {@code "..."} si dépassement,
     *         {@code "null"} si l'entrée est {@code null},
     *         la chaîne originale si sa longueur est inférieure ou égale à {@code max}
     */
    private String truncate(String str, int max) {
        if (str == null) return "null";
        return str.length() <= max ? str : str.substring(0, max) + "...";
    }
}