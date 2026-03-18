package com.penpot.ai.application.service;

import com.penpot.ai.application.persistance.Entity.*;
import com.penpot.ai.application.persistance.Repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * MessagePersistenceService — SRP : persister les échanges utilisateur/IA
 * dans les tables projects, conversations et messages.
 *
 * Appelé de façon asynchrone depuis ConversationChatUseCaseImpl pour ne pas
 * bloquer la réponse HTTP. Une failure de persistence ne doit jamais
 * impacter l'expérience utilisateur.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePersistenceService {

    private final ProjectRepository projectRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    /**
     * Persiste un échange complet (message utilisateur + réponse IA).
     *
     * @param projectId    UUID du projet Penpot (page ID)
     * @param sessionId    ID de session WebSocket — clé fonctionnelle de la conversation.
     *                     Si null, on utilise le projectId seul.
     * @param userMessage  Message envoyé par l'utilisateur
     * @param aiResponse   Réponse générée par le LLM
     */
    @Async
    @Transactional
    public void persist(
        String projectId,
        String sessionId,
        String userMessage,
        String aiResponse
    ) {
        try {
            log.info("[Persistence] persist() called — projectId={} sessionId={}", projectId, sessionId);

            UUID projectUuid = parseUuid(projectId);
            if (projectUuid == null) {
                log.warn("[Persistence] Invalid projectId, skipping: {}", projectId);
                return;
            }

            // Clé fonctionnelle de la conversation : sessionId si dispo, sinon projectId
            String conversationKey = (sessionId != null && !sessionId.isBlank())
                ? sessionId : projectId;
            UUID conversationUuid = parseUuid(conversationKey);
            if (conversationUuid == null) {
                log.warn("[Persistence] Invalid conversationKey, skipping: {}", conversationKey);
                return;
            }

            Project project = findOrCreateProject(projectUuid);
            Conversation conversation = findOrCreateConversation(
                conversationUuid, project
            );
            

            String safeUserMessage = sanitize(userMessage);
            String safeAiResponse = sanitize(aiResponse);

            Message message = new Message(
                conversation,
                project,
                safeUserMessage,
                safeAiResponse
            );
            messageRepository.save(message);

            log.debug("[Persistence] Saved message for conversation {} (project {})",
                conversationUuid, projectUuid);

        } catch (Exception e) {
            // Persistence best-effort : on logge sans propager
            log.error("[Persistence] Failed to persist message for project {}: {}",
                projectId, e.getMessage(), e);
        }
    }

    private Project findOrCreateProject(UUID projectId) {
        return projectRepository.findById(projectId).orElseGet(() -> {
            log.debug("[Persistence] Creating project: {}", projectId);
            Project p = new Project(projectId, projectId.toString());
            p.markAsNew();
            return projectRepository.save(p);
        });
    }
    private String sanitize(String value) {
            if (value == null || value.isBlank()) {
                return "[EMPTY]";
            }
            return value;
        }

    private Conversation findOrCreateConversation(UUID conversationId, Project project) {
        return conversationRepository.findByConversationId(conversationId).orElseGet(() -> {
            log.debug("[Persistence] Creating conversation: {}", conversationId);
            Conversation c = new Conversation(project, conversationId, null);
            return conversationRepository.save(c);
        });
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}