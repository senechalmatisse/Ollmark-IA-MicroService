package com.penpot.ai.application.tools;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.penpot.ai.application.tools.support.PenpotToolExecutor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PenpotShapeTools.class)
@DisplayName("PenpotShapeTools - Integration Tests")
class PenpotShapeToolsTest {

    @Autowired
    private PenpotShapeTools penpotShapeTools;

    @MockitoBean
    private PenpotToolExecutor toolExecutor;

    @Test
    @DisplayName("Workflow complet marketing : board + rectangle + text")
    void shouldCreateMarketingLayoutSuccessfully() {
        // GIVEN
        String boardId = UUID.randomUUID().toString();
        String rectId = UUID.randomUUID().toString();
        String textId = UUID.randomUUID().toString();

        when(toolExecutor.createShape(anyString(), eq("board")))
                .thenReturn(boardId);
        when(toolExecutor.createShape(anyString(), eq("rectangle")))
                .thenReturn(rectId);
        when(toolExecutor.createShape(anyString(), eq("text")))
                .thenReturn(textId);

        // WHEN
        String createdBoard = penpotShapeTools.createBoard(1080, 1080, "Instagram Post", "#FFFFFF");
        String createdRect = penpotShapeTools.createRectangle(100, 200, 400, 300, "#FF5733", "Hero Block");
        String createdText = penpotShapeTools.createText("OFFRE SPÉCIALE", 150, 250, 48, "bold", "#000000", "Title");

        // THEN
        assertThat(createdBoard).isEqualTo(boardId);
        assertThat(createdRect).isEqualTo(rectId);
        assertThat(createdText).isEqualTo(textId);

        verify(toolExecutor, times(3)).createShape(anyString(), anyString());
    }

    @Test
    @DisplayName("Boolean operation réelle entre deux shapes existantes")
    void shouldCombineTwoShapesWithUnion() {
        // GIVEN
        String shape1 = UUID.randomUUID().toString();
        String shape2 = UUID.randomUUID().toString();
        String booleanResult = UUID.randomUUID().toString();

        when(toolExecutor.createShape(anyString(), eq("boolean")))
                .thenReturn(booleanResult);

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        String result = penpotShapeTools.createBoolean(
                "union",
                shape1 + "," + shape2,
                "Combined Shape"
        );

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("boolean"));

        String generatedJs = codeCaptor.getValue();

        assertThat(generatedJs)
                .contains(shape1)
                .contains(shape2)
                .contains("createBoolean('union'")
                .contains("result.name = 'Combined Shape'")
                .contains("return result.id");

