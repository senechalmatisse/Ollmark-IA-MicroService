package com.penpot.ai.infrastructure.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodeExecutionResultFormatter — Unit")
public class CodeExecutionResultFormatterUnit {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CodeExecutionResultFormatter formatter;

    @Nested
    @DisplayName("format — non-Map input")
    class FormatNonMapTests {

        @Test
        @DisplayName("format — returns toString when result is not a Map")
        void format_returnsToStringWhenResultIsNotAMap() {
            // GIVEN
            String result = "plain string";

            // WHEN
            String output = formatter.format(result);

            // THEN
            assertThat(output).isEqualTo("plain string");
        }

        @Test
        @DisplayName("format — returns toString when result is a List")
        void format_returnsToStringWhenResultIsAList() {
            // GIVEN
            List<String> result = List.of("a", "b");

            // WHEN
            String output = formatter.format(result);

            // THEN
            assertThat(output).isEqualTo(result.toString());
        }

        @Test
        @DisplayName("format — returns toString when result is an Integer")
        void format_returnsToStringWhenResultIsAnInteger() {
            // GIVEN
            Integer result = 99;

            // WHEN
            String output = formatter.format(result);

            // THEN
            assertThat(output).isEqualTo("99");
        }
    }

    @Nested
    @DisplayName("format — Map with result and log keys")
    class FormatMapWithResultAndLogTests {

        @Test
        @DisplayName("format — output starts with '{' and ends with '}' for a Map input")
        void format_outputStartsWithBraceAndEndsWithBraceForMapInput() {
            // GIVEN
            Map<String, Object> data = Map.of("result", "ok");

            // WHEN
            String output = formatter.format(data);

            // THEN
            assertThat(output).startsWith("{").endsWith("}");
        }

        @Test
        @DisplayName("format — includes 'result' field when Map contains 'result' key with String value")
        void format_includesResultFieldWhenMapContainsResultKeyWithStringValue() {
            // GIVEN
            Map<String, Object> data = Map.of("result", "execution-output");

            // WHEN
            String output = formatter.format(data);

            // THEN
            assertThat(output).contains("\"result\"");
            assertThat(output).contains("execution-output");
        }

        @Test
        @DisplayName("format — includes 'log' field when Map contains both 'result' and non-null 'log'")
        void format_includesLogFieldWhenMapContainsBothResultAndNonNullLog() {
            // GIVEN
            Map<String, Object> data = new HashMap<>();
            data.put("result", "ok");
            data.put("log", "some log line");

            // WHEN
            String output = formatter.format(data);

            // THEN
            assertThat(output).contains("\"log\"");
            assertThat(output).contains("some log line");
        }

        @Test
        @DisplayName("format — omits 'log' field when Map contains 'log' key with null value")
        void format_omitsLogFieldWhenMapContainsLogKeyWithNullValue() {
            // GIVEN
            Map<String, Object> data = new HashMap<>();
            data.put("result", "ok");
            data.put("log", null);

            // WHEN
            String output = formatter.format(data);

            // THEN
            assertThat(output).doesNotContain("\"log\"");
        }

        @Test
        @DisplayName("format — produces output without 'result' field when Map does not contain 'result' key")
        void format_producesOutputWithoutResultFieldWhenMapDoesNotContainResultKey() {
            // GIVEN
            Map<String, Object> data = new HashMap<>();
            data.put("log", "just a log");

            // WHEN
            String output = formatter.format(data);

            // THEN
            assertThat(output).doesNotContain("\"result\"");
            assertThat(output).contains("\"log\"");
        }

        @Test
        @DisplayName("format — produces valid structure when Map is empty")
        void format_producesValidStructureWhenMapIsEmpty() {
            // GIVEN
            Map<String, Object> data = new HashMap<>();

            // WHEN
            String output = formatter.format(data);

            // THEN
            assertThat(output).startsWith("{").endsWith("}");
        }
    }

    @Nested
    @DisplayName("format — formatValue for Number and Boolean result values")
    class FormatValueNumberAndBooleanTests {

        @Test
        @DisplayName("format — renders Integer result value as plain number without quotes")
        void format_rendersIntegerResultValueAsPlainNumberWithoutQuotes() {
            // GIVEN
            Map<String, Object> data = Map.of("result", 42);

            // WHEN
            String output = formatter.format(data);

            // THEN
            assertThat(output).contains("\"result\": 42");
        }

        @Test
        @DisplayName("format — renders Boolean result value as plain boolean without quotes")
        void format_rendersBooleanResultValueAsPlainBooleanWithoutQuotes() {
            // GIVEN
            Map<String, Object> data = Map.of("result", true);

            // WHEN
            String output = formatter.format(data);

            // THEN
            assertThat(output).contains("\"result\": true");
        }
    }

    @Nested
    @DisplayName("format — formatValue for null result value")
    class FormatValueNullTests {

        @Test
        @DisplayName("format — renders null result value as 'null' literal")
        void format_rendersNullResultValueAsNullLiteral() {
            // GIVEN
            Map<String, Object> data = new HashMap<>();
            data.put("result", null);

            // WHEN
            String output = formatter.format(data);

            // THEN
            assertThat(output).contains("\"result\": null");
        }
    }

    @Nested
    @DisplayName("format — formatValue delegates to ObjectMapper for complex objects")
    class FormatValueComplexObjectTests {

        @Test
        @DisplayName("format — uses ObjectMapper to serialize nested Map result value")
        void format_usesObjectMapperToSerializeNestedMapResultValue()
                throws JsonProcessingException {
            // GIVEN
            Map<String, Object> nested = Map.of("inner", "value");
            Map<String, Object> data = Map.of("result", nested);
            when(objectMapper.writeValueAsString(nested)).thenReturn("{\"inner\":\"value\"}");

            // WHEN
            String output = formatter.format(data);

            // THEN
            verify(objectMapper).writeValueAsString(nested);
            assertThat(output).contains("{\"inner\":\"value\"}");
        }

        @Test
        @DisplayName("format — falls back to escapeJson when ObjectMapper throws JsonProcessingException for complex value")
        void format_fallsBackToEscapeJsonWhenObjectMapperThrowsJsonProcessingExceptionForComplexValue()
                throws JsonProcessingException {
            // GIVEN
            Map<String, Object> nested = Map.of("inner", "value");
            Map<String, Object> data = Map.of("result", nested);
            when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("forced") {});

            // WHEN
            String output = formatter.format(data);

            // THEN
            assertThat(output).isNotNull();
            assertThat(output).contains("\"result\"");
        }
    }

    @Nested
    @DisplayName("supports")
    class SupportsTests {

        @Test
        @DisplayName("supports — returns true for Map type")
        void supports_returnsTrueForMapType() {
            // GIVEN / WHEN / THEN
            assertThat(formatter.supports(Map.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("priority")
    class PriorityTests {

        @Test
        @DisplayName("priority — returns 15 (highest, takes precedence over JsonResultFormatter at 10)")
        void priority_returnsFifteen() {
            // GIVEN / WHEN
            int priority = formatter.priority();

            // THEN
            assertThat(priority).isEqualTo(15);
        }
    }
}