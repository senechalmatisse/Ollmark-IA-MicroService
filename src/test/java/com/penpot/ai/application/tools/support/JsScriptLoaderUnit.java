package com.penpot.ai.application.tools.support;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsScriptLoader — Unit")
class JsScriptLoaderUnit {

    private static final String REAL_SNIPPET_PATH = "snippets/collect-shapes-selection.js";

    /**
     * Nettoie le cache statique avant chaque test pour garantir
     * une isolation stricte (1 test = 1 cas d'usage indépendant).
     */
    @BeforeEach
    void setUp() throws Exception {
        Field cacheField = JsScriptLoader.class.getDeclaredField("CACHE");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, String> cache = (ConcurrentHashMap<String, String>) cacheField.get(null);
        cache.clear();
    }

    @Test
    void shouldThrowExceptionWhenScriptNotFound() {
        // GIVEN
        String invalidPath = "snippets/does-not-exist-script.js";

        // WHEN
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> JsScriptLoader.load(invalidPath)
        );

        // THEN
        assertTrue(exception.getMessage().contains("JS script not found in classpath"));
        assertTrue(exception.getMessage().contains(invalidPath));
    }

    @Test
    void shouldLoadScriptContentSuccessfully() {
        // GIVEN
        String validPath = REAL_SNIPPET_PATH;

        // WHEN
        String content = JsScriptLoader.load(validPath);

        // THEN
        assertNotNull(content);
        assertTrue(content.contains("const shapes = [...penpot.selection];"));
        assertTrue(content.contains("{{toolName}}"));
    }

    @Test
    void shouldCacheScriptAfterFirstLoad() throws Exception {
        // GIVEN
        String validPath = REAL_SNIPPET_PATH;
        JsScriptLoader.load(validPath);

        // WHEN
        Field cacheField = JsScriptLoader.class.getDeclaredField("CACHE");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, String> cache = (ConcurrentHashMap<String, String>) cacheField.get(null);

        // THEN
        assertTrue(cache.containsKey(validPath));
        assertEquals(1, cache.size());
    }

    @Test
    void shouldReplacePlaceholdersCorrectly() {
        // GIVEN
        String templatePath = REAL_SNIPPET_PATH;
        Map<String, String> replacements = Map.of("toolName", "InspectorTool");

        // WHEN
        String result = JsScriptLoader.loadWith(templatePath, replacements);

        // THEN
        assertTrue(result.contains("[InspectorTool] Using current selection:"));
        assertFalse(result.contains("{{toolName}}"));
    }

    @Test
    void shouldReturnUnmodifiedScriptWhenReplacementsMapIsEmpty() {
        // GIVEN
        String templatePath = REAL_SNIPPET_PATH;
        Map<String, String> emptyReplacements = Map.of();

        // WHEN
        String result = JsScriptLoader.loadWith(templatePath, emptyReplacements);

        // THEN
        assertTrue(result.contains("[{{toolName}}]"));
    }

    @Test
    void shouldIgnoreExtraPlaceholdersInMapThatAreNotInScript() {
        // GIVEN
        String templatePath = REAL_SNIPPET_PATH;
        Map<String, String> replacements = Map.of(
            "toolName", "ValidTool",
            "unknownParam", "GhostValue"
        );

        // WHEN
        String result = JsScriptLoader.loadWith(templatePath, replacements);

        // THEN
        assertTrue(result.contains("[ValidTool]"));
        assertFalse(result.contains("GhostValue"));
    }
}