package com.penpot.ai.adapters.out.ai;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

import java.util.*;

import com.penpot.ai.infrastructure.provider.AiProviderStrategy;
import com.penpot.ai.infrastructure.session.SessionContextHolder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.penpot.ai.application.advisor.*;
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
    private final ToolCallAdvisor toolCallAdvisor;

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
        ToolResultValidatorAdvisor toolResultValidatorAdvisor,
        ToolCallAdvisor toolCallAdvisor
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
        this.toolCallAdvisor = toolCallAdvisor;

        log.info("[UnifiedAiAdapter] Using provider: {}", providerStrategy.providerId());
    }

    @Override
    public Flux<String> chat(String conversationId, String userMessage, String sessionId) {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("[CHAT START] thread={} | conversationId={} | sessionId={}",
            Thread.currentThread().getId(), conversationId, sessionId);
        log.info("[CHAT PROMPT] message={}", userMessage.length() > 200 ? userMessage.substring(0, 200) + "..." : userMessage);

        if (sessionId != null && !sessionId.isBlank()) {
            SessionContextHolder.setSessionId(sessionId);
            log.info("[SESSION BIND] sessionId={} → ThreadLocal on thread {}", sessionId, Thread.currentThread().getId());
        } else {
            log.warn("[SESSION BIND] sessionId is null/blank — tools will get no session context !");
        }
        try {
            TaskComplexity complexity = complexityAnalyzer.analyze(userMessage);
            log.info("[CHAT] provider={} | complexity={}", providerStrategy.providerId(), complexity);

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
                    "conversationId", conversationId,
                    "sessionId", sessionId != null ? sessionId : ""
                ))
                .call()
                .content();

            if (response == null || response.isBlank()) {
                log.warn("[CHAT RESPONSE] Empty/null response for conversationId={}. Using fallback.", conversationId);
                response = EMPTY_RESPONSE_FALLBACK;
            }

            log.info("[CHAT END] conversationId={} | sessionId={} | responseLength={}",
                conversationId, sessionId, response.length());
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return Flux.just(response);

        } catch (Exception e) {
            log.error("[CHAT ERROR] conversationId={} | sessionId={} | error={}", conversationId, sessionId, e.getMessage(), e);
            return Flux.error(new ToolExecutionException(
                "Chat failed for conversation " + conversationId + ": " + e.getMessage(), e));
        } finally {
            log.info("[SESSION CLEAR] threadLocal cleared for thread {}", Thread.currentThread().getId());
            SessionContextHolder.clearAll();
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
        advisors.add(toolCallAdvisor);
        advisors.add(toolRetryLimiterAdvisor);
        advisors.add(toolErrorAdvisor);
        advisors.add(toolFailureRecoveryAdvisor);
        advisors.add(toolResultValidatorAdvisor);

        if (categories.contains(ToolCategory.TEMPLATE_SEARCH)) {
            advisors.add(retrievalAugmentationAdvisor);
        }

        advisors.add(new ReReadingAdvisor());
        advisors.add(new SimpleLoggerAdvisor());
        return advisors;
    }
}