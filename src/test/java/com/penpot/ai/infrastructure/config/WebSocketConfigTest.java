package com.penpot.ai.infrastructure.config;

import com.penpot.ai.adapters.in.websocket.PluginWebSocketHandler;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.config.annotation.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("WebSocketConfig — Integration")
public class WebSocketConfigTest {

    @MockitoBean
    private PluginWebSocketHandler pluginWebSocketHandler;

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Nested
    @DisplayName("registerWebSocketHandlers")
    class RegisterWebSocketHandlersTests {

        @Test
        @DisplayName("registerWebSocketHandlers — registers handler on /plugin endpoint")
        void registerWebSocketHandlers_registersHandlerOnPluginEndpoint() {
            // GIVEN
            WebSocketHandlerRegistry registry = mock(WebSocketHandlerRegistry.class);
            var registration = mock(WebSocketHandlerRegistration.class);
            when(registry.addHandler(any(), eq("/plugin"))).thenReturn(registration);
            when(registration.setAllowedOrigins(any())).thenReturn(registration);

            // WHEN
            webSocketConfig.registerWebSocketHandlers(registry);

            // THEN
            verify(registry, times(1)).addHandler(pluginWebSocketHandler, "/plugin");
        }

        @Test
        @DisplayName("registerWebSocketHandlers — sets allowed origins to wildcard *")
        void registerWebSocketHandlers_setsAllowedOriginsToWildcard() {
            // GIVEN
            WebSocketHandlerRegistry registry = mock(WebSocketHandlerRegistry.class);
            var registration = mock(WebSocketHandlerRegistration.class);
            when(registry.addHandler(any(), any(String.class))).thenReturn(registration);
            when(registration.setAllowedOrigins(any())).thenReturn(registration);

            // WHEN
            webSocketConfig.registerWebSocketHandlers(registry);

            // THEN
            verify(registration, times(1)).setAllowedOrigins("*");
        }

        @Test
        @DisplayName("registerWebSocketHandlers — uses the injected PluginWebSocketHandler bean")
        void registerWebSocketHandlers_usesInjectedPluginWebSocketHandlerBean() {
            // GIVEN
            WebSocketHandlerRegistry registry = mock(WebSocketHandlerRegistry.class);
            var registration = mock(WebSocketHandlerRegistration.class);
            when(registry.addHandler(any(), any(String.class))).thenReturn(registration);
            when(registration.setAllowedOrigins(any())).thenReturn(registration);

            // WHEN
            webSocketConfig.registerWebSocketHandlers(registry);

            // THEN
            verify(registry).addHandler(eq(pluginWebSocketHandler), any());
        }
    }
}