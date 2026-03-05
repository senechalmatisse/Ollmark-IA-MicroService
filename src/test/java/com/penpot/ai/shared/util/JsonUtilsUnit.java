package com.penpot.ai.shared.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JsonUtils — Unit")
public class JsonUtilsUnit {

    @Nested
    @DisplayName("escapeJson")
    class EscapeJsonTests {

        @Test
        @DisplayName("escapeJson — returns \"null\" string literal when input is null")
        void escapeJson_returnsNullLiteralWhenInputIsNull() {
            // GIVEN

            // WHEN
            String result = JsonUtils.escapeJson(null);

            // THEN
            assertThat(result).isEqualTo("null");
        }

        @Test
        @DisplayName("escapeJson — returns quoted empty string when input is empty")
        void escapeJson_returnsQuotedEmptyStringWhenInputIsEmpty() {
            // GIVEN

            // WHEN
            String result = JsonUtils.escapeJson("");

            // THEN
            assertThat(result).isEqualTo("\"\"");
        }

        @Test
        @DisplayName("escapeJson — returns quoted plain string when input has no special characters")
        void escapeJson_returnsQuotedPlainStringWhenInputHasNoSpecialChars() {
            // GIVEN

            // WHEN
            String result = JsonUtils.escapeJson("hello world");

            // THEN
            assertThat(result).isEqualTo("\"hello world\"");
        }

        @Test
        @DisplayName("escapeJson — escapes double quotes in the input string")
        void escapeJson_escapesDoubleQuotes() {
            // GIVEN

            // WHEN
            String result = JsonUtils.escapeJson("say \"hello\"");

            // THEN
            assertThat(result).isEqualTo("\"say \\\"hello\\\"\"");
        }

        @Test
        @DisplayName("escapeJson — escapes backslashes in the input string")
        void escapeJson_escapesBackslashes() {
            // GIVEN

            // WHEN
            String result = JsonUtils.escapeJson("C:\\Users\\penpot");

            // THEN
            assertThat(result).isEqualTo("\"C:\\\\Users\\\\penpot\"");
        }

        @Test
        @DisplayName("escapeJson — escapes newline characters in the input string")
        void escapeJson_escapesNewlines() {
            // GIVEN

            // WHEN
            String result = JsonUtils.escapeJson("line1\nline2");

            // THEN
            assertThat(result).isEqualTo("\"line1\\nline2\"");
        }

        @Test
        @DisplayName("escapeJson — escapes tab characters in the input string")
        void escapeJson_escapesTabs() {
            // GIVEN

            // WHEN
            String result = JsonUtils.escapeJson("col1\tcol2");

            // THEN
            assertThat(result).isEqualTo("\"col1\\tcol2\"");
        }

        @Test
        @DisplayName("escapeJson — handles unicode characters without error")
        void escapeJson_handlesUnicodeCharacters() {
            // GIVEN

            // WHEN
            String result = JsonUtils.escapeJson("émoji 🎨");

            // THEN
            assertThat(result).isNotNull().startsWith("\"").endsWith("\"");
        }
    }

    @Nested
    @DisplayName("isValidJson")
    class IsValidJsonTests {

        @ParameterizedTest
        @MethodSource("invalidJsonInputs")
        @DisplayName("isValidJson — returns false for invalid inputs")
        void isValidJson_returnsFalseForInvalidInputs(String input) {
            // GIVEN

            // WHEN
            boolean result = JsonUtils.isValidJson(input);

            // THEN
            assertThat(result).isFalse();
        }

        static Stream<String> invalidJsonInputs() {
            return Stream.of(
                null,
                "   ",
                "",
                "not json at all",
                "{key: value}"
            );
        }

        @Test
        @DisplayName("isValidJson — returns true when input is a valid JSON object")
        void isValidJson_returnsTrueWhenInputIsValidJsonObject() {
            // GIVEN

            // WHEN
            boolean result = JsonUtils.isValidJson("{\"key\": \"value\"}");

            // THEN
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isValidJson — returns true when input is a valid JSON array")
        void isValidJson_returnsTrueWhenInputIsValidJsonArray() {
            // GIVEN

            // WHEN
            boolean result = JsonUtils.isValidJson("[1, 2, 3]");

            // THEN
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isValidJson — returns true when input is a valid JSON string primitive")
        void isValidJson_returnsTrueWhenInputIsValidJsonStringPrimitive() {
            // GIVEN

            // WHEN
            boolean result = JsonUtils.isValidJson("\"hello\"");

            // THEN
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isValidJson — returns true when input is a valid JSON numeric primitive")
        void isValidJson_returnsTrueWhenInputIsValidJsonNumber() {
            // GIVEN

            // WHEN
            boolean result = JsonUtils.isValidJson("42");

            // THEN
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isValidJson — returns true when input is an empty JSON object")
        void isValidJson_returnsTrueWhenInputIsEmptyJsonObject() {
            // GIVEN

            // WHEN
            boolean result = JsonUtils.isValidJson("{}");

            // THEN
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isValidJson — returns true when input is a nested JSON object")
        void isValidJson_returnsTrueWhenInputIsNestedJsonObject() {
            // GIVEN

            // WHEN
            boolean result = JsonUtils.isValidJson("{\"outer\": {\"inner\": [1, 2, 3]}}");

            // THEN
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("truncateForLog")
    class TruncateForLogTests {

        @Test
        @DisplayName("truncateForLog — returns \"null\" string literal when input is null")
        void truncateForLog_returnsNullLiteralWhenInputIsNull() {
            // GIVEN

            // WHEN
            String result = JsonUtils.truncateForLog(null, 50);

            // THEN
            assertThat(result).isEqualTo("null");
        }

        @Test
        @DisplayName("truncateForLog — returns the original string when length is within maxLength")
        void truncateForLog_returnsOriginalStringWhenWithinMaxLength() {
            // GIVEN
            String json = "{\"key\": \"value\"}";

            // WHEN
            String result = JsonUtils.truncateForLog(json, 100);

            // THEN
            assertThat(result).isEqualTo(json);
        }

        @Test
        @DisplayName("truncateForLog — returns the original string when length is exactly at maxLength")
        void truncateForLog_returnsOriginalStringWhenExactlyAtMaxLength() {
            // GIVEN
            String json = "1234567890";

            // WHEN
            String result = JsonUtils.truncateForLog(json, 10);

            // THEN
            assertThat(result).isEqualTo(json);
        }

        @Test
        @DisplayName("truncateForLog — truncates and appends suffix when length exceeds maxLength")
        void truncateForLog_truncatesAndAppendsSuffixWhenExceedsMaxLength() {
            // GIVEN
            String json = "abcdefghij";

            // WHEN
            String result = JsonUtils.truncateForLog(json, 5);

            // THEN
            assertThat(result).isEqualTo("abcde... (truncated)");
        }

        @Test
        @DisplayName("truncateForLog — truncated result starts with the correct prefix of the input")
        void truncateForLog_truncatedResultStartsWithCorrectPrefix() {
            // GIVEN
            String json = "{\"key\": \"a very long value that should be cut\"}";

            // WHEN
            String result = JsonUtils.truncateForLog(json, 10);

            // THEN
            assertThat(result).startsWith("{\"key\": \"a").endsWith("... (truncated)");
        }

        @Test
        @DisplayName("truncateForLog — returns empty string as-is when maxLength is zero and input is empty")
        void truncateForLog_returnsEmptyStringAsIsWhenInputIsEmpty() {
            // GIVEN

            // WHEN
            String result = JsonUtils.truncateForLog("", 10);

            // THEN
            assertThat(result).isEmpty();
        }
    }
}