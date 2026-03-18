package com.penpot.ai.application.service;

import com.penpot.ai.application.persistance.Repositories.MessageRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService - Tests DELETE")
class MessageServiceDeleteTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    // =========================================================
    // deleteAllByConversationId
    // =========================================================

    @Test
    @DisplayName("deleteAllByConversationId - supprime tous les messages de la conversation")
    void deleteAllByConversationId_deletesMessages() {
        UUID conversationId = UUID.randomUUID();
        when(messageRepository.deleteAllByConversationId(conversationId)).thenReturn(3);

        messageService.deleteAllByConversationId(conversationId);

        verify(messageRepository, times(1)).deleteAllByConversationId(conversationId);
    }

    @Test
    @DisplayName("deleteAllByConversationId - ne lève pas d'exception si aucun message (0 supprimé)")
    void deleteAllByConversationId_zeroDeleted_noException() {
        UUID conversationId = UUID.randomUUID();
        when(messageRepository.deleteAllByConversationId(conversationId)).thenReturn(0);

        messageService.deleteAllByConversationId(conversationId);

        verify(messageRepository, times(1)).deleteAllByConversationId(conversationId);
    }

    @Test
    @DisplayName("deleteAllByConversationId - n'appelle le repo qu'une seule fois")
    void deleteAllByConversationId_callsRepoOnlyOnce() {
        UUID conversationId = UUID.randomUUID();
        when(messageRepository.deleteAllByConversationId(any())).thenReturn(1);

        messageService.deleteAllByConversationId(conversationId);

        verify(messageRepository, times(1)).deleteAllByConversationId(conversationId);
        verifyNoMoreInteractions(messageRepository);
    }

    // =========================================================
    // deleteById
    // =========================================================

    @Test
    @DisplayName("deleteById - supprime le message existant")
    void deleteById_deletesMessage() {
        UUID messageId = UUID.randomUUID();
        when(messageRepository.existsById(messageId)).thenReturn(true);
        doNothing().when(messageRepository).deleteById(messageId);

        messageService.deleteById(messageId);

        verify(messageRepository, times(1)).existsById(messageId);
        verify(messageRepository, times(1)).deleteById(messageId);
    }

    @Test
    @DisplayName("deleteById - lève NoSuchElementException si message inexistant")
    void deleteById_notFound_throwsNoSuchElementException() {
        UUID messageId = UUID.randomUUID();
        when(messageRepository.existsById(messageId)).thenReturn(false);

        assertThatThrownBy(() -> messageService.deleteById(messageId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(messageId.toString());

        verify(messageRepository, times(1)).existsById(messageId);
        verify(messageRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteById - ne supprime pas si message inexistant")
    void deleteById_notFound_neverCallsDelete() {
        UUID messageId = UUID.randomUUID();
        when(messageRepository.existsById(messageId)).thenReturn(false);

        try { messageService.deleteById(messageId); } catch (NoSuchElementException ignored) {}

        verify(messageRepository, never()).deleteById(any());
    }
}