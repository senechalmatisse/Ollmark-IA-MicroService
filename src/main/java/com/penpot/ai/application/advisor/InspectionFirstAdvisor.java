package com.penpot.ai.application.advisor;

import com.penpot.ai.application.tools.PenpotInspectorTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Advisor implémentant la stratégie "Inspection First".
 *
 * <h2>Objectif</h2>
 * Injecter automatiquement le contexte actuel de la page Penpot dans le prompt
 * lorsque la catégorie INSPECTION est détectée par le routeur d’intention.
 *
 * <h2>Fonctionnement</h2>
 * <ol>
 *   <li>Lit les catégories calculées par le routeur dans le contexte.</li>
 *   <li>Si une inspection est nécessaire, appelle {@link PenpotInspectorTools#getPageContext(String)}.</li>
 *   <li>Injecte la liste des UUID + propriétés dans le system prompt.</li>
 * </ol>
 *
 * @see com.penpot.ai.application.router.IntentRouterService
 * @see PenpotInspectorTools
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InspectionFirstAdvisor implements CallAdvisor {

    /** Clé de contexte contenant les catégories de tools détectées par le routeur. */
    public static final String CTX_TOOL_CATEGORIES = "toolCategories";

    /** Clé interne marquant qu'une injection de contexte a déjà été effectuée. */
    private static final String CTX_ALREADY_INJECTED = "inspectionInjected";

    private final PenpotInspectorTools inspectorTools;

    /**
     * Intercepte l'appel au modèle pour injecter le contexte de page si nécessaire.
     *
     * @param request requête chat en cours
     * @param chain   chaîne d'advisors
     * @return réponse du modèle (avec ou sans injection)
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        Map<String, Object> context = new HashMap<>(request.context());

        boolean alreadyInjected = Boolean.TRUE.equals(context.get(CTX_ALREADY_INJECTED));
        Set<String> categories = extractCategories(context.get(CTX_TOOL_CATEGORIES));

        boolean needsInspection =
                categories.contains("INSPECTION")
                        || categories.contains("COLOR_AND_STYLE")
                        || categories.contains("SHAPE_MODIFICATION")
                        || categories.contains("DELETION");

        if (needsInspection && !alreadyInjected) {
            log.debug("[InspectionFirstAdvisor] INSPECTION detected → injecting PAGE context");

            String inspectionJson = inspectorTools.getPageContext("compact");

            String injection = """
                # CONTEXTE ACTUEL DE LA PAGE
                Voici les éléments présents sur la page Penpot avec leurs propriétés et UUIDs.
                RÈGLES :
                - N'invente jamais d'UUID.
                - Utilise uniquement les UUIDs fournis ici.
                - Si ambigu, pose UNE question courte.
                - Cette liste est PLATE : elle ne contient PAS la hiérarchie réelle.
                OBLIGATOIRE :
                Pour toute question concernant children, parent, root, hiérarchie, index dans les layers, sélection ou état réel de la page,
                tu DOIS appeler le tool correspondant (getChildrenFromShape, getParentFromShape, getComponentRoot, getShapeParentIndex, getPropertiesFromShape).
                Ne déduis jamais la hiérarchie depuis cette liste.
                %s
                """.formatted(inspectionJson);

            Prompt augmented = request.prompt().augmentSystemMessage(injection);
            context.put(CTX_ALREADY_INJECTED, true);

            return chain.nextCall(new ChatClientRequest(augmented, context));
        }

        return chain.nextCall(request);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }

    @Override
    public String getName() {
        return "InspectionFirstAdvisor";
    }

    /**
     * Extrait un ensemble de catégories depuis la valeur brute stockée dans le contexte.
     *
     * <p>Supporte deux formats :</p>
     * <ul>
     *   <li>{@link Collection} (cas normal après routing)</li>
     *   <li>Valeur simple (fallback défensif)</li>
     * </ul>
     *
     * @param raw valeur brute issue du contexte
     * @return ensemble de catégories sous forme de chaînes
     */
    private Set<String> extractCategories(Object raw) {
        if (raw == null) {
            return Collections.emptySet();
        }
        if (raw instanceof Collection<?> col) {
            Set<String> out = new HashSet<>();
            for (Object o : col) out.add(String.valueOf(o));
            return out;
        }
        return Set.of(String.valueOf(raw));
    }
}