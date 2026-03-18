package com.penpot.ai.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration CORS lisant les origines autorisées depuis les properties
 * du profil actif (cors.allowed-origins).
 *
 * Chaque profil définit sa propre liste d'origines :
 * - local/dev : localhost + design.penpot.app
 * - prod      : uniquement https://design.penpot.app
 */
@Configuration
public class CorsConfig {

    /**
     * Origines autorisées, lues depuis cors.allowed-origins dans le profil actif.
     * Supporte une liste séparée par virgules.
     */
    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods("GET", "POST", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(false);
            }
        };
    }
}