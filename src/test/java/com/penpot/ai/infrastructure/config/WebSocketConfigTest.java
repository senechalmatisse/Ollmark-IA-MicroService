package com.penpot.ai.infrastructure.config;

import com.penpot.ai.adapters.in.websocket.PluginWebSocketHandler;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.config.annotation.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        WebSocketConfig.class,
})
@TestPropertySource(properties = {
        "penpot.ai.websocket-port=8080",
        "cors.allowed-origins=http://localhost,http://localhost:4200,https://design.penpot.app",
})
@DisplayName("WebSocketConfig — Integration")
class WebSocketConfigTest {

    @MockitoBean
    private PluginWebSocketHandler pluginWebSocketHandler;

    @Autowired
    private WebSocketConfig webSocketConfig;

    // ── Helper ────────────────────────────────────────────────────────────────

    private static MockedRegistry buildMockedRegistry() {
        WebSocketHandlerRegistry     registry     = mock(WebSocketHandlerRegistry.class);
        WebSocketHandlerRegistration registration = mock(WebSocketHandlerRegistration.class);
        when(registry.addHandler(any(), any(String.class))).thenReturn(registration);
        when(registration.setAllowedOrigins(any(String[].class))).thenReturn(registration);
        return new MockedRegistry(registry, registration);
    }

    private record MockedRegistry(
            WebSocketHandlerRegistry     registry,
            WebSocketHandlerRegistration registration) {}

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("registerWebSocketHandlers")
    class RegisterWebSocketHandlersTests {

        @Test
        @DisplayName("enregistre le handler sur l'endpoint /plugin")
        void registerWebSocketHandlers_registersHandlerOnPluginEndpoint() {
            var mocked = buildMockedRegistry();

            webSocketConfig.registerWebSocketHandlers(mocked.registry());

            verify(mocked.registry(), times(1))
                    .addHandler(pluginWebSocketHandler, "/plugin");
        }

        @Test
        @DisplayName("appelle setAllowedOrigins avec les origines du profil actif")
        void registerWebSocketHandlers_setsAllowedOriginsFromProperties() {
            var mocked = buildMockedRegistry();

            webSocketConfig.registerWebSocketHandlers(mocked.registry());

            verify(mocked.registration(), times(1))
                    .setAllowedOrigins(any(String[].class));
        }

        @Test
        @DisplayName("n'utilise jamais le wildcard * comme origine autorisée")
        void registerWebSocketHandlers_doesNotUseWildcardOrigin() {
            var mocked = buildMockedRegistry();
            ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);

            webSocketConfig.registerWebSocketHandlers(mocked.registry());

            verify(mocked.registration()).setAllowedOrigins(captor.capture());

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
            var mocked = buildMockedRegistry();

            webSocketConfig.registerWebSocketHandlers(mocked.registry());

            verify(mocked.registry()).addHandler(eq(pluginWebSocketHandler), any());
        }

        @Test
        @DisplayName("n'enregistre qu'un seul handler WebSocket")
        void registerWebSocketHandlers_registersExactlyOneHandler() {
            var mocked = buildMockedRegistry();

            webSocketConfig.registerWebSocketHandlers(mocked.registry());

            verify(mocked.registry(), times(1)).addHandler(any(), any());
        }
    }
}