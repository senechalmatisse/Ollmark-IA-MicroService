package com.penpot.ai.application.tools;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.penpot.ai.application.tools.support.PenpotToolExecutor;

@SpringBootTest
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
}