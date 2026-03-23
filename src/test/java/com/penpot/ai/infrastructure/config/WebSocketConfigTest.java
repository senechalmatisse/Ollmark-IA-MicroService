package com.penpot.ai.infrastructure.config;

import com.penpot.ai.adapters.in.websocket.PluginWebSocketHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketConfig — Unit tests")
class WebSocketConfigTest {

    @Mock
    private PluginWebSocketHandler pluginWebSocketHandler;

    @InjectMocks
    private WebSocketConfig webSocketConfig;

    // ── Helper ────────────────────────────────────────────────────────────────

    private static class MockedRegistry {
        final WebSocketHandlerRegistry registry;
        final WebSocketHandlerRegistration registration;

        MockedRegistry() {
            registry = mock(WebSocketHandlerRegistry.class);
            registration = mock(WebSocketHandlerRegistration.class);
            when(registry.addHandler(any(), any(String.class))).thenReturn(registration);
            when(registration.setAllowedOrigins(any(String[].class))).thenReturn(registration);
        }
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("registerWebSocketHandlers")
    class RegisterWebSocketHandlersTests {

        @Test
        @DisplayName("enregistre le handler sur l'endpoint /plugin")
        void registerWebSocketHandlers_registersHandlerOnPluginEndpoint() {
            var mocked = new MockedRegistry();

            webSocketConfig.registerWebSocketHandlers(mocked.registry);

            verify(mocked.registry, times(1))
                    .addHandler(pluginWebSocketHandler, "/plugin");
        }

        @Test
        @DisplayName("appelle setAllowedOrigins avec les origines du profil actif")
        void registerWebSocketHandlers_setsAllowedOriginsFromProperties() {
            var mocked = new MockedRegistry();

            webSocketConfig.registerWebSocketHandlers(mocked.registry);

            verify(mocked.registration, times(1))
                    .setAllowedOrigins(any(String[].class));
        }

        @Test
        @DisplayName("n'utilise jamais le wildcard * comme origine autorisée")
        void registerWebSocketHandlers_doesNotUseWildcardOrigin() {
            var mocked = new MockedRegistry();
            ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);

            webSocketConfig.registerWebSocketHandlers(mocked.registry);

            verify(mocked.registration).setAllowedOrigins(captor.capture());

            String[] origins = captor.getValue();
            assertNotNull(origins, "setAllowedOrigins doit recevoir un tableau non-null");
            assertTrue(origins.length > 0, "La liste d'origines ne doit pas être vide");
            for (String origin : origins) {
                assertNotEquals("*", origin.trim(),
                        "Le wildcard '*' ne doit jamais être utilisé");
            }
        }

        @Test
        @DisplayName("utilise bien le bean PluginWebSocketHandler injecté")
        void registerWebSocketHandlers_usesInjectedPluginWebSocketHandlerBean() {
            var mocked = new MockedRegistry();

            webSocketConfig.registerWebSocketHandlers(mocked.registry);

            verify(mocked.registry).addHandler(eq(pluginWebSocketHandler), any());
        }

        @Test
        @DisplayName("n'enregistre qu'un seul handler WebSocket")
        void registerWebSocketHandlers_registersExactlyOneHandler() {
            var mocked = new MockedRegistry();

            webSocketConfig.registerWebSocketHandlers(mocked.registry);

            verify(mocked.registry, times(1)).addHandler(any(), any());
        }
    }
}