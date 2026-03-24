package com.penpot.ai.application.tools;

import com.penpot.ai.application.tools.pipeline.*;
import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import com.penpot.ai.core.domain.logo.*;
import com.penpot.ai.core.domain.spec.SectionSpec;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe {@link PenpotContentTools}.
 * <p>
 * Cette classe vérifie la génération correcte des scripts JavaScript envoyés à l'exécuteur Penpot
 * pour la création d'éléments visuels (boutons, titres, paragraphes, images).
 * Elle utilise Mockito pour simuler le comportement du {@link PenpotToolExecutor}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class PenpotContentToolsUnit {

    @Mock
    private PenpotToolExecutor toolExecutor;

    @Mock
    private LogoPipeline logoPipeline;

    @Mock
    private A4SectionPipeline a4Pipeline;

    @Mock
    private SectionPipeline sectionPipeline;

    @InjectMocks
    private PenpotContentTools penpotContentTools;

    /**
     * Vérifie que la création d'un bouton applique des valeurs par défaut robustes
     * lorsque tous les paramètres d'entrée sont nuls.
     */
    @Test
    @DisplayName("createButton: Should use default values when all parameters are null")
    void createButton_ShouldUseDefaultValues_WhenParamsAreNull() {
        // Given
        String expectedType = "button";
        when(toolExecutor.createContent(anyString(), eq(expectedType))).thenReturn("btn-123");

        // When
        penpotContentTools.createButton(null, null, null, null, null, null, null, null);

        // Then
        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(jsCaptor.capture(), eq(expectedType));

        String js = jsCaptor.getValue();
        assertThat(js).contains("const minW = 200"); // Largeur par défaut
        assertThat(js).contains("const minH = 60");  // Hauteur par défaut
        assertThat(js).contains("const posX = 50");  // X par défaut
        assertThat(js).contains("const posY = 50");  // Y par défaut
        assertThat(js).contains("const bg = '#4F46E5'"); // Couleur fond par défaut
        assertThat(js).contains("const textColor = '#FFFFFF'"); // Couleur texte par défaut
        assertThat(js).contains("const radius = 12"); // Radius par défaut
        assertThat(js).contains("const label = ''"); // Label vide si null
    }

    /**
     * Vérifie que les valeurs spécifiées par l'utilisateur sont correctement intégrées
     * dans le script JS et que le calcul dynamique de la largeur est présent.
     */
    @Test
    @DisplayName("createButton: Should use provided values and calculate final width")
    void createButton_ShouldUseProvidedValues() {
        // Given
        String label = "Click Me";
        Integer x = 100, y = 150, width = 300, height = 80, radius = 20;
        String bgColor = "#FF5733", textColor = "#000000";
        
        when(toolExecutor.createContent(anyString(), eq("button"))).thenReturn("btn-456");

        // When
        penpotContentTools.createButton(label, x, y, width, height, bgColor, textColor, radius);

        // Then
        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(jsCaptor.capture(), eq("button"));

        String js = jsCaptor.getValue();
        assertThat(js).contains("const minW = 300");
        assertThat(js).contains("const minH = 80");
        assertThat(js).contains("const posX = 100");
        assertThat(js).contains("const posY = 150");
        assertThat(js).contains("const bg = '#FF5733'");
        assertThat(js).contains("const textColor = '#000000'");
        assertThat(js).contains("const label = 'Click Me'");
        assertThat(js).contains("Math.max(minW, estimatedTextWidth + padX * 2)");
    }

    /**
     * Vérifie la sécurité de l'injection de texte en s'assurant que les caractères spéciaux
     * (apostrophes, antislashs) sont correctement échappés pour éviter de casser le script JS.
     */
    @Test
    @DisplayName("createButton: Should escape special characters in label for JS safety")
    void createButton_ShouldEscapeSpecialCharacters() {
        // Given
        String complexLabel = "L'IA de \\Penpot";
        when(toolExecutor.createContent(anyString(), eq("button"))).thenReturn("btn-789");

        // When
        penpotContentTools.createButton(complexLabel, 0, 0, null, null, null, null, null);

        // Then
        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(jsCaptor.capture(), eq("button"));

        String js = jsCaptor.getValue();
        assertThat(js).contains("const label = 'L\\'IA de \\\\Penpot'");
    }

    /**
     * Vérifie que les chaînes vides ou composées d'espaces pour les couleurs sont 
     * traitées comme des valeurs nulles, entraînant un repli sur les couleurs par défaut.
     */
    @Test
    @DisplayName("createButton: Should handle empty strings for colors by falling back to defaults")
    void createButton_ShouldHandleEmptyStrings() {
        // Given
        when(toolExecutor.createContent(anyString(), eq("button"))).thenReturn("btn-000");

        // When
        penpotContentTools.createButton("Test", 0, 0, 100, 40, " ", "", 5);

        // Then
        String js = verifyAndCaptureJs();
        assertThat(js).contains("const bg = '#4F46E5'"); 
        assertThat(js).contains("const textColor = '#FFFFFF'"); 
    }

    /**
     * Méthode utilitaire privée pour capturer le script JS lors d'un appel au toolExecutor.
     * @return Le script JavaScript capturé.
     */
    private String verifyAndCaptureJs() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(captor.capture(), eq("button"));
        return captor.getValue();
    }

    /**
     * Vérifie que la création d'un titre utilise les constantes de style H1 (taille 48, gras).
     */
    @Test
    @DisplayName("createTitle: Should call toolExecutor with H1 size and bold weight")
    void createTitle_ShouldCallExecutorWithCorrectParams() {
        // Given
        String content = "Main Title";
        Integer x = 10, y = 20;
        String color = "#FF0000";
        when(toolExecutor.createContent(anyString(), eq("title"))).thenReturn("title-id");

        // When
        penpotContentTools.createTitle(content, x, y, color);

        // Then
        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(jsCaptor.capture(), eq("title"));

        String js = jsCaptor.getValue();
        assertThat(js).contains("text.fontSize = 48");
        assertThat(js).contains("text.fontWeight = '700'");
        assertThat(js).contains("fillColor: '#FF0000'");
        assertThat(js).contains("'" + content + "'");
    }

    /**
     * Vérifie que la création d'un sous-titre utilise les constantes de style H2 (taille 32).
     */
    @Test
    @DisplayName("createSubtitle: Should call toolExecutor with H2 size")
    void createSubtitle_ShouldCallExecutorWithCorrectParams() {
        // Given
        when(toolExecutor.createContent(anyString(), eq("subtitle"))).thenReturn("sub-id");

        // When
        penpotContentTools.createSubtitle("Sub Title", 5, 5, null);

        // Then
        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(jsCaptor.capture(), eq("subtitle"));

        assertThat(jsCaptor.getValue()).contains("text.fontSize = 32");
    }

    /**
     * Vérifie que l'image est créée avec l'URL et les dimensions personnalisées fournies.
     */
    @Test
    @DisplayName("createImage: Should use provided dimensions and URL")
    void createImage_ShouldUseProvidedValues() {
        // Given
        String url = "http://image.png";
        when(toolExecutor.createContent(anyString(), eq("image"))).thenReturn("img-id");

        // When
        penpotContentTools.createImage(url, 100, 100, 500, 400);

        // Then
        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(jsCaptor.capture(), eq("image"));

        String js = jsCaptor.getValue();
        assertThat(js).contains(url);
        assertThat(js).contains("rect.resize(500, 400)");
        assertThat(js).contains("rect.x = 100");
    }

    /**
     * Vérifie que la création d'image utilise des dimensions par défaut (300x200)
     * lorsque la largeur ou la hauteur ne sont pas spécifiées.
     */
    @Test
    @DisplayName("createImage: Should use default dimensions when width/height are null")
    void createImage_ShouldUseDefaults_WhenDimsAreNull() {
        // Given
        when(toolExecutor.createContent(anyString(), eq("image"))).thenReturn("img-id");

        // When
        penpotContentTools.createImage("url", 0, 0, null, null);

        // Then
        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(jsCaptor.capture(), eq("image"));

        assertThat(jsCaptor.getValue()).contains("rect.resize(300, 200)");
    }

    /**
     * Vérifie la robustesse de l'outil face à des données incohérentes (dimensions négatives ou nulles).
     * Le système doit ignorer ces valeurs au profit des constantes par défaut.
     */
    @Test
    @DisplayName("createButton: Should handle zero or negative dimensions by falling back to defaults")
    void createButton_ShouldHandleInvalidDimensions() {
        // Given
        when(toolExecutor.createContent(anyString(), eq("button"))).thenReturn("btn-dim");

        // When (Largeur et hauteur négatives ou zéro)
        penpotContentTools.createButton("Label", 0, 0, 0, -10, null, null, -5);

        // Then
        String js = verifyAndCaptureJs();
        assertThat(js).contains("const minW = 200"); // Fallback
        assertThat(js).contains("const minH = 60");  // Fallback
        assertThat(js).contains("const radius = 12"); // Fallback pour radius < 0
    }

    /**
     * Vérifie qu'un label null est converti en chaîne vide pour éviter les erreurs "undefined"
     * côté JavaScript.
     */
    @Test
    @DisplayName("createButton: Should handle null label gracefully")
    void createButton_ShouldHandleNullLabel() {
        // When
        penpotContentTools.createButton(null, 10, 10, null, null, null, null, null);

        // Then
        String js = verifyAndCaptureJs();
        assertThat(js).contains("const label = ''");
    }

    /**
     * Vérifie que l'absence de couleur pour un titre ne provoque pas d'exception
     * et génère un script valide.
     */
    @Test
    @DisplayName("createTitle: Should work correctly when color is null")
    void createTitle_ShouldHandleNullColor() {
        // When
        penpotContentTools.createTitle("No Color", 0, 0, null);

        // Then
        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(jsCaptor.capture(), eq("title"));

        // Le snippet createText gère le null en ne mettant pas de fillColor ou une valeur par défaut
        // Ici on vérifie juste que l'appel ne plante pas et que le titre est là
        assertThat(jsCaptor.getValue()).contains("'No Color'");
    }

    /**
     * Vérifie le comportement hybride lorsque seule une partie des dimensions est fournie (ex: largeur OK, hauteur nulle).
     */
    @Test
    @DisplayName("createImage: Should handle only width or only height as null")
    void createImage_ShouldHandlePartialDimensions() {
        // Given
        when(toolExecutor.createContent(anyString(), eq("image"))).thenReturn("img-partial");

        // When (Largeur fournie, hauteur null)
        penpotContentTools.createImage("url", 0, 0, 800, null);

        // Then
        ArgumentCaptor<String> jsCaptor = ArgumentCaptor.forClass(String.class);
        verify(toolExecutor).createContent(jsCaptor.capture(), eq("image"));
        assertThat(jsCaptor.getValue()).contains("rect.resize(800, 200)");
    }

    // ==================== createLogo() — contentType ====================

    @Test
    void shouldPassConstantLogoContentTypeToExecutorWhenCreatingLogo() {
        // GIVEN
        when(logoPipeline.execute(any())).thenReturn("logo-id");

        // WHEN
        penpotContentTools.createLogo("Ollca", "Livraison rapide", LogoStyle.GEOMETRIQUE, LogoLayout.HORIZONTAL, 10, 20);

        // THEN
        verify(logoPipeline).execute(any(LogoSpec.class));
    }

    @Test
    void shouldAlwaysUseLogoConstantRegardlessOfBrandNameWhenCreatingLogo() {
        // GIVEN
        when(logoPipeline.execute(any())).thenReturn("logo-id");
        ArgumentCaptor<LogoSpec> specCaptor = ArgumentCaptor.forClass(LogoSpec.class);

        // WHEN
        penpotContentTools.createLogo("Boulangerie Louise & Co!", null, LogoStyle.EMBLEME, LogoLayout.STACKED, 0, 0);

        // THEN
        verify(logoPipeline).execute(specCaptor.capture());
        assertThat(specCaptor.getValue().getBrandName()).isEqualTo("Boulangerie Louise & Co!");
    }

    // ==================== createLogo() — résultat et délégation ====================

    @Test
    void shouldReturnPipelineResponseWhenCreatingLogoSucceeds() {
        // GIVEN
        when(logoPipeline.execute(any())).thenReturn("logo-id");

        // WHEN
        String result = penpotContentTools.createLogo("Ollca", "Livraison rapide", LogoStyle.GEOMETRIQUE, LogoLayout.HORIZONTAL, 10, 20);

        // THEN
        assertThat(result).isEqualTo("logo-id");
    }

    @Test
    void shouldDelegateToLogoPipelineWhenCreatingLogo() {
        // GIVEN
        when(logoPipeline.execute(any())).thenReturn("logo-id");

        // WHEN
        penpotContentTools.createLogo("Ollca", "Livraison rapide", LogoStyle.GEOMETRIQUE, LogoLayout.HORIZONTAL, 10, 20);

        // THEN
        verify(logoPipeline).execute(any(LogoSpec.class));
        verifyNoInteractions(toolExecutor);
    }

    // ==================== createLogo() — construction de LogoSpec ====================

    @Test
    void shouldPassBrandNameAndTaglineInSpecToPipelineWhenCreatingLogo() {
        // GIVEN
        when(logoPipeline.execute(any())).thenReturn("logo-id");
        ArgumentCaptor<LogoSpec> specCaptor = ArgumentCaptor.forClass(LogoSpec.class);

        // WHEN
        penpotContentTools.createLogo("Boulangerie Louise", "Le pain artisan", LogoStyle.EMBLEME, LogoLayout.STACKED, 50, 80);

        // THEN
        verify(logoPipeline).execute(specCaptor.capture());
        assertThat(specCaptor.getValue().getBrandName()).isEqualTo("Boulangerie Louise");
        assertThat(specCaptor.getValue().getTagline()).isEqualTo("Le pain artisan");
    }

    @Test
    void shouldPassStyleAndLayoutInSpecToPipelineWhenCreatingLogo() {
        // GIVEN
        when(logoPipeline.execute(any())).thenReturn("logo-id");
        ArgumentCaptor<LogoSpec> specCaptor = ArgumentCaptor.forClass(LogoSpec.class);

        // WHEN
        penpotContentTools.createLogo("Brand", null, LogoStyle.GEOMETRIQUE, LogoLayout.VERTICAL, null, null);

        // THEN
        verify(logoPipeline).execute(specCaptor.capture());
        assertThat(specCaptor.getValue().getStyle()).isEqualTo(LogoStyle.GEOMETRIQUE);
        assertThat(specCaptor.getValue().getLayout()).isEqualTo(LogoLayout.VERTICAL);
    }

    @Test
    void shouldDefaultPositionTo100WhenXAndYAreNull() {
        // GIVEN
        when(logoPipeline.execute(any())).thenReturn("logo-id");
        ArgumentCaptor<LogoSpec> specCaptor = ArgumentCaptor.forClass(LogoSpec.class);

        // WHEN
        penpotContentTools.createLogo("Brand", null, LogoStyle.MINIMALISTE, LogoLayout.HORIZONTAL, null, null);

        // THEN
        verify(logoPipeline).execute(specCaptor.capture());
        assertThat(specCaptor.getValue().getX()).isEqualTo(100);
        assertThat(specCaptor.getValue().getY()).isEqualTo(100);
    }

    @Test
    void shouldUseProvidedPositionWhenXAndYAreNotNull() {
        // GIVEN
        when(logoPipeline.execute(any())).thenReturn("logo-id");
        ArgumentCaptor<LogoSpec> specCaptor = ArgumentCaptor.forClass(LogoSpec.class);

        // WHEN
        penpotContentTools.createLogo("Brand", null, LogoStyle.MINIMALISTE, LogoLayout.HORIZONTAL, 200, 350);

        // THEN
        verify(logoPipeline).execute(specCaptor.capture());
        assertThat(specCaptor.getValue().getX()).isEqualTo(200);
        assertThat(specCaptor.getValue().getY()).isEqualTo(350);
    }

    @Test
    void shouldDelegateToA4PipelineWhenCreatingSection() {
        // GIVEN
        when(a4Pipeline.execute(any())).thenReturn("section-id");
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");

        // WHEN
        penpotContentTools.createA4Section(spec, 100, 200);

        // THEN
        verify(a4Pipeline).execute(any(A4SectionPipeline.A4SectionRequest.class));
        verifyNoInteractions(toolExecutor);
    }

    @Test
    void shouldReturnPipelineResponseWhenCreatingA4Section() {
        // GIVEN
        when(a4Pipeline.execute(any())).thenReturn("section-id");
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");

        // WHEN
        String result = penpotContentTools.createA4Section(spec, 0, 0);

        // THEN
        assertThat(result).isEqualTo("section-id");
    }

    @Test
    void shouldReplaceNullSpecWithEmptySectionSpecWhenSpecIsNull() {
        // GIVEN
        when(a4Pipeline.execute(any())).thenReturn("section-id");
        ArgumentCaptor<A4SectionPipeline.A4SectionRequest> requestCaptor =
            ArgumentCaptor.forClass(A4SectionPipeline.A4SectionRequest.class);

        // WHEN
        penpotContentTools.createA4Section(null, 0, 0);

        // THEN
        verify(a4Pipeline).execute(requestCaptor.capture());
        assertThat(requestCaptor.getValue().spec()).isNotNull();
        assertThat(requestCaptor.getValue().spec()).isInstanceOf(SectionSpec.class);
    }

    @Test
    void shouldDefaultCoordinatesToZeroWhenXAndYAreNull() {
        // GIVEN
        when(a4Pipeline.execute(any())).thenReturn("section-id");
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        ArgumentCaptor<A4SectionPipeline.A4SectionRequest> requestCaptor =
            ArgumentCaptor.forClass(A4SectionPipeline.A4SectionRequest.class);

        // WHEN
        penpotContentTools.createA4Section(spec, null, null);

        // THEN
        verify(a4Pipeline).execute(requestCaptor.capture());
        assertThat(requestCaptor.getValue().x()).isEqualTo(0);
        assertThat(requestCaptor.getValue().y()).isEqualTo(0);
    }

    @Test
    void shouldPassProvidedCoordinatesToPipelineRequest() {
        // GIVEN
        when(a4Pipeline.execute(any())).thenReturn("section-id");
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Titre");
        ArgumentCaptor<A4SectionPipeline.A4SectionRequest> requestCaptor =
            ArgumentCaptor.forClass(A4SectionPipeline.A4SectionRequest.class);

        // WHEN
        penpotContentTools.createA4Section(spec, 150, 300);

        // THEN
        verify(a4Pipeline).execute(requestCaptor.capture());
        assertThat(requestCaptor.getValue().x()).isEqualTo(150);
        assertThat(requestCaptor.getValue().y()).isEqualTo(300);
    }

    @Test
    void shouldPassSpecUnchangedToPipelineRequestWhenSpecIsNotNull() {
        // GIVEN
        when(a4Pipeline.execute(any())).thenReturn("section-id");
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Mon Titre");
        spec.setSubtitle("Mon Sous-titre");
        ArgumentCaptor<A4SectionPipeline.A4SectionRequest> requestCaptor =
            ArgumentCaptor.forClass(A4SectionPipeline.A4SectionRequest.class);

        // WHEN
        penpotContentTools.createA4Section(spec, 0, 0);

        // THEN
        verify(a4Pipeline).execute(requestCaptor.capture());
        assertThat(requestCaptor.getValue().spec()).isSameAs(spec);
    }

    @Test
    void shouldDelegateToSectionPipelineWhenCreatingSection() {
        // GIVEN
        when(sectionPipeline.execute(any())).thenReturn("ok");
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Hero");

        // WHEN
        penpotContentTools.createSection(spec, 10, 20);

        // THEN
        verify(sectionPipeline).execute(any(SectionPipeline.SectionRequest.class));
        verifyNoInteractions(toolExecutor);
    }

    @Test
    void shouldReturnPipelineResponseWhenCreatingSection() {
        // GIVEN
        when(sectionPipeline.execute(any())).thenReturn("section-result");
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Hero");

        // WHEN
        String result = penpotContentTools.createSection(spec, 0, 0);

        // THEN
        assertThat(result).isEqualTo("section-result");
    }

    @Test
    void shouldDefaultCoordinatesToXEighty_YOneTwentyWhenNullIsProvided() {
        // GIVEN
        when(sectionPipeline.execute(any())).thenReturn("ok");
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Hero");
        ArgumentCaptor<SectionPipeline.SectionRequest> captor =
            ArgumentCaptor.forClass(SectionPipeline.SectionRequest.class);

        // WHEN
        penpotContentTools.createSection(spec, null, null);

        // THEN
        verify(sectionPipeline).execute(captor.capture());
        assertThat(captor.getValue().x()).isEqualTo(80);
        assertThat(captor.getValue().y()).isEqualTo(120);
    }

    @Test
    void shouldPassProvidedCoordinatesToSectionPipelineRequest() {
        // GIVEN
        when(sectionPipeline.execute(any())).thenReturn("ok");
        SectionSpec spec = new SectionSpec();
        spec.setTitle("Hero");
        ArgumentCaptor<SectionPipeline.SectionRequest> captor =
            ArgumentCaptor.forClass(SectionPipeline.SectionRequest.class);

        // WHEN
        penpotContentTools.createSection(spec, 15, 25);

        // THEN
        verify(sectionPipeline).execute(captor.capture());
        assertThat(captor.getValue().x()).isEqualTo(15);
        assertThat(captor.getValue().y()).isEqualTo(25);
    }

    @Test
    void shouldPassNullSpecDirectlyToSectionPipelineForValidation() {
        // GIVEN
        when(sectionPipeline.execute(any())).thenReturn("ok");
        ArgumentCaptor<SectionPipeline.SectionRequest> captor =
            ArgumentCaptor.forClass(SectionPipeline.SectionRequest.class);

        // WHEN
        penpotContentTools.createSection(null, 0, 0);

        // THEN
        verify(sectionPipeline).execute(captor.capture());
        assertThat(captor.getValue().spec()).isNull();
    }
}