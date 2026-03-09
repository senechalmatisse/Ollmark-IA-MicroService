package com.penpot.ai.infrastructure.strategy;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultResultFormatter — Unit")
public class DefaultResultFormatterUnit {

    private DefaultResultFormatter formatter;

    @BeforeEach
    void setUp() { formatter = new DefaultResultFormatter(); }

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
        @DisplayName("format — returns toString value when result is a String")
        void format_returnsToStringValueWhenResultIsString() {
            // GIVEN
            String result = "hello world";

            // WHEN
            String output = formatter.format(result);

            // THEN
            assertThat(output).isEqualTo("hello world");
        }
    }

    @Nested
    @DisplayName("supports")
    class SupportsTests {

        @Test
        @DisplayName("supports — returns true for Object type")
        void supports_returnsTrueForObjectType() {
            // GIVEN / WHEN / THEN
            assertThat(formatter.supports(Object.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("priority")
    class PriorityTests {

        @Test
        @DisplayName("priority — returns 0 (lowest, used as final fallback)")
        void priority_returnsZero() {
            // GIVEN / WHEN
            int priority = formatter.priority();

            // THEN
            assertThat(priority).isZero();
        }
    }
}