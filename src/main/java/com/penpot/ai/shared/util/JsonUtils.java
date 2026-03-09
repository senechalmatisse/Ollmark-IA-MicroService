package com.penpot.ai.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilitaires pour la manipulation JSON.
 * Classe utilitaire statique pour les opérations JSON courantes.
 */
@Slf4j
@UtilityClass
public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Échappe une chaîne pour inclusion dans un JSON en déléguant à Jackson.
     * Gère tous les cas edge (caractères unicode, surrogates, etc.) sans risque d'oubli.
     *
     * @param str la chaîne à échapper
     * @return la chaîne JSON encodée entre guillemets, ou {@code "null"} si str est null
     */
    public static String escapeJson(String str) {
        if (str == null) return "null";
        try {
            return MAPPER.writeValueAsString(str);
        } catch (JsonProcessingException e) {
            log.warn("Unexpected JSON serialization error for string, falling back to manual escape", e);
            return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
    }

    /**
     * Vérifie si une chaîne est un JSON valide.
     * 
     * @param json la chaîne à vérifier
     * @return true si JSON valide
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isBlank()) return false;
        try {
            MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * Tronque une chaîne JSON pour les logs.
     * 
     * @param json la chaîne JSON
     * @param maxLength longueur maximale
     * @return la chaîne tronquée si nécessaire
     */
    public static String truncateForLog(String json, int maxLength) {
        if (json == null) return "null";
        if (json.length() <= maxLength) return json;
        return json.substring(0, maxLength) + "... (truncated)";
    }
}