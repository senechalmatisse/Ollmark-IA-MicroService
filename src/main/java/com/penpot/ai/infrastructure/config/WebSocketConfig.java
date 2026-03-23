package com.penpot.ai.infrastructure.config;

import com.penpot.ai.adapters.in.websocket.PluginWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * Configuration WebSocket pour la communication avec le plugin Penpot.
 * Configure l'infrastructure WebSocket en mode refactorisé.
 */
@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * Handler pour les connexions WebSocket du plugin.
     * Injection simple sans dépendance circulaire.
     */
    private final PluginWebSocketHandler pluginWebSocketHandler;

    /**
     * Port WebSocket configuré (non utilisé directement ici,
     * mais disponible pour logging ou validation).
     */
    @Value("${penpot.ai.websocket-port:8080}")
    private int websocketPort;

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Configure le conteneur WebSocket avec une taille de buffer augmentée.
     * 
     * @return le bean de configuration du conteneur
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(1024 * 1024); // 1 MB
        container.setMaxBinaryMessageBufferSize(1024 * 1024); // 1 MB
        log.info("WebSocket buffer size set to 1 MB");
        return container;
    }

    /**
     * Enregistre les handlers WebSocket avec leurs endpoints.
     * Configure l'endpoint /plugin pour accepter les connexions WebSocket
     * du plugin Penpot avec CORS désactivé (tous les origins acceptés).
     * 
     * @param registry le registre pour enregistrer les handlers WebSocket
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("Registering WebSocket handler on /plugin (port: {})", websocketPort);

        registry.addHandler(pluginWebSocketHandler, "/plugin")
                .setAllowedOrigins(allowedOrigins);

        log.info("WebSocket handler registered — allowed origins: {}",
                String.join(", ", allowedOrigins));
    }
}