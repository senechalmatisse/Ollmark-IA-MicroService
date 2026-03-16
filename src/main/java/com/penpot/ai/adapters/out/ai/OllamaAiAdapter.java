package com.penpot.ai.adapters.out.ai;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.*;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.penpot.ai.application.advisor.*;
import com.penpot.ai.application.router.ToolCategoryResolver;
import com.penpot.ai.application.service.*;
import com.penpot.ai.core.domain.*;
import com.penpot.ai.core.ports.out.*;
import com.penpot.ai.infrastructure.config.OllamaConfig.ChatClientFactory;
import com.penpot.ai.shared.exception.ToolExecutionException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Adaptateur de sortie constituant le point de bascule principal vers le service d'intelligence artificielle conversationnelle.
 * <p>
 * Ce composant d'infrastructure implémente le port métier {@link AiServicePort} et orchestre un pipeline de traitement complet.
 * Dans un premier temps, chaque message entrant est évalué par le {@link RequestComplexityAnalyzer} afin de déterminer le profil matériel adéquat. 
 * Ensuite, le message est soumis au routeur via le port {@link ToolRouterPort}, qui identifie l'intention de l'utilisateur et sélectionne les catégories d'outils pertinentes. 
 * Le résolveur {@link ToolCategoryResolver} convertit alors ces catégories en instances d'outils concrètes. 
 * Finalement, la fabrique {@link ChatClientFactory} instancie le client d'exécution final, enrichi des outils filtrés et des divers conseillers nécessaires.
 * </p>
 *
 * <h2>Pipeline complet de traitement (Routing)</h2>
 * <pre>
 * userMessage
 * │
 * ├─ (1) RequestComplexityAnalyzer  →  TaskComplexity (SIMPLE / CREATIVE / COMPLEX)
 * │
 * ├─ (2) ToolRouterPort.route()     →  Set&lt;ToolCategory&gt; (ex: {COLOR_AND_STYLE, INSPECTION})
 * │       (via modèle léger)
 * │
 * ├─ (3) ToolCategoryResolver       →  Object[] tools (sous-ensemble filtré d'outils)
 * │       (via PenpotToolRegistry)
 * │
 * └─ (4) ChatClientFactory          →  Exécution du ChatClient adapté
 *        (via modèle lourd)
 * + Injection des outils filtrés
 * + Ajout des Advisors (RAG, Memory, etc.)
 * → Retourne un Flux de chaînes de caractères (Streaming)
 * </pre>
 * @see ToolRouterPort       Port définissant le routage des requêtes.
 * @see ToolCategoryResolver Service de résolution liant les catégories aux implémentations techniques.
 * @see ChatClientFactory    Usine générant les clients IA selon la complexité exigée.
 * @see RequestComplexityAnalyzer Composant d'analyse sémantique initiale.
 */
@Slf4j
@Component
@RefreshScope
@Primary
public class OllamaAiAdapter implements AiServicePort {

    /**
     * Factory spécialisée permettant d'adapter les paramètres du client IA
     * en fonction de la complexité détectée.
     */
    private final ChatClientFactory chatClientFactory;

    /** Analyseur en charge de déterminer la complexité inhérente aux requêtes utilisateur. */
    private final RequestComplexityAnalyzer complexityAnalyzer;

    /**
     * Gestionnaire de la mémoire conversationnelle,
     * permettant de maintenir le contexte entre les interactions.
     */
    private final ChatMemory chatMemory;

    /** Service de configuration des prompts système. */
    private final PromptsConfigService promptsConfigService;

    /**
     * Conseiller gérant l'architecture RAG modulaire,
     * englobant la réécriture de requêtes,
     * le multi-requêtage et la récupération documentaire.
     */
    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;

    /**
     * Conseiller assurant la prévalence des phases d'inspection
     * avant toute tentative de modification.
     */
    private final InspectionFirstAdvisor inspectionFirstAdvisor;

    /**
     * Conseiller de garde-fou forçant une réponse générique sans code
     * lorsque les informations fournies sont insuffisantes.
     */
    private final MissingInformationAdvisor missingInformationAdvisor;

    /**
     * Port de routage chargé d'analyser l'intention utilisateur
     * pour en déduire les catégories d'outils nécessaires.
     */
    private final ToolRouterPort toolRouter;

    /**
     * Composant de résolution traduisant un ensemble de catégories conceptuelles
     * en un tableau d'instances d'outils techniques Spring AI.
     */
    private final ToolCategoryResolver toolCategoryResolver;

    private final ToolErrorAdvisor toolErrorAdvisor;
    private final ToolFailureRecoveryAdvisor toolFailureRecoveryAdvisor;
    private final ToolRetryLimiterAdvisor toolRetryLimiterAdvisor;

    private final ToolResultValidatorAdvisor toolResultValidatorAdvisor;

    private final SessionContextHolder sessionContextHolder;

    private final MessageService messageService;
    private final ToolCallAdvisor toolCallAdvisor;
    private final ReReadingAdvisor reReadingAdvisor;
    private final SimpleLoggerAdvisor simpleLoggerAdvisor;

    public OllamaAiAdapter(
        ChatClientFactory chatClientFactory,
        RequestComplexityAnalyzer complexityAnalyzer,
        ChatMemory chatMemory,
        PromptsConfigService promptsConfigService,
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor,
        ToolRouterPort toolRouter,
        SessionContextHolder sessionContextHolder,
        ToolCategoryResolver toolCategoryResolver,
        InspectionFirstAdvisor inspectionFirstAdvisor,
        ToolErrorAdvisor toolErrorAdvisor,
        ToolFailureRecoveryAdvisor toolFailureRecoveryAdvisor,
        ToolRetryLimiterAdvisor toolRetryLimiterAdvisor,
        ToolResultValidatorAdvisor toolResultValidatorAdvisor,
        MissingInformationAdvisor missingInformationAdvisor,
        MessageService messageService,
        ToolCallAdvisor toolCallAdvisor,
        ReReadingAdvisor reReadingAdvisor,
        SimpleLoggerAdvisor simpleLoggerAdvisor
    ) {
        this.chatClientFactory = chatClientFactory;
        this.complexityAnalyzer = complexityAnalyzer;
        this.chatMemory = chatMemory;
        this.promptsConfigService = promptsConfigService;
        this.retrievalAugmentationAdvisor = retrievalAugmentationAdvisor;
        this.toolRouter = toolRouter;
        this.sessionContextHolder = sessionContextHolder;
        this.toolCategoryResolver = toolCategoryResolver;
        this.inspectionFirstAdvisor = inspectionFirstAdvisor;
        this.toolErrorAdvisor = toolErrorAdvisor;
        this.toolFailureRecoveryAdvisor = toolFailureRecoveryAdvisor;
        this.toolRetryLimiterAdvisor = toolRetryLimiterAdvisor;
        this.toolResultValidatorAdvisor = toolResultValidatorAdvisor;
        this.missingInformationAdvisor = missingInformationAdvisor;
        this.messageService = messageService;
        this.toolCallAdvisor = toolCallAdvisor;
        this.reReadingAdvisor = reReadingAdvisor;
        this.simpleLoggerAdvisor = simpleLoggerAdvisor;
    }

    /**
     * Pilote le traitement complet d'une requête conversationnelle et retourne une réponse sous forme de flux de données continu (streaming).
     * En suivant l'architecture du pipeline,
     * <ol>
     *   <li>la méthode évalue en premier lieu la complexité de l'interaction.</li> 
     *   <li>Elle délègue ensuite l'analyse sémantique au routeur afin de circonscrire strictement les outils mis à disposition du modèle.</li>
     *   <li>Une fois les instances résolues et le client approprié instancié, l'exécution finale est déclenchée avec injection du contexte, des conseillers et des instructions systèmes.</li>
     * </ol>
     *
     * @param conversationId L'identifiant unique permettant de lier l'interaction à son contexte mémoriel.
     * @param userMessage    La requête textuelle formulée par l'utilisateur.
     * @param userToken      Le jeton de sécurité ou d'identification de l'utilisateur (peut être nul).
     * @return               Un flux réactif ({@link Flux}) émettant les fragments de texte générés par le modèle au fil de l'eau.
     */
    @Override
    public Flux<String> chat(String conversationId, String userMessage) {
        try {
            // Étape 1 : Évaluation de la complexité
            TaskComplexity complexity = complexityAnalyzer.analyze(userMessage);
            log.info("Processing chat (conversation={}, complexity={}, messageLength={})",
                conversationId, complexity, userMessage.length());

            // Étape 2 : Routage sémantique
            Set<ToolCategory> categories = toolRouter.route(userMessage);
            log.info("[Router] → categories: {}", categories);

            // Étape 3 : Résolution des dépendances techniques
            Object[] tools = toolCategoryResolver.resolveTools(categories);
            log.info("[Registry] → {} tool instance(s) selected", tools.length);

            // Étape 4 : Exécution
            ChatClient adaptedClient = chatClientFactory.buildForComplexity(complexity);
            String sessionId = extractSessionId(conversationId);
            sessionContextHolder.set(sessionId);

            try {
                String response = adaptedClient.prompt()
                    .system(promptsConfigService.getInitialInstructions())
                    .user(userMessage)
                    .advisors(buildAdvisors(categories))
                    .advisors(advisor -> advisor
                        .param(CONVERSATION_ID, conversationId)
                        .param(
                            InspectionFirstAdvisor.CTX_TOOL_CATEGORIES,
                            categories.stream().map(Enum::name).toList()
                        )
                    )
                    .tools(tools)
                    .toolContext(Map.of(
                        "activeCategories", categories.stream().map(Enum::name).toList(),
                        "conversationId", conversationId,
                        "sessionId", extractSessionId(conversationId)
                    ))
                    .call()
                    .content();

                return Flux.just(response)
                    .doOnError(e -> log.error("Stream error for conversation: {}", conversationId, e));
            } finally {
                sessionContextHolder.clear();
            }
        } catch (Exception e) {
            log.error("Failed to init stream for conversation: {}", conversationId, e);
            return Flux.error(new ToolExecutionException(
                "Stream init failed for conversation " + conversationId + ": " + e.getMessage(), e
            ));
        }
    }

    @Override
    @Transactional
    public void clearConversation(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("Conversation ID cannot be null or empty");
        }

        try {
            log.info("Clearing conversation history for: {}", conversationId);
            messageService.deleteByConversationIdPrefix(conversationId + ":%");
            chatMemory.clear(conversationId);
            log.info("Cleared conversation: {}", conversationId);
        } catch (Exception e) {
            log.error("Failed to clear conversation: {}", conversationId, e);
            throw new ToolExecutionException(
                "Failed to clear conversation " + conversationId + ": " + e.getMessage(), e
            );
        }
    }

    /**
     * Agence dynamiquement la liste des conseillers à injecter dans le pipeline d'exécution conversationnel.
     *
     * @param categories L'ensemble des catégories sémantiques ciblées pour la requête courante.
     * @return           Une liste ordonnée d'instances {@link Advisor} prêtes à être attachées au client IA.
     */
    private List<Advisor> buildAdvisors(Set<ToolCategory> categories) {
        List<Advisor> advisors = new ArrayList<>();

        advisors.add(inspectionFirstAdvisor);

        advisors.add(toolCallAdvisor);

        //advisors.add(missingInformationAdvisor);
        advisors.add(toolRetryLimiterAdvisor);
        advisors.add(toolErrorAdvisor);
        advisors.add(toolFailureRecoveryAdvisor);

        advisors.add(toolResultValidatorAdvisor);

        if (categories.contains(ToolCategory.TEMPLATE_SEARCH) 
            || categories.contains(ToolCategory.CONTENT_AND_TEXT)) {
            advisors.add(retrievalAugmentationAdvisor);
        }

        advisors.add(reReadingAdvisor);
        advisors.add(simpleLoggerAdvisor);

        return advisors;
    }

    private String extractSessionId(String conversationKey) {
        if (conversationKey == null) return "";
        int sep = conversationKey.indexOf(':');
        return sep >= 0 ? conversationKey.substring(sep + 1) : "";
    }
}