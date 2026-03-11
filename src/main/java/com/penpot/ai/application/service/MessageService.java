package com.penpot.ai.application.service;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.penpot.ai.application.DTO.MessageDTO;
import com.penpot.ai.application.persistance.Repositories.MessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    // Récupérer les N derniers messages d'une conversation ascendant : 

    public List<MessageDTO> getLastMessages(UUID conversationId, int nMessages) {
        List<MessageDTO> messages = messageRepository.findLastNMessages(conversationId, nMessages);
        messages.sort(Comparator.comparing(MessageDTO::getCreatedAt));
        return messages;
    }

    // Récupérer le dernier message d'une conversation à partir de son id
    
    public MessageDTO getLastMessage(UUID conversationId) {
        return messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversationId).orElseThrow(() -> new NoSuchElementException("Message not found"));
    }
}
