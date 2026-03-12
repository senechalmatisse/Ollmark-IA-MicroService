package com.penpot.ai.application.controller;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.penpot.ai.application.DTO.ConversationDTO;
import com.penpot.ai.application.DTO.ConversationMetaDataDTO;
import com.penpot.ai.application.service.ConversationService;
import org.springframework.http.HttpStatus;
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
    public Page<ConversationDTO> getProjectConversations(@PathVariable String projectId) {
        return conversationService.getAllProjectConversations(projectId);
    }

    // Récupérer toutes les conversations d'un utilisateur dans un projet
    @GetMapping("/project/{projectId}/user/{userId}")
    public Page<ConversationDTO> getUserProjectConversations(
            @PathVariable String projectId,
            @PathVariable String userId) {

        return conversationService.getAllConversationsByUserIdAndProjectId(userId, projectId);
    }

    // Récupérer les métadonnées d'une conversation
    @GetMapping("/{conversationId}/metadata")
    public ConversationMetaDataDTO getConversationMetaData(@PathVariable String conversationId) {
        return conversationService.getConversationMetaData(conversationId);
    }

    // Supprimer une conversation et tous ses messages (cascade)
    @DeleteMapping("/{conversationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConversation(@PathVariable UUID conversationId) {
        conversationService.deleteConversation(conversationId);
    }
    
    // Supprimer toutes les conversations d'un projet (et leurs messages en cascade)
    @DeleteMapping("/project/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllProjectConversations(@PathVariable UUID projectId) {
        conversationService.deleteAllByProjectId(projectId);
    }
}
