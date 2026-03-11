package com.penpot.ai.application.persistance.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.penpot.ai.application.persistance.Entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    // Récupérer les N derniers messages d'une conversation ascendant : 
    @Query(value = """
        SELECT * FROM message
        WHERE conversation_id = :conversationId
        ORDER BY created_at DESC
        LIMIT :nMessages
        """, nativeQuery = true)
    List<Message> findLastNMessages(@Param("conversationId") UUID conversationId, @Param("nMessages") int nMessages);

    // Récupérer le dernier message d'une conversation à partir de son id
    
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}