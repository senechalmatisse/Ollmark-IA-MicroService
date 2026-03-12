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

import java.util.List;
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
            c.getId().toString(),
            c.getConversationId().toString(),
            c.getUserId().toString(),
            c.getProject().getId().toString(),
            c.getMessages().stream().map(this::messageToDTO).toList()
        );
    }

    private MessageDTO messageToDTO(Message m) {
        return new MessageDTO(
            m.getId().toString(),
            m.getConversation().getId().toString(),
            m.getProject().getId().toString(),
            m.getContentUser(),
            m.getContentAssistant(),
            m.getCreatedAt()
        );
    }


    // Récupérer toutes conversations d'un projet donné
    public Page<ConversationDTO> getAllProjectConversations(String projectId){
        Page<Conversation> page = conversationRepository.findAllByProject_Id(UUID.fromString(projectId), convPageable);
    
        // Convertir chaque Conversation en ConversationDTO
        return page.map(this::toDTO);
    }

    // Récupérer toutes conversations d'un utilisateur donné dans un projet 
    public Page<ConversationDTO> getAllConversationsByUserIdAndProjectId(String userId, String projectId){
        Page<Conversation> page = conversationRepository.findAllByUserIdAndProject_Id(userId, UUID.fromString(projectId), convUserPageable);

        // Convertir chaque Conversation en ConversationDTO
        return page.map(this::toDTO);
    }

    // Récupérer les métadonnées d'une conversationsans sans les messages
    public ConversationMetaDataDTO getConversationMetaData(String conversationId){
        ConversationMetaDataDTO conversation =  conversationRepository.findMetaDataByConversationId(UUID.fromString(conversationId)).orElseThrow(()-> new RuntimeException("ConversationDTO Meta Data Not Found"));
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
