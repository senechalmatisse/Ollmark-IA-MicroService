package com.penpot.ai.core.domain;

import org.junit.jupiter.api.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Core Domain — Unit")
public class CoreDomainUnit {

    @Nested @DisplayName("TaskType")
    class TaskTypeTests {

        @Test @DisplayName("getTaskName — returns 'executeCode' for EXECUTE_CODE")
        void getTaskName_returnsExecuteCodeForExecuteCode() {
            assertThat(TaskType.EXECUTE_CODE.getTaskName()).isEqualTo("executeCode");
        }

        @Test @DisplayName("getTaskName — returns 'fetchStructure' for FETCH_STRUCTURE")
        void getTaskName_returnsFetchStructureForFetchStructure() {
            assertThat(TaskType.FETCH_STRUCTURE.getTaskName()).isEqualTo("fetchStructure");
        }

        @Test @DisplayName("getTaskName — returns 'modifyShape' for MODIFY_SHAPE")
        void getTaskName_returnsModifyShapeForModifyShape() {
            assertThat(TaskType.MODIFY_SHAPE.getTaskName()).isEqualTo("modifyShape");
        }

        @Test @DisplayName("getTaskName — returns 'createElement' for CREATE_ELEMENT")
        void getTaskName_returnsCreateElementForCreateElement() {
            assertThat(TaskType.CREATE_ELEMENT.getTaskName()).isEqualTo("createElement");
        }

        @Test @DisplayName("values — enum contains exactly 4 values")
        void values_enumContainsExactlyFourValues() {
            assertThat(TaskType.values()).hasSize(4);
        }

        @Nested @DisplayName("fromString")
        class FromStringTests {

            @Test @DisplayName("fromString — returns EXECUTE_CODE for 'executeCode'")
            void fromString_returnsExecuteCodeForExecuteCode() {
                // GIVEN / WHEN
                TaskType result = TaskType.fromString("executeCode");

                // THEN
                assertThat(result).isEqualTo(TaskType.EXECUTE_CODE);
            }

            @Test @DisplayName("fromString — returns FETCH_STRUCTURE for 'fetchStructure'")
            void fromString_returnsFetchStructureForFetchStructure() {
                // GIVEN / WHEN
                TaskType result = TaskType.fromString("fetchStructure");

                // THEN
                assertThat(result).isEqualTo(TaskType.FETCH_STRUCTURE);
            }

            @Test @DisplayName("fromString — returns MODIFY_SHAPE for 'modifyShape'")
            void fromString_returnsModifyShapeForModifyShape() {
                // GIVEN / WHEN
                TaskType result = TaskType.fromString("modifyShape");

                // THEN
                assertThat(result).isEqualTo(TaskType.MODIFY_SHAPE);
            }

            @Test @DisplayName("fromString — returns CREATE_ELEMENT for 'createElement'")
            void fromString_returnsCreateElementForCreateElement() {
                // GIVEN / WHEN
                TaskType result = TaskType.fromString("createElement");

                // THEN
                assertThat(result).isEqualTo(TaskType.CREATE_ELEMENT);
            }

            @Test @DisplayName("fromString — throws IllegalArgumentException for unknown taskName")
            void fromString_throwsIllegalArgumentExceptionForUnknownTaskName() {
                // GIVEN
                String unknown = "unknownTask";

                // WHEN / THEN
                assertThatThrownBy(() -> TaskType.fromString(unknown))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Unknown task type: unknownTask");
            }

            @Test @DisplayName("fromString — throws IllegalArgumentException when taskName is null")
            void fromString_throwsWhenTaskNameIsNull() {
                // GIVEN / WHEN / THEN
                assertThatThrownBy(() -> TaskType.fromString(null))
                    .isInstanceOf(IllegalArgumentException.class);
            }

            @Test @DisplayName("fromString — throws IllegalArgumentException when taskName is empty")
            void fromString_throwsWhenTaskNameIsEmpty() {
                // GIVEN / WHEN / THEN
                assertThatThrownBy(() -> TaskType.fromString(""))
                    .isInstanceOf(IllegalArgumentException.class);
            }

