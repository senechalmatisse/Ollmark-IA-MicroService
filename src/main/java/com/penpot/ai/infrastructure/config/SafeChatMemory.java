package com.penpot.ai.infrastructure.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SafeChatMemory implements ChatMemory {

    private final ChatMemory delegate;
    

    public SafeChatMemory(ChatMemory delegate) {
        this.delegate = delegate;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {

        List<Message> safeMessages = messages.stream()
            .filter(m -> {

                // 🔥 cas critique : assistant message sans texte
                if (m instanceof org.springframework.ai.chat.messages.AssistantMessage
                    && m.getText() == null) {

                    // 👉 on NE PERSISTE PAS ce message
                    return false;
                }

                return true;
            })
            .collect(Collectors.toList());

        if (!safeMessages.isEmpty()) {
            delegate.add(conversationId, safeMessages);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        return delegate.get(conversationId);
    }

    @Override
    public void clear(String conversationId) {
        delegate.clear(conversationId);
    }
}