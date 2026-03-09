package com.penpot.ai.adapters.out.ai;

import com.penpot.ai.core.domain.TaskComplexity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RequestComplexityAnalyzer}.
 *
 * Naming convention: should[Description]_given[Context]_when[Action]
 * Each test follows GIVEN / WHEN / THEN structure via comments.
 */
@DisplayName("RequestComplexityAnalyzer")
class RequestComplexityAnalyzerUnit {

    private RequestComplexityAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new RequestComplexityAnalyzer();
    }

    // =========================================================================
    // NULL / BLANK
    // =========================================================================

    @Nested
    @DisplayName("Edge cases — null or blank input")
    class EdgeCases {

        @ParameterizedTest(name = "input=''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("shouldReturnSimple_givenNullOrBlankMessage_whenAnalyzed")
        void shouldReturnSimple_givenNullOrBlankMessage_whenAnalyzed(String message) {
            // GIVEN a null or blank message

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should default to SIMPLE
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }
    }

    // =========================================================================
    // COMPLEX
    // =========================================================================

    @Nested
    @DisplayName("COMPLEX detection")
    class ComplexDetection {

        @Test
        @DisplayName("shouldReturnComplex_givenMessageExceedingLengthThreshold_whenAnalyzed")
        void shouldReturnComplex_givenMessageExceedingLengthThreshold_whenAnalyzed() {
            // GIVEN
            String message = "a".repeat(151);

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithTwoOrMoreSequenceConjunctions_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithTwoOrMoreSequenceConjunctions_whenAnalyzed() {
            // GIVEN
            String message = "Change the color puis resize the element ensuite move it";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithCreateKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithCreateKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Crée un post instagram pour mon produit";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithLandingPageKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithLandingPageKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Build a landing page for my startup";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithFullPageKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithFullPageKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Design a full page layout for the homepage";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithGenerateKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithGenerateKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Generate a complete marketing design";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithStepByStepKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithStepByStepKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Design this step by step";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithCompleteEmailKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithCompleteEmailKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Create a complete email for the campaign";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithPlanKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithPlanKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Plan the overall architecture of this design";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithFrenchSequenceConjunctions_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithFrenchSequenceConjunctions_whenAnalyzed() {
            // GIVEN
            String message = "D'abord change la couleur puis ensuite déplace l'élément";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenCaseInsensitiveComplexKeyword_whenAnalyzed")
        void shouldReturnComplex_givenCaseInsensitiveComplexKeyword_whenAnalyzed() {
            // GIVEN
            String message = "CRÉE une landing page complète";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }
    }

    @Nested
    @DisplayName("CREATIVE detection")
    class CreativeDetection {

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithSuggestKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithSuggestKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Suggest a color for this button";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithStyleKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithStyleKeyword_whenAnalyzed() {
            // GIVEN
            String message = "What style should I use for this component?";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithPaletteKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithPaletteKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Choose a palette for this design";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithLayoutKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithLayoutKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Improve the layout of this section";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithModernKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithModernKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Make it look modern and clean";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenIntermediateLengthMessageWithoutSimpleKeywords_whenAnalyzed")
        void shouldReturnCreative_givenIntermediateLengthMessageWithoutSimpleKeywords_whenAnalyzed() {
            // GIVEN
            String message = "I want to make this section look much more appealing for the user";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithImproveKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithImproveKeyword_whenAnalyzed() {
            // GIVEN
            String message = "How can I improve this design?";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithFrenchSuggestKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithFrenchSuggestKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Suggère-moi une couleur pour ce fond";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }
    }

    @Nested
    @DisplayName("SIMPLE detection")
    class SimpleDetection {

        @Test
        @DisplayName("shouldReturnSimple_givenShortMessageWithNoKeywords_whenAnalyzed")
        void shouldReturnSimple_givenShortMessageWithNoKeywords_whenAnalyzed() {
            // GIVEN
            String message = "ok";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithDeleteKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithDeleteKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Delete this element";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithMoveKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithMoveKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Move the button to the right";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithResizeKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithResizeKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Resize this rectangle";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithRenameKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithRenameKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Rename this layer to Header";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithOpacityKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithOpacityKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Set opacity to 50%";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithCoordinateKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithCoordinateKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Set x=100 y=200";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithFrenchDeleteKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithFrenchDeleteKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Supprime cet élément";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithFrenchMoveKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithFrenchMoveKeyword_whenAnalyzed() {
            // GIVEN
            String message = "Déplace ce rectangle vers le bas";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenExactly150CharMessage_whenAnalyzed")
        void shouldReturnSimple_givenExactly150CharMessage_whenAnalyzed() {
            // GIVEN
            String message = "a".repeat(150);

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isIn(TaskComplexity.CREATIVE, TaskComplexity.SIMPLE);
        }
    }

    @Nested
    @DisplayName("Priority rules")
    class PriorityRules {

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithBothComplexAndCreativeKeywords_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithBothComplexAndCreativeKeywords_whenAnalyzed() {
            // GIVEN
            String message = "Crée un layout moderne pour cette page";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenLongMessageWithCreativeKeywords_whenAnalyzed")
        void shouldReturnComplex_givenLongMessageWithCreativeKeywords_whenAnalyzed() {
            // GIVEN
            String message = "Please suggest a modern style and palette for this design, "
                + "I want it to look professional and colorful and well composed overall with good typography and mood";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithCreativeButNoComplexOrSimpleKeywords_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithCreativeButNoComplexOrSimpleKeywords_whenAnalyzed() {
            // GIVEN
            String message = "recommend a professional theme";

            // WHEN
            TaskComplexity result = analyzer.analyze(message);

            // THEN
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }
    }
}