            @Test @DisplayName("fromString — is case-sensitive: 'ExecuteCode' does not match 'executeCode'")
            void fromString_isCaseSensitive() {
                // GIVEN / WHEN / THEN
                assertThatThrownBy(() -> TaskType.fromString("ExecuteCode"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown task type: ExecuteCode");
            }
        }
    }

    @Nested @DisplayName("DesignPlan")
    class DesignPlanTests {

        @Nested @DisplayName("explain factory method")
        class ExplainTests {

            @Test @DisplayName("explain — returns plan with action 'explain'")
            void explain_returnsPlanWithActionExplain() {
                // GIVEN / WHEN
                DesignPlan plan = DesignPlan.explain("msg");

                // THEN
                assertThat(plan.action()).isEqualTo("explain");
            }

            @Test @DisplayName("explain — returns plan with complexity 'simple'")
            void explain_returnsPlanWithComplexitySimple() {
                // GIVEN / WHEN
                DesignPlan plan = DesignPlan.explain("msg");

                // THEN
                assertThat(plan.complexity()).isEqualTo("simple");
            }

            @Test @DisplayName("explain — returns plan with null templateId")
            void explain_returnsPlanWithNullTemplateId() {
                // GIVEN / WHEN
                DesignPlan plan = DesignPlan.explain("msg");

                // THEN
                assertThat(plan.templateId()).isNull();
            }

            @Test @DisplayName("explain — returns plan with empty shapes list")
            void explain_returnsPlanWithEmptyShapesList() {
                // GIVEN / WHEN
                DesignPlan plan = DesignPlan.explain("msg");

                // THEN
                assertThat(plan.shapes()).isEmpty();
            }

            @Test @DisplayName("explain — returns plan with empty globalParameters map")
            void explain_returnsPlanWithEmptyGlobalParametersMap() {
                // GIVEN / WHEN
                DesignPlan plan = DesignPlan.explain("msg");

                // THEN
                assertThat(plan.globalParameters()).isEmpty();
            }

            @Test @DisplayName("explain — returns plan with userFacingMessage equal to provided message")
            void explain_returnsPlanWithUserFacingMessageEqualToProvidedMessage() {
                // GIVEN
                String message = "I can help you with that";

                // WHEN
                DesignPlan plan = DesignPlan.explain(message);

                // THEN
                assertThat(plan.userFacingMessage()).isEqualTo(message);
            }

            @Test @DisplayName("explain — returns plan with executionOrder containing '1. Explain to user'")
            void explain_returnsPlanWithExecutionOrderContainingExplainStep() {
                // GIVEN / WHEN
                DesignPlan plan = DesignPlan.explain("msg");

                // THEN
                assertThat(plan.executionOrder()).containsExactly("1. Explain to user");
            }
        }

        @Nested @DisplayName("hasShapes")
        class HasShapesTests {

            @Test @DisplayName("hasShapes — returns false when shapes list is null")
            void hasShapes_returnsFalseWhenShapesListIsNull() {
                // GIVEN
                DesignPlan plan = new DesignPlan(
                    "create", "simple", null, null, Map.of(), List.of(), "msg");

                // WHEN / THEN
                assertThat(plan.hasShapes()).isFalse();
            }

            @Test @DisplayName("hasShapes — returns false when shapes list is empty")
            void hasShapes_returnsFalseWhenShapesListIsEmpty() {
                // GIVEN
                DesignPlan plan = new DesignPlan(
                    "create", "simple", null, List.of(), Map.of(), List.of(), "msg");

                // WHEN / THEN
                assertThat(plan.hasShapes()).isFalse();
            }

            @Test @DisplayName("hasShapes — returns true when shapes list contains one shape")
            void hasShapes_returnsTrueWhenShapesListContainsOneShape() {
                // GIVEN
                DesignPlan.ShapeInstruction shape = new DesignPlan.ShapeInstruction(
                    "createRectangle", "rect-1", Map.of("x", 0), null);
                DesignPlan plan = new DesignPlan(
                    "create", "simple", null, List.of(shape), Map.of(), List.of(), "msg");

                // WHEN / THEN
                assertThat(plan.hasShapes()).isTrue();
            }
        }

