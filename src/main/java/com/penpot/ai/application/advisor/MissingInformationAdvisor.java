package com.penpot.ai.application.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Advisor de garde-fou pour les requêtes incomplètes.
 *
 * <p>Ajoute une consigne système explicite : si les informations sont insuffisantes
 * ou ambiguës, l'assistant doit répondre de manière générique et ne jamais exposer
 * de code (ni snippets, ni pseudo-code, ni commandes).</p>
 */
@Slf4j
@Component
public class MissingInformationAdvisor implements CallAdvisor {

    private static final String MISSING_INFO_POLICY = """
        # RÈGLE DE GARDE-FOU : INFORMATIONS INSUFFISANTES
        Si la demande manque d'informations essentielles, est ambiguë,
        ou ne permet pas d'identifier précisément la cible/action :
        - Ne fournis AUCUN code (JavaScript, JSON, pseudo-code, commandes shell, etc.).
        - Ne propose pas de snippet partiel.
        - Réponds uniquement avec un message générique de clarification.

        Format attendu de réponse dans ce cas :
        "Je n'ai pas assez d'informations pour répondre précisément. Merci de préciser le contexte et l'objectif attendu."
        """;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        log.debug("[MissingInformationAdvisor] Injecting missing-information guardrail policy");

        Prompt augmented = request.prompt().augmentSystemMessage(MISSING_INFO_POLICY);
        return chain.nextCall(new ChatClientRequest(augmented, request.context()));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 40;
    }

    @Override
    public String getName() {
        return "MissingInformationAdvisor";
    }
}