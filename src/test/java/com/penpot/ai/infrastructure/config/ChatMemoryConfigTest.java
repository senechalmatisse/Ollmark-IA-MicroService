package com.penpot.ai.infrastructure.config;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatMemoryConfig")
class ChatMemoryConfigTest {

    @Nested
    @DisplayName("chatMemory bean — Spring Integration")
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = ChatMemoryConfig.class)
    @TestPropertySource(properties = "penpot.ai.chat.memory.max-messages=10")
    class ChatMemoryBeanTests {

        @MockitoBean
        private ChatMemoryRepository chatMemoryRepository;

        @Autowired
        private ChatMemory chatMemory;

        @Test
        @DisplayName("bean chargé et non-null")
        void chatMemory_beanIsLoadedInSpringContextAndIsNotNull() {
            assertThat(chatMemory).isNotNull();
        }

        @Test
        @DisplayName("implémentation = MessageWindowChatMemory")
        void chatMemory_isAMessageWindowChatMemoryInstance() {
            assertThat(chatMemory).isInstanceOf(MessageWindowChatMemory.class);
        }
    }

    @Nested
    @DisplayName("messageChatMemoryAdvisor bean — Spring Integration")
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = ChatMemoryConfig.class)
    @TestPropertySource(properties = "penpot.ai.chat.memory.max-messages=10")
    class MessageChatMemoryAdvisorBeanTests {

        @MockitoBean
        private ChatMemoryRepository chatMemoryRepository;

        @Autowired
        private MessageChatMemoryAdvisor messageChatMemoryAdvisor;

        @Test
        @DisplayName("bean chargé et non-null")
        void messageChatMemoryAdvisor_beanIsLoadedInSpringContextAndIsNotNull() {
            assertThat(messageChatMemoryAdvisor).isNotNull();
        }

        @Test
        @DisplayName("implémentation = MessageChatMemoryAdvisor")
        void messageChatMemoryAdvisor_isAMessageChatMemoryAdvisorInstance() {
            assertThat(messageChatMemoryAdvisor).isInstanceOf(MessageChatMemoryAdvisor.class);
        }
    }

    @Nested
    @DisplayName("ChatMemory behavior — Unit")
    class ChatMemoryBehaviorTests {

        private ChatMemory memory;

        @BeforeEach
        void setUp() {
            memory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();
        }

        @Test
        @DisplayName("retourne une liste vide pour un conversationId inconnu")
        void chatMemory_returnsEmptyListForUnknownConversationId() {
            String unknownId = "unknown-conv-" + System.nanoTime();

            List<Message> messages = memory.get(unknownId);

            assertThat(messages).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("stocke et récupère un message pour un conversationId donné")
        void chatMemory_storesAndRetrievesMessageForConversationId() {
            String conversationId = "test-conv-" + System.nanoTime();
            UserMessage userMessage = new UserMessage("hello from test");

            memory.add(conversationId, userMessage);
            List<Message> messages = memory.get(conversationId);

            assertThat(messages).hasSize(1);
            assertThat(messages.get(0).getText()).isEqualTo("hello from test");
        }

        @Test
        @DisplayName("stocke plusieurs messages et les récupère tous")
        void chatMemory_storesMultipleMessagesAndRetrievesThemAll() {
            String conversationId = "multi-conv-" + System.nanoTime();

            memory.add(conversationId, new UserMessage("first message"));
            memory.add(conversationId, new UserMessage("second message"));

            assertThat(memory.get(conversationId)).hasSize(2);
        }

        @Test
        @DisplayName("vide tous les messages pour un conversationId donné")
        void chatMemory_clearsAllMessagesForConversationId() {
            String conversationId = "clear-conv-" + System.nanoTime();
            memory.add(conversationId, new UserMessage("msg1"));
            memory.add(conversationId, new UserMessage("msg2"));

            memory.clear(conversationId);

            assertThat(memory.get(conversationId)).isEmpty();
        }

        @Test
        @DisplayName("isole les conversations par conversationId")
        void chatMemory_keepsConversationsIsolatedByConversationId() {
            String conv1 = "isolation-conv-1-" + System.nanoTime();
            String conv2 = "isolation-conv-2-" + System.nanoTime();

            memory.add(conv1, new UserMessage("msg for conv1"));

            assertThat(memory.get(conv2)).isEmpty();
        }

        @Test
        @DisplayName("respecte la fenêtre maxMessages configurée")
        void chatMemory_respectsMaxMessagesWindow() {
            String conversationId = "window-conv-" + System.nanoTime();
            ChatMemory limitedMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(3)
                .build();

            for (int i = 1; i <= 5; i++) {
                limitedMemory.add(conversationId, new UserMessage("message " + i));
            }

            List<Message> messages = limitedMemory.get(conversationId);
            assertThat(messages).hasSize(3);
            assertThat(messages.get(2).getText()).isEqualTo("message 5");
        }
    }
}