        @Nested @DisplayName("hasTemplate")
        class HasTemplateTests {

            @Test @DisplayName("hasTemplate — returns false when templateId is null")
            void hasTemplate_returnsFalseWhenTemplateIdIsNull() {
                // GIVEN
                DesignPlan plan = new DesignPlan(
                    "create", "simple", null, List.of(), Map.of(), List.of(), "msg");

                // WHEN / THEN
                assertThat(plan.hasTemplate()).isFalse();
            }

            @Test @DisplayName("hasTemplate — returns false when templateId is empty string")
            void hasTemplate_returnsFalseWhenTemplateIdIsEmptyString() {
                // GIVEN
                DesignPlan plan = new DesignPlan(
                    "create", "simple", "", List.of(), Map.of(), List.of(), "msg");

                // WHEN / THEN
                assertThat(plan.hasTemplate()).isFalse();
            }

            @Test @DisplayName("hasTemplate — returns false when templateId is blank")
            void hasTemplate_returnsFalseWhenTemplateIdIsBlank() {
                // GIVEN
                DesignPlan plan = new DesignPlan(
                    "create", "simple", "   ", List.of(), Map.of(), List.of(), "msg");

                // WHEN / THEN
                assertThat(plan.hasTemplate()).isFalse();
            }

            @Test @DisplayName("hasTemplate — returns true when templateId is non-blank")
            void hasTemplate_returnsTrueWhenTemplateIdIsNonBlank() {
                // GIVEN
                DesignPlan plan = new DesignPlan(
                    "create", "simple", "tmpl-001", List.of(), Map.of(), List.of(), "msg");

                // WHEN / THEN
                assertThat(plan.hasTemplate()).isTrue();
            }
        }
    }

    @Nested @DisplayName("TaskResult")
    class TaskResultTests {

        @Nested @DisplayName("success(Object)")
        class SuccessTests {

            @Test @DisplayName("success — isSuccess returns true")
            void success_isSuccessReturnsTrue() {
                // GIVEN / WHEN
                TaskResult r = TaskResult.success("data");

                // THEN
                assertThat(r.isSuccess()).isTrue();
            }

            @Test @DisplayName("success — data is present with provided value")
            void success_dataIsPresentWithProvidedValue() {
                // GIVEN / WHEN
                TaskResult r = TaskResult.success("my-data");

                // THEN
                assertThat(r.getData()).isPresent();
                assertThat(r.getData()).contains("my-data");
            }

            @Test @DisplayName("success — error is empty")
            void success_errorIsEmpty() {
                // GIVEN / WHEN
                TaskResult r = TaskResult.success("data");

                // THEN
                assertThat(r.getError()).isEmpty();
            }

            @Test @DisplayName("success — logs is empty by default")
            void success_logsIsEmptyByDefault() {
                // GIVEN / WHEN
                TaskResult r = TaskResult.success("data");

                // THEN
                assertThat(r.getLogs()).isEmpty();
            }

            @Test @DisplayName("success — data is empty when null is provided")
            void success_dataIsEmptyWhenNullIsProvided() {
                // GIVEN / WHEN
                TaskResult r = TaskResult.success(null);

                // THEN
                assertThat(r.isSuccess()).isTrue();
                assertThat(r.getData()).isEmpty();
            }
        }

        @Nested @DisplayName("success(Object, List<String>)")
        class SuccessWithLogsTests {

            @Test @DisplayName("success with logs — logs equal provided list")
            void successWithLogs_logsEqualProvidedList() {
                // GIVEN
                List<String> logs = List.of("step 1", "step 2");

                // WHEN
                TaskResult r = TaskResult.success("data", logs);

                // THEN
                assertThat(r.getLogs()).containsExactly("step 1", "step 2");
            }

