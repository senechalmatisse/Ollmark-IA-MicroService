package com.penpot.ai.application.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.penpot.ai.application.tools.support.PenpotToolExecutor;

@ExtendWith(MockitoExtension.class)
@DisplayName("PenpotAssetTools — Unit")
class PenpotAssetToolsUnit {

    @Mock
    private PenpotToolExecutor toolExecutor;

    @InjectMocks
    private PenpotAssetTools penpotAssetTools;

    private static final String SHAPE_ID = "abc-123-def-456";
    private static final String STUB_SUCCESS = "{\"success\": true, \"operation\": \"fillApplied\", \"shapeId\": \"abc-123-def-456\"}";

    @Nested
    @DisplayName("applyFillColor")
    class ApplyFillColorTests {

        @Test
        @DisplayName("applyFillColor — delegates to toolExecutor with operation 'fillApplied' and correct shapeId")
        void applyFillColor_delegatesToToolExecutorWithCorrectOperationNameAndShapeId() {
            // GIVEN
            String fillColor = "#FF0000";
            float  opacity   = 0.8f;
            when(toolExecutor.applyStyle(anyString(), eq("fillApplied"), eq(SHAPE_ID), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            String result = penpotAssetTools.applyFillColor(SHAPE_ID, fillColor, opacity);

            // THEN
            assertThat(result).isEqualTo(STUB_SUCCESS);
            verify(toolExecutor, times(1))
                .applyStyle(anyString(), eq("fillApplied"), eq(SHAPE_ID), anyString());
        }

        @Test
        @DisplayName("applyFillColor — uses default opacity 1.0 when opacity parameter is null")
        void applyFillColor_usesDefaultOpacityOneWhenOpacityIsNull() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            penpotAssetTools.applyFillColor(SHAPE_ID, "#00FF00", null);

            // THEN
            ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(anyString(), anyString(), anyString(), detailsCaptor.capture());
            assertThat(detailsCaptor.getValue()).contains("1.00");
        }

        @Test
        @DisplayName("applyFillColor — uses default opacity 1.0 when opacity is negative")
        void applyFillColor_usesDefaultOpacityOneWhenOpacityIsNegative() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            penpotAssetTools.applyFillColor(SHAPE_ID, "#FFFFFF", -0.5f);

            // THEN
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(codeCaptor.capture(), anyString(), anyString(), anyString());
            assertThat(codeCaptor.getValue()).contains("fillOpacity: 1.00");
        }

        @Test
        @DisplayName("applyFillColor — uses default opacity 1.0 when opacity is greater than 1")
        void applyFillColor_usesDefaultOpacityOneWhenOpacityIsGreaterThanOne() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            penpotAssetTools.applyFillColor(SHAPE_ID, "#FFFFFF", 1.5f);

