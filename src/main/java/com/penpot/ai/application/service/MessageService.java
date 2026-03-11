package com.penpot.ai.application.service;
import org.springframework.stereotype.Service;

import com.penpot.ai.application.DTO.MessageDTO;
import com.penpot.ai.application.persistance.Entity.Message;
import com.penpot.ai.application.persistance.Repositories.MessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    // Récupérer les N derniers messages d'une conversation, triés par createdAt asc
    public List<MessageDTO> getLastMessages(UUID conversationId, int nMessages) {
        List<Message> messages = messageRepository.findLastNMessages(conversationId, nMessages);

        // Tri par date
        messages.sort(Comparator.comparing(Message::getCreatedAt));

        // Conversion en DTO
        return messages.stream()
                .map(m -> new MessageDTO(
                        m.getId(),
                        m.getConversation().getId(),
                        m.getProject().getId(),
                        m.getContentUser(),
                        m.getContentAssistant(),
                        m.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // Récupérer le dernier message d'une conversation à partir de son id
    public MessageDTO getLastMessage(UUID conversationId) {
        Message m = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Message not found"));

        return new MessageDTO(
                m.getId(),
                m.getConversation().getId(),
                m.getProject().getId(),
                m.getContentUser(),
                m.getContentAssistant(),
                m.getCreatedAt()
        );
    }
}
