package com.penpot.ai.application.tools;

import com.penpot.ai.application.service.RagTemplateService;
import com.penpot.ai.core.domain.TaskResult;
import com.penpot.ai.core.ports.in.ExecuteCodeUseCase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PenpotAssetTools — Integration")
class PenpotAssetToolsTest {

    @MockitoBean
    private ExecuteCodeUseCase executeCodeUseCase;

    @MockitoBean
    private RagTemplateService ragTemplateService;

    @Autowired
    private PenpotAssetTools penpotAssetTools;

    private static final String SHAPE_ID = "shape-integration-001";

    @BeforeEach
    void resetMocks() {
        reset(executeCodeUseCase);
    }

    @Nested
    @DisplayName("applyFillColor")
    class ApplyFillColorIntegrationTests {

        @Test
        @DisplayName("applyFillColor — returns success response when use case execution succeeds")
        void applyFillColor_returnsSuccessResponseWhenUseCaseExecutionSucceeds() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{\"id\": \"" + SHAPE_ID + "\", \"fill\": \"#FF0000\"}"));

            // WHEN
            String result = penpotAssetTools.applyFillColor(SHAPE_ID, "#FF0000", 1.0f);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("fillApplied");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("applyFillColor — uses default opacity 1.0 when opacity is null")
        void applyFillColor_usesDefaultOpacityWhenOpacityIsNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));

            // WHEN
            String result = penpotAssetTools.applyFillColor(SHAPE_ID, "#FF0000", null);

            // THEN
            assertThat(result).contains("\"success\": true");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("applyFillColor — uses default opacity 1.0 when opacity is out of range")
        void applyFillColor_usesDefaultOpacityWhenOpacityIsOutOfRange() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));

            // WHEN
            penpotAssetTools.applyFillColor(SHAPE_ID, "#FF0000", -0.5f);
            penpotAssetTools.applyFillColor(SHAPE_ID, "#FF0000", 1.5f);

            // THEN
            verify(executeCodeUseCase, times(2)).execute(any());
        }
    }

    @Nested
    @DisplayName("applyGradient")
    class ApplyGradientIntegrationTests {

        @Test
        @DisplayName("applyGradient — returns success response when use case execution succeeds")
        void applyGradient_returnsSuccessResponseWhenUseCaseExecutionSucceeds() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{\"id\": \"" + SHAPE_ID + "\", \"gradient\": \"linear\"}"));

            // WHEN
            String result = penpotAssetTools.applyGradient(SHAPE_ID, "#FF0000", "#0000FF", 45f);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("gradientApplied");
        }

        @Test
        @DisplayName("applyGradient — uses default angle 0 when angle is null")
        void applyGradient_usesDefaultAngleWhenAngleIsNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));

            // WHEN
            String result = penpotAssetTools.applyGradient(SHAPE_ID, "#FF0000", "#0000FF", null);

            // THEN
            assertThat(result).contains("\"success\": true");
        }
    }

    @Nested
    @DisplayName("applyStroke")
    class ApplyStrokeIntegrationTests {

        @Test
        @DisplayName("applyStroke — returns success response when use case execution succeeds")
        void applyStroke_returnsSuccessResponseWhenUseCaseExecutionSucceeds() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{\"id\": \"" + SHAPE_ID + "\", \"stroke\": \"#000000\"}"));

            // WHEN
            String result = penpotAssetTools.applyStroke(SHAPE_ID, "#000000", 2f);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("strokeApplied");
        }

        @Test
        @DisplayName("applyStroke — uses default width 1px when strokeWidth is null")
        void applyStroke_usesDefaultWidthWhenStrokeWidthIsNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));

            // WHEN
            String result = penpotAssetTools.applyStroke(SHAPE_ID, "#000000", null);

            // THEN
            assertThat(result).contains("\"success\": true");
        }

        @Test
        @DisplayName("applyStroke — uses default width 1px when strokeWidth is zero or negative")
        void applyStroke_usesDefaultWidthWhenStrokeWidthIsZeroOrNegative() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));

            // WHEN
            penpotAssetTools.applyStroke(SHAPE_ID, "#000000", 0f);
            penpotAssetTools.applyStroke(SHAPE_ID, "#000000", -1f);

            // THEN
            verify(executeCodeUseCase, times(2)).execute(any());
        }
    }

    @Nested
    @DisplayName("applyShadow")
    class ApplyShadowIntegrationTests {

        @Test
        @DisplayName("applyShadow — returns success response when use case execution succeeds")
        void applyShadow_returnsSuccessResponseWhenUseCaseExecutionSucceeds() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{\"id\": \"" + SHAPE_ID + "\", \"shadow\": {}}"));

            // WHEN
            String result = penpotAssetTools.applyShadow(SHAPE_ID, 4f, 4f, 8f, "#00000066");

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("shadowApplied");
        }

        @Test
        @DisplayName("applyShadow — uses default color #00000066 when shadowColor is null")
        void applyShadow_usesDefaultColorWhenShadowColorIsNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));

            // WHEN
            String result = penpotAssetTools.applyShadow(SHAPE_ID, 4f, 4f, 8f, null);

            // THEN
            assertThat(result).contains("\"success\": true");
        }

        @Test
        @DisplayName("applyShadow — uses default color #00000066 when shadowColor is blank")
        void applyShadow_usesDefaultColorWhenShadowColorIsBlank() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));

            // WHEN
            String result = penpotAssetTools.applyShadow(SHAPE_ID, 4f, 4f, 8f, "   ");

            // THEN
            assertThat(result).contains("\"success\": true");
        }
    }

    @Nested
    @DisplayName("updateOpacity")
    class UpdateOpacityIntegrationTests {

        @Test
        @DisplayName("updateOpacity — returns success response when use case execution succeeds")
        void updateOpacity_returnsSuccessResponseWhenUseCaseExecutionSucceeds() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{\"id\": \"" + SHAPE_ID + "\", \"opacity\": 0.5}"));

            // WHEN
            String result = penpotAssetTools.updateOpacity(SHAPE_ID, 0.5f);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("opacityUpdated");
        }

        @Test
        @DisplayName("updateOpacity — returns error JSON when opacity is null")
        void updateOpacity_returnsErrorJsonWhenOpacityIsNull() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.updateOpacity(SHAPE_ID, null);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Opacity must be between 0.0 and 1.0");
            verify(executeCodeUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("updateOpacity — returns error JSON when opacity is negative")
        void updateOpacity_returnsErrorJsonWhenOpacityIsNegative() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.updateOpacity(SHAPE_ID, -0.1f);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Opacity must be between 0.0 and 1.0");
            verify(executeCodeUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("updateOpacity — returns error JSON when opacity is greater than 1")
        void updateOpacity_returnsErrorJsonWhenOpacityIsGreaterThanOne() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.updateOpacity(SHAPE_ID, 1.5f);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Opacity must be between 0.0 and 1.0");
            verify(executeCodeUseCase, never()).execute(any());
        }
    }

    @Nested
    @DisplayName("updateBorderRadius")
    class UpdateBorderRadiusIntegrationTests {

        @Test
        @DisplayName("updateBorderRadius — returns error JSON and never calls use case when all parameters are null")
        void updateBorderRadius_returnsErrorJsonAndNeverCallsUseCaseWhenAllParametersAreNull() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.updateBorderRadius(SHAPE_ID, null, null, null, null, null);

            // THEN
            assertThat(result).contains("\"success\": false");
            verify(executeCodeUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("updateBorderRadius — returns error JSON and never calls use case when all values are negative")
        void updateBorderRadius_returnsErrorJsonAndNeverCallsUseCaseWhenAllValuesAreNegative() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.updateBorderRadius(SHAPE_ID, -5f, -1f, -1f, -1f, -1f);

            // THEN
            assertThat(result).contains("\"success\": false");
            verify(executeCodeUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("updateBorderRadius — returns success response when use case succeeds with uniform radius")
        void updateBorderRadius_returnsSuccessResponseWhenUseCaseSucceedsWithUniformRadius() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{\"id\":\"" + SHAPE_ID + "\",\"borderRadius\":10}"));

            // WHEN
            String result = penpotAssetTools.updateBorderRadius(SHAPE_ID, 10f, null, null, null, null);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("borderRadiusUpdated");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("updateBorderRadius — returns success response when use case succeeds with all four individual corners")
        void updateBorderRadius_returnsSuccessResponseWhenUseCaseSucceedsWithAllFourIndividualCorners() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success(
                    "{\"id\":\"" + SHAPE_ID + "\","
                    + "\"borderRadiusTopLeft\":10,"
                    + "\"borderRadiusTopRight\":20,"
                    + "\"borderRadiusBottomRight\":30,"
                    + "\"borderRadiusBottomLeft\":40}"
                ));

            // WHEN
            String result = penpotAssetTools.updateBorderRadius(SHAPE_ID, null, 10f, 20f, 30f, 40f);

            // THEN
            assertThat(result).contains("\"success\": true");
            verify(executeCodeUseCase, times(1)).execute(any());
        }
    }
}