            // THEN
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(codeCaptor.capture(), anyString(), anyString(), anyString());
            assertThat(codeCaptor.getValue()).contains("fillOpacity: 1.00");
        }
    }

    @Nested
    @DisplayName("applyGradient")
    class ApplyGradientTests {

        @Test
        @DisplayName("applyGradient — delegates to toolExecutor with operation 'gradientApplied' and correct shapeId")
        void applyGradient_delegatesToToolExecutorWithCorrectOperationNameAndShapeId() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), eq("gradientApplied"), eq(SHAPE_ID), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            String result = penpotAssetTools.applyGradient(SHAPE_ID, "#FF0000", "#0000FF", 45f);

            // THEN
            assertThat(result).isEqualTo(STUB_SUCCESS);
            verify(toolExecutor, times(1))
                .applyStyle(anyString(), eq("gradientApplied"), eq(SHAPE_ID), anyString());
        }

        @Test
        @DisplayName("applyGradient — uses default angle 0 when angle parameter is null")
        void applyGradient_usesDefaultAngleZeroWhenAngleIsNull() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            penpotAssetTools.applyGradient(SHAPE_ID, "#FF0000", "#0000FF", null);

            // THEN
            ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(anyString(), anyString(), anyString(), detailsCaptor.capture());
            assertThat(detailsCaptor.getValue()).contains("0°");
        }
    }

    @Nested
    @DisplayName("applyStroke")
    class ApplyStrokeTests {

        @Test
        @DisplayName("applyStroke — delegates to toolExecutor with operation 'strokeApplied' and correct shapeId")
        void applyStroke_delegatesToToolExecutorWithCorrectOperationNameAndShapeId() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), eq("strokeApplied"), eq(SHAPE_ID), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            String result = penpotAssetTools.applyStroke(SHAPE_ID, "#000000", 2f);

            // THEN
            assertThat(result).isEqualTo(STUB_SUCCESS);
            verify(toolExecutor, times(1))
                .applyStyle(anyString(), eq("strokeApplied"), eq(SHAPE_ID), anyString());
        }

        @Test
        @DisplayName("applyStroke — uses default width 1px when strokeWidth parameter is null")
        void applyStroke_usesDefaultWidthOnePxWhenStrokeWidthIsNull() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            penpotAssetTools.applyStroke(SHAPE_ID, "#000000", null);

            // THEN
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(codeCaptor.capture(), anyString(), anyString(), anyString());
            assertThat(codeCaptor.getValue()).contains("strokeWidth: 1.0");
        }

        @Test
        @DisplayName("applyStroke — uses default width 1px when strokeWidth is zero or negative")
        void applyStroke_usesDefaultWidthOnePxWhenStrokeWidthIsZeroOrNegative() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            penpotAssetTools.applyStroke(SHAPE_ID, "#000000", -2f);

            // THEN
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(codeCaptor.capture(), anyString(), anyString(), anyString());
            assertThat(codeCaptor.getValue()).contains("strokeWidth: 1.0");
        }
    }

    @Nested
    @DisplayName("applyShadow")
    class ApplyShadowTests {

        @Test
        @DisplayName("applyShadow — delegates to toolExecutor with operation 'shadowApplied' and correct shapeId")
        void applyShadow_delegatesToToolExecutorWithCorrectOperationNameAndShapeId() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), eq("shadowApplied"), eq(SHAPE_ID), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            String result = penpotAssetTools.applyShadow(SHAPE_ID, 4f, 4f, 8f, "#00000066");

            // THEN
            assertThat(result).isEqualTo(STUB_SUCCESS);
            verify(toolExecutor, times(1))
                .applyStyle(anyString(), eq("shadowApplied"), eq(SHAPE_ID), anyString());
        }

        @Test
        @DisplayName("applyShadow — uses default shadow color #00000066 when shadowColor parameter is null")
        void applyShadow_usesDefaultShadowColorWhenShadowColorIsNull() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            penpotAssetTools.applyShadow(SHAPE_ID, 2f, 2f, 4f, null);

            // THEN
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(codeCaptor.capture(), anyString(), anyString(), anyString());
            assertThat(codeCaptor.getValue()).contains("#00000066");
        }

        @Test
        @DisplayName("applyShadow — uses default shadow color #00000066 when shadowColor parameter is blank")
        void applyShadow_usesDefaultShadowColorWhenShadowColorIsBlank() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            penpotAssetTools.applyShadow(SHAPE_ID, 2f, 2f, 4f, "   ");

            // THEN
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(codeCaptor.capture(), anyString(), anyString(), anyString());
            assertThat(codeCaptor.getValue()).contains("#00000066");
        }
    }

    @Nested
    @DisplayName("updateOpacity")
    class UpdateOpacityTests {

        @Test
        @DisplayName("updateOpacity — delegates to toolExecutor with operation 'opacityUpdated' when opacity is valid")
        void updateOpacity_delegatesToToolExecutorWithCorrectOperationNameWhenOpacityIsValid() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), eq("opacityUpdated"), eq(SHAPE_ID), anyString()))
                .thenReturn(STUB_SUCCESS);

            // WHEN
            String result = penpotAssetTools.updateOpacity(SHAPE_ID, 0.5f);

            // THEN
            assertThat(result).isEqualTo(STUB_SUCCESS);
            verify(toolExecutor, times(1))
                .applyStyle(anyString(), eq("opacityUpdated"), eq(SHAPE_ID), anyString());
        }

        @Test
        @DisplayName("updateOpacity — returns error JSON and never calls toolExecutor when opacity is null")
        void updateOpacity_returnsErrorJsonAndNeverCallsToolExecutorWhenOpacityIsNull() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.updateOpacity(SHAPE_ID, null);

            // THEN
            assertThat(result)
                .contains("\"success\": false")
                .contains("Opacity must be between 0.0 and 1.0");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("updateOpacity — returns error JSON and never calls toolExecutor when opacity is negative")
        void updateOpacity_returnsErrorJsonAndNeverCallsToolExecutorWhenOpacityIsNegative() {
            // GIVEN
            Float invalidOpacity = -0.1f;

            // WHEN
            String result = penpotAssetTools.updateOpacity(SHAPE_ID, invalidOpacity);

            // THEN
            assertThat(result)
                .contains("\"success\": false")
                .contains("Opacity must be between 0.0 and 1.0");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("updateOpacity — returns error JSON and never calls toolExecutor when opacity is greater than 1")
        void updateOpacity_returnsErrorJsonAndNeverCallsToolExecutorWhenOpacityIsGreaterThanOne() {
            // GIVEN
            Float invalidOpacity = 1.1f;

            // WHEN
            String result = penpotAssetTools.updateOpacity(SHAPE_ID, invalidOpacity);

            // THEN
            assertThat(result)
                .contains("\"success\": false")
                .contains("Opacity must be between 0.0 and 1.0");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("updateBorderRadius")
    class UpdateBorderRadiusTests {

        private static final String STUB_BORDER =
                "{\"success\": true, \"operation\": \"borderRadiusUpdated\", \"shapeId\": \"abc-123-def-456\"}";

        @Test
        @DisplayName("updateBorderRadius — returns error JSON and never calls toolExecutor when all parameters are null")
        void updateBorderRadius_returnsErrorJsonWhenAllParametersAreNull() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.updateBorderRadius(SHAPE_ID, null, null, null, null, null);

            // THEN
            assertThat(result)
                .contains("\"success\": false")
                .contains("At least one border radius parameter must be provided");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("updateBorderRadius — returns error JSON and never calls toolExecutor when all values are negative")
        void updateBorderRadius_returnsErrorJsonWhenAllValuesAreNegative() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.updateBorderRadius(SHAPE_ID, -1f, -1f, -1f, -1f, -1f);

            // THEN
            assertThat(result).contains("\"success\": false");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("updateBorderRadius — generated JS sets each corner property when all four corners are provided without radius")
        void updateBorderRadius_generatedJsSetsEachCornerPropertyWhenAllFourCornersProvidedWithoutRadius() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_BORDER);

            // WHEN
            penpotAssetTools.updateBorderRadius(SHAPE_ID, null, 10f, 20f, 30f, 40f);

            // THEN
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(codeCaptor.capture(), anyString(), anyString(), anyString());
            String code = codeCaptor.getValue();
            assertThat(code)
                .contains("shape.borderRadius = 0")
                .contains("borderRadiusTopLeft")
                .contains("borderRadiusTopRight")
                .contains("borderRadiusBottomRight")
                .contains("borderRadiusBottomLeft")
                .contains("10.00")
                .contains("20.00")
                .contains("30.00")
                .contains("40.00");
        }

        @Test
        @DisplayName("updateBorderRadius — generated JS uses 0 as implicit base when some individual corners are provided without radius")
        void updateBorderRadius_generatedJsUsesZeroAsImplicitBaseWhenIndividualCornersProvidedWithoutRadius() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_BORDER);

            // WHEN
            penpotAssetTools.updateBorderRadius(SHAPE_ID, null, null, 25f, null, null);

            // THEN
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(codeCaptor.capture(), anyString(), anyString(), anyString());
            String code = codeCaptor.getValue();
            assertThat(code)
                .contains("shape.borderRadius = 0.00")
                .contains("borderRadiusTopRight")
                .contains("25.00");
        }

        @Test
        @DisplayName("updateBorderRadius — condition short-circuits at !hasBotLeft when bottomLeft is the only valid parameter")
        void updateBorderRadius_shortCircuitsAtHasBotLeft_whenBottomLeftIsTheOnlyValidParameter() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_BORDER);

            // WHEN
            String result = penpotAssetTools.updateBorderRadius(SHAPE_ID, null, null, null, null, 15f);

            // THEN
            assertThat(result).isEqualTo(STUB_BORDER);
            verify(toolExecutor, times(1)).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("updateBorderRadius — condition short-circuits at !hasBotRight when bottomRight is the only valid parameter")
        void updateBorderRadius_shortCircuitsAtHasBotRight_whenBottomRightIsTheOnlyValidParameter() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_BORDER);

            // WHEN
            String result = penpotAssetTools.updateBorderRadius(SHAPE_ID, null, null, null, 15f, null);

            // THEN
            assertThat(result).isEqualTo(STUB_BORDER);
            verify(toolExecutor, times(1)).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("updateBorderRadius — delegates to toolExecutor with operation 'borderRadiusUpdated' and correct shapeId")
        void updateBorderRadius_delegatesToToolExecutorWithCorrectOperationAndShapeId() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), eq("borderRadiusUpdated"), eq(SHAPE_ID), anyString()))
                .thenReturn(STUB_BORDER);

            // WHEN
            String result = penpotAssetTools.updateBorderRadius(SHAPE_ID, 10f, null, null, null, null);

            // THEN
            assertThat(result).isEqualTo(STUB_BORDER);
            verify(toolExecutor, times(1))
                .applyStyle(anyString(), eq("borderRadiusUpdated"), eq(SHAPE_ID), anyString());
        }

