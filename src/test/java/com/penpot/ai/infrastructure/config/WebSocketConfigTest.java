package com.penpot.ai.infrastructure.config;

import com.penpot.ai.adapters.in.websocket.PluginWebSocketHandler;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.config.annotation.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketConfig — Unit")
class WebSocketConfigTest {

    // ── Constantes de test ────────────────────────────────────────────────────

    private static final String[] ALLOWED_ORIGINS = {
        "http://localhost",
        "http://localhost:4200",
        "https://design.penpot.app"
    };
    private static final int WEBSOCKET_PORT = 8080;

    // ── Collaborateurs mockés ─────────────────────────────────────────────────

    @Mock
    private PluginWebSocketHandler pluginWebSocketHandler;

    @Mock
    private WebSocketHandlerRegistry registry;

    @Mock
    private WebSocketHandlerRegistration registration;

    private WebSocketConfig webSocketConfig;

    // ── Setup ─────────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        /*
         * On instancie directement le SUT en injectant les valeurs
         * normalement portées par @Value — DIP respecté : WebSocketConfig
         * ne connaît que les abstractions (WebSocketHandlerRegistry),
         * pas le contexte Spring.
         */
        webSocketConfig = new WebSocketConfig(
            pluginWebSocketHandler,
            WEBSOCKET_PORT,
            ALLOWED_ORIGINS
        );

        // Chaîne fluente : registry.addHandler(...) → registration
        when(registry.addHandler(any(), any(String.class))).thenReturn(registration);
        when(registration.setAllowedOrigins(any(String[].class))).thenReturn(registration);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("registerWebSocketHandlers")
    class RegisterWebSocketHandlersTests {

        @Test
        @DisplayName("enregistre le handler sur l'endpoint /plugin")
        void registerWebSocketHandlers_registersHandlerOnPluginEndpoint() {
            webSocketConfig.registerWebSocketHandlers(registry);

            verify(registry, times(1)).addHandler(pluginWebSocketHandler, "/plugin");
        }

        @Test
        @DisplayName("n'enregistre qu'un seul handler WebSocket")
        void registerWebSocketHandlers_registersExactlyOneHandler() {
            webSocketConfig.registerWebSocketHandlers(registry);

            verify(registry, times(1)).addHandler(any(), any());
        }

        @Test
        @DisplayName("utilise bien le bean PluginWebSocketHandler injecté")
        void registerWebSocketHandlers_usesInjectedPluginWebSocketHandlerBean() {
            webSocketConfig.registerWebSocketHandlers(registry);

            verify(registry).addHandler(eq(pluginWebSocketHandler), any());
        }

        @Test
        @DisplayName("appelle setAllowedOrigins avec les origines configurées")
        void registerWebSocketHandlers_setsAllowedOriginsFromProperties() {
            webSocketConfig.registerWebSocketHandlers(registry);

            verify(registration, times(1)).setAllowedOrigins(any(String[].class));
        }

        @Test
        @DisplayName("n'utilise jamais le wildcard * comme origine autorisée")
        void registerWebSocketHandlers_doesNotUseWildcardOrigin() {
            ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);

            webSocketConfig.registerWebSocketHandlers(registry);

            verify(registration).setAllowedOrigins(captor.capture());
            String[] origins = captor.getValue();

            assertNotNull(origins, "setAllowedOrigins doit recevoir un tableau non-null");
            assertTrue(origins.length > 0, "La liste d'origines ne doit pas être vide");
            for (String origin : origins) {
                assertNotEquals("*", origin.trim(),
                        "Le wildcard '*' ne doit jamais être utilisé");
            }
        }

        @Test
        @DisplayName("transmet exactement les origines configurées")
        void registerWebSocketHandlers_passesExactConfiguredOrigins() {
            ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);

            webSocketConfig.registerWebSocketHandlers(registry);

            verify(registration).setAllowedOrigins(captor.capture());
            assertArrayEquals(ALLOWED_ORIGINS, captor.getValue(),
                    "Les origines transmises doivent correspondre exactement à la configuration");
        }
    }
}