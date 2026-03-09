package com.penpot.ai.infrastructure.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

/**
 * Configuration Spring permettant de définir les règles CORS
 * (Cross-Origin Resource Sharing) pour l'application.
 *
 * <p>
 * Les règles configurées ici permettent notamment :
 * </p>
 * <ul>
 *     <li>d'autoriser les requêtes provenant de certaines origines locales</li>
 *     <li>de définir les méthodes HTTP autorisées</li>
 *     <li>de contrôler les en-têtes HTTP acceptés</li>
 *     <li>de spécifier si les credentials sont autorisés</li>
 * </ul>
 */
@Configuration
public class CorsConfig {

    /**
     * Définit un {@link WebMvcConfigurer} permettant de configurer
     * les règles CORS de l'application Spring MVC.
     *
     * <p>
     * Les règles appliquées dans cette configuration sont les suivantes :
     * </p>
     * <ul>
     *     <li>autorise les requêtes sur toutes les routes de l'application</li>
     *     <li>autorise les origines locales utilisées par le frontend</li>
     *     <li>autorise les méthodes HTTP GET, POST et DELETE</li>
     *     <li>autorise tous les en-têtes HTTP</li>
     *     <li>désactive l'utilisation des credentials (cookies, authentification HTTP)</li>
     * </ul>
     *
     * <p>
     * Cette configuration est particulièrement utile lorsque le frontend
     * est exécuté localement sur un serveur de développement (par exemple
     * Angular, Vue ou React).
     * </p>
     *
     * @return une instance de {@link WebMvcConfigurer} configurant
     * les règles CORS de l'application
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            /**
             * Configure les mappings CORS pour les endpoints exposés
             * par l'application.
             *
             * @param registry registre permettant de définir les règles
             *                 de mapping CORS
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(false);
            }
        };
    }
}