package com.penpot.ai.application.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.penpot.ai.application.DTO.ConversationDTO;
import com.penpot.ai.application.DTO.ConversationMetaDataDTO;
import com.penpot.ai.application.DTO.MessageDTO;
import com.penpot.ai.application.persistance.Entity.Conversation;
import com.penpot.ai.application.persistance.Entity.Message;
import com.penpot.ai.application.persistance.Repositories.ConversationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    private Pageable convUserPageable = PageRequest.of(0, 20);
    private Pageable convPageable = PageRequest.of(0, 20);
    

    private ConversationDTO toDTO(Conversation c) {
        return new ConversationDTO(
            c.getId(),
            c.getConversationId(),
            c.getUserId(),
            c.getProject().getId(),
            c.getMessages().stream().map(this::messageToDTO).toList()
        );
    }

    private MessageDTO messageToDTO(Message m) {
        return new MessageDTO(
            m.getId(),
            m.getConversation().getId(),
            m.getProject().getId(),
            m.getContentUser(),
            m.getContentAssistant(),
            m.getCreatedAt()
        );
    }

    // Récupérer toutes conversations d'un projet donné
    public Page<ConversationDTO> getAllProjectConversations(UUID projectId){
        Page<Conversation> page = conversationRepository.findAllByProjectId(projectId, convPageable);
        return page.map(this::toDTO);
    }

    // Récupérer toutes conversations d'un utilisateur donné dans un projet 
    public Page<ConversationDTO> getAllConversationsByUserIdAndProjectId(UUID userId, UUID projectId){
        Page<Conversation> page = conversationRepository.findAllByUserIdAndProjectId(userId, projectId, convUserPageable);
        return page.map(this::toDTO);
    }

    // Récupérer les métadonnées d'une conversationsans sans les messages
    public ConversationMetaDataDTO getConversationMetaData(UUID conversationId){
        ConversationMetaDataDTO conversation =  conversationRepository.findMetaDataByConversationId(conversationId).orElseThrow(()-> new RuntimeException("ConversationDTO Meta Data Not Found"));
        return conversation;
    }

    // Supprimer une conversation et tous ses messages (CascadeType.ALL sur messages)
    @Transactional
    public void deleteConversation(UUID conversationId) {
        log.debug("Suppression de la conversation {}", conversationId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));
        conversationRepository.delete(conversation);
        log.debug("Conversation {} supprimée avec ses messages en cascade", conversationId);
    }

    // Supprimer toutes les conversations d'un projet (et leurs messages en cascade)
    @Transactional
    public void deleteAllByProjectId(UUID projectId) {
        log.debug("Suppression de toutes les conversations du projet {}", projectId);
        Page<Conversation> conversations = conversationRepository.findAllByProjectId(projectId, convPageable);
        conversationRepository.deleteAll(conversations);
        log.debug("{} conversation(s) supprimée(s) pour le projet {}", projectId);
    }
}