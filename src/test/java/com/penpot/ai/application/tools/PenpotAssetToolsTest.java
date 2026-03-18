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

import org.mockito.ArgumentCaptor;
import com.penpot.ai.core.domain.ExecuteCodeCommand;

/**
 * Tests d'intégration pour {@link PenpotAssetTools}.
 * <p>
 * Cette classe valide le comportement des outils de manipulation d'assets Penpot,
 * en s'assurant que les commandes de style (couleur, dégradé, ombre, bordure) 
 * sont correctement transformées en scripts d'exécution.
 * </p>
 * * <p><b>Composants testés :</b></p>
 * <ul>
 * <li><b>Application du Style :</b> Remplissage, contours (stroke) et ombres.</li>
 * <li><b>Gestion des Fallbacks :</b> Validation des valeurs par défaut pour les paramètres nuls ou hors limites.</li>
 * <li><b>Validation métier :</b> Vérification stricte des plages de valeurs (ex: opacité entre 0 et 1).</li>
 * </ul>
 * * <p>Les dépendances externes comme {@link ExecuteCodeUseCase} sont mockées pour 
 * simuler les retours du moteur d'exécution Penpot.</p>
 */
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
    /**
     * Réinitialise les mocks avant chaque test pour éviter la pollution entre les cas de test.
     */
    @BeforeEach
    void resetMocks() {
        reset(executeCodeUseCase);
    }
    /**
     * Tests liés à l'application de couleurs de remplissage (Fill Color).
     * Vérifie la gestion de l'opacité et le formatage de la réponse.
     */
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

        @Test
        @DisplayName("applyFillColor — generated JS contains shapeId, fillColor and fillOpacity")
        void applyFillColor_generatedJsContainsShapeIdFillColorAndFillOpacity() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.applyFillColor(SHAPE_ID, "#FF0000", 0.8f);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("getShapeById('" + SHAPE_ID + "')");
            assertThat(code).contains("fillColor: '#FF0000'");
            assertThat(code).contains("fillOpacity: 0.80");
        }

        @Test
        @DisplayName("applyFillColor — generated JS uses fallback fillOpacity 1.00 when opacity is out of range")
        void applyFillColor_generatedJsUsesFallbackOpacityWhenOpacityIsOutOfRange() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.applyFillColor(SHAPE_ID, "#FF0000", -0.5f);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("fillOpacity: 1.00");
        }
    }
    /**
     * Tests liés à l'application de dégradés linéaires.
     * Vérifie notamment le comportement par défaut de l'angle du dégradé.
     */
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

        @Test
        @DisplayName("applyGradient — generated JS contains linear gradient type, colors and angle")
        void applyGradient_generatedJsContainsLinearGradientTypeColorsAndAngle() {
        // GIVEN
        when(executeCodeUseCase.execute(any()))
            .thenReturn(TaskResult.success("{}"));
        ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

        // WHEN
        penpotAssetTools.applyGradient(SHAPE_ID, "#FF0000", "#0000FF", 45f);

        // THEN
        verify(executeCodeUseCase).execute(captor.capture());
        String code = captor.getValue().getCode();
        assertThat(code).contains("getShapeById('" + SHAPE_ID + "')");
        assertThat(code).contains("type: 'linear'");
        assertThat(code).contains("color: '#FF0000'");
        assertThat(code).contains("color: '#0000FF'");
        assertThat(code).contains("angle: 45");
        }

        @Test
        @DisplayName("applyGradient — generated JS uses left-to-right coordinates when angle is null (default 0°)")
        void applyGradient_generatedJsUsesLeftToRightCoordinatesWhenAngleIsNull() {
        // GIVEN
        when(executeCodeUseCase.execute(any()))
            .thenReturn(TaskResult.success("{}"));
        ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

        // WHEN
        penpotAssetTools.applyGradient(SHAPE_ID, "#FF0000", "#0000FF", null);

        // THEN
        verify(executeCodeUseCase).execute(captor.capture());
        String code = captor.getValue().getCode();
        assertThat(code).contains("startX: 0.0000");
        assertThat(code).contains("endX: 1.0000");
        assertThat(code).contains("startY: 0.5000");
        assertThat(code).contains("endY: 0.5000");
        assertThat(code).contains("angle: 0");
        }
    }
    /**
     * Tests liés à la gestion des contours (Stroke).
     * Valide que les épaisseurs nulles ou négatives sont remplacées par la valeur par défaut (1px).
     */
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
        @Test
        @DisplayName("applyStroke — generated JS contains strokeColor, strokeWidth and strokeAlignment")
        void applyStroke_generatedJsContainsStrokeColorWidthAndAlignment() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.applyStroke(SHAPE_ID, "#000000", 2f);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("getShapeById('" + SHAPE_ID + "')");
            assertThat(code).contains("strokeColor: '#000000'");
            assertThat(code).contains("strokeWidth: 2.0");
            assertThat(code).contains("strokeAlignment: 'center'");
        }

        @Test
        @DisplayName("applyStroke — generated JS uses default strokeWidth 1.0 when strokeWidth is null")
        void applyStroke_generatedJsUsesDefaultWidthWhenStrokeWidthIsNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.applyStroke(SHAPE_ID, "#000000", null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("strokeWidth: 1.0");
        }
    }
    /**
     * Tests liés à l'application d'ombres portées.
     * Vérifie que la couleur par défaut est appliquée si aucune n'est spécifiée.
     */
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

        @Test
        @DisplayName("applyShadow — generated JS contains offsetX, offsetY, blur and color")
        void applyShadow_generatedJsContainsOffsetBlurAndColor() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.applyShadow(SHAPE_ID, 4f, 4f, 8f, "#00000066");

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("getShapeById('" + SHAPE_ID + "')");
            assertThat(code).contains("offsetX: 4.0");
            assertThat(code).contains("offsetY: 4.0");
            assertThat(code).contains("blur: 8.0");
            assertThat(code).contains("color: '#00000066'");
        }

        @Test
        @DisplayName("applyShadow — generated JS uses default color #00000066 when shadowColor is null")
        void applyShadow_generatedJsUsesDefaultColorWhenShadowColorIsNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.applyShadow(SHAPE_ID, 4f, 4f, 8f, null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("color: '#00000066'");
        }
    }
    /**
     * Tests liés à la mise à jour de l'opacité globale d'un élément.
     * <p>
     * Contrairement aux autres outils, cette méthode applique une validation stricte
     * et retourne un échec JSON si la valeur est en dehors de l'intervalle [0.0, 1.0].
     * </p>
     */
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

        @Test
        @DisplayName("updateOpacity — generated JS contains shape.opacity assignment with correct value")
        void updateOpacity_generatedJsContainsOpacityAssignment() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.updateOpacity(SHAPE_ID, 0.5f);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("getShapeById('" + SHAPE_ID + "')");
            assertThat(code).contains("shape.opacity = 0.50");
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

        @Test
        @DisplayName("updateBorderRadius — generated JS contains uniform borderRadius assignment")
        void updateBorderRadius_generatedJsContainsUniformBorderRadiusAssignment() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.updateBorderRadius(SHAPE_ID, 10f, null, null, null, null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("getShapeById('" + SHAPE_ID + "')");
            assertThat(code).contains("'borderRadius' in shape");
            assertThat(code).contains("shape.borderRadius = 10.00");
        }

        @Test
        @DisplayName("updateBorderRadius — generated JS contains individual corner assignments when all four corners are specified")
        void updateBorderRadius_generatedJsContainsIndividualCornerAssignmentsWhenAllFourCornersSpecified() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.updateBorderRadius(SHAPE_ID, null, 10f, 20f, 30f, 40f);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("shape.borderRadiusTopLeft = 10.00");
            assertThat(code).contains("shape.borderRadiusTopRight = 20.00");
            assertThat(code).contains("shape.borderRadiusBottomRight = 30.00");
            assertThat(code).contains("shape.borderRadiusBottomLeft = 40.00");
        }
    }
