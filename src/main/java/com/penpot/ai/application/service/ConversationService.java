package com.penpot.ai.application.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.penpot.ai.application.DTO.ConversationDTO;
import com.penpot.ai.application.DTO.ConversationMetaDataDTO;
import com.penpot.ai.application.DTO.MessageDTO;
import com.penpot.ai.application.persistance.Entity.Conversation;
import com.penpot.ai.application.persistance.Entity.Message;
import com.penpot.ai.application.persistance.Repositories.ConversationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    
        // Convertir chaque Conversation en ConversationDTO
        return page.map(this::toDTO);
    }

    // Récupérer toutes conversations d'un utilisateur donné dans un projet 
    public Page<ConversationDTO> getAllConversationsByUserIdAndProjectId(UUID userId, UUID projectId){
        Page<Conversation> page = conversationRepository.findAllByUserIdAndProjectId(userId, projectId, convUserPageable);

        // Convertir chaque Conversation en ConversationDTO
        return page.map(this::toDTO);
    }

    // Récupérer les métadonnées d'une conversationsans sans les messages
    public ConversationMetaDataDTO getConversationMetaData(UUID conversationId){
        ConversationMetaDataDTO conversation =  conversationRepository.findMetaDataByConversationId(conversationId).orElseThrow(()-> new RuntimeException("ConversationDTO Meta Data Not Found"));
        return conversation;
    }
}
