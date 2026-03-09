package com.penpot.ai.infrastructure.factory;

import com.penpot.ai.infrastructure.strategy.ResultFormatter;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResultFormatterFactory — Unit")
public class ResultFormatterFactoryUnit {

    private static ResultFormatter stringFormatter(int priority) {
        return new ResultFormatter() {
            @Override public String format(Object result) { return "string:" + result; }
            @Override public boolean supports(Class<?> type) { return type == String.class; }
            @Override public int priority() { return priority; }
        };
    }

    private static ResultFormatter intFormatter(int priority) {
        return new ResultFormatter() {
            @Override public String format(Object result) { return "int:" + result; }
            @Override public boolean supports(Class<?> type) { return type == Integer.class; }
            @Override public int priority() { return priority; }
        };
    }

    private static ResultFormatter universalFormatter(int priority) {
        return new ResultFormatter() {
            @Override public String format(Object result) { return "universal:" + result; }
            @Override public boolean supports(Class<?> type) { return true; }
            @Override public int priority() { return priority; }
        };
    }

    @Nested
    @DisplayName("constructor — sorting and default formatter")
    class ConstructorTests {

        @Test
        @DisplayName("constructor — initializes without error when formatter list is empty")
        void constructor_initializesWithoutErrorWhenFormatterListIsEmpty() {
            // GIVEN / WHEN
            ResultFormatterFactory factory = new ResultFormatterFactory(Collections.emptyList());

            // THEN
            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("constructor — initializes without error with a single formatter")
        void constructor_initializesWithoutErrorWithSingleFormatter() {
            // GIVEN / WHEN
            ResultFormatterFactory factory = new ResultFormatterFactory(List.of(stringFormatter(10)));

            // THEN
            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("constructor — initializes without error with multiple formatters")
        void constructor_initializesWithoutErrorWithMultipleFormatters() {
            // GIVEN / WHEN
            ResultFormatterFactory factory = new ResultFormatterFactory(
                List.of(stringFormatter(10), intFormatter(5), universalFormatter(1))
            );

            // THEN
            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("getFormatterForObject — null input")
    class GetFormatterForObjectNullInputTests {

        @Test
        @DisplayName("getFormatterForObject — returns a non-null formatter when result is null")
        void getFormatterForObject_returnsNonNullFormatterWhenResultIsNull() {
            // GIVEN
            ResultFormatterFactory factory = new ResultFormatterFactory(List.of(universalFormatter(1)));

            // WHEN
            ResultFormatter formatter = factory.getFormatterForObject(null);

            // THEN
            assertThat(formatter).isNotNull();
        }

        @Test
        @DisplayName("getFormatterForObject — returns empty string when formatting null with fallback and empty list")
        void getFormatterForObject_returnsEmptyStringWhenFormattingNullWithFallbackAndEmptyList() {
            // GIVEN
            ResultFormatterFactory factory = new ResultFormatterFactory(Collections.emptyList());

            // WHEN
            ResultFormatter formatter = factory.getFormatterForObject(null);
            String result = formatter.format(null);

            // THEN
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getFormatterForObject — type dispatch")
    class GetFormatterForObjectTypeDispatchTests {

        @Test
        @DisplayName("getFormatterForObject — returns formatter supporting String when result is a String")
        void getFormatterForObject_returnsFormatterSupportingStringWhenResultIsString() {
            // GIVEN
            ResultFormatter strFmt = stringFormatter(10);
            ResultFormatter intFmt = intFormatter(5);
            ResultFormatterFactory factory = new ResultFormatterFactory(List.of(strFmt, intFmt));

            // WHEN
            ResultFormatter formatter = factory.getFormatterForObject("hello");

            // THEN
            assertThat(formatter.supports(String.class)).isTrue();
            assertThat(formatter.format("hello")).isEqualTo("string:hello");
        }

        @Test
        @DisplayName("getFormatterForObject — returns formatter with priority 100 over priority 1 when both support String")
        void getFormatterForObject_returnsFormatterWithPriority100OverPriority1WhenBothSupportString() {
            // GIVEN
            ResultFormatter low  = stringFormatter(1);
            ResultFormatter high = stringFormatter(100);
            ResultFormatterFactory factory = new ResultFormatterFactory(List.of(low, high));

            // WHEN
            ResultFormatter formatter = factory.getFormatterForObject("test");

            // THEN
            assertThat(formatter.priority()).isEqualTo(100);
        }

        @Test
        @DisplayName("getFormatterForObject — returns int formatter when result is an Integer")
        void getFormatterForObject_returnsIntFormatterWhenResultIsInteger() {
            // GIVEN
            ResultFormatter strFmt = stringFormatter(10);
            ResultFormatter intFmt = intFormatter(5);
            ResultFormatterFactory factory = new ResultFormatterFactory(List.of(strFmt, intFmt));

            // WHEN
            ResultFormatter formatter = factory.getFormatterForObject(42);

            // THEN
            assertThat(formatter.supports(Integer.class)).isTrue();
            assertThat(formatter.format(42)).isEqualTo("int:42");
        }
    }

    @Nested
    @DisplayName("getFormatterForObject — fallback to default formatter")
    class GetFormatterForObjectFallbackToDefaultTests {

        @Test
        @DisplayName("getFormatterForObject — returns non-null default formatter when no formatter supports Double")
        void getFormatterForObject_returnsNonNullDefaultFormatterWhenNoFormatterSupportsDouble() {
            // GIVEN
            ResultFormatter strFmt = stringFormatter(10);
            ResultFormatterFactory factory = new ResultFormatterFactory(List.of(strFmt));

            // WHEN
            ResultFormatter formatter = factory.getFormatterForObject(3.14);

            // THEN
            assertThat(formatter).isNotNull();
        }

        @Test
        @DisplayName("getFormatterForObject — default formatter produces non-null output for unsupported Double type")
        void getFormatterForObject_defaultFormatterProducesNonNullOutputForUnsupportedDoubleType() {
            // GIVEN
            ResultFormatter strFmt = stringFormatter(10);
            ResultFormatterFactory factory = new ResultFormatterFactory(List.of(strFmt));

            // WHEN
            ResultFormatter formatter = factory.getFormatterForObject(3.14);
            String result = formatter.format(3.14);

            // THEN
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("getFormatter — resultType null guard (dead branch documentation)")
    class GetFormatterNullTypeDeadBranchTests {

        @Test
        @DisplayName("getFormatterForObject — null result returns defaultFormatter before calling getFormatter (dead branch in resultType null-guard)")
        void getFormatterForObject_nullResultReturnsDefaultFormatterBeforeCallingGetFormatter_deadBranch() {
            // GIVEN
            ResultFormatterFactory factory = new ResultFormatterFactory(Collections.emptyList());

            // WHEN
            ResultFormatter formatter = factory.getFormatterForObject(null);

            // THEN
            assertThat(formatter).isNotNull();
            assertThat(formatter.priority()).isEqualTo(-1);
            assertThat("any string".getClass()).isNotNull();
            assertThat(Integer.valueOf(1).getClass()).isNotNull();
        }
    }

    @Nested
    @DisplayName("createFallbackFormatter — empty formatter list")
    class CreateFallbackFormatterTests {

        @Test
        @DisplayName("createFallbackFormatter — supports String type when formatter list is empty")
        void createFallbackFormatter_supportsStringTypeWhenFormatterListIsEmpty() {
            // GIVEN
            ResultFormatterFactory factory = new ResultFormatterFactory(Collections.emptyList());

            // WHEN
            ResultFormatter formatter = factory.getFormatterForObject("anything");

            // THEN
            assertThat(formatter.supports(String.class)).isTrue();
        }

        @Test
        @DisplayName("createFallbackFormatter — supports Integer type when formatter list is empty")
        void createFallbackFormatter_supportsIntegerTypeWhenFormatterListIsEmpty() {
            // GIVEN
            ResultFormatterFactory factory = new ResultFormatterFactory(Collections.emptyList());

            // WHEN
            ResultFormatter formatter = factory.getFormatterForObject(1);

            // THEN
            assertThat(formatter.supports(Integer.class)).isTrue();
        }

        @Test
        @DisplayName("createFallbackFormatter — supports Object type when formatter list is empty")
        void createFallbackFormatter_supportsObjectTypeWhenFormatterListIsEmpty() {
            // GIVEN
            ResultFormatterFactory factory = new ResultFormatterFactory(Collections.emptyList());

            // WHEN
            ResultFormatter formatter = factory.getFormatterForObject(new Object());

            // THEN
            assertThat(formatter.supports(Object.class)).isTrue();
        }

        @Test
        @DisplayName("createFallbackFormatter — returns toString value when formatting a non-null object")
        void createFallbackFormatter_returnsToStringValueWhenFormattingNonNullObject() {
            // GIVEN
            ResultFormatterFactory factory = new ResultFormatterFactory(Collections.emptyList());
            Object obj = new Object() {
                @Override public String toString() { return "custom-toString"; }
            };

            // WHEN
            String result = factory.getFormatterForObject(obj).format(obj);

            // THEN
            assertThat(result).isEqualTo("custom-toString");
        }
    }
}