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
 * Composant qui détecte les paramètres manquants lors de l''appel d''une fonction.
 *
 * Rôle: Après qu''une fonction soit exécutée, ce composant vérifie
 * si ses paramètres requis sont présents. Si des paramètres manquent, il génère un message
 * utilisateur structuré listant ce qui fait défaut.
 *
 * Exemple : Une fonction changeColor(uuid, colorHex) est appelée
 * avec uuid="ABC123" mais sans colorHex. Le composant génère :
 * "Manque : La teinte HEX de la couleur. Exemple : #FF5733"
 * 
 * Sécurité : Le message ne révèle jamais le nom de la fonction,
 * seulement les paramètres requis qui manquent.
 */
@Slf4j
@Component
public class MissingInformationAdvisor implements CallAdvisor {

    private static final String MISSING_INFO_POLICY = """
        # INFORMATIONS MANQUANTES
        Si des paramètres requis n''ont pas été fournis :
        - Énumère chaque paramètre manquant
        - Indique pourquoi il est nécessaire
        - Donne un exemple de valeur valide
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
