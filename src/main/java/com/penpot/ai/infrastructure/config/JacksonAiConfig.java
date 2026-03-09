package com.penpot.ai.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.*;

/**
 * Configuration Spring dédiée à la personnalisation du comportement
 * de la bibliothèque Jackson utilisée pour la sérialisation et la
 * désérialisation JSON dans l’application.
 */
@Configuration
public class JacksonAiConfig {

    /**
     * Personnalise le {@link com.fasterxml.jackson.databind.ObjectMapper}
     * utilisé par Spring Boot pour la désérialisation JSON.
     *
     * <p>Lorsque cette fonctionnalité est activée :</p>
     * <ul>
     *     <li>si une valeur JSON correspond à une constante d’énumération inconnue,</li>
     *     <li>Jackson utilise la valeur par défaut définie dans l’énumération,</li>
     *     <li>au lieu de lever une exception de désérialisation.</li>
     * </ul>
     *
     * @return un customizer appliqué au builder Jackson utilisé
     *         par Spring Boot
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.featuresToEnable(
            DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE
        );
    }
}