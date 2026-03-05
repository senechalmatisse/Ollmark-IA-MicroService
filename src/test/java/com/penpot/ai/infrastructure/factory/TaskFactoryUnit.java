package com.penpot.ai.infrastructure.factory;

import com.penpot.ai.core.domain.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskFactory — Unit")
public class TaskFactoryUnit {

    @InjectMocks
    private TaskFactory taskFactory;

    @Nested
    @DisplayName("createExecuteCodeTask — task structure")
    class CreateExecuteCodeTaskStructureTests {

        @Test
        @DisplayName("createExecuteCodeTask — returns task with type EXECUTE_CODE")
        void createExecuteCodeTask_returnsTaskWithTypeExecuteCode() {
            // GIVEN
            String code = "penpot.getPage();";

            // WHEN
            Task task = taskFactory.createExecuteCodeTask(code, null);

            // THEN
            assertThat(task.getType()).isEqualTo(TaskType.EXECUTE_CODE);
        }
    }

    @Nested
    @DisplayName("validateCode — null and blank inputs")
    class ValidateCodeNullAndBlankTests {

        @Test
        @DisplayName("createExecuteCodeTask — throws IllegalArgumentException with message 'Code cannot be null or empty' when code is null")
        void createExecuteCodeTask_throwsIllegalArgumentExceptionWhenCodeIsNull() {
            // GIVEN
            String code = null;

            // WHEN / THEN
            assertThatThrownBy(() -> taskFactory.createExecuteCodeTask(code, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Code cannot be null or empty");
        }

        @Test
        @DisplayName("createExecuteCodeTask — throws IllegalArgumentException with message 'Code cannot be null or empty' when code is empty")
        void createExecuteCodeTask_throwsIllegalArgumentExceptionWhenCodeIsEmpty() {
            // GIVEN
            String code = "";

            // WHEN / THEN
            assertThatThrownBy(() -> taskFactory.createExecuteCodeTask(code, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Code cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("validateCode — length limit")
    class ValidateCodeLengthTests {

        @Test
        @DisplayName("createExecuteCodeTask — throws IllegalArgumentException when code is 100001 characters (exceeds 100000 limit)")
        void createExecuteCodeTask_throwsIllegalArgumentExceptionWhenCodeIs100001Characters() {
            // GIVEN
            String tooLongCode = "a".repeat(100_001);

            // WHEN / THEN
            assertThatThrownBy(() -> taskFactory.createExecuteCodeTask(tooLongCode, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Code is too long (max 100,000 characters)");
        }
    }
}