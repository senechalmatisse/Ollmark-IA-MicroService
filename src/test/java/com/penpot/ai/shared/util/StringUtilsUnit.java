package com.penpot.ai.shared.util;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("JsonUtils — Unit")
public class StringUtilsUnit {

    @Nested
    @DisplayName("jsSafe")
    class JsSafeTests {

        @Test
        void shouldReturnEmptyStringWhenInputIsNull() {
            // GIVEN
            String input = null;

            // WHEN
            String result = JsStringUtils.jsSafe(input);

            // THEN
            assertEquals("", result);
        }

        @Test
        void shouldEscapeBackslashes() {
            // GIVEN
            String input = "Path\\To\\File";

            // WHEN
            String result = JsStringUtils.jsSafe(input);

            // THEN
            assertEquals("Path\\\\To\\\\File", result);
        }

        @Test
        void shouldEscapeSingleQuotes() {
            // GIVEN
            String input = "L'outil de l'IA";

            // WHEN
            String result = JsStringUtils.jsSafe(input);

            // THEN
            assertEquals("L\\'outil de l\\'IA", result);
        }

        @Test
        void shouldEscapeNewLines() {
            // GIVEN
            String input = "Ligne 1\nLigne 2";

            // WHEN
            String result = JsStringUtils.jsSafe(input);

            // THEN
            assertEquals("Ligne 1\\nLigne 2", result);
        }
    }
}