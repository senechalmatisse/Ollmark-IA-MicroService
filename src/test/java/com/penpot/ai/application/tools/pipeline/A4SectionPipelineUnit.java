package com.penpot.ai.application.tools.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.application.tools.pipeline.A4SectionPipeline.A4SectionRequest;
import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import com.penpot.ai.core.domain.spec.SectionSpec;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class A4SectionPipelineUnit {

    @Mock
    private PenpotToolExecutor toolExecutor;

    @Spy
    private ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private A4SectionPipeline pipeline;

    private SectionSpec fullSpec;

    @BeforeEach
    void setUp() {
        fullSpec = new SectionSpec();
        fullSpec.setTitle("Mon Titre A4");
        fullSpec.setSubtitle("Mon Sous-titre");
        fullSpec.setParagraph("Un paragraphe de test.");
    }

    @Test
    void shouldReturnValidationErrorWhenSpecIsNull() {
        // GIVEN
        A4SectionRequest request = new A4SectionRequest(null, 0, 0);

        // WHEN
        String result = pipeline.execute(request);

        // THEN
        assertThat(result).contains("\"success\": false");
        assertThat(result).contains("must not be null");
        verifyNoInteractions(toolExecutor);
    }

    @Test
    void shouldReturnValidationErrorWhenAllTextFieldsAreBlank() {
        // GIVEN
        SectionSpec emptySpec = new SectionSpec();
        A4SectionRequest request = new A4SectionRequest(emptySpec, 0, 0);

        // WHEN
        String result = pipeline.execute(request);

        // THEN
        assertThat(result).contains("\"success\": false");
        assertThat(result).contains("Title, subtitle or paragraph must be provided");
        verifyNoInteractions(toolExecutor);
    }

    @Test
    void shouldReturnValidationErrorWhenTitleContainsOnlyWhitespace() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setTitle("   ");
        A4SectionRequest request = new A4SectionRequest(spec, 0, 0);

        // WHEN
        String result = pipeline.execute(request);

        // THEN
        assertThat(result).contains("\"success\": false");
        verifyNoInteractions(toolExecutor);
    }

    @Test
    void shouldPassValidationWhenOnlySubtitleIsProvided() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setSubtitle("Just a subtitle");
        when(toolExecutor.createContent(anyString(), eq("a4-section")))
            .thenReturn("{\"id\": \"sub-section-id\"}");

        // WHEN
        String result = pipeline.execute(new A4SectionRequest(spec, 0, 0));

        // THEN
        assertThat(result).isEqualTo("sub-section-id");
    }

    @Test
    void shouldPassValidationWhenOnlyParagraphIsProvided() {
        // GIVEN
        SectionSpec spec = new SectionSpec();
        spec.setParagraph("Some paragraph content");
        when(toolExecutor.createContent(anyString(), eq("a4-section")))
            .thenReturn("{\"id\": \"para-section-id\"}");

        // WHEN
        String result = pipeline.execute(new A4SectionRequest(spec, 0, 0));

        // THEN
        assertThat(result).isEqualTo("para-section-id");
    }

    @Test
    void shouldInjectDefaultCoordinatesInJsScriptWhenXAndYAreZero() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("a4-section")))
            .thenReturn("{\"id\": \"group-123\"}");

        // WHEN
        pipeline.execute(new A4SectionRequest(fullSpec, 0, 0));

        // THEN
        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(jsCaptor.capture(), eq("a4-section"));
        assertThat(jsCaptor.getValue()).contains("bg.x = 0");
        assertThat(jsCaptor.getValue()).contains("bg.y = 0");
    }

    @Test
    void shouldInjectProvidedCoordinatesInJsScript() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("a4-section")))
            .thenReturn("{\"id\": \"section-abc\"}");

        // WHEN
        pipeline.execute(new A4SectionRequest(fullSpec, 100, 200));

        // THEN
        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(jsCaptor.capture(), eq("a4-section"));
        assertThat(jsCaptor.getValue()).contains("bg.x = 100");
        assertThat(jsCaptor.getValue()).contains("bg.y = 200");
    }

    @Test
    void shouldIncludeA4DimensionsAndGroupStructureInGeneratedScript() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("a4-section")))
            .thenReturn("{\"id\": \"group-999\"}");

        // WHEN
        pipeline.execute(new A4SectionRequest(fullSpec, 0, 0));

        // THEN
        String js = captureJs();
        assertThat(js).contains("PAGE_W = 595");
        assertThat(js).contains("PAGE_H = 842");
        assertThat(js).contains("group.name = \"A4 Marketing Section\"");
        assertThat(js).contains("created.push(bg)");
        assertThat(js).contains("penpot.group(created)");
    }

    @Test
    void shouldReturnSectionIdFromTopLevelIdFieldWhenJsonIsFlat() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("a4-section")))
            .thenReturn("{\"id\": \"simple-id\"}");

        // WHEN
        String result = pipeline.execute(new A4SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(result).isEqualTo("simple-id");
    }

    @Test
    void shouldReturnSectionIdFromNestedResultFieldWhenJsonIsNested() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("a4-section")))
            .thenReturn("{\"result\": {\"sectionId\": \"nested-id\"}}");

        // WHEN
        String result = pipeline.execute(new A4SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(result).isEqualTo("nested-id");
    }

    @Test
    void shouldFallbackToNestedSectionIdWhenTopLevelIdIsBlank() {
        // GIVEN
        String json = """
            { "id": "  ", "result": { "sectionId": "fallback-id" } }
            """;
        when(toolExecutor.createContent(anyString(), eq("a4-section"))).thenReturn(json);

        // WHEN
        String result = pipeline.execute(new A4SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(result).isEqualTo("fallback-id");
    }

    @Test
    void shouldReturnJsonErrorWhenToolResponseContainsNoRecognizedIdField() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("a4-section")))
            .thenReturn("{\"status\": \"success\"}");

        // WHEN
        String result = pipeline.execute(new A4SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(result).contains("\"success\": false");
        assertThat(result).contains("No sectionId found");
    }

    @Test
    void shouldReturnJsonErrorWhenToolResponseIsInvalidJson() {
        // GIVEN
        when(toolExecutor.createContent(anyString(), eq("a4-section")))
            .thenReturn("not-a-json");

        // WHEN
        String result = pipeline.execute(new A4SectionRequest(fullSpec, 0, 0));

        // THEN
        assertThat(result).contains("\"success\": false");
        assertThat(result).contains("Invalid A4 tool response");
    }

    @Test
    void shouldUseA4SectionAsContentType() {
        assertThat(pipeline.contentType()).isEqualTo("a4-section");
    }

    private String captureJs() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(captor.capture(), eq("a4-section"));
        return captor.getValue();
    }
}