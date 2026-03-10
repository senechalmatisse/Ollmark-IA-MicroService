package com.penpot.ai.application.persistence;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
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
    List<Message> findLastNMessages(UUID conversationId, int nMessages);

    // Récupérer le dernier message d'une conversation
    Optional<Message> findFirstByConversationOrderByCreatedAtDesc(Conversation conversation);
}