            @Test @DisplayName("success with logs — logs is empty when null provided")
            void successWithLogs_logsIsEmptyWhenNullProvided() {
                // GIVEN / WHEN
                TaskResult r = TaskResult.success("data", null);

                // THEN
                assertThat(r.getLogs()).isEmpty();
            }
        }

        @Nested @DisplayName("failure(String)")
        class FailureTests {

            @Test @DisplayName("failure — isSuccess returns false")
            void failure_isSuccessReturnsFalse() {
                // GIVEN / WHEN
                TaskResult r = TaskResult.failure("error");

                // THEN
                assertThat(r.isSuccess()).isFalse();
            }

            @Test @DisplayName("failure — error is present with provided message")
            void failure_errorIsPresentWithProvidedMessage() {
                // GIVEN / WHEN
                TaskResult r = TaskResult.failure("something went wrong");

                // THEN
                assertThat(r.getError()).isPresent();
                assertThat(r.getError()).contains("something went wrong");
            }

            @Test @DisplayName("failure — data is empty")
            void failure_dataIsEmpty() {
                // GIVEN / WHEN
                TaskResult r = TaskResult.failure("error");

                // THEN
                assertThat(r.getData()).isEmpty();
            }

            @Test @DisplayName("failure — logs is empty by default")
            void failure_logsIsEmptyByDefault() {
                // GIVEN / WHEN
                TaskResult r = TaskResult.failure("error");

                // THEN
                assertThat(r.getLogs()).isEmpty();
            }
        }

        @Nested @DisplayName("failure(String, List<String>)")
        class FailureWithLogsTests {

            @Test @DisplayName("failure with logs — logs equal provided list")
            void failureWithLogs_logsEqualProvidedList() {
                // GIVEN
                List<String> logs = List.of("step 1 failed", "rollback done");

                // WHEN
                TaskResult r = TaskResult.failure("error", logs);

                // THEN
                assertThat(r.getLogs()).containsExactly("step 1 failed", "rollback done");
            }

            @Test @DisplayName("failure with logs — logs is empty when null provided")
            void failureWithLogs_logsIsEmptyWhenNullProvided() {
                // GIVEN / WHEN
                TaskResult r = TaskResult.failure("error", null);

                // THEN
                assertThat(r.getLogs()).isEmpty();
            }
        }
    }

    @Nested @DisplayName("ExecuteCodeCommand")
    class ExecuteCodeCommandTests {

        @Nested @DisplayName("of(String code)")
        class OfCodeTests {

            @Test @DisplayName("of — returns command with provided code")
            void of_returnsCommandWithProvidedCode() {
                // GIVEN / WHEN
                ExecuteCodeCommand cmd = ExecuteCodeCommand.of("penpot.getPage()");

                // THEN
                assertThat(cmd.getCode()).isEqualTo("penpot.getPage()");
            }

            @Test @DisplayName("of — userToken is empty")
            void of_userTokenIsEmpty() {
                // GIVEN / WHEN
                ExecuteCodeCommand cmd = ExecuteCodeCommand.of("code");

                // THEN
                assertThat(cmd.getUserToken()).isEmpty();
            }
        }

        @Nested @DisplayName("of(String code, String userToken)")
        class OfCodeAndTokenTests {

            @Test @DisplayName("of with token — userToken is present with provided value")
            void ofWithToken_userTokenIsPresentWithProvidedValue() {
                // GIVEN / WHEN
                ExecuteCodeCommand cmd = ExecuteCodeCommand.of("code", "tok-abc");

                // THEN
                assertThat(cmd.getUserToken()).isPresent();
                assertThat(cmd.getUserToken()).contains("tok-abc");
            }

            @Test @DisplayName("of with null token — userToken is empty")
            void ofWithNullToken_userTokenIsEmpty() {
                // GIVEN / WHEN
                ExecuteCodeCommand cmd = ExecuteCodeCommand.of("code", null);

                // THEN
                assertThat(cmd.getUserToken()).isEmpty();
            }
        }

