package com.penpot.ai.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité pour le service ms-ollmark-ia.
 * <p>
 * Cette classe implémente la correction pour la vulnérabilité AUDIT2-VULN9 (Exposition Swagger).
 * Elle adopte une stratégie de sécurité hybride :
 * <ul>
 * <li>Sécurisation des outils de documentation (Swagger/OpenAPI) via HTTP Basic.</li>
 * <li>Libre accès temporaire aux endpoints IA (en attendant l'implémentation complète des JWT/Sessions).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.swagger.user:admin}")
    private String swaggerUser;

    @Value("${app.security.swagger.password:password_to_change}")
    private String swaggerPassword;

    /**
     * Définit la chaîne de filtres de sécurité.
     * La logique est de restreindre l'accès à Swagger aux seuls utilisateurs possédant le rôle 'ADMIN',
     * tout en laissant les autres routes accessibles pour ne pas bloquer les fonctionnalités du plugin.
     *
     * @param http l'objet HttpSecurity à configurer
     * @return la chaîne de filtres construite
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactivation du CSRF car nous sommes sur une architecture API/Stateless
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Protection spécifique des routes Swagger
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").hasRole("ADMIN")
                // On laisse tout le reste ouvert (Endpoints IA, WebSocket) pour le moment
                .anyRequest().permitAll()
            )
            // Utilisation de l'authentification Basic (navigateur) pour Swagger
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * Configuration d'un utilisateur technique en mémoire pour l'accès à la documentation.
     * <p>
     * C'est ici que le rôle 'ADMIN' est rattaché à l'utilisateur défini dans la configuration.
     * Ce rôle n'existe que durant la durée de vie du processus Java.
     *
     * @param encoder le bean d'encodage de mot de passe
     * @return un gestionnaire d'utilisateurs en mémoire
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.withUsername(swaggerUser)
                .password(encoder.encode(swaggerPassword))
                .roles("ADMIN") // Le rôle est défini ici "en dur" dans la RAM
                .build();
        
        return new InMemoryUserDetailsManager(admin);
    }

    /**
     * Définit l'algorithme de hachage pour les mots de passe.
     * BCrypt est utilisé pour garantir une sécurité robuste même pour les accès mémoire.
     *
     * @return l'instance de BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}