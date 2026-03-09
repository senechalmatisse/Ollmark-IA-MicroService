package com.penpot.ai.shared.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exception — Unit")
public class PenpotAiExceptionUnit {

    @Nested
    @DisplayName("PenpotAiException")
    class PenpotAiExceptionTests {

        @Test
        @DisplayName("PenpotAiException(message) — getMessage returns the provided message")
        void constructor_message_getMessageReturnsProvidedMessage() {
            // GIVEN
            String message = "base error";

            // WHEN
            PenpotAiException ex = new PenpotAiException(message);

            // THEN
            assertThat(ex.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("PenpotAiException(message) — getCause returns null when no cause is provided")
        void constructor_message_getCauseReturnsNullWhenNoCauseProvided() {
            // GIVEN

            // WHEN
            PenpotAiException ex = new PenpotAiException("base error");

            // THEN
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("PenpotAiException(message, cause) — getMessage returns the provided message")
        void constructor_messageAndCause_getMessageReturnsProvidedMessage() {
            // GIVEN
            String message = "base error with cause";
            Throwable cause = new RuntimeException("root cause");

            // WHEN
            PenpotAiException ex = new PenpotAiException(message, cause);

            // THEN
            assertThat(ex.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("PenpotAiException(message, cause) — getCause returns the provided cause")
        void constructor_messageAndCause_getCauseReturnsProvidedCause() {
            // GIVEN
            Throwable cause = new RuntimeException("root cause");

            // WHEN
            PenpotAiException ex = new PenpotAiException("base error with cause", cause);

            // THEN
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("PenpotAiException — is a RuntimeException")
        void isARuntimeException() {
            // GIVEN

            // WHEN
            PenpotAiException ex = new PenpotAiException("base error");

            // THEN
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("ValidationException")
    class ValidationExceptionTests {

        @Test
        @DisplayName("ValidationException(message) — getMessage returns the provided message")
        void constructor_message_getMessageReturnsProvidedMessage() {
            // GIVEN
            String message = "validation failed";

            // WHEN
            ValidationException ex = new ValidationException(message);

            // THEN
            assertThat(ex.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("ValidationException(message) — getCause returns null")
        void constructor_message_getCauseReturnsNull() {
            // GIVEN

            // WHEN
            ValidationException ex = new ValidationException("validation failed");

            // THEN
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("ValidationException — is a PenpotAiException")
        void isAPenpotAiException() {
            // GIVEN

            // WHEN
            ValidationException ex = new ValidationException("validation failed");

            // THEN
            assertThat(ex).isInstanceOf(PenpotAiException.class);
        }

        @Test
        @DisplayName("ValidationException — is a RuntimeException")
        void isARuntimeException() {
            // GIVEN

            // WHEN
            ValidationException ex = new ValidationException("validation failed");

            // THEN
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    // =========================================================================
    // ToolExecutionException
    // =========================================================================

    @Nested
    @DisplayName("ToolExecutionException")
    class ToolExecutionExceptionTests {

        @Test
        @DisplayName("ToolExecutionException(message, cause) — getMessage returns the provided message")
        void constructor_messageAndCause_getMessageReturnsProvidedMessage() {
            // GIVEN
            String message = "tool execution failed";
            Throwable cause = new IllegalStateException("underlying error");

            // WHEN
            ToolExecutionException ex = new ToolExecutionException(message, cause);

            // THEN
            assertThat(ex.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("ToolExecutionException(message, cause) — getCause returns the provided cause")
        void constructor_messageAndCause_getCauseReturnsProvidedCause() {
            // GIVEN
            Throwable cause = new IllegalStateException("underlying error");

            // WHEN
            ToolExecutionException ex = new ToolExecutionException("tool execution failed", cause);

            // THEN
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("ToolExecutionException — is a PenpotAiException")
        void isAPenpotAiException() {
            // GIVEN

            // WHEN
            ToolExecutionException ex = new ToolExecutionException("tool execution failed", new RuntimeException());

            // THEN
            assertThat(ex).isInstanceOf(PenpotAiException.class);
        }
    }

    @Nested
    @DisplayName("PluginConnectionException")
    class PluginConnectionExceptionTests {

        @Test
        @DisplayName("PluginConnectionException(message) — getMessage returns the provided message")
        void constructor_message_getMessageReturnsProvidedMessage() {
            // GIVEN
            String message = "no plugin connection available";

            // WHEN
            PluginConnectionException ex = new PluginConnectionException(message);

            // THEN
            assertThat(ex.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("PluginConnectionException(message) — getCause returns null")
        void constructor_message_getCauseReturnsNull() {
            // GIVEN

            // WHEN
            PluginConnectionException ex = new PluginConnectionException("no plugin connection available");

            // THEN
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("PluginConnectionException — is a PenpotAiException")
        void isAPenpotAiException() {
            // GIVEN

            // WHEN
            PluginConnectionException ex = new PluginConnectionException("no plugin connection available");

            // THEN
            assertThat(ex).isInstanceOf(PenpotAiException.class);
        }
    }

    @Nested
    @DisplayName("TaskExecutionException")
    class TaskExecutionExceptionTests {

        @Test
        @DisplayName("TaskExecutionException(message) — getMessage returns the provided message")
        void constructor_message_getMessageReturnsProvidedMessage() {
            // GIVEN
            String message = "task execution failed";

            // WHEN
            TaskExecutionException ex = new TaskExecutionException(message);

            // THEN
            assertThat(ex.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("TaskExecutionException(message) — getCause returns null when no cause is provided")
        void constructor_message_getCauseReturnsNullWhenNoCauseProvided() {
            // GIVEN

            // WHEN
            TaskExecutionException ex = new TaskExecutionException("task execution failed");

            // THEN
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("TaskExecutionException(message, cause) — getMessage returns the provided message")
        void constructor_messageAndCause_getMessageReturnsProvidedMessage() {
            // GIVEN
            String message = "task execution failed with cause";
            Throwable cause = new RuntimeException("root cause");

            // WHEN
            TaskExecutionException ex = new TaskExecutionException(message, cause);

            // THEN
            assertThat(ex.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("TaskExecutionException(message, cause) — getCause returns the provided cause")
        void constructor_messageAndCause_getCauseReturnsProvidedCause() {
            // GIVEN
            Throwable cause = new RuntimeException("root cause");

            // WHEN
            TaskExecutionException ex = new TaskExecutionException("task execution failed with cause", cause);

            // THEN
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("TaskExecutionException — is a PenpotAiException")
        void isAPenpotAiException() {
            // GIVEN

            // WHEN
            TaskExecutionException ex = new TaskExecutionException("task execution failed");

            // THEN
            assertThat(ex).isInstanceOf(PenpotAiException.class);
        }
    }

    @Nested
    @DisplayName("FormattingException")
    class FormattingExceptionTests {

        @Test
        @DisplayName("FormattingException(message, cause) — getMessage returns the provided message")
        void constructor_messageAndCause_getMessageReturnsProvidedMessage() {
            // GIVEN
            String message = "formatting error";
            Throwable cause = new IllegalArgumentException("parse error");

            // WHEN
            FormattingException ex = new FormattingException(message, cause);

            // THEN
            assertThat(ex.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("FormattingException(message, cause) — getCause returns the provided cause")
        void constructor_messageAndCause_getCauseReturnsProvidedCause() {
            // GIVEN
            Throwable cause = new IllegalArgumentException("parse error");

            // WHEN
            FormattingException ex = new FormattingException("formatting error", cause);

            // THEN
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("FormattingException — is a PenpotAiException")
        void isAPenpotAiException() {
            // GIVEN

            // WHEN
            FormattingException ex = new FormattingException("formatting error", new RuntimeException());

            // THEN
            assertThat(ex).isInstanceOf(PenpotAiException.class);
        }

        @Test
        @DisplayName("FormattingException — is a RuntimeException")
        void isARuntimeException() {
            // GIVEN

            // WHEN
            FormattingException ex = new FormattingException("formatting error", new RuntimeException());

            // THEN
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}