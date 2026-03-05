package com.penpot.ai.shared.util;

import com.penpot.ai.shared.exception.ValidationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ValidationUtils — Unit")
public class ValidationUtilsUnit {

    @Nested
    @DisplayName("requireNonBlank")
    class RequireNonBlankTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("requireNonBlank — throws ValidationException when value is null, empty or blank")
        void requireNonBlank_throwsValidationExceptionWhenValueIsNullOrEmptyOrBlank(String value) {
            // GIVEN
            String fieldName = "username";

            // WHEN / THEN
            assertThatThrownBy(() -> ValidationUtils.requireNonBlank(value, fieldName))
                .isInstanceOf(ValidationException.class)
                .hasMessage("username cannot be null or empty");
        }

        @Test
        @DisplayName("requireNonBlank — throws ValidationException with fieldName interpolated in message")
        void requireNonBlank_throwsValidationExceptionWithFieldNameInMessage() {
            // GIVEN
            String fieldName = "projectId";

            // WHEN / THEN
            assertThatThrownBy(() -> ValidationUtils.requireNonBlank(null, fieldName))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("projectId");
        }

        @Test
        @DisplayName("requireNonBlank — does not throw when value is a valid non-blank string")
        void requireNonBlank_doesNotThrowWhenValueIsValid() {
            // GIVEN
            String value     = "valid-value";
            String fieldName = "username";

            // WHEN / THEN
            assertThatNoException()
                .isThrownBy(() -> ValidationUtils.requireNonBlank(value, fieldName));
        }

        @Test
        @DisplayName("requireNonBlank — does not throw when value contains only non-space characters")
        void requireNonBlank_doesNotThrowWhenValueIsSingleCharacter() {
            // GIVEN / WHEN / THEN
            assertThatNoException()
                .isThrownBy(() -> ValidationUtils.requireNonBlank("x", "field"));
        }
    }

    @Nested
    @DisplayName("validateString")
    class ValidateStringTests {

        @Test
        @DisplayName("validateString — throws ValidationException when value is null")
        void validateString_throwsValidationExceptionWhenValueIsNull() {
            // GIVEN / WHEN / THEN
            assertThatThrownBy(() -> ValidationUtils.validateString(null, "description", 100))
                .isInstanceOf(ValidationException.class)
                .hasMessage("description cannot be null or empty");
        }

        @Test
        @DisplayName("validateString — throws ValidationException when value is blank")
        void validateString_throwsValidationExceptionWhenValueIsBlank() {
            // GIVEN / WHEN / THEN
            assertThatThrownBy(() -> ValidationUtils.validateString("  ", "description", 100))
                .isInstanceOf(ValidationException.class)
                .hasMessage("description cannot be null or empty");
        }

        @Test
        @DisplayName("validateString — throws ValidationException when value exceeds maxLength")
        void validateString_throwsValidationExceptionWhenValueExceedsMaxLength() {
            // GIVEN
            String value     = "a".repeat(11);
            String fieldName = "code";
            int    maxLength = 10;

            // WHEN / THEN
            assertThatThrownBy(() -> ValidationUtils.validateString(value, fieldName, maxLength))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("code too long")
                .hasMessageContaining("11 characters")
                .hasMessageContaining("max 10");
        }

        @Test
        @DisplayName("validateString — throws ValidationException with exact character count in message when value exceeds maxLength")
        void validateString_throwsValidationExceptionWithExactCountInMessage() {
            // GIVEN
            String value = "a".repeat(25);

            // WHEN / THEN
            assertThatThrownBy(() -> ValidationUtils.validateString(value, "tag", 20))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("25 characters (max 20)");
        }

        @Test
        @DisplayName("validateString — does not throw when value is exactly at maxLength boundary")
        void validateString_doesNotThrowWhenValueIsExactlyAtMaxLength() {
            // GIVEN
            String value = "a".repeat(10);

            // WHEN / THEN
            assertThatNoException()
                .isThrownBy(() -> ValidationUtils.validateString(value, "code", 10));
        }

        @Test
        @DisplayName("validateString — does not throw when maxLength is zero (no length limit)")
        void validateString_doesNotThrowWhenMaxLengthIsZero() {
            // GIVEN
            String value = "a".repeat(10_000);

            // WHEN / THEN
            assertThatNoException()
                .isThrownBy(() -> ValidationUtils.validateString(value, "content", 0));
        }

        @Test
        @DisplayName("validateString — does not throw when value is valid and within maxLength")
        void validateString_doesNotThrowWhenValueIsValidAndWithinMaxLength() {
            // GIVEN / WHEN / THEN
            assertThatNoException()
                .isThrownBy(() -> ValidationUtils.validateString("hello", "greeting", 10));
        }

        @Test
        @DisplayName("validateString — null value is rejected by requireNonBlank before reaching the null-guard in requireMaxLength (dead branch)")
        void validateString_nullIsRejectedBeforeRequireMaxLength_deadBranch() {
            // GIVEN

            // WHEN / THEN
            assertThatThrownBy(() -> ValidationUtils.validateString(null, "field", 10))
                .isInstanceOf(ValidationException.class)
                .hasMessage("field cannot be null or empty");

            assertThatThrownBy(() -> ValidationUtils.validateString(null, "field", 0))
                .isInstanceOf(ValidationException.class)
                .hasMessage("field cannot be null or empty");
        }
    }
}