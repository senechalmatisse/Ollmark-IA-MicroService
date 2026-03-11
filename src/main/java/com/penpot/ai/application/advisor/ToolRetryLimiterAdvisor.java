package com.penpot.ai.application.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Advisor responsable de limiter le nombre de tentatives de réexécution
 * d’un tool par le modèle LLM.
 *
 * <h2>Contexte</h2>
 *
 * Dans un système utilisant le <b>tool calling</b>, le modèle peut tenter
 * de corriger une erreur en réappelant le même tool plusieurs fois.
 *
 * Exemple :
 *
 * <pre>{@code
 * LLM → call tool
 * Tool → error
 *
 * LLM → retry tool
 * Tool → error
 *
 * LLM → retry tool
 * Tool → error
 * }</pre>
 *
 * Sans limitation, cela peut provoquer :
 *
 * <ul>
 * <li>des boucles infinies de retry</li>
 * <li>une consommation excessive de tokens</li>
 * <li>une dégradation des performances</li>
 * <li>une mauvaise expérience utilisateur</li>
 * </ul>
 *
 *
 * <h2>Objectif de cet advisor</h2>
 *
 * Cet advisor implémente une stratégie simple de <b>limitation du nombre
 * de retries</b>.
 *
 * Il maintient un compteur dans le <b>contexte de la requête</b>
 * afin de suivre le nombre de tentatives déjà effectuées.
 *
 *
 * <h2>Fonctionnement</h2>
 *
 * Le compteur est stocké dans le contexte sous la clé :
 *
 * <pre>{@code
 * toolRetryCount
 * }</pre>
 *
 * À chaque passage dans l'advisor :
 *
 * <ol>
 * <li>le compteur est récupéré depuis le contexte</li>
 * <li>si la limite n'est pas atteinte, il est incrémenté</li>
 * <li>si la limite est atteinte, un message système est injecté
 * pour empêcher un nouveau retry</li>
 * </ol>
 *
 *
 * <h2>Limite actuelle</h2>
 *
 * <pre>{@code
 * MAX_RETRIES = 2
 * }</pre>
 *
 * Ce qui donne le comportement suivant :
 *
 * <pre>
 * tentative 1 → autorisée
 * tentative 2 → autorisée
 * tentative 3 → bloquée
 * </pre>
 *
 *
 * <h2>Flux d'exécution</h2>
 *
 * <pre>
 * ChatClientRequest
 *        │
 *        ▼
 * ToolRetryLimiterAdvisor
 *        │
 *        ├─ retryCount < MAX_RETRIES
 *        │       │
 *        │       ▼
 *        │   incrément compteur
 *        │
 *        │
 *        └─ retryCount ≥ MAX_RETRIES
 *                │
 *                ▼
 *       injection SystemMessage :
 *
 *       TOOL RETRY LIMIT REACHED
 *
 *       Do NOT retry the same tool again.
 *       Try another tool or explain the issue.
 * </pre>
 *
 *
 * <h2>Exemple concret</h2>
 *
 * Si un tool échoue plusieurs fois :
 *
 * <pre>{@code
 * Tool execution failed
 * Tool execution failed
 * Tool execution failed
 * }</pre>
 *
 * Le prompt injecté devient :
 *
 * <pre>{@code
 * TOOL RETRY LIMIT REACHED
 *
 * The previous tool failed multiple times.
 * Do NOT retry the same tool again.
 *
 * Instead:
 * - try another tool
 * - or explain the issue to the user.
 * }</pre>
 *
 * Le LLM doit alors :
 *
 * <ul>
 * <li>choisir un autre tool</li>
 * <li>modifier sa stratégie</li>
 * <li>expliquer l’erreur à l’utilisateur</li>
 * </ul>
 *
 *
 * <h2>Position dans la chaîne d'advisors</h2>
 *
 * L'ordre d'exécution est défini par :
 *
 * <pre>{@code
 * Ordered.HIGHEST_PRECEDENCE + 160
 * }</pre>
 *
 * Ce positionnement garantit que :
 *
 * <ul>
 * <li>les erreurs peuvent être détectées par
 * {@link ToolFailureRecoveryAdvisor}</li>
 * <li>les réponses peuvent être validées par
 * {@link ToolResultValidatorAdvisor}</li>
 * <li>les retries restent contrôlés</li>
 * </ul>
 *
 *
 * <h2>Relation avec les autres advisors du pipeline</h2>
 *
 * <table border="1">
 * <tr>
 * <th>Advisor</th>
 * <th>Responsabilité</th>
 * </tr>
 *
 * <tr>
 * <td>ToolErrorAdvisor</td>
 * <td>capture les exceptions Java</td>
 * </tr>
 *
 * <tr>
 * <td>ToolFailureRecoveryAdvisor</td>
 * <td>détecte les erreurs retournées par un tool</td>
 * </tr>
 *
 * <tr>
 * <td>ToolResultValidatorAdvisor</td>
 * <td>valide la cohérence de la réponse du modèle</td>
 * </tr>
 *
 * <tr>
 * <td>ToolRetryLimiterAdvisor</td>
 * <td>limite les boucles de retry</td>
 * </tr>
 *
 * </table>
 *
 *
 * <h2>Avantages</h2>
 *
 * <ul>
 * <li>empêche les boucles infinies du LLM</li>
 * <li>réduit la consommation de tokens</li>
 * <li>améliore la stabilité du système</li>
 * <li>force le modèle à changer de stratégie</li>
 * </ul>
 *
 *
 * @see CallAdvisor
 * @see ToolErrorAdvisor
 * @see ToolFailureRecoveryAdvisor
 * @see ToolResultValidatorAdvisor
 */
@Slf4j
@Component
public class ToolRetryLimiterAdvisor implements CallAdvisor {

    private static final String RETRY_COUNTER = "toolRetryCount";
    private static final int MAX_RETRIES = 2;

    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain
    ) {

        Map<String, Object> context = new HashMap<>(request.context());

        int retryCount = (int) context.getOrDefault(RETRY_COUNTER, 0);

        log.info("[ToolRetryLimiterAdvisor] triggered - retryCount={}", retryCount);

        // Si la limite est atteinte
        if (retryCount >= MAX_RETRIES) {

            log.warn("[ToolRetryLimiterAdvisor] Retry limit reached");

            String stopPrompt = """
                    TOOL RETRY LIMIT REACHED
                    
                    The previous tool failed multiple times.
                    Do NOT retry the same tool again.
                    
                    Instead:
                    - try another tool
                    - or explain the issue to the user.
                    """;

            Prompt augmentedPrompt =
                    request.prompt().augmentSystemMessage(stopPrompt);

            log.info("[ToolRetryLimiterAdvisor] Injecting retry stop prompt");

            return chain.nextCall(
                    new ChatClientRequest(augmentedPrompt, context)
            );
        }

        int nextRetry = retryCount + 1;

        context.put(RETRY_COUNTER, nextRetry);

        log.info("[ToolRetryLimiterAdvisor] Allowing retry attempt {}", nextRetry);

        return chain.nextCall(
                new ChatClientRequest(request.prompt(), context)
        );
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 160;
    }

    @Override
    public String getName() {
        return "ToolRetryLimiterAdvisor";
    }
}