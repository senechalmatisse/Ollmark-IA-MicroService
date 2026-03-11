package com.penpot.ai.application.controller;

import org.springframework.web.bind.annotation.GetMapping;

import com.penpot.ai.application.service.MessageService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.penpot.ai.application.DTO.ConversationMetaDataDTO;
import com.penpot.ai.application.service.ConversationService;
import com.penpot.ai.application.service.ProjectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Récupérer les N derniers messages d'une conversation
    @GetMapping("/conversation/{conversationId}")
    public List<Message> getLastMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "20") int nMessages) {

        return messageService.getLastMessages(conversationId, nMessages);
    }

    // Récupérer le dernier message
    @GetMapping("/conversation/{conversationId}/last")
    public Message getLastMessage(@PathVariable UUID conversationId) {
        return messageService.getLastMessage(conversationId);
    }
}