        @Nested @DisplayName("validate")
        class ValidateTests {

            @Test @DisplayName("validate — does not throw when code is valid")
            void validate_doesNotThrowWhenCodeIsValid() {
                // GIVEN
                ExecuteCodeCommand cmd = ExecuteCodeCommand.of("penpot.getPage()");

                // WHEN / THEN
                cmd.validate();
            }

            @Test @DisplayName("validate — throws IllegalArgumentException with message when code is null")
            void validate_throwsIllegalArgumentExceptionWhenCodeIsNull() {
                // GIVEN
                ExecuteCodeCommand cmd = ExecuteCodeCommand.builder().build();

                // WHEN / THEN
                assertThatThrownBy(cmd::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Code cannot be null or empty");
            }

            @Test @DisplayName("validate — throws IllegalArgumentException when code is blank")
            void validate_throwsIllegalArgumentExceptionWhenCodeIsBlank() {
                // GIVEN
                ExecuteCodeCommand cmd = ExecuteCodeCommand.builder().code("   ").build();

                // WHEN / THEN
                assertThatThrownBy(cmd::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Code cannot be null or empty");
            }

            @Test @DisplayName("validate — throws IllegalArgumentException when code is empty string")
            void validate_throwsIllegalArgumentExceptionWhenCodeIsEmptyString() {
                // GIVEN
                ExecuteCodeCommand cmd = ExecuteCodeCommand.builder().code("").build();

                // WHEN / THEN
                assertThatThrownBy(cmd::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Code cannot be null or empty");
            }
        }
    }

    @Nested @DisplayName("SessionCriteria")
    class SessionCriteriaTests {

        @Nested @DisplayName("forUser factory method")
        class ForUserTests {

            @Test @DisplayName("forUser — userToken is present with provided value")
            void forUser_userTokenIsPresentWithProvidedValue() {
                // GIVEN / WHEN
                SessionCriteria c = SessionCriteria.forUser("user-token-123");

                // THEN
                assertThat(c.getUserToken()).isPresent();
                assertThat(c.getUserToken()).contains("user-token-123");
            }

            @Test @DisplayName("forUser — requireActive is true by default")
            void forUser_requireActiveIsTrueByDefault() {
                // GIVEN / WHEN
                SessionCriteria c = SessionCriteria.forUser("tok");

                // THEN
                assertThat(c.isRequireActive()).isTrue();
            }

            @Test @DisplayName("forUser — userToken is empty when null is provided")
            void forUser_userTokenIsEmptyWhenNullIsProvided() {
                // GIVEN / WHEN
                SessionCriteria c = SessionCriteria.forUser(null);

                // THEN
                assertThat(c.getUserToken()).isEmpty();
            }
        }

        @Nested @DisplayName("any factory method")
        class AnyTests {

            @Test @DisplayName("any — userToken is empty")
            void any_userTokenIsEmpty() {
                // GIVEN / WHEN
                SessionCriteria c = SessionCriteria.any();

                // THEN
                assertThat(c.getUserToken()).isEmpty();
            }

            @Test @DisplayName("any — requireActive is true by default")
            void any_requireActiveIsTrueByDefault() {
                // GIVEN / WHEN
                SessionCriteria c = SessionCriteria.any();

                // THEN
                assertThat(c.isRequireActive()).isTrue();
            }
        }

        @Nested @DisplayName("builder")
        class BuilderTests {

            @Test @DisplayName("builder — allows setting requireActive to false")
            void builder_allowsSettingRequireActiveToFalse() {
                // GIVEN / WHEN
                SessionCriteria c = SessionCriteria.builder().requireActive(false).build();

                // THEN
                assertThat(c.isRequireActive()).isFalse();
            }

            @Test @DisplayName("builder — userToken defaults to Optional.empty when not set")
            void builder_userTokenDefaultsToEmptyWhenNotSet() {
                // GIVEN / WHEN
                SessionCriteria c = SessionCriteria.builder().build();

                // THEN
                assertThat(c.getUserToken()).isEmpty();
            }
        }
    }
}