        assertThat(result).isEqualTo(booleanResult);
    }

    @Test
    @DisplayName("Création étoile + fallback paramètres par défaut")
    void shouldCreateStarWithDefaultFallbacks() {
        // GIVEN
        String starId = UUID.randomUUID().toString();

        when(toolExecutor.createShape(anyString(), eq("star")))
                .thenReturn(starId);

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        String result = penpotShapeTools.createStar(
                50, 50,
                120, 120,
                null,
                null,
                null,
                null
        );

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("star"));

        String js = codeCaptor.getValue();

        assertThat(js)
                .contains("createShapeFromSvg")
                .contains("fill='#CCCCCC'") 
                .contains("group.x = 50")
                .contains("return group.id");

        assertThat(result).isEqualTo(starId);
    }

    @Test
    @DisplayName("Robustesse : type boolean null → fallback union")
    void shouldFallbackToUnionWhenBooleanTypeIsNull() {
        // GIVEN
        String id = UUID.randomUUID().toString();

        when(toolExecutor.createShape(anyString(), eq("boolean")))
                .thenReturn(id);

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotShapeTools.createBoolean(
                null,
                "uuid1,uuid2",
                null
        );

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("boolean"));

        assertThat(codeCaptor.getValue())
                .contains("createBoolean('union'");
    }

    // ─── createRectangle ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createRectangle : vérifie le JS produit (position, resize, fills, name)")
    void shouldGenerateCorrectJs_ForRectangle() {
        // GIVEN
        Integer x = 100, y = 200, width = 400, height = 300;
        String fillColor = "#FF5733";
        String name = "Hero Block";
        String rectId = UUID.randomUUID().toString();
        when(toolExecutor.createShape(anyString(), eq("rectangle"))).thenReturn(rectId);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        String result = penpotShapeTools.createRectangle(x, y, width, height, fillColor, name);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("rectangle"));
        String js = codeCaptor.getValue();

        assertThat(js)
                .contains("penpot.createRectangle()")
                .contains("rect.x = 100;")
                .contains("rect.y = 200;")
                .contains("rect.resize(400, 300);")
                .contains("rect.fills = [{ fillColor: '#FF5733' }];")
                .contains("rect.name = 'Hero Block';")
                .contains("return rect.id;");

        assertThat(result).isEqualTo(rectId);
    }

    // ─── createEllipse ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createEllipse : vérifie le JS produit (position, resize, fills, name)")
    void shouldGenerateCorrectJs_ForEllipse() {
        // GIVEN
        Integer x = 50, y = 75, width = 200, height = 150;
        String fillColor = "#3399FF";
        String name = "Mon Cercle";
        String ellipseId = UUID.randomUUID().toString();
        when(toolExecutor.createShape(anyString(), eq("ellipse"))).thenReturn(ellipseId);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        String result = penpotShapeTools.createEllipse(x, y, width, height, fillColor, name);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("ellipse"));
        String js = codeCaptor.getValue();

        assertThat(js)
                .contains("penpot.createEllipse()")
                .contains("ellipse.x = 50;")
                .contains("ellipse.y = 75;")
                .contains("ellipse.resize(200, 150);")
                .contains("ellipse.fills = [{ fillColor: '#3399FF' }];")
                .contains("ellipse.name = 'Mon Cercle';")
                .contains("return ellipse.id;");

        assertThat(result).isEqualTo(ellipseId);
    }

    // ─── createText ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createText : vérifie le JS produit (content, position, fontSize, fontWeight, fills, name)")
    void shouldGenerateCorrectJs_ForText() {
        // GIVEN
        String content = "OFFRE SPECIALE";
        Integer x = 150, y = 250, fontSize = 48;
        String fontWeight = "bold";
        String fillColor = "#000000";
        String name = "Title";
        String textId = UUID.randomUUID().toString();
        when(toolExecutor.createShape(anyString(), eq("text"))).thenReturn(textId);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        String result = penpotShapeTools.createText(content, x, y, fontSize, fontWeight, fillColor, name);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("text"));
        String js = codeCaptor.getValue();

        assertThat(js)
                .contains("penpot.createText(")
                .contains("text.x = 150;")
                .contains("text.y = 250;")
                .contains("text.fontSize = 48;")
                .contains("text.fontWeight = 'bold';")
                .contains("text.fills = [{ fillColor: '#000000' }];")
                .contains("text.name = 'Title';")
                .contains("return text.id;");

        assertThat(result).isEqualTo(textId);
    }

    // ─── createBoard ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBoard : vérifie le JS produit (dimensions, name, backgroundColor)")
    void shouldGenerateCorrectJs_ForBoard() {
        // GIVEN
        Integer width = 1080, height = 1080;
        String name = "Instagram Post";
        String backgroundColor = "#FFFFFF";
        String boardId = UUID.randomUUID().toString();
        when(toolExecutor.createShape(anyString(), eq("board"))).thenReturn(boardId);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        String result = penpotShapeTools.createBoard(width, height, name, backgroundColor);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("board"));
        String js = codeCaptor.getValue();

        assertThat(js)
                .contains("penpot.createBoard()")
                .contains("board.resize(1080, 1080);")
                .contains("board.name = 'Instagram Post';")
                .contains("board.fills = [{ fillColor: '#FFFFFF' }];")
                .contains("return board.id;");

        assertThat(result).isEqualTo(boardId);
    }

    // ─── createStar ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createStar : vérifie le JS produit (SVG, position, fill, name)")
    void shouldGenerateCorrectJs_ForStar() {
        // GIVEN
        Integer x = 50, y = 60, width = 120, height = 120;
        Integer points = 5, innerRadius = 38;
        String fillColor = "#FFD700";
        String name = "Etoile";
        String starId = UUID.randomUUID().toString();
        when(toolExecutor.createShape(anyString(), eq("star"))).thenReturn(starId);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        String result = penpotShapeTools.createStar(x, y, width, height, points, innerRadius, fillColor, name);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("star"));
        String js = codeCaptor.getValue();

        assertThat(js)
                .contains("createShapeFromSvg")
                .contains("fill='#FFD700'")
                .contains("group.x = 50;")
                .contains("group.y = 60;")
                .contains("group.name = 'Etoile';")
                .contains("return group.id;");

        assertThat(result).isEqualTo(starId);
    }

    @Test
    @DisplayName("createStar : fallback couleur #CCCCCC quand fillColor est null")
    void shouldUseDefaultColor_WhenStarFillColorIsNull() {
        // GIVEN
        when(toolExecutor.createShape(anyString(), eq("star"))).thenReturn("id");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotShapeTools.createStar(0, 0, 100, 100, null, null, null, null);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("star"));
        assertThat(codeCaptor.getValue()).contains("fill='#CCCCCC'");
    }

    // ─── createTriangle ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createTriangle equilateral : vérifie le JS produit (SVG, position, fill, name)")
    void shouldGenerateCorrectJs_ForTriangleEquilateral() {
        // GIVEN
        Integer x = 10, y = 20, width = 200, height = 180;
        String type = "equilateral";
        String fillColor = "#AA00FF";
        String name = "Tri";
        String triangleId = UUID.randomUUID().toString();
        when(toolExecutor.createShape(anyString(), eq("triangle"))).thenReturn(triangleId);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        String result = penpotShapeTools.createTriangle(x, y, width, height, type, fillColor, name);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("triangle"));
        String js = codeCaptor.getValue();

        assertThat(js)
                .contains("createShapeFromSvg")
                .contains("fill='#AA00FF'")
                .contains("M 100.0,0")
                .contains("group.x = 10;")
                .contains("group.y = 20;")
                .contains("group.name = 'Tri';")
                .contains("return group.id;");

        assertThat(result).isEqualTo(triangleId);
    }

    @Test
    @DisplayName("createTriangle right : vérifie que le path commence par M 0,0")
    void shouldGenerateCorrectPath_ForTriangleRight() {
        // GIVEN
        Integer x = 0, y = 0, width = 100, height = 80;
        String type = "right";
        when(toolExecutor.createShape(anyString(), eq("triangle"))).thenReturn("id");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotShapeTools.createTriangle(x, y, width, height, type, null, null);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("triangle"));
        assertThat(codeCaptor.getValue())
                .contains("M 0,0")
                .contains("L 100,80")
                .contains("L 0,80");
    }

    @Test
    @DisplayName("createTriangle isosceles : vérifie que le path contient le sommet central")
    void shouldGenerateCorrectPath_ForTriangleIsosceles() {
        // GIVEN
        Integer x = 0, y = 0, width = 100, height = 80;
        String type = "isosceles";
        when(toolExecutor.createShape(anyString(), eq("triangle"))).thenReturn("id");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotShapeTools.createTriangle(x, y, width, height, type, null, null);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("triangle"));
        assertThat(codeCaptor.getValue())
                .contains("M 50.0,0")
                .contains("L 100,80")
                .contains("L 0,80");
    }

    // ─── createBoolean ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBoolean union : vérifie le JS produit (getShapeById, type, name)")
    void shouldGenerateCorrectJs_ForBooleanUnion() {
        // GIVEN
        String shape1 = UUID.randomUUID().toString();
        String shape2 = UUID.randomUUID().toString();
        String boolType = "union";
        String name = "Fusion";
        String boolId = UUID.randomUUID().toString();
        when(toolExecutor.createShape(anyString(), eq("boolean"))).thenReturn(boolId);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        String result = penpotShapeTools.createBoolean(boolType, shape1 + "," + shape2, name);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("boolean"));
        String js = codeCaptor.getValue();

        assertThat(js)
                .contains("getShapeById('" + shape1 + "')")
                .contains("getShapeById('" + shape2 + "')")
                .contains("createBoolean('union'")
                .contains("result.name = 'Fusion'")
                .contains("return result.id");

        assertThat(result).isEqualTo(boolId);
    }

    @Test
    @DisplayName("createBoolean subtract : vérifie la résolution vers 'difference'")
    void shouldResolveToDifference_WhenBooleanTypeIsSubtract() {
        // GIVEN
        String boolType = "subtract";
        when(toolExecutor.createShape(anyString(), eq("boolean"))).thenReturn("id");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotShapeTools.createBoolean(boolType, "uuid-a,uuid-b", null);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("boolean"));
        assertThat(codeCaptor.getValue()).contains("createBoolean('difference'");
    }

    @Test
    @DisplayName("createBoolean intersect : vérifie la résolution vers 'intersection'")
    void shouldResolveToIntersection_WhenBooleanTypeIsIntersect() {
        // GIVEN
        String boolType = "intersect";
        when(toolExecutor.createShape(anyString(), eq("boolean"))).thenReturn("id");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotShapeTools.createBoolean(boolType, "uuid-a,uuid-b", null);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("boolean"));
        assertThat(codeCaptor.getValue()).contains("createBoolean('intersection'");
    }

    @Test
    @DisplayName("createBoolean null type : fallback vers 'union'")
    void shouldFallbackToUnion_WhenBooleanTypeIsNull() {
        // GIVEN
        when(toolExecutor.createShape(anyString(), eq("boolean"))).thenReturn("id");
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // WHEN
        penpotShapeTools.createBoolean(null, "uuid1,uuid2", null);

        // THEN
        verify(toolExecutor).createShape(codeCaptor.capture(), eq("boolean"));
        assertThat(codeCaptor.getValue()).contains("createBoolean('union'");
    }

    // ─── Workflow ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Workflow complet : board + rectangle + text → 3 appels createShape")
    void shouldInvokeCreateShape_ThreeTimesForMarketingLayout() {
        // GIVEN
        String boardId = UUID.randomUUID().toString();
        String rectId  = UUID.randomUUID().toString();
        String textId  = UUID.randomUUID().toString();

        when(toolExecutor.createShape(anyString(), eq("board"))).thenReturn(boardId);
        when(toolExecutor.createShape(anyString(), eq("rectangle"))).thenReturn(rectId);
        when(toolExecutor.createShape(anyString(), eq("text"))).thenReturn(textId);

        // WHEN
        String createdBoard = penpotShapeTools.createBoard(1080, 1080, "Instagram Post", "#FFFFFF");
        String createdRect  = penpotShapeTools.createRectangle(100, 200, 400, 300, "#FF5733", "Hero Block");
        String createdText  = penpotShapeTools.createText("OFFRE SPECIALE", 150, 250, 48, "bold", "#000000", "Title");

        // THEN
        assertThat(createdBoard).isEqualTo(boardId);
        assertThat(createdRect).isEqualTo(rectId);
        assertThat(createdText).isEqualTo(textId);

        verify(toolExecutor, times(3)).createShape(anyString(), anyString());
    }
}