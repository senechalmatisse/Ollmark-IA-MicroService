package com.penpot.ai.application.tools.support;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

@DisplayName("PenpotJsSnippets — Unit")
class PenpotJsSnippetsUnit {

    @Test
    void shouldGenerateCreateTextScriptWithRequiredParamsOnly() {
        // GIVEN
        String content = "Hello";
        int x = 100;
        int y = 200;

        // WHEN
        String result = PenpotJsSnippets.createText(content, x, y, null, null, null, null);

        // THEN
        assertTrue(result.contains("const text = penpot.createText('Hello');"));
        assertTrue(result.contains("text.x = 100;"));
        assertTrue(result.contains("text.y = 200;"));
        assertFalse(result.contains("text.fontSize"));
        assertFalse(result.contains("text.fontWeight"));
        assertFalse(result.contains("text.fills"));
        assertFalse(result.contains("text.name"));
        assertTrue(result.endsWith("return text.id;\n"));
    }

    @Test
    void shouldGenerateCreateTextScriptWithFontSize() {
        // GIVEN
        String content = "Texte";
        int x = 0;
        int y = 0;
        Integer fontSize = 24;

        // WHEN
        String result = PenpotJsSnippets.createText(content, x, y, fontSize, null, null, null);

        // THEN
        assertTrue(result.contains("text.fontSize = 24;"));
    }

    @Test
    void shouldGenerateCreateTextScriptWithFontWeight() {
        // GIVEN
        String content = "Texte";
        int x = 0;
        int y = 0;
        String fontWeight = "bold";

        // WHEN
        String result = PenpotJsSnippets.createText(content, x, y, null, fontWeight, null, null);

        // THEN
        assertTrue(result.contains("text.fontWeight = 'bold';"));
    }

    @Test
    void shouldGenerateCreateTextScriptWithFillColor() {
        // GIVEN
        String content = "Texte";
        int x = 0;
        int y = 0;
        String fillColor = "#FF0000";

        // WHEN
        String result = PenpotJsSnippets.createText(content, x, y, null, null, fillColor, null);

        // THEN
        assertTrue(result.contains("text.fills = [{ fillColor: '#FF0000' }];"));
    }

    @Test
    void shouldGenerateCreateTextScriptWithName() {
        // GIVEN
        String content = "Texte";
        int x = 0;
        int y = 0;
        String name = "Mon Titre";

        // WHEN
        String result = PenpotJsSnippets.createText(content, x, y, null, null, null, name);

        // THEN
        assertTrue(result.contains("text.name = 'Mon Titre';"));
    }

    @Test
    void shouldIgnoreFontSizeWhenZeroOrNegative() {
        // GIVEN
        String content = "Texte";
        int x = 0;
        int y = 0;
        Integer invalidFontSize = 0;

        // WHEN
        String result = PenpotJsSnippets.createText(content, x, y, invalidFontSize, null, null, null);

        // THEN
        assertFalse(result.contains("text.fontSize"));
    }

    @Test
    void shouldIgnoreFontWeightWhenBlank() {
        // GIVEN
        String content = "Texte";
        int x = 0;
        int y = 0;
        String blankFontWeight = "   ";

        // WHEN
        String result = PenpotJsSnippets.createText(content, x, y, null, blankFontWeight, null, null);

        // THEN
        assertFalse(result.contains("text.fontWeight"));
    }

    @Test
    void shouldIgnoreFillColorWhenBlank() {
        // GIVEN
        String content = "Texte";
        int x = 0;
        int y = 0;
        String blankFillColor = "";

        // WHEN
        String result = PenpotJsSnippets.createText(content, x, y, null, null, blankFillColor, null);

        // THEN
        assertFalse(result.contains("text.fills"));
    }

    @Test
    void shouldIgnoreNameWhenBlank() {
        // GIVEN
        String content = "Texte";
        int x = 0;
        int y = 0;
        String blankName = " \n \t ";

        // WHEN
        String result = PenpotJsSnippets.createText(content, x, y, null, null, null, blankName);

        // THEN
        assertFalse(result.contains("text.name"));
    }

