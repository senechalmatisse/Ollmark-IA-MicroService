package com.penpot.ai.application.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.penpot.ai.application.persistance.Entity.Conversation;
import com.penpot.ai.application.persistance.Entity.Project;
import com.penpot.ai.application.persistance.Repositories.ConversationRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationService - Tests DELETE")
class ConversationServiceDeleteTest {

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private ConversationService conversationService;

    private Pageable convPageable = PageRequest.of(0, 20);

    // =========================================================
    // deleteConversation
    // =========================================================

    @Test
    @DisplayName("deleteConversation - supprime la conversation existante")
    void deleteConversation_deletesConversation() {
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = buildConversation(conversationId);

        // Mock la méthode correcte appelée par le service
        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));

        conversationService.deleteConversation(conversationId);

        verify(conversationRepository, times(1)).findById(conversationId);
        verify(conversationRepository, times(1)).delete(conversation);
    }

    @Test
    @DisplayName("deleteConversation - lève NoSuchElementException si conversation inexistante")
    void deleteConversation_notFound_throwsException() {
        UUID conversationId = UUID.randomUUID();

        assertThatThrownBy(() -> conversationService.deleteConversation(conversationId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(conversationId.toString());

        verify(conversationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteConversation - ne supprime pas si conversation inexistante")
    void deleteConversation_notFound_neverCallsDelete() {
        UUID conversationId = UUID.randomUUID();

        try { conversationService.deleteConversation(conversationId); } catch (NoSuchElementException ignored) {}

        verify(conversationRepository, never()).delete(any(Conversation.class));
    }

    @Test
    @DisplayName("deleteConversation - supprime exactement la bonne entité")
    void deleteConversation_deletesCorrectEntity() {
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = buildConversation(conversationId);

        // Mock la méthode correcte
        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));

        conversationService.deleteConversation(conversationId);

        // Vérifie que la bonne entité a été supprimée
        verify(conversationRepository).delete(conversation);
    }

    // =========================================================
    // deleteAllByProjectId
    // =========================================================

    @Test
    @DisplayName("deleteAllByProjectId - supprime toutes les conversations du projet")
    void deleteAllByProjectId_deletesAll() {
        UUID projectId = UUID.randomUUID();
        List<Conversation> conversations = List.of(
                buildConversation(UUID.randomUUID()),
                buildConversation(UUID.randomUUID()),
                buildConversation(UUID.randomUUID())
        );

        // Crée un Page à partir de la liste
        Page<Conversation> page = new PageImpl<>(conversations);

        // Stub la méthode avec un matcher pour le Pageable
        when(conversationRepository.findAllByProjectId(eq(projectId), any(Pageable.class)))
                .thenReturn(page);

        conversationService.deleteAllByProjectId(projectId);

        // Vérifie l'appel avec n'importe quel Pageable
        verify(conversationRepository, times(1))
                .findAllByProjectId(eq(projectId), any(Pageable.class));

        // Vérifie la suppression
        verify(conversationRepository, times(1)).deleteAll(page);
    }

    @Test
    @DisplayName("deleteAllByProjectId - n'appelle deleteAll qdeleteConversation_deletesCorrectEntityu'une seule fois")
    void deleteAllByProjectId_callsDeleteAllOnlyOnce() {
        UUID projectId = UUID.randomUUID();
        when(conversationRepository.findAllByProjectId(projectId,convPageable)).thenReturn(Page.empty());

        conversationService.deleteAllByProjectId(projectId);

        verify(conversationRepository, times(1)).deleteAll(any());
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Conversation buildConversation(UUID conversationId) {
        Project project = new Project("Test Project");
        Conversation c = new Conversation(project, conversationId, UUID.randomUUID());
        return c;
    }
}