package com.penpot.ai.application.tools.support;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ToolResponseBuilderUnit {

    @Test
    void shouldReturnJsonWithSuccessFalseWhenBuildingErrorResponse() {
        // GIVEN
        String message = "Something went wrong";

        // WHEN
        String response = ToolResponseBuilder.error(message);

        // THEN
        assertThat(response).contains("\"success\": false");
    }

    @Test
    void shouldReturnJsonContainingErrorMessageWhenBuildingErrorResponse() {
        // GIVEN
        String message = "Shape not found";

        // WHEN
        String response = ToolResponseBuilder.error(message);

        // THEN
        assertThat(response).contains("Shape not found");
    }

    @Test
    void shouldEscapeSpecialCharactersInErrorMessage() {
        // GIVEN
        String message = "Error: \"quotes\" and \\ backslash";

        // WHEN
        String response = ToolResponseBuilder.error(message);

        // THEN
        assertThat(response).contains("\"success\": false");
        assertThat(response).doesNotContainPattern("\\\\\"[^\"]*\\\\\".*\\\\\"");
    }

    @Test
    void shouldReturnJsonWithSuccessTrueWhenBuildingSuccessResponse() {
        // GIVEN
        String message = "Operation completed";

        // WHEN
        String response = ToolResponseBuilder.success(message);

        // THEN
        assertThat(response).contains("\"success\": true");
    }

    @Test
    void shouldReturnJsonContainingMessageWhenBuildingSuccessResponse() {
        // GIVEN
        String message = "Operation completed";

        // WHEN
        String response = ToolResponseBuilder.success(message);

        // THEN
        assertThat(response).contains("Operation completed");
    }

    @Test
    void shouldNotContainErrorKeyWhenBuildingSuccessResponse() {
        // GIVEN
        String message = "All good";

        // WHEN
        String response = ToolResponseBuilder.success(message);

        // THEN
        assertThat(response).doesNotContain("\"error\"");
    }

    @Test
    void shouldReturnJsonWithSuccessTrueWhenShapeIsCreated() {
        // GIVEN
        String shapeType = "rectangle";
        String shapeId = "uuid-123";

        // WHEN
        String response = ToolResponseBuilder.shapeCreated(shapeType, shapeId);

        // THEN
        assertThat(response).contains("\"success\": true");
    }

    @Test
    void shouldReturnJsonContainingShapeTypeWhenShapeIsCreated() {
        // GIVEN
        String shapeType = "ellipse";
        String shapeId = "uuid-456";

        // WHEN
        String response = ToolResponseBuilder.shapeCreated(shapeType, shapeId);

        // THEN
        assertThat(response).contains("ellipse");
    }

    @Test
    void shouldReturnJsonContainingShapeIdWhenShapeIsCreated() {
        // GIVEN
        String shapeType = "rectangle";
        String shapeId = "uuid-789";

        // WHEN
        String response = ToolResponseBuilder.shapeCreated(shapeType, shapeId);

        // THEN
        assertThat(response).contains("uuid-789");
    }

    @Test
    void shouldReturnJsonContainingSaveThisIdInstructionWhenShapeIsCreated() {
        // GIVEN
        String shapeType = "board";
        String shapeId = "uuid-abc";

        // WHEN
        String response = ToolResponseBuilder.shapeCreated(shapeType, shapeId);

        // THEN
        assertThat(response).contains("SAVE THIS ID!");
    }

    @Test
    void shouldReturnJsonContainingShapeIdInsideInstructionsWhenShapeIsCreated() {
        // GIVEN
        String shapeType = "path";
        String shapeId = "uuid-def";

        // WHEN
        String response = ToolResponseBuilder.shapeCreated(shapeType, shapeId);

        // THEN
        assertThat(response).contains("SHAPE_ID: uuid-def");
    }

    @Test
    void shouldReturnJsonWithSuccessTrueWhenContentIsCreated() {
        // GIVEN
        String contentType = "title";
        String shapeId = "text-uuid-111";

        // WHEN
        String response = ToolResponseBuilder.contentCreated(contentType, shapeId);

        // THEN
        assertThat(response).contains("\"success\": true");
    }

    @Test
    void shouldReturnJsonContainingContentTypeWhenContentIsCreated() {
        // GIVEN
        String contentType = "paragraph";
        String shapeId = "text-uuid-222";

        // WHEN
        String response = ToolResponseBuilder.contentCreated(contentType, shapeId);

        // THEN
        assertThat(response).contains("paragraph");
    }

    @Test
    void shouldReturnJsonContainingShapeIdWhenContentIsCreated() {
        // GIVEN
        String contentType = "subtitle";
        String shapeId = "text-uuid-333";

        // WHEN
        String response = ToolResponseBuilder.contentCreated(contentType, shapeId);

        // THEN
        assertThat(response).contains("text-uuid-333");
    }

    @Test
    void shouldNotContainInstructionsKeyWhenContentIsCreated() {
        // GIVEN
        String contentType = "image";
        String shapeId = "img-uuid-444";

        // WHEN
        String response = ToolResponseBuilder.contentCreated(contentType, shapeId);

        // THEN
        assertThat(response).doesNotContain("instructions");
    }

    @Test
    void shouldReturnJsonWithSuccessTrueWhenBuildingShapeOperationResponse() {
        // GIVEN
        String operation = "rotated";
        String shapeId = "shape-op-111";
        String details = "Rotated 45 degrees";

        // WHEN
        String response = ToolResponseBuilder.shapeOperation(operation, shapeId, details);

        // THEN
        assertThat(response).contains("\"success\": true");
    }

    @Test
    void shouldReturnJsonContainingOperationNameWhenBuildingShapeOperationResponse() {
        // GIVEN
        String operation = "scaled";
        String shapeId = "shape-op-222";
        String details = "Scaled to 200%";

        // WHEN
        String response = ToolResponseBuilder.shapeOperation(operation, shapeId, details);

        // THEN
        assertThat(response).contains("scaled");
    }

    @Test
    void shouldReturnJsonContainingShapeIdWhenBuildingShapeOperationResponse() {
        // GIVEN
        String operation = "moved";
        String shapeId = "shape-op-333";
        String details = "Moved to (10, 20)";

        // WHEN
        String response = ToolResponseBuilder.shapeOperation(operation, shapeId, details);

        // THEN
        assertThat(response).contains("shape-op-333");
    }

    @Test
    void shouldReturnJsonContainingDetailsWhenBuildingShapeOperationResponse() {
        // GIVEN
        String operation = "filled";
        String shapeId = "shape-op-444";
        String details = "color: #FF0000";

        // WHEN
        String response = ToolResponseBuilder.shapeOperation(operation, shapeId, details);

        // THEN
        assertThat(response).contains("color: #FF0000");
    }

    @Test
    void shouldReturnJsonWithSuccessTrueWhenBuildingMultiShapeOperationResponse() {
        // GIVEN
        String operation = "aligned";
        List<String> ids = List.of("id-1", "id-2", "id-3");

        // WHEN
        String response = ToolResponseBuilder.multiShapeOperation(operation, ids);

        // THEN
        assertThat(response).contains("\"success\": true");
    }

    @Test
    void shouldReturnJsonContainingOperationNameWhenBuildingMultiShapeOperationResponse() {
        // GIVEN
        String operation = "distributed";
        List<String> ids = List.of("id-a", "id-b");

        // WHEN
        String response = ToolResponseBuilder.multiShapeOperation(operation, ids);

        // THEN
        assertThat(response).contains("distributed");
    }

    @Test
    void shouldReturnJsonContainingCorrectCountWhenBuildingMultiShapeOperationResponse() {
        // GIVEN
        String operation = "aligned";
        List<String> ids = List.of("id-1", "id-2", "id-3");

        // WHEN
        String response = ToolResponseBuilder.multiShapeOperation(operation, ids);

        // THEN
        assertThat(response).contains("\"count\": 3");
    }

    @Test
    void shouldReturnJsonContainingAllIdsJoinedWhenBuildingMultiShapeOperationResponse() {
        // GIVEN
        String operation = "grouped";
        List<String> ids = List.of("id-x", "id-y");

        // WHEN
        String response = ToolResponseBuilder.multiShapeOperation(operation, ids);

        // THEN
        assertThat(response).contains("id-x,id-y");
    }

    @Test
    void shouldReturnJsonContainingInstructionsWithShapeCountWhenBuildingMultiShapeOperationResponse() {
        // GIVEN
        String operation = "aligned";
        List<String> ids = List.of("id-1", "id-2");

        // WHEN
        String response = ToolResponseBuilder.multiShapeOperation(operation, ids);

        // THEN
        assertThat(response).contains("2 shapes");
    }

    @Test
    void shouldReturnJsonWithCountZeroWhenIdsListIsEmpty() {
        // GIVEN
        String operation = "aligned";
        List<String> ids = List.of();

        // WHEN
        String response = ToolResponseBuilder.multiShapeOperation(operation, ids);

        // THEN
        assertThat(response).contains("\"count\": 0");
    }

    @Test
    void shouldReturnJsonWithSuccessTrueWhenGroupIsCreated() {
        // GIVEN
        String groupId = "group-uuid-111";

        // WHEN
        String response = ToolResponseBuilder.groupCreated(groupId);

        // THEN
        assertThat(response).contains("\"success\": true");
    }

    @Test
    void shouldReturnJsonContainingGroupIdWhenGroupIsCreated() {
        // GIVEN
        String groupId = "group-uuid-222";

        // WHEN
        String response = ToolResponseBuilder.groupCreated(groupId);

        // THEN
        assertThat(response).contains("group-uuid-222");
    }

    @Test
    void shouldReturnJsonContainingGroupTipInstructionWhenGroupIsCreated() {
        // GIVEN
        String groupId = "group-uuid-333";

        // WHEN
        String response = ToolResponseBuilder.groupCreated(groupId);

        // THEN
        assertThat(response).contains("You can now work with the entire group as a single shape");
    }

    @Test
    void shouldReturnJsonWithSuccessTrueWhenShapeIsCloned() {
        // GIVEN
        String cloneId = "clone-uuid-111";

        // WHEN
        String response = ToolResponseBuilder.shapeCloned(cloneId);

        // THEN
        assertThat(response).contains("\"success\": true");
    }

    @Test
    void shouldReturnJsonContainingCloneIdWhenShapeIsCloned() {
        // GIVEN
        String cloneId = "clone-uuid-222";

        // WHEN
        String response = ToolResponseBuilder.shapeCloned(cloneId);

        // THEN
        assertThat(response).contains("clone-uuid-222");
    }

    @Test
    void shouldReturnJsonContainingSaveIdTipInstructionWhenShapeIsCloned() {
        // GIVEN
        String cloneId = "clone-uuid-333";

        // WHEN
        String response = ToolResponseBuilder.shapeCloned(cloneId);

        // THEN
        assertThat(response).contains("Save this ID to manipulate the clone");
    }
}