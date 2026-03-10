package com.penpot.ai.application.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.penpot.ai.application.DTO.ConversationMetaDataDTO;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Récupérer toutes conversations d'une proejt donné
    //TODO ajouter cela dans l'appel a ce repository: Pageable conAscvPageable = PageRequest.of(0, 20);
    Page<Conversation> findAllByProjectId(UUID projectId, Pageable convPageable);

    //Récupérer toutes conversations d'une utilisateur donné dans un projet 
    // Ajouter cela dans l'appel au repositiry: Pageable convUserPageable = PageRequest.of(0, 20);
    Page<Conversation> findAllByUserIdAndProjectId(UUID userId, UUID projectId, Pageable pageable); 

    // Récupérer une conversation par son id
    Optional<Conversation> findById(UUID conversationId);

    // Récupérer les métadonnées d'une conversationsans sans les messages
    // TODO remplir les méta données du dto a récupérer 
    @Query("SELECT ConversationMetaDataDTO " + "FROM Conversation c WHERE c.id = :conversationId")
    Optional<ConversationMetaDataDTO> findByIdWithoutMessages(UUID conversationId);
}