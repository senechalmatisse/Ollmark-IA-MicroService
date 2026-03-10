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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Récupérer les N derniers messages d'une conversation ascendant : Pageable conAscvPageable = PageRequest.of(0, 20, Sort.by("createdAt").ascending());
    @Query(value = "SELECT * FROM message WHERE conversation_id = :conversationId ORDER BY created_at DESC LIMIT :nMessages", nativeQuery = true)
    List<Message> findLastNMessages(@Param("conversationId") UUID conversationId, @Param("nMessages") int nMessages);

    // Récupérer le dernier message d'une conversation
    Optional<Message> findFirstByConversationOrderByCreatedAtDesc(Conversation conversation);
}