package com.penpot.ai.adapters.out.ai;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.penpot.ai.infrastructure.provider.AiProviderStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.penpot.ai.application.advisor.InspectionFirstAdvisor;
import com.penpot.ai.application.advisor.ReReadingAdvisor;
import com.penpot.ai.adapters.out.ai.RequestComplexityAnalyzer;
import com.penpot.ai.application.advisor.ToolErrorAdvisor;
import com.penpot.ai.application.advisor.ToolFailureRecoveryAdvisor;
import com.penpot.ai.application.advisor.ToolResultValidatorAdvisor;
import com.penpot.ai.application.advisor.ToolRetryLimiterAdvisor;
import com.penpot.ai.application.router.ToolCategoryResolver;
import com.penpot.ai.application.service.PromptsConfigService;
import com.penpot.ai.core.domain.TaskComplexity;
import com.penpot.ai.core.domain.ToolCategory;
import com.penpot.ai.core.ports.out.AiServicePort;
import com.penpot.ai.core.ports.out.ToolRouterPort;
import com.penpot.ai.shared.exception.ToolExecutionException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Adaptateur AI unifié — utilise la stratégie active (Ollama ou OpenRouter).
 *
 * Pour switcher de fournisseur, il suffit de changer dans application.yml :
 *   penpot.ai.provider: ollama      # local
 *   penpot.ai.provider: openrouter  # cloud
 *
 * Remplace OllamaAiAdapter et OpenRouterAiAdapter.
 */
@Slf4j
@Component
@Primary
public class UnifiedAiAdapter implements AiServicePort {

    private static final String EMPTY_RESPONSE_FALLBACK =
        "L'action a été exécutée, mais le modèle n'a retourné aucun texte de confirmation.";

    private final AiProviderStrategy providerStrategy;
    private final RequestComplexityAnalyzer complexityAnalyzer;
    private final ChatMemory chatMemory;
    private final PromptsConfigService promptsConfigService;
    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;
    private final InspectionFirstAdvisor inspectionFirstAdvisor;
    private final ToolRouterPort toolRouter;
    private final ToolCategoryResolver toolCategoryResolver;
    private final ToolErrorAdvisor toolErrorAdvisor;
    private final ToolFailureRecoveryAdvisor toolFailureRecoveryAdvisor;
    private final ToolRetryLimiterAdvisor toolRetryLimiterAdvisor;
    private final ToolResultValidatorAdvisor toolResultValidatorAdvisor;

    public UnifiedAiAdapter(
        AiProviderStrategy providerStrategy,
        RequestComplexityAnalyzer complexityAnalyzer,
        ChatMemory chatMemory,
        PromptsConfigService promptsConfigService,
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor,
        ToolRouterPort toolRouter,
        ToolCategoryResolver toolCategoryResolver,
        InspectionFirstAdvisor inspectionFirstAdvisor,
        ToolErrorAdvisor toolErrorAdvisor,
        ToolFailureRecoveryAdvisor toolFailureRecoveryAdvisor,
        ToolRetryLimiterAdvisor toolRetryLimiterAdvisor,
        ToolResultValidatorAdvisor toolResultValidatorAdvisor
    ) {
        this.providerStrategy = providerStrategy;
        this.complexityAnalyzer = complexityAnalyzer;
        this.chatMemory = chatMemory;
        this.promptsConfigService = promptsConfigService;
        this.retrievalAugmentationAdvisor = retrievalAugmentationAdvisor;
        this.toolRouter = toolRouter;
        this.toolCategoryResolver = toolCategoryResolver;
        this.inspectionFirstAdvisor = inspectionFirstAdvisor;
        this.toolErrorAdvisor = toolErrorAdvisor;
        this.toolFailureRecoveryAdvisor = toolFailureRecoveryAdvisor;
        this.toolRetryLimiterAdvisor = toolRetryLimiterAdvisor;
        this.toolResultValidatorAdvisor = toolResultValidatorAdvisor;

        log.info("[UnifiedAiAdapter] Using provider: {}", providerStrategy.providerId());
    }

    @Override
    public Flux<String> chat(String conversationId, String userMessage) {
        try {
            TaskComplexity complexity = complexityAnalyzer.analyze(userMessage);
            log.info("Processing chat (provider={}, conversation={}, complexity={})",
                providerStrategy.providerId(), conversationId, complexity);

            Set<ToolCategory> categories = toolRouter.route(userMessage);
            log.info("[Router] → categories: {}", categories);

            Object[] tools = toolCategoryResolver.resolveTools(categories);
            log.info("[Registry] → {} tool instance(s) selected", tools.length);

            ChatClient client = providerStrategy.buildForComplexity(complexity);

            String response = client.prompt()
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
                    "conversationId", conversationId
                ))
                .call()
                .content();

            if (response == null || response.isBlank()) {
                log.warn("[UnifiedAiAdapter] Empty/null response received for conversation={}. Using fallback text.",
                    conversationId);
                response = EMPTY_RESPONSE_FALLBACK;
            }

            return Flux.just(response);

        } catch (Exception e) {
            log.error("Failed to process chat for conversation: {}", conversationId, e);
            return Flux.error(new ToolExecutionException(
                "Chat failed for conversation " + conversationId + ": " + e.getMessage(), e));
        }
    }

    @Override
    @Transactional
    public void clearConversation(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("Conversation ID cannot be null or empty");
        }
        try {
            chatMemory.clear(conversationId);
            log.info("Cleared conversation: {}", conversationId);
        } catch (Exception e) {
            log.error("Failed to clear conversation: {}", conversationId, e);
            throw new ToolExecutionException(
                "Failed to clear conversation " + conversationId + ": " + e.getMessage(), e
            );
        }
    }

    private List<Advisor> buildAdvisors(Set<ToolCategory> categories) {
        List<Advisor> advisors = new ArrayList<>();
        advisors.add(inspectionFirstAdvisor);
        advisors.add(ToolCallAdvisor.builder().build());
        advisors.add(toolRetryLimiterAdvisor);
        advisors.add(toolErrorAdvisor);
        advisors.add(toolFailureRecoveryAdvisor);
        advisors.add(toolResultValidatorAdvisor);

        if (categories.contains(ToolCategory.TEMPLATE_SEARCH)
            || categories.contains(ToolCategory.SHAPE_CREATION)) {
            advisors.add(retrievalAugmentationAdvisor);
        }

        advisors.add(new ReReadingAdvisor());
        advisors.add(new SimpleLoggerAdvisor());
        return advisors;
    }
}