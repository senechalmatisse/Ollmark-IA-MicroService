package com.penpot.ai.application.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Advisor responsable de la gestion des <b>exceptions lors de l'exécution des tools</b>.
 *
 * <h2>Contexte</h2>
 * Dans l'architecture Spring AI utilisée dans Penpot AI, les tools (fonctions
 * exécutées par le LLM) peuvent produire des erreurs lors de leur exécution :
 *
 * <ul>
 *     <li>erreur réseau lors de l'appel au plugin</li>
 *     <li>erreur JavaScript côté Penpot</li>
 *     <li>paramètres incorrects fournis par le LLM</li>
 *     <li>timeout ou réponse invalide</li>
 * </ul>
 *
 * Sans interception, ces erreurs provoqueraient :
 *
 * <ul>
 *     <li>une exception Java interrompant la requête</li>
 *     <li>une erreur visible côté utilisateur</li>
 *     <li>la perte du contexte conversationnel</li>
 * </ul>
 *
 * Cet advisor implémente donc une stratégie de <b>récupération d'erreur contrôlée</b>.
 *
 *
 * <h2>Principe de fonctionnement</h2>
 *
 * L'advisor intercepte les exceptions levées pendant l'exécution d'un tool
 * et transforme l'erreur en <b>instruction système injectée dans le prompt</b>.
 *
 * Le modèle LLM peut alors :
 *
 * <ul>
 *     <li>réessayer l'appel au tool avec des paramètres corrigés</li>
 *     <li>choisir un autre tool</li>
 *     <li>expliquer le problème à l'utilisateur</li>
 * </ul>
 *
 *
 * <h2>Flux d'exécution</h2>
 *
 * <pre>
 * ChatClientRequest
 *        │
 *        ▼
 * ToolErrorAdvisor
 *        │
 *        ├─ cas normal
 *        │       │
 *        │       ▼
 *        │   chain.nextCall(request)
 *        │
 *        └─ cas erreur tool
 *                │
 *                ▼
 *       catch(Exception e)
 *                │
 *                ▼
 *       injection d'un SystemMessage :
 *
 *       TOOL EXECUTION ERROR
 *       A tool execution failed.
 *       Error message: ...
 *
 *                │
 *                ▼
 *       nouvel appel LLM avec prompt enrichi
 * </pre>
 *
 *
 * <h2>Exemple</h2>
 *
 * Tool appelé par le LLM :
 *
 * <pre>{@code
 * createRectangle(width=200,height=100)
 * }</pre>
 *
 * Si le tool échoue :
 *
 * <pre>{@code
 * RuntimeException: Invalid color value
 * }</pre>
 *
 * Le prompt devient :
 *
 * <pre>{@code
 * TOOL EXECUTION ERROR
 *
 * A tool execution failed.
 * Error message:
 * Invalid color value
 *
 * Decide how to continue:
 * - retry the tool
 * - use another tool
 * - or explain the problem.
 * }</pre>
 *
 * Le LLM peut alors corriger automatiquement sa stratégie.
 *
 *
 * <h2>Position dans la chaîne d'advisors</h2>
 *
 * L'ordre d'exécution est défini via :
 *
 * <pre>{@code
 * Ordered.HIGHEST_PRECEDENCE + 200
 * }</pre>
 *
 * Ce positionnement garantit que :
 *
 * <ul>
 *     <li>les outils sont déjà exécutés</li>
 *     <li>les erreurs peuvent être capturées</li>
 *     <li>les advisors de validation ou retry peuvent intervenir ensuite</li>
 * </ul>
 *
 *
 * <h2>Différence avec les autres advisors liés aux tools</h2>
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
 * <td>détecte les réponses JSON indiquant une erreur</td>
 * </tr>
 *
 * <tr>
 * <td>ToolResultValidatorAdvisor</td>
 * <td>valide la cohérence de la réponse du modèle</td>
 * </tr>
 *
 * <tr>
 * <td>ToolRetryLimiterAdvisor</td>
 * <td>limite le nombre de retries</td>
 * </tr>
 *
 * </table>
 *
 *
 * <h2>Avantages de cette approche</h2>
 *
 * <ul>
 * <li>évite l'interruption brutale du pipeline IA</li>
 * <li>permet au LLM de corriger lui-même ses erreurs</li>
 * <li>améliore la robustesse du système</li>
 * <li>maintient le contexte conversationnel intact</li>
 * </ul>
 *
 *
 * @see CallAdvisor
 * @see ToolFailureRecoveryAdvisor
 * @see ToolRetryLimiterAdvisor
 * @see ToolResultValidatorAdvisor
 */
@Slf4j
@Component
public class ToolErrorAdvisor implements CallAdvisor {

    /**
     * Intercepte l'appel au modèle et capture les exceptions liées
     * à l'exécution des tools.
     *
     * <p>Si aucune erreur n'est détectée, la requête est simplement
     * propagée à l'advisor suivant dans la chaîne.</p>
     *
     * <p>En cas d'exception :</p>
     *
     * <ul>
     * <li>l'erreur est loggée</li>
     * <li>un message système est injecté dans le prompt</li>
     * <li>le modèle est rappelé avec le contexte enrichi</li>
     * </ul>
     *
     * @param request requête actuelle envoyée au LLM
     * @param chain   chaîne d'exécution des advisors
     *
     * @return réponse du modèle après traitement
     */
    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain
    ) {

        log.warn("ToolErrorAdvisor triggered");

        try {
            return chain.nextCall(request);
        }

        catch (Exception e) {

            log.warn("[ToolErrorAdvisor] Tool execution failed: {}", e.getMessage());

            String toolErrorPrompt = """
                    TOOL EXECUTION ERROR
                    
                    A tool execution failed.
                    Error message:
                    %s
                    
                    Decide how to continue:
                    - retry the tool
                    - use another tool
                    - or explain the problem.
                    """.formatted(e.getMessage());

            Prompt augmented =
                    request.prompt()
                           .augmentSystemMessage(toolErrorPrompt);

            Map<String,Object> context = new HashMap<>(request.context());

            return chain.nextCall(
                    new ChatClientRequest(augmented, context)
            );
        }
    }

    /**
     * Définit la position de l'advisor dans la chaîne d'exécution.
     *
     * <p>Une valeur proche de {@code HIGHEST_PRECEDENCE} permet de
     * capturer rapidement les erreurs générées lors de l'appel aux tools.</p>
     *
     * @return ordre d'exécution de l'advisor
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 200;
    }

    /**
     * Nom de l'advisor utilisé dans les logs et les diagnostics.
     *
     * @return nom unique de l'advisor
     */
    @Override
    public String getName() {
        return "ToolErrorAdvisor";
    }
}