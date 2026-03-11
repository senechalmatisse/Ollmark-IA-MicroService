package com.penpot.ai.application.service;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.penpot.ai.application.persistence.MessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    // Récupérer les N derniers messages d'une conversation ascendant : 

    public List<Message> getLastMessages(UUID conversationId, int nMessages) {
        List<Message> messages = messageRepository.findLastNMessages(conversationId, nMessages);
        messages.sort(Comparator.comparing(Message::getCreatedAt));
        return messages;
    }

    // Récupérer le dernier message d'une conversation à partir de son id
    
    public Message getLastMessage(UUID conversationId) {
        return messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversationId).orElseThrow(() -> new NoSuchElementException("Message not found"));
    }
}
