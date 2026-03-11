package com.penpot.ai.application.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.penpot.ai.application.DTO.ConversationMetaDataDTO;
import com.penpot.ai.application.service.ConversationService;
import com.penpot.ai.application.service.ProjectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;

@Slf4j
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    // Récupérer toutes les conversations d'un projet
    @GetMapping("/project/{projectId}")
    public Page<Conversation> getProjectConversations(@PathVariable UUID projectId) {
        return conversationService.getAllProjectConversations(projectId);
    }

    // Récupérer toutes les conversations d'un utilisateur dans un projet
    @GetMapping("/project/{projectId}/user/{userId}")
    public Page<Conversation> getUserProjectConversations(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {

        return conversationService.getAllConversationsByUserIdAndProjectId(userId, projectId);
    }

    // Récupérer les métadonnées d'une conversation
    @GetMapping("/{conversationId}/metadata")
    public ConversationMetaDataDTO getConversationMetaData(@PathVariable UUID conversationId) {
        return conversationService.getConversationMetaData(conversationId);
    }
}
