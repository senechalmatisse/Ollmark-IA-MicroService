package com.penpot.ai.application.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Advisor chargé de détecter et gérer les <b>échecs fonctionnels retournés par les tools</b>.
 *
 * <h2>Contexte</h2>
 * Dans l'architecture Penpot AI, certains tools peuvent réussir leur exécution
 * au niveau technique (pas d'exception Java) mais retourner un résultat indiquant
 * un échec logique.
 *
 * Exemple typique :
 *
 * <pre>{@code
 * {
 *   "error": true,
 *   "message": "Shape not found"
 * }
 * }</pre>
 *
 * Dans ce cas :
 *
 * <ul>
 * <li>le tool a bien répondu</li>
 * <li>mais l'opération demandée n'a pas pu être réalisée</li>
 * </ul>
 *
 * Sans traitement spécifique, le LLM pourrait :
 *
 * <ul>
 * <li>continuer en ignorant l'erreur</li>
 * <li>répéter exactement la même action</li>
 * <li>produire un résultat incohérent</li>
 * </ul>
 *
 *
 * <h2>Objectif de cet advisor</h2>
 *
 * Cet advisor inspecte la réponse générée après l'appel d'un tool
 * et détecte les cas où le résultat contient un indicateur d'erreur.
 *
 * Lorsqu'une erreur est détectée :
 *
 * <ul>
 * <li>un message système est injecté dans le prompt</li>
 * <li>le modèle est invité à analyser l'erreur</li>
 * <li>le modèle peut corriger ses paramètres et réessayer</li>
 * </ul>
 *
 *
 * <h2>Flux d'exécution</h2>
 *
 * <pre>
 * ChatClientRequest
 *        │
 *        ▼
 * chain.nextCall(request)
 *        │
 *        ▼
 * ChatClientResponse
 *        │
 *        ├─ réponse valide
 *        │       │
 *        │       ▼
 *        │   retour normal
 *        │
 *        └─ réponse contenant :
 *               "error": true
 *                │
 *                ▼
 *       injection SystemMessage :
 *
 *       TOOL EXECUTION FAILED
 *
 *       The previous tool returned an error.
 *       Analyze the error and retry with corrected parameters.
 *
 *                │
 *                ▼
 *       nouvel appel LLM
 * </pre>
 *
 *
 * <h2>Exemple concret</h2>
 *
 * Tool appelé :
 *
 * <pre>{@code
 * getShapeById("123")
 * }</pre>
 *
 * Réponse du tool :
 *
 * <pre>{@code
 * {
 *   "error": true,
 *   "message": "Shape not found"
 * }
 * }</pre>
 *
 * Le prompt injecté devient :
 *
 * <pre>{@code
 * TOOL EXECUTION FAILED
 *
 * The previous tool returned an error.
 * Analyze the error and retry with corrected parameters.
 * Do not repeat the same failing call.
 * }</pre>
 *
 * Le LLM peut alors :
 *
 * <ul>
 * <li>corriger l'identifiant</li>
 * <li>utiliser un autre tool</li>
 * <li>expliquer l'erreur à l'utilisateur</li>
 * </ul>
 *
 *
 * <h2>Position dans la chaîne d'advisors</h2>
 *
 * L'ordre est défini par :
 *
 * <pre>{@code
 * Ordered.HIGHEST_PRECEDENCE + 150
 * }</pre>
 *
 * Ce positionnement garantit que :
 *
 * <ul>
 * <li>les tools ont déjà été exécutés</li>
 * <li>les erreurs logiques peuvent être détectées</li>
 * <li>les advisors de retry ou validation peuvent intervenir ensuite</li>
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
 * <td>capture les exceptions Java lors de l'exécution d'un tool</td>
 * </tr>
 *
 * <tr>
 * <td>ToolFailureRecoveryAdvisor</td>
 * <td>détecte les erreurs fonctionnelles retournées par les tools</td>
 * </tr>
 *
 * <tr>
 * <td>ToolResultValidatorAdvisor</td>
 * <td>vérifie la validité de la réponse produite par le LLM</td>
 * </tr>
 *
 * <tr>
 * <td>ToolRetryLimiterAdvisor</td>
 * <td>limite les boucles infinies de retry</td>
 * </tr>
 *
 * </table>
 *
 *
 * <h2>Avantages</h2>
 *
 * <ul>
 * <li>détecte les erreurs fonctionnelles invisibles pour Java</li>
 * <li>permet au LLM de corriger ses appels</li>
 * <li>améliore la robustesse du pipeline tool-calling</li>
 * <li>évite les répétitions d'appels invalides</li>
 * </ul>
 *
 *
 * @see CallAdvisor
 * @see ToolErrorAdvisor
 * @see ToolRetryLimiterAdvisor
 * @see ToolResultValidatorAdvisor
 */
@Slf4j
@Component
public class ToolFailureRecoveryAdvisor implements CallAdvisor {

    private static final String RECOVERY_PROMPT = """
        TOOL EXECUTION FAILED
        The previous tool returned an error.
        Analyze the error and retry with corrected parameters.
        Do not repeat the same failing call.
        """;

    @Override
    public ChatClientResponse adviseCall(
        ChatClientRequest request,
        CallAdvisorChain chain
    ) {
        ChatClientResponse response = chain.nextCall(request);
        if (response == null || response.chatResponse() == null) return response;

        String text = response.chatResponse()
            .getResult()
            .getOutput()
            .getText();

        if (text != null && text.contains("\"error\": true")) {
            log.warn("[ToolFailureRecoveryAdvisor] Tool error detected — flagging for retry limiter");
            Map<String, Object> context = new HashMap<>(request.context());
            context.put("toolErrorDetected", true);
            Prompt augmented = request.prompt().augmentSystemMessage(RECOVERY_PROMPT);
            return chain.nextCall(new ChatClientRequest(augmented, context));
        }

        return response;
    }

    /**
     * Position de l'advisor dans la chaîne d'exécution.
     *
     * @return ordre d'exécution relatif
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 150;
    }

    /**
     * Nom de l'advisor utilisé dans les logs et diagnostics.
     *
     * @return nom unique de l'advisor
     */
    @Override
    public String getName() {
        return "ToolFailureRecoveryAdvisor";
    }
}