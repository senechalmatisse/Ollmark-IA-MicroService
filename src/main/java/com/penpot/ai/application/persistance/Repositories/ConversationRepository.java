package com.penpot.ai.application.persistance.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.penpot.ai.application.DTO.ConversationMetaDataDTO;
import com.penpot.ai.application.persistance.Entity.Conversation;

import java.util.Date;
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
    // Ajouter cela dans l'appel au repositiry: 
    Page<Conversation> findAllByUserIdAndProjectId(UUID userId, UUID projectId, Pageable pageable); 

    // Récupérer les métadonnées d'une conversationsans sans les messages
    // TODO remplir les méta données du dto a récupérer 
    @Query("""
        SELECT new com.penpot.ai.application.DTO.ConversationMetaDataDTO(
            c.id,
            c.name,
            c.project.id,
            c.createdAt
        )
        FROM Conversation c
        WHERE c.id = :conversationId
    """)
    Optional<ConversationMetaDataDTO> findMetaDataByConversationId(UUID conversationId);
}