@Test
        @DisplayName("updateBorderRadius — generated JS applies uniform base and overrides all individual corners when all are provided")
        void updateBorderRadius_generatedJsAppliesUniformBaseAndOverridesAllCornersWhenAllProvided() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_BORDER);

            // WHEN - both uniform and all individual are provided
            penpotAssetTools.updateBorderRadius(SHAPE_ID, 5f, 10f, 20f, 30f, 40f);

            // THEN
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(codeCaptor.capture(), anyString(), anyString(), anyString());
            String code = codeCaptor.getValue();
            assertThat(code)
                .contains("shape.borderRadius = 5.00") // base = s.radius()
                .contains("borderRadiusTopLeft")       // hasTopLeft is true
                .contains("borderRadiusTopRight")
                .contains("borderRadiusBottomRight")
                .contains("borderRadiusBottomLeft");
        }

        @Test
        @DisplayName("updateBorderRadius — generated JS applies uniform base and overrides some corners when mix of parameters is provided")
        void updateBorderRadius_generatedJsAppliesUniformBaseAndOverridesSomeCornersWhenMixProvided() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_BORDER);

            // WHEN
            penpotAssetTools.updateBorderRadius(SHAPE_ID, 15f, 10f, null, null, null);

            // THEN
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(codeCaptor.capture(), anyString(), anyString(), anyString());
            String code = codeCaptor.getValue();
            assertThat(code)
                .contains("shape.borderRadius = 15.00")
                .contains("borderRadiusTopLeft = 10.00");
        }

        @Test
        @DisplayName("updateBorderRadius — log details are strictly uniform when only uniform radius is provided")
        void updateBorderRadius_logDetailsAreStrictlyUniformWhenOnlyRadiusProvided() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_BORDER);

            // WHEN
            penpotAssetTools.updateBorderRadius(SHAPE_ID, 25f, null, null, null, null);

            // THEN
            ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(anyString(), anyString(), anyString(), detailsCaptor.capture());
            assertThat(detailsCaptor.getValue())
                .isEqualTo("Border radius set uniformly to 25.0px");
        }

        @Test
        @DisplayName("updateBorderRadius — log details contain base when mix of radius and individual corners are provided")
        void updateBorderRadius_logDetailsContainBaseWhenMixOfRadiusAndCornersProvided() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_BORDER);

            // WHEN
            penpotAssetTools.updateBorderRadius(SHAPE_ID, 12f, 10f, null, null, null);

            // THEN
            ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(anyString(), anyString(), anyString(), detailsCaptor.capture());
            assertThat(detailsCaptor.getValue())
                .contains("base=12.0")
                .contains("TL=10.0");
        }

        @Test
        @DisplayName("updateBorderRadius — allIndividual short-circuits at hasBotRight when bottomRight is missing")
        void updateBorderRadius_allIndividualShortCircuitsAtHasBotRight() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_BORDER);

            // WHEN
            String result = penpotAssetTools.updateBorderRadius(SHAPE_ID, null, 10f, 20f, null, 40f);

            // THEN
            assertThat(result).isEqualTo(STUB_BORDER);
            verify(toolExecutor, times(1)).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("updateBorderRadius — allIndividual short-circuits at hasBotLeft when bottomLeft is missing")
        void updateBorderRadius_allIndividualShortCircuitsAtHasBotLeft() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_BORDER);

            // WHEN
            String result = penpotAssetTools.updateBorderRadius(SHAPE_ID, null, 10f, 20f, 30f, null);

            // THEN
            assertThat(result).isEqualTo(STUB_BORDER);
            verify(toolExecutor, times(1)).applyStyle(anyString(), anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("addInteraction")
    class AddInteractionTests {

        private static final String STUB_INTERACTION =
            "{\"success\": true, \"operation\": \"interactionAdded\", \"shapeId\": \"abc-123-def-456\"}";

        @Test
        @DisplayName("addInteraction — returns error JSON and never calls toolExecutor when trigger is null")
        void addInteraction_returnsErrorJsonWhenTriggerIsNull() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, null, "navigate-to", "dest-id", null);

            // THEN
            assertThat(result)
                .contains("\"success\": false")
                .contains("Trigger must be provided");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("addInteraction — returns error JSON and never calls toolExecutor when trigger is blank")
        void addInteraction_returnsErrorJsonWhenTriggerIsBlank() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "   ", "navigate-to", "dest-id", null);

            // THEN
            assertThat(result).contains("\"success\": false").contains("Trigger must be provided");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("addInteraction — returns error JSON and never calls toolExecutor when actionType is null")
        void addInteraction_returnsErrorJsonWhenActionTypeIsNull() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "click", null, "dest-id", null);

            // THEN
            assertThat(result).contains("\"success\": false").contains("Action type must be provided");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("addInteraction — returns error JSON and never calls toolExecutor when actionType is blank")
        void addInteraction_returnsErrorJsonWhenActionTypeIsBlank() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "click", "  ", "dest-id", null);

            // THEN
            assertThat(result).contains("\"success\": false").contains("Action type must be provided");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("addInteraction — returns error JSON when navigate-to is used without destinationId")
        void addInteraction_returnsErrorJsonWhenNavigateToHasNoDestinationId() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "click", "navigate-to", null, null);

            // THEN
            assertThat(result).contains("\"success\": false").contains("navigate-to").contains("destinationId");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("addInteraction — returns error JSON when navigate-to is used with blank destinationId")
        void addInteraction_returnsErrorJsonWhenNavigateToHasBlankDestinationId() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "click", "navigate-to", "  ", null);

            // THEN
            assertThat(result).contains("\"success\": false").contains("destinationId");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("addInteraction — returns error JSON when open-overlay is used without destinationId")
        void addInteraction_returnsErrorJsonWhenOpenOverlayHasNoDestinationId() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "click", "open-overlay", null, null);

            // THEN
            assertThat(result).contains("\"success\": false").contains("open-overlay");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("addInteraction — returns error JSON when toggle-overlay is used without destinationId")
        void addInteraction_returnsErrorJsonWhenToggleOverlayHasNoDestinationId() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "click", "toggle-overlay", null, null);

            // THEN
            assertThat(result).contains("\"success\": false").contains("toggle-overlay");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("addInteraction — delegates to toolExecutor with operation 'interactionAdded' and correct shapeId")
        void addInteraction_delegatesToToolExecutorWithCorrectOperationAndShapeId() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), eq("interactionAdded"), eq(SHAPE_ID), anyString()))
                .thenReturn(STUB_INTERACTION);

            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "click", "navigate-to", "dest-board-id", null);

            // THEN
            assertThat(result).isEqualTo(STUB_INTERACTION);
            verify(toolExecutor, times(1)).applyStyle(anyString(), eq("interactionAdded"), eq(SHAPE_ID), anyString());
        }

        @Test
        @DisplayName("addInteraction — close-overlay does not require destinationId")
        void addInteraction_closeOverlayDoesNotRequireDestinationId() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_INTERACTION);

            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "click", "close-overlay", null, null);

            // THEN
            assertThat(result).isEqualTo(STUB_INTERACTION);
            verify(toolExecutor, times(1)).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("addInteraction — prev-screen does not require destinationId")
        void addInteraction_prevScreenDoesNotRequireDestinationId() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_INTERACTION);

            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "click", "prev-screen", null, null);

            // THEN
            assertThat(result).isEqualTo(STUB_INTERACTION);
            verify(toolExecutor, times(1)).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("addInteraction — generated JS includes delay argument when trigger is after-delay")
        void addInteraction_generatedJsIncludesDelayArgumentWhenTriggerIsAfterDelay() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_INTERACTION);

            // WHEN
            penpotAssetTools.addInteraction(SHAPE_ID, "after-delay", "navigate-to", "board-xyz", 1500);

            // THEN
            ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(cap.capture(), anyString(), anyString(), anyString());
            assertThat(cap.getValue()).contains("1500");
        }

        @Test
        @DisplayName("addInteraction — generated JS defaults delay to 0 when provided delay is negative")
        void addInteraction_generatedJsDefaultsDelayToZeroWhenDelayIsNegative() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_INTERACTION);

            // WHEN
            penpotAssetTools.addInteraction(SHAPE_ID, "after-delay", "navigate-to", "board-xyz", -500);

            // THEN
            ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(cap.capture(), anyString(), anyString(), anyString());
            assertThat(cap.getValue()).contains(", 0");
        }

        @Test
        @DisplayName("addInteraction — generated JS contains open-url action object with provided URL")
        void addInteraction_generatedJsContainsOpenUrlActionObjectWithProvidedUrl() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_INTERACTION);

            // WHEN
            penpotAssetTools.addInteraction(SHAPE_ID, "click", "open-url", "https://penpot.app", null);

            // THEN
            ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(cap.capture(), anyString(), anyString(), anyString());
            assertThat(cap.getValue()).contains("{ type: 'open-url', url: 'https://penpot.app' }");
        }

        @Test
        @DisplayName("addInteraction — generated JS contains open-url action object with empty URL when destinationId is null")
        void addInteraction_generatedJsContainsOpenUrlActionObjectWithEmptyUrlWhenDestinationIdIsNull() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_INTERACTION);

            // WHEN
            penpotAssetTools.addInteraction(SHAPE_ID, "click", "open-url", null, null);

            // THEN
            ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(cap.capture(), anyString(), anyString(), anyString());
            assertThat(cap.getValue()).contains("{ type: 'open-url', url: '' }");
        }

        @Test
        @DisplayName("addInteraction — generated JS contains generic action object for default switch case")
        void addInteraction_generatedJsContainsGenericActionObjectForDefaultCase() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_INTERACTION);

            // WHEN
            penpotAssetTools.addInteraction(SHAPE_ID, "click", "custom-action", null, null);

            // THEN
            ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
            verify(toolExecutor).applyStyle(cap.capture(), anyString(), anyString(), anyString());
            assertThat(cap.getValue()).contains("{ type: 'custom-action' }");
        }
    }

    @Nested
    @DisplayName("removeInteraction")
    class RemoveInteractionTests {

        private static final String STUB_REMOVED =
            "{\"success\": true, \"operation\": \"interactionRemoved\", \"shapeId\": \"abc-123-def-456\"}";

        @Test
        @DisplayName("removeInteraction — returns error JSON and never calls toolExecutor when index is negative")
        void removeInteraction_returnsErrorJsonWhenIndexIsNegative() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.removeInteraction(SHAPE_ID, -1);

            // THEN
            assertThat(result)
                .contains("\"success\": false")
                .contains("Interaction index must be >= 0");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("removeInteraction — delegates to toolExecutor with operation 'interactionRemoved' and correct shapeId")
        void removeInteraction_delegatesToToolExecutorWithCorrectOperationAndShapeId() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), eq("interactionRemoved"), eq(SHAPE_ID), anyString()))
                .thenReturn(STUB_REMOVED);

            // WHEN
            String result = penpotAssetTools.removeInteraction(SHAPE_ID, 0);

            // THEN
            assertThat(result).isEqualTo(STUB_REMOVED);
            verify(toolExecutor, times(1)).applyStyle(anyString(), eq("interactionRemoved"), eq(SHAPE_ID), anyString());
        }

        @Test
        @DisplayName("removeInteraction — accepts index 0 (first interaction)")
        void removeInteraction_acceptsIndexZero() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(STUB_REMOVED);

            // WHEN
            String result = penpotAssetTools.removeInteraction(SHAPE_ID, 0);

            // THEN
            assertThat(result).isEqualTo(STUB_REMOVED);
            verify(toolExecutor, times(1)).applyStyle(anyString(), anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("replaceImage")
    class ReplaceImageTests {

        private static final String STUB_IMAGE =
            "{\"success\": true, \"operation\": \"imageReplaced\", \"shapeId\": \"abc-123-def-456\"}";
        private static final String IMAGE_URL = "https://example.com/image.jpg";

        @Test
        @DisplayName("replaceImage — returns error JSON and never calls toolExecutor when newImageUrl is null")
        void replaceImage_returnsErrorJsonWhenUrlIsNull() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.replaceImage(SHAPE_ID, null, null);

            // THEN
            assertThat(result)
                .contains("\"success\": false")
                .contains("newImageUrl must be provided");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("replaceImage — returns error JSON and never calls toolExecutor when newImageUrl is blank")
        void replaceImage_returnsErrorJsonWhenUrlIsBlank() {
            // GIVEN

            // WHEN
            String result = penpotAssetTools.replaceImage(SHAPE_ID, "   ", null);

            // THEN
            assertThat(result)
                .contains("\"success\": false")
                .contains("newImageUrl must be provided");
            verify(toolExecutor, never()).applyStyle(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("replaceImage — delegates to toolExecutor with operation 'imageReplaced' and correct shapeId")
        void replaceImage_delegatesToToolExecutorWithCorrectOperationAndShapeId() {
            // GIVEN
            when(toolExecutor.applyStyle(anyString(), eq("imageReplaced"), eq(SHAPE_ID), anyString()))
                .thenReturn(STUB_IMAGE);

            // WHEN
            String result = penpotAssetTools.replaceImage(SHAPE_ID, IMAGE_URL, null);

            // THEN
            assertThat(result).isEqualTo(STUB_IMAGE);
            verify(toolExecutor, times(1)).applyStyle(anyString(), eq("imageReplaced"), eq(SHAPE_ID), anyString());
        }
    }
}