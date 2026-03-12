package com.penpot.ai.application.persistance.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.penpot.ai.application.DTO.ConversationMetaDataDTO;
import com.penpot.ai.application.persistance.Entity.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    // Récupérer toutes conversations d'une proejt donné
    Page<Conversation> findAllByProjectId(UUID projectId, Pageable convPageable);

    //Récupérer toutes conversations d'un utilisateur donné dans un projet 
    Page<Conversation> findAllByUserIdAndProjectId(UUID userId, UUID projectId, Pageable pageable); 

    // Récupérer les métadonnées d'une conversationsans sans les messages
    @Query("""
    SELECT new com.penpot.ai.application.DTO.ConversationMetaDataDTO(
            c.id,
            c.conversationId,
            c.project.id,
            c.userId,
            c.createdAt
        )
        FROM Conversation c
        WHERE c.id = :conversationId
    """)
    Optional<ConversationMetaDataDTO> findMetaDataByConversationId(UUID conversationId);

    // Trouver une conversation par son conversationId métier (UUID fonctionnel)
    Optional<Conversation> findByConversationId(UUID conversationId);

    // Trouver toutes les conversations d'un projet (sans pagination, pour suppression en bloc)
    List<Conversation> findAllByProjectId(UUID projectId);
}