/**
 * Tests liés à l'ajout d'interactions sur une forme.
 * Vérifie la validation des paramètres et le code JS produit (trigger, action, delay).
 */
    @Nested
    @DisplayName("addInteraction")
    class AddInteractionIntegrationTests {

        private static final String DEST_ID = "11111111-1111-1111-1111-111111111111";

        @Test
        @DisplayName("addInteraction — returns success response when use case execution succeeds")
        void addInteraction_returnsSuccessResponseWhenUseCaseExecutionSucceeds() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{\"id\": \"" + SHAPE_ID + "\", \"trigger\": \"click\"}"));

            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "click", "navigate-to", DEST_ID, null);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("interactionAdded");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("addInteraction — generated JS contains trigger, addInteraction call and destination lookup")
        void addInteraction_generatedJsContainsTriggerAndDestinationLookup() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.addInteraction(SHAPE_ID, "click", "navigate-to", DEST_ID, null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("getShapeById('" + SHAPE_ID + "')");
            assertThat(code).contains("getShapeById('" + DEST_ID + "')");
            assertThat(code).contains("shape.addInteraction('click', action)");
            assertThat(code).contains("type: 'navigate-to'");
        }

        @Test
        @DisplayName("addInteraction — generated JS contains delay argument when trigger is after-delay")
        void addInteraction_generatedJsContainsDelayArgumentWhenTriggerIsAfterDelay() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.addInteraction(SHAPE_ID, "after-delay", "prev-screen", null, 500);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("shape.addInteraction('after-delay', action, 500)");
            assertThat(code).contains("type: 'prev-screen'");
        }

        @Test
        @DisplayName("addInteraction — generated JS contains no delay argument when trigger is not after-delay")
        void addInteraction_generatedJsContainsNoDelayArgumentWhenTriggerIsNotAfterDelay() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.addInteraction(SHAPE_ID, "click", "prev-screen", null, null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("shape.addInteraction('click', action)");
            assertThat(code).doesNotContain("shape.addInteraction('click', action,");
        }

        @Test
        @DisplayName("addInteraction — returns error JSON and never calls use case when trigger is blank")
        void addInteraction_returnsErrorJsonAndNeverCallsUseCaseWhenTriggerIsBlank() {
            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "  ", "navigate-to", DEST_ID, null);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Trigger must be provided");
            verify(executeCodeUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("addInteraction — returns error JSON and never calls use case when actionType is blank")
        void addInteraction_returnsErrorJsonAndNeverCallsUseCaseWhenActionTypeIsBlank() {
            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "click", "  ", DEST_ID, null);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Action type must be provided");
            verify(executeCodeUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("addInteraction — returns error JSON and never calls use case when navigate-to has no destinationId")
        void addInteraction_returnsErrorJsonAndNeverCallsUseCaseWhenNavigateToHasNoDestinationId() {
            // WHEN
            String result = penpotAssetTools.addInteraction(SHAPE_ID, "click", "navigate-to", null, null);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("requires a destinationId");
            verify(executeCodeUseCase, never()).execute(any());
        }
    }

    /**
 * Tests liés à la suppression d'interactions sur une forme.
 * Vérifie la validation de l'index et le code JS produit.
 */
    @Nested
    @DisplayName("removeInteraction")
    class RemoveInteractionIntegrationTests {

        @Test
        @DisplayName("removeInteraction — returns success response when use case execution succeeds")
        void removeInteraction_returnsSuccessResponseWhenUseCaseExecutionSucceeds() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{\"id\": \"" + SHAPE_ID + "\", \"removedIndex\": 0}"));

            // WHEN
            String result = penpotAssetTools.removeInteraction(SHAPE_ID, 0);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("interactionRemoved");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("removeInteraction — generated JS contains correct index, bounds check and removeInteraction call")
        void removeInteraction_generatedJsContainsIndexBoundsCheckAndRemoveCall() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.removeInteraction(SHAPE_ID, 0);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("getShapeById('" + SHAPE_ID + "')");
            assertThat(code).contains("if (0 >= interactions.length)");
            assertThat(code).contains("interactions[0]");
            assertThat(code).contains("shape.removeInteraction(interaction)");
        }

        @Test
        @DisplayName("removeInteraction — generated JS targets the correct index when index is greater than 0")
        void removeInteraction_generatedJsTargetsCorrectIndexWhenIndexIsGreaterThanZero() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.removeInteraction(SHAPE_ID, 2);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("if (2 >= interactions.length)");
            assertThat(code).contains("interactions[2]");
        }

        @Test
        @DisplayName("removeInteraction — returns error JSON and never calls use case when index is negative")
        void removeInteraction_returnsErrorJsonAndNeverCallsUseCaseWhenIndexIsNegative() {
            // WHEN
            String result = penpotAssetTools.removeInteraction(SHAPE_ID, -1);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("Interaction index must be >= 0");
            verify(executeCodeUseCase, never()).execute(any());
        }
    }
