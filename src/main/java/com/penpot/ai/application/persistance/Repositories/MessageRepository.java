package com.penpot.ai.application.persistance.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
        SELECT * FROM messages
        WHERE conversation_id = :conversationId
        ORDER BY created_at DESC
        LIMIT :nMessages
    """, nativeQuery = true)
    List<Message> findLastNMessages(@Param("conversationId") UUID conversationId, @Param("nMessages") int nMessages);

    // Récupérer le dernier message d'une conversation à partir de son id
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    // Supprime tous les messages d'une conversation via son conversationId métier
    // Retourne le nombre de messages supprimés
    @Modifying
    @Query("DELETE FROM Message m WHERE m.conversation.conversationId = :conversationId")
    int deleteAllByConversationId(@Param("conversationId") UUID conversationId);
}