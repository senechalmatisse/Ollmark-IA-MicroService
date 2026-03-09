package com.penpot.ai.infrastructure.config;

import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ChatMemoryConfig — Integration")
public class ChatMemoryConfigTest {

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private MessageChatMemoryAdvisor messageChatMemoryAdvisor;

    @Nested
    @DisplayName("chatMemory bean")
    class ChatMemoryBeanTests {

        @Test
        @DisplayName("chatMemory — bean is loaded in Spring context and is not null")
        void chatMemory_beanIsLoadedInSpringContextAndIsNotNull() {
            // GIVEN / WHEN / THEN
            assertThat(chatMemory).isNotNull();
        }

        @Test
        @DisplayName("chatMemory — is a MessageWindowChatMemory instance")
        void chatMemory_isAMessageWindowChatMemoryInstance() {
            // GIVEN / WHEN / THEN
            assertThat(chatMemory).isInstanceOf(MessageWindowChatMemory.class);
        }
    }

    @Nested
    @DisplayName("messageChatMemoryAdvisor bean")
    class MessageChatMemoryAdvisorBeanTests {

        @Test
        @DisplayName("messageChatMemoryAdvisor — bean is loaded in Spring context and is not null")
        void messageChatMemoryAdvisor_beanIsLoadedInSpringContextAndIsNotNull() {
            // GIVEN / WHEN / THEN
            assertThat(messageChatMemoryAdvisor).isNotNull();
        }

        @Test
        @DisplayName("messageChatMemoryAdvisor — is a MessageChatMemoryAdvisor instance")
        void messageChatMemoryAdvisor_isAMessageChatMemoryAdvisorInstance() {
            // GIVEN / WHEN / THEN
            assertThat(messageChatMemoryAdvisor).isInstanceOf(MessageChatMemoryAdvisor.class);
        }
    }

    @Nested
    @DisplayName("ChatMemory behavior")
    class ChatMemoryBehaviorTests {

        @Test
        @DisplayName("chatMemory — returns empty list for an unknown conversationId")
        void chatMemory_returnsEmptyListForUnknownConversationId() {
            // GIVEN
            String unknownConversationId = "unknown-conv-" + System.nanoTime();

            // WHEN
            List<Message> messages = chatMemory.get(unknownConversationId);

            // THEN
            assertThat(messages).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("chatMemory — stores and retrieves a message for a given conversationId")
        void chatMemory_storesAndRetrievesMessageForConversationId() {
            // GIVEN
            String conversationId = "test-conv-" + System.nanoTime();
            UserMessage userMessage = new UserMessage("hello from test");

            // WHEN
            chatMemory.add(conversationId, userMessage);
            List<Message> messages = chatMemory.get(conversationId);

            // THEN
            assertThat(messages).isNotNull().hasSize(1);
            assertThat(messages.get(0).getText()).isEqualTo("hello from test");
        }

        @Test
        @DisplayName("chatMemory — stores multiple messages and retrieves them all for a given conversationId")
        void chatMemory_storesMultipleMessagesAndRetrievesThemAll() {
            // GIVEN
            String conversationId = "multi-conv-" + System.nanoTime();
            UserMessage msg1 = new UserMessage("first message");
            UserMessage msg2 = new UserMessage("second message");

            // WHEN
            chatMemory.add(conversationId, msg1);
            chatMemory.add(conversationId, msg2);
            List<Message> messages = chatMemory.get(conversationId);

            // THEN
            assertThat(messages).hasSize(2);
        }

        @Test
        @DisplayName("chatMemory — clears all messages for a given conversationId")
        void chatMemory_clearsAllMessagesForConversationId() {
            // GIVEN
            String conversationId = "clear-conv-" + System.nanoTime();
            chatMemory.add(conversationId, new UserMessage("msg1"));
            chatMemory.add(conversationId, new UserMessage("msg2"));

            // WHEN
            chatMemory.clear(conversationId);

            // THEN
            List<Message> messages = chatMemory.get(conversationId);
            assertThat(messages).isEmpty();
        }

        @Test
        @DisplayName("chatMemory — keeps conversations isolated by conversationId")
        void chatMemory_keepsConversationsIsolatedByConversationId() {
            // GIVEN
            String conv1 = "isolation-conv-1-" + System.nanoTime();
            String conv2 = "isolation-conv-2-" + System.nanoTime();
            chatMemory.add(conv1, new UserMessage("msg for conv1"));

            // WHEN
            List<Message> messagesConv2 = chatMemory.get(conv2);

            // THEN
            assertThat(messagesConv2).isEmpty();
        }
    }
}