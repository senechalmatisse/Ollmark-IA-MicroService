package com.penpot.ai.application.tools.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.penpot.ai.application.tools.pipeline.SectionPipeline.SectionRequest;
import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import com.penpot.ai.core.domain.marketing.MarketingLayoutType;
import com.penpot.ai.core.domain.marketing.MarketingStyle;
import com.penpot.ai.core.domain.spec.SectionSpec;

@ExtendWith(MockitoExtension.class)
class SectionPipelineUnit {

    @Mock
    private PenpotToolExecutor toolExecutor;

    @InjectMocks
    private SectionPipeline pipeline;

    private SectionSpec fullSpec;

    @BeforeEach
    void setUp() {
        fullSpec = new SectionSpec();
        fullSpec.setTitle("Titre hero");
        fullSpec.setSubtitle("Sous-titre");
        fullSpec.setParagraph("Paragraphe marketing");
    }

    // ==================== validate() ====================

    @Test
    void shouldReturnValidationErrorWhenSpecIsNull() {
        // GIVEN
        SectionRequest request = new SectionRequest(null, 0, 0);

        // WHEN
        String result = pipeline.execute(request);

        // THEN
        assertThat(result).contains("\"success\": false");
        assertThat(result).contains("SectionSpec cannot be null");
        verifyNoInteractions(toolExecutor);
    }

    @Test
    void shouldReturnValidationErrorWhenAllTextFieldsAreBlank() {
        // GIVEN
        SectionSpec empty = new SectionSpec();
        SectionRequest request = new SectionRequest(empty, 0, 0);

        // WHEN
        String result = pipeline.execute(request);

        // THEN
        assertThat(result).contains("\"success\": false");
        assertThat(result).contains("SectionSpec must contain title, subtitle or paragraph");
        verifyNoInteractions(toolExecutor);
    }

    @Test
    void shouldReturnValidationErrorWhenAllFieldsContainOnlyWhitespace() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("   ");
        spec.setSubtitle("   ");
        spec.setParagraph("   ");

        // WHEN
        String result = pipeline.execute(new SectionRequest(spec, 0, 0));

        // THEN
        assertThat(result).contains("\"success\": false");
        verifyNoInteractions(toolExecutor);
    }

    // ==================== contentType() ====================

    @Test
    void shouldUseSectionAsContentType() {
        assertThat(pipeline.contentType()).isEqualTo("section");
    }

    // ==================== buildJsCode() — coordonnées ====================

    @Test
    void shouldInjectDefaultCoordinatesWhenXIs80AndYIs120() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"result\": {\"sectionId\": \"s-1\"}}");

        // WHEN
        pipeline.execute(new SectionRequest(fullSpec, 80, 120));

        // THEN
        String js = captureJs();
        assertThat(js).contains("section.x = 80;");
        assertThat(js).contains("section.y = 120;");
    }

    @Test
    void shouldInjectProvidedCoordinatesInGeneratedScript() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"result\": {\"sectionId\": \"s-2\"}}");

        // WHEN
        pipeline.execute(new SectionRequest(fullSpec, 15, 25));

        // THEN
        String js = captureJs();
        assertThat(js).contains("section.x = 15;");
        assertThat(js).contains("section.y = 25;");
    }

    @Test
    void shouldIncludeGroupStructureInGeneratedScript() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"result\": {\"sectionId\": \"s-3\"}}");

        // WHEN
        pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        String js = captureJs();
        assertThat(js).contains("group.name = \"HeroSection\";");
        assertThat(js).contains("created.push(section);");
    }

    // ==================== buildHeroBase() — hauteurs par layout ====================

    @Test
    void shouldUseDefaultHeightOf480WhenLayoutIsNull() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"result\": {\"sectionId\": \"s-4\"}}");

        // fullSpec.layout est null par défaut

        // WHEN
        pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(captureJs()).contains("section.resize(1120, 480);");
    }

    @Test
    void shouldUseHeight440ForHeroCentered() {
        // GIVEN
        fullSpec.setLayout(MarketingLayoutType.HERO_CENTERED);
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"result\": {\"sectionId\": \"s-5\"}}");

        // WHEN
        pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(captureJs()).contains("section.resize(1120, 440);");
    }

    @Test
    void shouldUseHeight540ForHeroWithStats() {
        // GIVEN
        fullSpec.setLayout(MarketingLayoutType.HERO_WITH_STATS);
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"result\": {\"sectionId\": \"s-6\"}}");

        // WHEN
        pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(captureJs()).contains("section.resize(1120, 540);");
    }

    @Test
    void shouldUseHeight420ForPromoSection() {
        // GIVEN
        fullSpec.setLayout(MarketingLayoutType.PROMO_SECTION);
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"result\": {\"sectionId\": \"s-7\"}}");

        // WHEN
        pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(captureJs()).contains("section.resize(1120, 420);");
    }

    // ==================== parseResult() — extraction sectionId ====================

    @Test
    void shouldExtractSectionIdFromDirectResultPath() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"result\": {\"sectionId\": \"direct-777\"}}");

        // WHEN
        String result = pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(result).contains("direct-777");
    }

    @Test
    void shouldExtractSectionIdFromTopLevelUuidIdField() {
        // GIVEN
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"id\": \"" + uuid + "\"}");

        // WHEN
        String result = pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(result).contains(uuid);
    }

    @Test
    void shouldExtractSectionIdFromEmbeddedJsonInsideIdField() {
        // GIVEN
        String raw = "{\"id\": \"{\\\"result\\\":{\\\"sectionId\\\":\\\"embedded-999\\\"}}\"}";
        when(toolExecutor.createContent(anyString(), eq("section"))).thenReturn(raw);

        // WHEN
        String result = pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(result).contains("embedded-999");
    }

    @Test
    void shouldReturnErrorWhenNoSectionIdFoundInResponse() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"foo\": \"bar\"}");

        // WHEN
        String result = pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(result).contains("\"success\": false");
        assertThat(result).contains("No sectionId found in tool response");
    }

    @Test
    void shouldReturnErrorWhenToolResponseIsInvalidJson() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("not-a-json");

        // WHEN
        String result = pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(result).contains("\"success\": false");
    }

    @Test
    void shouldGenerateGradientFillWhenThemeIsGradient() {
        // GIVEN
        fullSpec.setStyle(MarketingStyle.MODERN_GRADIENT);
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"result\": {\"sectionId\": \"t-1\"}}");

        // WHEN
        pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(captureJs()).contains("fillColorGradient");
    }

    @Test
    void shouldGenerateSolidFillWhenThemeIsSolid() {
        // GIVEN
        fullSpec.setStyle(MarketingStyle.GLASSMORPHISM);
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"result\": {\"sectionId\": \"t-2\"}}");

        // WHEN
        pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        String js = captureJs();
        assertThat(js).contains("fillColor");
        assertThat(js).doesNotContain("fillColorGradient");
    }

    @Test
    void shouldReturnErrorWhenTopLevelIdIsNotAUuid() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("section")))
            .thenReturn("{\"id\": \"not-a-uuid\"}");

        // WHEN
        String result = pipeline.execute(new SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(result).contains("\"success\": false");
    }

    private String captureJs() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(captor.capture(), eq("section"));
        return captor.getValue();
    }
}