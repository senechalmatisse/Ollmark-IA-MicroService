package com.penpot.ai.application.tools.support;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chargeur de scripts JavaScript depuis les ressources du classpath.
 *
 * <p>Tous les scripts sont lus depuis {@code src/main/resources/js/} et mis en cache
 * dès le premier accès. Les paramètres dynamiques sont injectés via des placeholders
 * nommés de la forme {@code {{paramName}}}.</p>
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * String js = JsScriptLoader.load("snippets/find-shape.js")
 *     .replace("{{shapeId}}", shapeId);
 * }</pre>
 */
@Slf4j
@UtilityClass
public class JsScriptLoader {

    private static final String BASE_PATH = "/js/";
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    /**
     * Charge un script JavaScript depuis le classpath et le met en cache.
     *
     * @param resourcePath chemin relatif depuis {@code /js/} (ex: {@code "snippets/find-shape.js"})
     * @return le contenu du script sous forme de chaîne
     * @throws IllegalStateException si le fichier n'existe pas ou ne peut pas être lu
     */
    public static String load(String resourcePath) {
        return CACHE.computeIfAbsent(resourcePath, path -> {
            String fullPath = BASE_PATH + path;
            try (InputStream is = JsScriptLoader.class.getResourceAsStream(fullPath)) {
                if (is == null) {
                    throw new IllegalStateException("JS script not found in classpath: " + fullPath);
                }
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                log.debug("Loaded and cached JS script: {}", fullPath);
                return content;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load JS script: " + fullPath, e);
            }
        });
    }

    /**
     * Charge un script et remplace un ensemble de placeholders {@code {{key}}} par leur valeur.
     *
     * @param resourcePath chemin relatif depuis {@code /js/}
     * @param replacements map de {@code placeholder → valeur}
     * @return le script avec tous les placeholders remplacés
     */
    public static String loadWith(String resourcePath, Map<String, String> replacements) {
        String script = load(resourcePath);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            script = script.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return script;
    }
}