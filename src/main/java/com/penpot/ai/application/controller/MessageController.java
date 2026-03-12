package com.penpot.ai.application.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
 

import com.penpot.ai.application.service.MessageService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;


import com.penpot.ai.application.DTO.MessageDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Récupérer les N derniers messages d'une conversation
    @GetMapping("/conversation/{conversationId}")
    public List<MessageDTO> getLastMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "20") int nMessages) {

        if (nMessages > 20) {
            throw new IllegalArgumentException("nMessages ne peut pas dépasser 20");
        }

        return messageService.getLastMessages(conversationId, nMessages);
    }

    // Récupérer le dernier message
    @GetMapping("/conversation/{conversationId}/last")
    public MessageDTO getLastMessage(@PathVariable String conversationId) {
        return messageService.getLastMessage(conversationId);
    }

    // Supprimer tous les messages d'une conversation (reset historique)
    @DeleteMapping("/conversation/{conversationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearConversationMessages(@PathVariable UUID conversationId) {
        messageService.deleteAllByConversationId(conversationId);
    }

    // Supprimer un message individuel par son id
    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable UUID messageId) {
        messageService.deleteById(messageId);
    }
}
