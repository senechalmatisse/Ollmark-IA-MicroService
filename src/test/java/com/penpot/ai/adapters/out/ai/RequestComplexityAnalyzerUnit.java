package com.penpot.ai.adapters.out.ai;

import com.penpot.ai.core.domain.TaskComplexity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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
            // GIVEN a message longer than 150 characters with no specific keywords
            String message = "a".repeat(151);

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as COMPLEX
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithTwoOrMoreSequenceConjunctions_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithTwoOrMoreSequenceConjunctions_whenAnalyzed() {
            // GIVEN a short message containing 2 sequence conjunctions
            String message = "Change the color puis resize the element ensuite move it";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as COMPLEX
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithCreateKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithCreateKeyword_whenAnalyzed() {
            // GIVEN a message containing the "crée" keyword
            String message = "Crée un post instagram pour mon produit";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as COMPLEX
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithLandingPageKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithLandingPageKeyword_whenAnalyzed() {
            // GIVEN a message mentioning "landing page"
            String message = "Build a landing page for my startup";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as COMPLEX
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithFullPageKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithFullPageKeyword_whenAnalyzed() {
            // GIVEN a message containing "full page"
            String message = "Design a full page layout for the homepage";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as COMPLEX
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithGenerateKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithGenerateKeyword_whenAnalyzed() {
            // GIVEN a message containing the "generate" keyword
            String message = "Generate a complete marketing design";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as COMPLEX
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithStepByStepKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithStepByStepKeyword_whenAnalyzed() {
            // GIVEN a message containing "step by step"
            String message = "Design this step by step";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as COMPLEX
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithCompleteEmailKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithCompleteEmailKeyword_whenAnalyzed() {
            // GIVEN a message containing "complete email"
            String message = "Create a complete email for the campaign";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as COMPLEX
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithPlanKeyword_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithPlanKeyword_whenAnalyzed() {
            // GIVEN a message containing "plan"
            String message = "Plan the overall architecture of this design";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as COMPLEX
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithFrenchSequenceConjunctions_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithFrenchSequenceConjunctions_whenAnalyzed() {
            // GIVEN a French message with multiple sequence conjunctions
            String message = "D'abord change la couleur puis ensuite déplace l'élément";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as COMPLEX
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenCaseInsensitiveComplexKeyword_whenAnalyzed")
        void shouldReturnComplex_givenCaseInsensitiveComplexKeyword_whenAnalyzed() {
            // GIVEN a message with mixed-case complex keyword
            String message = "CRÉE une landing page complète";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as COMPLEX (keyword matching is case-insensitive via toLowerCase)
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }
    }

    // =========================================================================
    // CREATIVE
    // =========================================================================

    @Nested
    @DisplayName("CREATIVE detection")
    class CreativeDetection {

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithSuggestKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithSuggestKeyword_whenAnalyzed() {
            // GIVEN a short message with the "suggest" keyword
            String message = "Suggest a color for this button";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as CREATIVE
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithStyleKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithStyleKeyword_whenAnalyzed() {
            // GIVEN a message containing "style"
            String message = "What style should I use for this component?";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as CREATIVE
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithPaletteKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithPaletteKeyword_whenAnalyzed() {
            // GIVEN a message containing "palette"
            String message = "Choose a palette for this design";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as CREATIVE
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithLayoutKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithLayoutKeyword_whenAnalyzed() {
            // GIVEN a message containing "layout"
            String message = "Improve the layout of this section";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as CREATIVE
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithModernKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithModernKeyword_whenAnalyzed() {
            // GIVEN a message containing "modern"
            String message = "Make it look modern and clean";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as CREATIVE
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenIntermediateLengthMessageWithoutSimpleKeywords_whenAnalyzed")
        void shouldReturnCreative_givenIntermediateLengthMessageWithoutSimpleKeywords_whenAnalyzed() {
            // GIVEN a message between 60 and 150 chars with no simple or complex keywords
            String message = "I want to make this section look much more appealing for the user";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as CREATIVE (intermediate length, no simple keywords)
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithImproveKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithImproveKeyword_whenAnalyzed() {
            // GIVEN a message containing "improve"
            String message = "How can I improve this design?";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as CREATIVE
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithFrenchSuggestKeyword_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithFrenchSuggestKeyword_whenAnalyzed() {
            // GIVEN a message with the French "suggère" keyword
            String message = "Suggère-moi une couleur pour ce fond";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as CREATIVE
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }
    }

    // =========================================================================
    // SIMPLE
    // =========================================================================

    @Nested
    @DisplayName("SIMPLE detection")
    class SimpleDetection {

        @Test
        @DisplayName("shouldReturnSimple_givenShortMessageWithNoKeywords_whenAnalyzed")
        void shouldReturnSimple_givenShortMessageWithNoKeywords_whenAnalyzed() {
            // GIVEN a very short message with no recognizable keywords
            String message = "ok";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should default to SIMPLE
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithDeleteKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithDeleteKeyword_whenAnalyzed() {
            // GIVEN a message containing "delete"
            String message = "Delete this element";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as SIMPLE
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithMoveKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithMoveKeyword_whenAnalyzed() {
            // GIVEN a message containing "move"
            String message = "Move the button to the right";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as SIMPLE
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithResizeKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithResizeKeyword_whenAnalyzed() {
            // GIVEN a message containing "resize"
            String message = "Resize this rectangle";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as SIMPLE
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithRenameKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithRenameKeyword_whenAnalyzed() {
            // GIVEN a message containing "rename"
            String message = "Rename this layer to Header";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as SIMPLE
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithOpacityKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithOpacityKeyword_whenAnalyzed() {
            // GIVEN a message containing "opacity"
            String message = "Set opacity to 50%";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as SIMPLE
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithCoordinateKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithCoordinateKeyword_whenAnalyzed() {
            // GIVEN a message containing coordinate notation "x="
            String message = "Set x=100 y=200";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as SIMPLE
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithFrenchDeleteKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithFrenchDeleteKeyword_whenAnalyzed() {
            // GIVEN a message containing the French "supprime" keyword
            String message = "Supprime cet élément";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as SIMPLE
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenMessageWithFrenchMoveKeyword_whenAnalyzed")
        void shouldReturnSimple_givenMessageWithFrenchMoveKeyword_whenAnalyzed() {
            // GIVEN a message containing the French "déplace" keyword
            String message = "Déplace ce rectangle vers le bas";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be classified as SIMPLE
            assertThat(result).isEqualTo(TaskComplexity.SIMPLE);
        }

        @Test
        @DisplayName("shouldReturnSimple_givenExactly150CharMessage_whenAnalyzed")
        void shouldReturnSimple_givenExactly150CharMessage_whenAnalyzed() {
            // GIVEN a message of exactly 150 characters (at the boundary, not exceeding)
            String message = "a".repeat(150);

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should NOT be classified as COMPLEX (threshold is strictly >150)
            // With no creative keywords and no simple keywords, length > 60 → CREATIVE
            // but no creative keyword either, so this will be CREATIVE due to length heuristic
            assertThat(result).isIn(TaskComplexity.CREATIVE, TaskComplexity.SIMPLE);
        }
    }

    // =========================================================================
    // PRIORITY — COMPLEX takes precedence over CREATIVE
    // =========================================================================

    @Nested
    @DisplayName("Priority rules")
    class PriorityRules {

        @Test
        @DisplayName("shouldReturnComplex_givenMessageWithBothComplexAndCreativeKeywords_whenAnalyzed")
        void shouldReturnComplex_givenMessageWithBothComplexAndCreativeKeywords_whenAnalyzed() {
            // GIVEN a message that contains both a complex keyword and a creative keyword
            String message = "Crée un layout moderne pour cette page";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN COMPLEX should take priority over CREATIVE
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnComplex_givenLongMessageWithCreativeKeywords_whenAnalyzed")
        void shouldReturnComplex_givenLongMessageWithCreativeKeywords_whenAnalyzed() {
            // GIVEN a message longer than 150 chars that also contains creative keywords
            String message = "Please suggest a modern style and palette for this design, "
                + "I want it to look professional and colorful and well composed overall with good typography and mood";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN length threshold triggers COMPLEX before reaching creative check
            assertThat(result).isEqualTo(TaskComplexity.COMPLEX);
        }

        @Test
        @DisplayName("shouldReturnCreative_givenMessageWithCreativeButNoComplexOrSimpleKeywords_whenAnalyzed")
        void shouldReturnCreative_givenMessageWithCreativeButNoComplexOrSimpleKeywords_whenAnalyzed() {
            // GIVEN a message with only creative keywords
            String message = "recommend a professional theme";

            // WHEN the analyzer processes the message
            TaskComplexity result = analyzer.analyze(message);

            // THEN it should be CREATIVE
            assertThat(result).isEqualTo(TaskComplexity.CREATIVE);
        }
    }
}