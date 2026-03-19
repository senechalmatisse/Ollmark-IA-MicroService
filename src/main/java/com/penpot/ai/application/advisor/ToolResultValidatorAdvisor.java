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
 * Advisor chargé de <b>valider la cohérence des résultats produits par le modèle</b>
 * après l'exécution d'un tool.
 *
 * <h2>Contexte</h2>
 *
 * Dans un système utilisant le <b>tool calling</b>, le modèle LLM peut parfois produire
 * des réponses incorrectes même si le tool s'est exécuté correctement.
 *
 * Les problèmes les plus fréquents sont :
 *
 * <ul>
 * <li>invention d'identifiants d'objets (UUID fictifs)</li>
 * <li>réponse vide ou trop courte</li>
 * <li>propagation d'une erreur JSON retournée par un tool</li>
 * </ul>
 *
 * Exemple d'hallucination classique :
 *
 * <pre>{@code
 * createRectangle("rect1")
 * }</pre>
 *
 * alors que l'UUID réel retourné par le tool est :
 *
 * <pre>{@code
 * 4e8b7a2e-8e65-4e4d-b8e2-92dcb0b0e1b1
 * }</pre>
 *
 * Ce type d'erreur peut provoquer :
 *
 * <ul>
 * <li>des appels de tools invalides</li>
 * <li>des modifications sur des éléments inexistants</li>
 * <li>un comportement incohérent dans l'éditeur Penpot</li>
 * </ul>
 *
 *
 * <h2>Objectif de cet advisor</h2>
 *
 * Cet advisor agit comme un <b>validateur de sortie</b> du modèle.
 * Il inspecte la réponse générée et détecte certains patterns d'erreurs.
 *
 * Lorsqu'une réponse invalide est détectée :
 *
 * <ul>
 * <li>un message système est injecté dans le prompt</li>
 * <li>le modèle est invité à corriger sa réponse</li>
 * <li>le pipeline relance un appel au modèle</li>
 * </ul>
 *
 *
 * <h2>Règles de validation appliquées</h2>
 *
 * Trois catégories d'erreurs sont actuellement détectées :
 *
 * <h3>1. UUID fictifs</h3>
 *
 * Détection de valeurs typiques générées par hallucination :
 *
 * <pre>{@code
 * rect1
 * shape1
 * myRectangle
 * }</pre>
 *
 *
 * <h3>2. Réponse vide ou trop courte</h3>
 *
 * Les réponses contenant moins de 5 caractères sont considérées comme invalides.
 *
 * Exemple :
 *
 * <pre>{@code
 * ""
 * }</pre>
 *
 *
 * <h3>3. Erreur retournée par un tool</h3>
 *
 * Détection de réponses contenant :
 *
 * <pre>{@code
 * "error": true
 * }</pre>
 *
 * indiquant un échec du tool.
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
 *        └─ réponse invalide
 *                │
 *                ▼
 *       injection SystemMessage :
 *
 *       INVALID TOOL RESULT
 *
 *       Rules:
 *       - Never invent shape IDs
 *       - Only use UUID returned by tools
 *       - Retry tool if needed
 *
 *                │
 *                ▼
 *       nouvel appel au modèle
 * </pre>
 *
 *
 * <h2>Exemple concret</h2>
 *
 * Réponse invalide du modèle :
 *
 * <pre>{@code
 * Move shape rect1 to x=200
 * }</pre>
 *
 * Le validator déclenche alors un nouveau prompt :
 *
 * <pre>{@code
 * INVALID TOOL RESULT
 *
 * The previous response generated an invalid result.
 *
 * Rules:
 * - Never invent shape IDs
 * - Only use UUID returned by tools
 * - Do not produce empty responses
 * }</pre>
 *
 * Le LLM peut alors corriger sa réponse en utilisant un UUID valide.
 *
 *
 * <h2>Position dans la chaîne d'advisors</h2>
 *
 * L'ordre d'exécution est défini par :
 *
 * <pre>{@code
 * Ordered.HIGHEST_PRECEDENCE + 170
 * }</pre>
 *
 * Ce positionnement garantit que :
 *
 * <ul>
 * <li>les tools ont déjà été exécutés</li>
 * <li>les erreurs fonctionnelles peuvent être détectées</li>
 * <li>les advisors de retry peuvent intervenir ensuite</li>
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
 * <td>capture les exceptions Java lors de l'exécution d'un tool</td>
 * </tr>
 *
 * <tr>
 * <td>ToolFailureRecoveryAdvisor</td>
 * <td>détecte les erreurs fonctionnelles retournées par un tool</td>
 * </tr>
 *
 * <tr>
 * <td>ToolResultValidatorAdvisor</td>
 * <td>valide la cohérence de la réponse générée par le modèle</td>
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
 * <li>réduction des hallucinations liées aux UUID</li>
 * <li>détection des réponses incohérentes</li>
 * <li>amélioration de la robustesse du pipeline tool-calling</li>
 * <li>auto-correction par le modèle</li>
 * </ul>
 *
 *
 * @see CallAdvisor
 * @see ToolErrorAdvisor
 * @see ToolFailureRecoveryAdvisor
 * @see ToolRetryLimiterAdvisor
 */
@Slf4j
@Component
public class ToolResultValidatorAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain
    ) {

        ChatClientResponse response = chain.nextCall(request);

        if (response == null || response.chatResponse() == null) {
            return response;
        }

        String text = response.chatResponse()
                .getResult()
                .getOutput()
                .getText();

        if (text == null || text.isBlank()) {
            return response;
        }

        boolean invalid = false;

        // 1️⃣ UUID fictifs
        if (text.contains("rect1")
                || text.contains("shape1")
                || text.contains("myRectangle")) {
            invalid = true;
        }

        // 2️⃣ réponse vide
        if (text.trim().length() < 5) {
            invalid = true;
        }

        // 3️⃣ erreur JSON
        if (text.contains("\"error\": true")) {
            invalid = true;
        }

        if (invalid) {

            log.warn("[ToolResultValidatorAdvisor] Invalid tool result detected");

            String validationPrompt = """
                    INVALID TOOL RESULT
                    
                    The previous response generated an invalid result.
                    
                    Rules:
                    - Never invent shape IDs.
                    - Only use UUID returned by tools.
                    - Do not produce empty responses.
                    - If a tool failed, retry with corrected parameters.
                    """;

            Prompt augmented =
                    request.prompt()
                           .augmentSystemMessage(validationPrompt);

            Map<String,Object> context = new HashMap<>(request.context());

            return chain.nextCall(
                    new ChatClientRequest(augmented, context)
            );
        }

        return response;
    }

    /**
     * Ordre d'exécution de l'advisor dans la chaîne.
     *
     * @return priorité relative dans la chaîne d'advisors
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 170;
    }

    /**
     * Nom de l'advisor utilisé dans les logs et diagnostics.
     *
     * @return nom unique de l'advisor
     */
    @Override
    public String getName() {
        return "ToolResultValidatorAdvisor";
    }
}