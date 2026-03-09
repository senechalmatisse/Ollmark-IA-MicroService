package com.penpot.ai.infrastructure.strategy;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TextResultFormatter — Unit")
public class TextResultFormatterUnit {

    private TextResultFormatter formatter;

    @BeforeEach
    void setUp() { formatter = new TextResultFormatter(); }

    @Nested
    @DisplayName("format")
    class FormatTests {

        @Test
        @DisplayName("format — returns empty string when result is null")
        void format_returnsEmptyStringWhenResultIsNull() {
            // GIVEN
            Object result = null;

            // WHEN
            String output = formatter.format(result);

            // THEN
            assertThat(output).isEmpty();
        }

        @Test
        @DisplayName("format — returns the string value unchanged when result is a String")
        void format_returnsStringValueUnchangedWhenResultIsString() {
            // GIVEN
            String result = "hello";

            // WHEN
            String output = formatter.format(result);

            // THEN
            assertThat(output).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("supports")
    class SupportsTests {

        @Test
        @DisplayName("supports — returns true for String type")
        void supports_returnsTrueForStringType() {
            // GIVEN / WHEN / THEN
            assertThat(formatter.supports(String.class)).isTrue();
        }

        @Test
        @DisplayName("supports — returns true for Integer type (subclass of Number)")
        void supports_returnsTrueForIntegerTypeAsSubclassOfNumber() {
            // GIVEN / WHEN / THEN
            assertThat(formatter.supports(Integer.class)).isTrue();
        }

        @Test
        @DisplayName("supports — returns true for Boolean type")
        void supports_returnsTrueForBooleanType() {
            // GIVEN / WHEN / THEN
            assertThat(formatter.supports(Boolean.class)).isTrue();
        }

        @Test
        @DisplayName("supports — returns true for int primitive type")
        void supports_returnsTrueForIntPrimitiveType() {
            // GIVEN / WHEN / THEN
            assertThat(formatter.supports(int.class)).isTrue();
        }

        @Test
        @DisplayName("supports — returns false for Object type")
        void supports_returnsFalseForObjectType() {
            // GIVEN / WHEN / THEN
            assertThat(formatter.supports(Object.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("priority")
    class PriorityTests {

        @Test
        @DisplayName("priority — returns 5")
        void priority_returnsFive() {
            // GIVEN / WHEN
            int priority = formatter.priority();

            // THEN
            assertThat(priority).isEqualTo(5);
        }
    }
}