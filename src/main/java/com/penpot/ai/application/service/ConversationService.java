package com.penpot.ai.application.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.penpot.ai.application.DTO.ConversationMetaDataDTO;
import com.penpot.ai.application.persistence.ConversationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    private Pageable convUserPageable = PageRequest.of(0, 20);
    private Pageable convPageable = PageRequest.of(0, 20);
    
    // Récupérer toutes conversations d'un projet donné
    public Page<Conversation> getAllProjectConversations(UUID projectId){
        return conversationRepository.findAllByProjectId(projectId, convPageable);
    }

    // Récupérer toutes conversations d'un utilisateur donné dans un projet 
    public Page<Conversation> getAllConversationsByUserIdAndProjectId(UUID userId, UUID projectId){
        return conversationRepository.findAllByUserIdAndProjectId(userId, projectId, convUserPageable);
    }

    // Récupérer les métadonnées d'une conversationsans sans les messages
    public ConversationMetaDataDTO getConversationMetaData(UUID conversationId){
        return conversationRepository.findMetaDataByConversationId(conversationId).orElseThrow(()-> throwException("Conversation Meta Data Not Found"));
    }
}