/**
 * Tests liés au remplacement d'image sur une forme existante.
 * Vérifie la validation de l'URL, le keepAspectRatio et le code JS produit.
 */
    @Nested
    @DisplayName("replaceImage")
    class ReplaceImageIntegrationTests {

        private static final String IMAGE_URL = "https://example.com/image.png";

        @Test
        @DisplayName("replaceImage — returns success response when use case execution succeeds")
        void replaceImage_returnsSuccessResponseWhenUseCaseExecutionSucceeds() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{\"id\": \"" + SHAPE_ID + "\", \"imageId\": \"img-001\"}"));

            // WHEN
            String result = penpotAssetTools.replaceImage(SHAPE_ID, IMAGE_URL, false);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("imageReplaced");
            verify(executeCodeUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("replaceImage — generated JS contains uploadMediaUrl with correct URL and keepAspectRatio false")
        void replaceImage_generatedJsContainsUploadMediaUrlAndKeepAspectRatioFalse() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.replaceImage(SHAPE_ID, IMAGE_URL, false);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            String code = captor.getValue().getCode();
            assertThat(code).contains("getShapeById('" + SHAPE_ID + "')");
            assertThat(code).contains("uploadMediaUrl('IA-Replace', '" + IMAGE_URL + "')");
            assertThat(code).contains("keepAspectRatio: false");
        }

        @Test
        @DisplayName("replaceImage — generated JS contains keepAspectRatio true when keepAspectRatio is true")
        void replaceImage_generatedJsContainsKeepAspectRatioTrueWhenFlagIsTrue() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.replaceImage(SHAPE_ID, IMAGE_URL, true);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("keepAspectRatio: true");
        }

        @Test
        @DisplayName("replaceImage — generated JS uses keepAspectRatio false when keepAspectRatio is null")
        void replaceImage_generatedJsUsesKeepAspectRatioFalseWhenFlagIsNull() {
            // GIVEN
            when(executeCodeUseCase.execute(any()))
                .thenReturn(TaskResult.success("{}"));
            ArgumentCaptor<ExecuteCodeCommand> captor = ArgumentCaptor.forClass(ExecuteCodeCommand.class);

            // WHEN
            penpotAssetTools.replaceImage(SHAPE_ID, IMAGE_URL, null);

            // THEN
            verify(executeCodeUseCase).execute(captor.capture());
            assertThat(captor.getValue().getCode()).contains("keepAspectRatio: false");
        }

        @Test
        @DisplayName("replaceImage — returns error JSON and never calls use case when newImageUrl is blank")
        void replaceImage_returnsErrorJsonAndNeverCallsUseCaseWhenNewImageUrlIsBlank() {
            // WHEN
            String result = penpotAssetTools.replaceImage(SHAPE_ID, "   ", false);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("newImageUrl must be provided");
            verify(executeCodeUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("replaceImage — returns error JSON and never calls use case when newImageUrl is null")
        void replaceImage_returnsErrorJsonAndNeverCallsUseCaseWhenNewImageUrlIsNull() {
            // WHEN
            String result = penpotAssetTools.replaceImage(SHAPE_ID, null, false);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("newImageUrl must be provided");
            verify(executeCodeUseCase, never()).execute(any());
        }
    }
}