package com.penpot.ai.application.service;

import com.penpot.ai.core.domain.TemplateSpecs;
import com.penpot.ai.model.MarketingTemplate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TemplateSpecsExtractor — Unit")
public class TemplateSpecsExtractorUnit {

    @InjectMocks
    private TemplateSpecsExtractor extractor;

    @Mock
    private MarketingTemplate template;

    private void setupTemplate(String id, String type, Map<String, Object> recipe) {
        when(template.getId()).thenReturn(id);
        when(template.getType()).thenReturn(type);
        when(template.getDescription()).thenReturn("Test template");
        when(template.getTags()).thenReturn(List.of("test", "unit"));
    }

    private Map<String, Object> minimalRecipe() {
        return new HashMap<>(Map.of("canvas_size", "1080x1080"));
    }

    @Nested @DisplayName("extractSpecs — guard clauses")
    class ExtractSpecsGuardTests {

        @Test @DisplayName("extractSpecs — returns non-null TemplateSpecs for valid template")
        void extractSpecs_returnsNonNullTemplateSpecsForValidTemplate() {
            // GIVEN
            setupTemplate("tmpl-ok", "social_media_post", minimalRecipe());

            // WHEN
            TemplateSpecs specs = extractor.extractSpecs(template);

            // THEN
            assertThat(specs).isNotNull();
        }
    }

    @Nested @DisplayName("extractDimensions")
    class ExtractDimensionsTests {

        @Test @DisplayName("extractDimensions — uses default 1080x1080 for unknown type when canvas_size absent")
        void extractDimensions_usesDefault1080x1080ForUnknownTypeWhenCanvasSizeAbsent() {
            // GIVEN
            Map<String, Object> recipe = new HashMap<>(Map.of("layout_mode", "default"));
            setupTemplate("t", "unknown_type", recipe);

            // WHEN
            TemplateSpecs specs = extractor.extractSpecs(template);

            // THEN
            assertThat(specs.getDimensions().getWidth()).isEqualTo(1080);
            assertThat(specs.getDimensions().getHeight()).isEqualTo(1080);
        }
    }
}