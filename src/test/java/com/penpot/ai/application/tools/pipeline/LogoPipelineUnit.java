package com.penpot.ai.application.tools.pipeline;

import com.penpot.ai.application.tools.logo.*;
import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import com.penpot.ai.core.domain.logo.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("LogoPipeline — Unit")
class LogoPipelineUnit {

    private PenpotToolExecutor toolExecutor;
    private LogoIntentEngine intentEngine;
    private LogoThemeEngine themeEngine;
    private LogoRenderer logoRenderer;
    private LogoPipeline pipeline;

    @BeforeEach
    void setUp() {
        toolExecutor = mock(PenpotToolExecutor.class);
        intentEngine = mock(LogoIntentEngine.class);
        themeEngine  = mock(LogoThemeEngine.class);
        logoRenderer = mock(LogoRenderer.class);
        pipeline = new LogoPipeline(toolExecutor, intentEngine, themeEngine, logoRenderer);
    }

    // -------------------------------------------------------------------------
    // contentType
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("shouldReturnLogoAsContentType")
    void shouldReturnLogoAsContentType() {
        // WHEN
        String type = pipeline.contentType();

        // THEN
        assertThat(type).isEqualTo("logo");
    }

    // -------------------------------------------------------------------------
    // buildJsCode — orchestration
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("shouldPassInitialIntentFromIntentEngineToThemeEngine")
    void shouldPassInitialIntentFromIntentEngineToThemeEngine() {
        // GIVEN
        LogoSpec spec = logoSpec("TechNova", null, LogoStyle.GEOMETRIQUE, LogoLayout.VERTICAL, 50, 50);
        LogoIntent initialIntent = stubIntent(true);
        LogoIntent themedIntent  = stubIntent(false);

        when(intentEngine.analyze(spec)).thenReturn(initialIntent);
        when(themeEngine.applyTheme(spec, initialIntent)).thenReturn(themedIntent);
        when(logoRenderer.render(spec, themedIntent)).thenReturn("js");
        when(toolExecutor.createContent(anyString(), eq("logo"))).thenReturn("{\"success\":true}");

        // WHEN
        pipeline.execute(spec);

        // THEN
        verify(themeEngine).applyTheme(spec, initialIntent);
    }

    @Test
    @DisplayName("shouldPassThemedIntentFromThemeEngineToRenderer")
    void shouldPassThemedIntentFromThemeEngineToRenderer() {
        // GIVEN
        LogoSpec spec = logoSpec("Village Bio", "Produits locaux", LogoStyle.EMBLEME, LogoLayout.EMBLEM, 0, 0);
        LogoIntent initialIntent = stubIntent(false);
        LogoIntent themedIntent  = stubIntent(true);

        when(intentEngine.analyze(spec)).thenReturn(initialIntent);
        when(themeEngine.applyTheme(spec, initialIntent)).thenReturn(themedIntent);
        when(logoRenderer.render(spec, themedIntent)).thenReturn("js");
        when(toolExecutor.createContent(anyString(), eq("logo"))).thenReturn("{\"success\":true}");

        // WHEN
        pipeline.execute(spec);

        // THEN
        verify(logoRenderer).render(spec, themedIntent);
    }

    @Test
    @DisplayName("shouldSendRenderedJsCodeToToolExecutor")
    void shouldSendRenderedJsCodeToToolExecutor() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", "Click & Collect", LogoStyle.ABSTRAIT, LogoLayout.HORIZONTAL, 100, 100);
        String expectedJs = "const logo = penpot.createRectangle();";

        when(intentEngine.analyze(spec)).thenReturn(stubIntent(false));
        when(themeEngine.applyTheme(any(), any())).thenReturn(stubIntent(false));
        when(logoRenderer.render(any(), any())).thenReturn(expectedJs);
        when(toolExecutor.createContent(anyString(), eq("logo"))).thenReturn("{\"success\":true}");

        // WHEN
        pipeline.execute(spec);

        // THEN
        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(jsCaptor.capture(), eq("logo"));
        assertThat(jsCaptor.getValue()).isEqualTo(expectedJs);
    }

    // -------------------------------------------------------------------------
    // execute — résultat et gestion d'erreur
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("shouldReturnToolExecutorRawResult_whenPipelineSucceeds")
    void shouldReturnToolExecutorRawResult_whenPipelineSucceeds() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", null, LogoStyle.MINIMALISTE, LogoLayout.STACKED, 10, 20);
        String executorResponse = "{\"success\":true,\"logoId\":\"logo-42\"}";

        when(intentEngine.analyze(any())).thenReturn(stubIntent(false));
        when(themeEngine.applyTheme(any(), any())).thenReturn(stubIntent(false));
        when(logoRenderer.render(any(), any())).thenReturn("js");
        when(toolExecutor.createContent(anyString(), eq("logo"))).thenReturn(executorResponse);

        // WHEN
        String result = pipeline.execute(spec);

        // THEN
        assertThat(result).isEqualTo(executorResponse);
    }

    @Test
    @DisplayName("shouldReturnErrorResponse_whenToolExecutorThrows")
    void shouldReturnErrorResponse_whenToolExecutorThrows() {
        // GIVEN
        LogoSpec spec = logoSpec("Ollca", null, LogoStyle.ABSTRAIT, LogoLayout.HORIZONTAL, 0, 0);

        when(intentEngine.analyze(any())).thenReturn(stubIntent(false));
        when(themeEngine.applyTheme(any(), any())).thenReturn(stubIntent(false));
        when(logoRenderer.render(any(), any())).thenReturn("js");
        when(toolExecutor.createContent(anyString(), eq("logo")))
                .thenThrow(new RuntimeException("executor unavailable"));

        // WHEN
        String result = pipeline.execute(spec);

        // THEN
        assertThat(result).contains("executor unavailable");
    }

    @Test
    @DisplayName("shouldReturnErrorResponseAndSkipDownstreamEngines_whenIntentEngineFails")
    void shouldReturnErrorResponseAndSkipDownstreamEngines_whenIntentEngineFails() {
        // GIVEN
        LogoSpec spec = logoSpec("Crash", null, LogoStyle.GEOMETRIQUE, LogoLayout.HORIZONTAL, 0, 0);

        when(intentEngine.analyze(any())).thenThrow(new RuntimeException("intent engine crash"));

        // WHEN
        String result = pipeline.execute(spec);

        // THEN
        assertThat(result).contains("intent engine crash");
        verifyNoInteractions(themeEngine, logoRenderer, toolExecutor);
    }

    @Test
    @DisplayName("shouldSkipValidation_andPropagateErrorFromIntentEngine_whenSpecIsNull")
    void shouldSkipValidation_andPropagateErrorFromIntentEngine_whenSpecIsNull() {
        // GIVEN — LogoPipeline n'override pas validate(), donc null atteint directement intentEngine
        when(intentEngine.analyze(null)).thenThrow(new RuntimeException("null spec"));

        // WHEN
        String result = pipeline.execute(null);

        // THEN
        assertThat(result).contains("null spec");
        verify(intentEngine).analyze(null);
    }

    private LogoSpec logoSpec(String brand, String tagline, LogoStyle style, LogoLayout layout, int x, int y) {
        return LogoSpec.builder()
                .brandName(brand).tagline(tagline)
                .style(style).layout(layout)
                .x(x).y(y)
                .build();
    }

    private LogoIntent stubIntent(boolean startup) {
        return new LogoIntent(startup, startup ? 999 : 4, 1.0,
                "#FF5C00", "#000000", "#1A1A1A", true, startup);
    }
}