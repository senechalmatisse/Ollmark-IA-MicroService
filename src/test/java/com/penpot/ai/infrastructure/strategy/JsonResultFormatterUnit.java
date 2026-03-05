package com.penpot.ai.infrastructure.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JsonResultFormatter — Unit")
public class JsonResultFormatterUnit {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JsonResultFormatter formatter;

    @Nested
    @DisplayName("format — null input")
    class FormatNullTests {

        @Test
        @DisplayName("format — returns 'null' string literal when result is null")
        void format_returnsNullLiteralWhenResultIsNull() {
            // GIVEN
            Object result = null;

            // WHEN
            String output = formatter.format(result);

            // THEN
            assertThat(output).isEqualTo("null");
        }
    }

    @Nested
    @DisplayName("format — successful serialization")
    class FormatSuccessTests {

        @Test
        @DisplayName("format — returns pretty-printed JSON when ObjectMapper serializes a Map successfully")
        void format_returnsPrettyPrintedJsonWhenObjectMapperSerializesMapSuccessfully()
                throws JsonProcessingException {
            // GIVEN
            Map<String, Object> result = Map.of("key", "value");
            ObjectWriter writerMock = mock(ObjectWriter.class);
            when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(writerMock);
            when(writerMock.writeValueAsString(result)).thenReturn("{\n  \"key\" : \"value\"\n}");

            // WHEN
            String output = formatter.format(result);

            // THEN
            assertThat(output).isEqualTo("{\n  \"key\" : \"value\"\n}");
        }

    }

    @Nested
    @DisplayName("format — JsonProcessingException fallback")
    class FormatFallbackTests {

        @Test
        @DisplayName("format — returns toString value when ObjectMapper throws JsonProcessingException")
        void format_returnsToStringValueWhenObjectMapperThrowsJsonProcessingException()
                throws JsonProcessingException {
            // GIVEN
            Object result = new Object() {
                @Override public String toString() { return "fallback-value"; }
            };
            ObjectWriter writerMock = mock(ObjectWriter.class);
            when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(writerMock);
            when(writerMock.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("forced failure") {});

            // WHEN
            String output = formatter.format(result);

            // THEN
            assertThat(output).isEqualTo("fallback-value");
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

        @Test
        @DisplayName("supports — returns true for List type")
        void supports_returnsTrueForListType() {
            // GIVEN / WHEN / THEN
            assertThat(formatter.supports(List.class)).isTrue();
        }

        @Test
        @DisplayName("supports — returns true for class whose name starts with 'com.penpot'")
        void supports_returnsTrueForClassWhoseNameStartsWithComPenpot() {
            // GIVEN
            Class<?> penpotClass = com.penpot.ai.core.domain.Task.class;

            // WHEN / THEN
            assertThat(formatter.supports(penpotClass)).isTrue();
        }

        @Test
        @DisplayName("supports — returns false for String type")
        void supports_returnsFalseForStringType() {
            // GIVEN / WHEN / THEN
            assertThat(formatter.supports(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("priority")
    class PriorityTests {

        @Test
        @DisplayName("priority — returns 10")
        void priority_returnsTen() {
            // GIVEN / WHEN
            int priority = formatter.priority();

            // THEN
            assertThat(priority).isEqualTo(10);
        }
    }
}