    @Test
    void shouldLoadSelectionSnippetWhenIdsListIsNull() {
        try (MockedStatic<JsScriptLoader> mockedLoader = mockStatic(JsScriptLoader.class)) {
            // GIVEN
            List<String> ids = null;
            String toolName = "TestTool";
            mockedLoader.when(() -> JsScriptLoader.loadWith(eq("snippets/collect-shapes-selection.js"), anyMap()))
                        .thenReturn("mocked-selection-script");

            // WHEN
            String result = PenpotJsSnippets.collectShapesOrFallback(ids, toolName);

            // THEN
            assertEquals("mocked-selection-script", result);
        }
    }

    @Test
    void shouldLoadSelectionSnippetWhenIdsListIsEmpty() {
        try (MockedStatic<JsScriptLoader> mockedLoader = mockStatic(JsScriptLoader.class)) {
            // GIVEN
            List<String> ids = Collections.emptyList();
            String toolName = "TestTool";
            mockedLoader.when(() -> JsScriptLoader.loadWith(eq("snippets/collect-shapes-selection.js"), anyMap()))
                        .thenReturn("mocked-selection-script");

            // WHEN
            String result = PenpotJsSnippets.collectShapesOrFallback(ids, toolName);

            // THEN
            assertEquals("mocked-selection-script", result);
        }
    }

    @Test
    void shouldGenerateCollectionScriptWhenIdsAreProvided() {
        // GIVEN
        List<String> ids = List.of("uuid-1", "uuid-2");
        String toolName = "MonOutil";

        // WHEN
        String result = PenpotJsSnippets.collectShapesOrFallback(ids, toolName);

        // THEN
        assertTrue(result.contains("const shapes = [];"));
        assertTrue(result.contains("penpot.currentPage.getShapeById('uuid-1')"));
        assertTrue(result.contains("penpot.currentPage.getShapeById('uuid-2')"));
        assertTrue(result.contains("[MonOutil]")); 
        assertTrue(result.contains("if (shapes.length === 0) shapes.push(...penpot.selection);"));
    }

    @Test
    void shouldLoadSelectionSnippetWhenShapeIdsIsNull() {
        try (MockedStatic<JsScriptLoader> mockedLoader = mockStatic(JsScriptLoader.class)) {
            // GIVEN
            List<String> shapeIds = null;
            mockedLoader.when(() -> JsScriptLoader.load("snippets/find-first-shape-selection.js"))
                        .thenReturn("mocked-first-shape-selection");

            // WHEN
            String result = PenpotJsSnippets.findFirstShapeOrFallback(shapeIds);

            // THEN
            assertEquals("mocked-first-shape-selection", result);
        }
    }

    @Test
    void shouldLoadSelectionSnippetWhenShapeIdsIsEmpty() {
        try (MockedStatic<JsScriptLoader> mockedLoader = mockStatic(JsScriptLoader.class)) {
            // GIVEN
            List<String> shapeIds = Collections.emptyList();
            mockedLoader.when(() -> JsScriptLoader.load("snippets/find-first-shape-selection.js"))
                        .thenReturn("mocked-first-shape-selection");

            // WHEN
            String result = PenpotJsSnippets.findFirstShapeOrFallback(shapeIds);

            // THEN
            assertEquals("mocked-first-shape-selection", result);
        }
    }

    @Test
    void shouldLoadSpecificShapeSnippetWhenShapeIdsHasElements() {
        try (MockedStatic<JsScriptLoader> mockedLoader = mockStatic(JsScriptLoader.class)) {
            // GIVEN
            List<String> shapeIds = List.of("target-uuid", "ignored-uuid");
            mockedLoader.when(() -> JsScriptLoader.loadWith(eq("snippets/find-first-shape.js"), eq(Map.of("shapeId", "target-uuid"))))
                        .thenReturn("mocked-specific-shape");

            // WHEN
            String result = PenpotJsSnippets.findFirstShapeOrFallback(shapeIds);

            // THEN
            assertEquals("mocked-specific-shape", result);
        }
    }

    @Test
    void shouldLoadSpecificShapeSnippet() {
        try (MockedStatic<JsScriptLoader> mockedLoader = mockStatic(JsScriptLoader.class)) {
            // GIVEN
            String shapeId = "my-unique-shape-id";
            mockedLoader.when(() -> JsScriptLoader.loadWith(eq("snippets/find-shape.js"), eq(Map.of("shapeId", "my-unique-shape-id"))))
                        .thenReturn("mocked-find-shape");

            // WHEN
            String result = PenpotJsSnippets.findShapeOrFallback(shapeId);

            // THEN
            assertEquals("mocked-find-shape", result);
        }
    }
}