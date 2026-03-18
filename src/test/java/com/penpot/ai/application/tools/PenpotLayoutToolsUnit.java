package com.penpot.ai.application.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.application.tools.support.JsScriptLoader;
import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import com.penpot.ai.application.tools.support.ToolResponseBuilder;
import com.penpot.ai.core.domain.TaskResult;

/**
 * Tests unitaires de {@link PenpotLayoutTools}.
 *
 * <p>Ces tests vérifient principalement :</p>
 * <ul>
 *   <li>la sélection du bon script JavaScript selon l'action demandée ;</li>
 *   <li>la normalisation des entrées utilisateur (alignement, axes, IDs, offsets) ;</li>
 *   <li>la construction correcte du code JavaScript transmis au {@link PenpotToolExecutor} ;</li>
 *   <li>le formatage correct de la réponse à partir des différents formats de {@link TaskResult} ;</li>
 *   <li>les chemins de repli lorsque les données de retour sont absentes, invalides ou inexploitables.</li>
 * </ul>
 *
 * <p>Le but est de couvrir les branches réellement importantes du composant, sans écrire
 * des tests triviaux ou redondants.</p>
 */
class PenpotLayoutToolsUnitTest {

    private ObjectMapper objectMapper;
    private PenpotToolExecutor toolExecutor;
    private PenpotLayoutTools tools;

    /**
     * Initialise une instance fraîche du composant testé avec un {@link ObjectMapper}
     * réel et un {@link PenpotToolExecutor} mocké.
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        toolExecutor = mock(PenpotToolExecutor.class);
        tools = new PenpotLayoutTools(objectMapper, toolExecutor);
    }

    /**
     * Vérifie que {@code alignShapes} rejette immédiatement :
     * <ul>
     *   <li>un alignement {@code null} ;</li>
     *   <li>un alignement non supporté.</li>
     * </ul>
     *
     * <p>Ce test garantit que le validateur d'entrée coupe l'exécution avant tout appel
     * au {@link PenpotToolExecutor}.</p>
     */
    @Test
    void alignShapes_rejectsNullAndUnsupportedAlignment_withoutCallingExecutor() {
        String nullResult = tools.alignShapes("id1,id2", null);
        String invalidResult = tools.alignShapes("id1,id2", "diagonal");

        assertEquals(
            ToolResponseBuilder.error("Invalid alignment: null"),
            nullResult
        );
        assertEquals(
            ToolResponseBuilder.error("Invalid alignment: diagonal"),
            invalidResult
        );

        verifyNoInteractions(toolExecutor);
    }

    /**
     * Vérifie que {@code alignShapes} :
     * <ul>
     *   <li>normalise l'alias français {@code "centre horizontal"} en {@code "center"} ;</li>
     *   <li>charge le script horizontal ;</li>
     *   <li>nettoie la liste des IDs fournis ;</li>
     *   <li>lit correctement les IDs depuis une chaîne JSON renvoyée par le résultat.</li>
     * </ul>
     */
    @Test
    void alignShapes_usesHorizontalScript_normalizesFrenchAlias_andReadsIdsFromJsonString() {
        ExecutionCapture capture = stubExecutorReturning(
            "{\"ids\":[\"id-1\",\"\",null,\"id-2\"]}"
        );

        try (MockedStatic<JsScriptLoader> js = mockStatic(JsScriptLoader.class)) {
            js.when(() -> JsScriptLoader.load("tools/layout/align-horizontal.js"))
                .thenReturn("/*ALIGN_HORIZONTAL*/");

            String result = tools.alignShapes(" id-1 , , id-2 ", "centre horizontal");

            assertEquals("aligned", capture.label);
            assertContains(capture.code, "const shapeIds = ['id-1', 'id-2'];");
            assertContains(capture.code, "const alignment = 'center';");
            assertContains(capture.code, "/*ALIGN_HORIZONTAL*/");

            assertEquals(
                ToolResponseBuilder.multiShapeOperation("aligned", List.of("id-1", "id-2")),
                result
            );
        }
    }

    /**
     * Vérifie que {@code alignShapes} :
     * <ul>
     *   <li>normalise l'expression {@code "milieu vertical"} vers l'alignement vertical centré ;</li>
     *   <li>charge le script vertical ;</li>
     *   <li>convertit {@code middle} en {@code center} côté JavaScript ;</li>
     *   <li>lit correctement les IDs depuis un champ imbriqué {@code result} contenant du JSON.</li>
     * </ul>
     */
    @Test
    void alignShapes_usesVerticalScript_forMiddleSynonym_andReadsNestedResults() {
        ExecutionCapture capture = stubExecutorReturning(
            Map.of(
                "result",
                "{\"results\":[{\"id\":\"g1\"},{\"id\":\"\"},{\"foo\":\"x\"},null,{\"id\":\"g2\"}]}"
            )
        );

        try (MockedStatic<JsScriptLoader> js = mockStatic(JsScriptLoader.class)) {
            js.when(() -> JsScriptLoader.load("tools/layout/align-vertical.js"))
                .thenReturn("/*ALIGN_VERTICAL*/");

            String result = tools.alignShapes("g1,g2", "milieu vertical");

            assertEquals("aligned", capture.label);
            assertContains(capture.code, "const shapeIds = ['g1', 'g2'];");
            assertContains(capture.code, "const alignment = 'center';");
            assertContains(capture.code, "/*ALIGN_VERTICAL*/");

            assertEquals(
                ToolResponseBuilder.multiShapeOperation("aligned", List.of("g1", "g2")),
                result
            );
        }
    }

    /**
     * Vérifie que {@code distributeShapes} rejette :
     * <ul>
     *   <li>un axe {@code null} ;</li>
     *   <li>un axe inconnu.</li>
     * </ul>
     *
     * <p>Le test confirme aussi qu'aucune exécution n'est déclenchée dans ces cas.</p>
     */
    @Test
    void distributeShapes_rejectsNullAndInvalidAxis_withoutCallingExecutor() {
        String nullAxis = tools.distributeShapes("a,b,c", null);
        String invalidAxis = tools.distributeShapes("a,b,c", "diagonal");

        assertEquals(
            ToolResponseBuilder.error("Invalid axis: null"),
            nullAxis
        );
        assertEquals(
            ToolResponseBuilder.error("Invalid axis: diagonal"),
            invalidAxis
        );

        verifyNoInteractions(toolExecutor);
    }

    /**
     * Vérifie que {@code distributeShapes} :
     * <ul>
     *   <li>sélectionne le script horizontal ;</li>
     *   <li>tolère les espaces autour de l'axe fourni ;</li>
     *   <li>retourne une réponse vide lorsque les données de sortie ne sont pas du JSON exploitable.</li>
     * </ul>
     */
    @Test
    void distributeShapes_horizontal_usesHorizontalScript_andReturnsEmptyWhenDataIsPlainText() {
        ExecutionCapture capture = stubExecutorReturning("not-json-at-all");

        try (MockedStatic<JsScriptLoader> js = mockStatic(JsScriptLoader.class)) {
            js.when(() -> JsScriptLoader.load("tools/layout/distribute-horizontal.js"))
                .thenReturn("/*DIST_HORIZONTAL*/");

            String result = tools.distributeShapes("a,b,c", " horizontal ");

            assertEquals("distributed", capture.label);
            assertContains(capture.code, "const shapeIds = ['a', 'b', 'c'];");
            assertContains(capture.code, "/*DIST_HORIZONTAL*/");

            assertEquals(
                ToolResponseBuilder.multiShapeOperation("distributed", List.of()),
                result
            );
        }
    }

    /**
     * Vérifie que {@code distributeShapes} :
     * <ul>
     *   <li>sélectionne le script vertical ;</li>
     *   <li>récupère correctement les IDs depuis un {@code Map} retourné par l'exécution.</li>
     * </ul>
     */
    @Test
    void distributeShapes_vertical_usesVerticalScript_andReadsIdsFromMap() {
        ExecutionCapture capture = stubExecutorReturning(
            Map.of("ids", List.of("v1", "v2", "v3"))
        );

        try (MockedStatic<JsScriptLoader> js = mockStatic(JsScriptLoader.class)) {
            js.when(() -> JsScriptLoader.load("tools/layout/distribute-vertical.js"))
                .thenReturn("/*DIST_VERTICAL*/");

            String result = tools.distributeShapes("v1,v2,v3", "vertical");

            assertEquals("distributed", capture.label);
            assertContains(capture.code, "const shapeIds = ['v1', 'v2', 'v3'];");
            assertContains(capture.code, "/*DIST_VERTICAL*/");

            assertEquals(
                ToolResponseBuilder.multiShapeOperation("distributed", List.of("v1", "v2", "v3")),
                result
            );
        }
    }

    /**
     * Vérifie que {@code groupShapes} :
     * <ul>
     *   <li>transforme un nom de groupe vide en {@code null} côté JavaScript ;</li>
     *   <li>supporte une liste d'IDs nulle ;</li>
     *   <li>retombe sur {@code unknown} lorsque {@code groupId} est vide ou blanc.</li>
     * </ul>
     */
    @Test
    void groupShapes_withBlankName_andNullIds_setsNullGroupName_andFallsBackToUnknown() {
        ExecutionCapture capture = stubExecutorReturning(
            Map.of("groupId", "   ")
        );

        try (MockedStatic<JsScriptLoader> js = mockStatic(JsScriptLoader.class)) {
            js.when(() -> JsScriptLoader.load("tools/layout/group-shapes.js"))
                .thenReturn("/*GROUP*/");

            String result = tools.groupShapes(null, "   ");

            assertEquals("group shapes", capture.label);
            assertContains(capture.code, "const shapeIds = [];");
            assertContains(capture.code, "const groupName = null;");
            assertContains(capture.code, "/*GROUP*/");

            assertEquals(
                ToolResponseBuilder.groupCreated("unknown"),
                result
            );
        }
    }

    /**
     * Vérifie que {@code groupShapes} :
     * <ul>
     *   <li>injecte le nom du groupe lorsqu'il est fourni ;</li>
     *   <li>retourne l'identifiant du groupe créé lorsqu'il est présent dans la sortie JSON.</li>
     * </ul>
     */
    @Test
    void groupShapes_withProvidedName_returnsCreatedGroup() {
        ExecutionCapture capture = stubExecutorReturning(
            "{\"groupId\":\"group-42\"}"
        );

        try (MockedStatic<JsScriptLoader> js = mockStatic(JsScriptLoader.class)) {
            js.when(() -> JsScriptLoader.load("tools/layout/group-shapes.js"))
                .thenReturn("/*GROUP*/");

            String result = tools.groupShapes("id1,id2", "Team Alpha");

            assertEquals("group shapes", capture.label);
            assertContains(capture.code, "const shapeIds = ['id1', 'id2'];");
            assertContains(capture.code, "const groupName = 'Team Alpha';");
            assertContains(capture.code, "/*GROUP*/");

            assertEquals(
                ToolResponseBuilder.groupCreated("group-42"),
                result
            );
        }
    }

    /**
     * Vérifie que {@code cloneShape} :
     * <ul>
     *   <li>applique les offsets par défaut {@code 20/20} quand ils sont absents ;</li>
     *   <li>retombe sur {@code unknown} lorsque la sortie ne contient aucune donnée exploitable.</li>
     * </ul>
     */
    @Test
    void cloneShape_usesDefaultOffsets_andFallsBackToUnknownWhenNoData() {
        ExecutionCapture capture = stubExecutorReturning(null);

        try (MockedStatic<JsScriptLoader> js = mockStatic(JsScriptLoader.class)) {
            js.when(() -> JsScriptLoader.load("tools/layout/clone-shape.js"))
                .thenReturn("/*CLONE*/");

            String result = tools.cloneShape("shape-1", null, null);

            assertEquals("clone shape", capture.label);
            assertContains(capture.code, "const shapeId = 'shape-1';");
            assertContains(capture.code, "const offsetX = 20;");
            assertContains(capture.code, "const offsetY = 20;");
            assertContains(capture.code, "/*CLONE*/");

            assertEquals(
                ToolResponseBuilder.shapeCloned("unknown"),
                result
            );
        }
    }

    /**
     * Vérifie que {@code cloneShape} :
     * <ul>
     *   <li>injecte correctement des offsets explicites ;</li>
     *   <li>récupère le {@code cloneId} même lorsqu'il est imbriqué dans un champ {@code result}.</li>
     * </ul>
     */
    @Test
    void cloneShape_usesCustomOffsets_andReadsNestedCloneId() {
        ExecutionCapture capture = stubExecutorReturning(
            Map.of("result", Map.of("cloneId", "clone-7"))
        );

        try (MockedStatic<JsScriptLoader> js = mockStatic(JsScriptLoader.class)) {
            js.when(() -> JsScriptLoader.load("tools/layout/clone-shape.js"))
                .thenReturn("/*CLONE*/");

            String result = tools.cloneShape("shape-9", 45, 60);

            assertEquals("clone shape", capture.label);
            assertContains(capture.code, "const shapeId = 'shape-9';");
            assertContains(capture.code, "const offsetX = 45;");
            assertContains(capture.code, "const offsetY = 60;");
            assertContains(capture.code, "/*CLONE*/");

            assertEquals(
                ToolResponseBuilder.shapeCloned("clone-7"),
                result
            );
        }
    }

    /**
     * Vérifie que {@code addShapeToBoard} :
     * <ul>
     *   <li>injecte correctement l'identifiant du board de destination ;</li>
     *   <li>nettoie les IDs de shapes transmis ;</li>
     *   <li>retourne les IDs effectivement exploités dans la réponse finale.</li>
     * </ul>
     */
    @Test
    void addShapeToBoard_injectsBoardId_andReadsIdsList() {
        ExecutionCapture capture = stubExecutorReturning(
            Map.of("ids", Arrays.asList("s1", "", null, "s2"))
        );

        try (MockedStatic<JsScriptLoader> js = mockStatic(JsScriptLoader.class)) {
            js.when(() -> JsScriptLoader.load("tools/layout/add-shapes-to-board.js"))
                .thenReturn("/*ADD_TO_BOARD*/");

            String result = tools.addShapeToBoard(" s1 , , s2 ", "board-123");

            assertEquals("moved to board", capture.label);
            assertContains(capture.code, "const shapeIds = ['s1', 's2'];");
            assertContains(capture.code, "const boardId = 'board-123';");
            assertContains(capture.code, "/*ADD_TO_BOARD*/");

            assertEquals(
                ToolResponseBuilder.multiShapeOperation("moved to board", List.of("s1", "s2")),
                result
            );
        }
    }

    /**
     * Vérifie que {@code removeShapeFromParent} reste robuste lorsque la sortie ressemble
     * à du JSON mais n'est pas parsable.
     *
     * <p>Dans ce cas, le composant ne doit pas échouer et doit produire une réponse
     * cohérente avec une liste d'IDs vide.</p>
     */
    @Test
    void removeShapeFromParent_handlesMalformedJsonStringGracefully() {
        ExecutionCapture capture = stubExecutorReturning("{oops}");

        try (MockedStatic<JsScriptLoader> js = mockStatic(JsScriptLoader.class)) {
            js.when(() -> JsScriptLoader.load("tools/layout/remove-shapes.js"))
                .thenReturn("/*REMOVE*/");

            String result = tools.removeShapeFromParent("id1,id2");

            assertEquals("extracted", capture.label);
            assertContains(capture.code, "const shapeIds = ['id1', 'id2'];");
            assertContains(capture.code, "/*REMOVE*/");

            assertEquals(
                ToolResponseBuilder.multiShapeOperation("extracted", List.of()),
                result
            );
        }
    }

    /**
     * Vérifie que {@code ungroupShapes} sait extraire les IDs depuis le champ
     * {@code results} lorsque celui-ci contient :
     * <ul>
     *   <li>des entrées valides ;</li>
     *   <li>des IDs vides ;</li>
     *   <li>des entrées incomplètes ;</li>
     *   <li>des valeurs nulles.</li>
     * </ul>
     *
     * <p>Seuls les IDs valides doivent être conservés.</p>
     */
    @Test
    void ungroupShapes_readsIdsFromResultsList() {
        ExecutionCapture capture = stubExecutorReturning(
            Map.of(
                "results",
                Arrays.asList(
                    Map.of("id", "u1"),
                    Map.of("id", ""),
                    Map.of("foo", "x"),
                    null,
                    Map.of("id", "u2")
                )
            )
        );

        try (MockedStatic<JsScriptLoader> js = mockStatic(JsScriptLoader.class)) {
            js.when(() -> JsScriptLoader.load("tools/layout/ungroup-shapes.js"))
                .thenReturn("/*UNGROUP*/");

            String result = tools.ungroupShapes("u1,u2");

            assertEquals("ungrouped", capture.label);
            assertContains(capture.code, "const shapeIds = ['u1', 'u2'];");
            assertContains(capture.code, "/*UNGROUP*/");

            assertEquals(
                ToolResponseBuilder.multiShapeOperation("ungrouped", List.of("u1", "u2")),
                result
            );
        }
    }

    /**
     * Vérifie de manière factorisée les quatre méthodes publiques de gestion du z-order :
     * <ul>
     *   <li>{@code sendBackward}</li>
     *   <li>{@code bringForward}</li>
     *   <li>{@code sendToBack}</li>
     *   <li>{@code bringToFront}</li>
     * </ul>
     *
     * <p>Le test s'assure que chacune injecte la bonne action JavaScript et retourne
     * le bon libellé métier.</p>
     */
    @Test
    void zOrder_publicMethods_useExpectedActionsAndLabels() {
        assertZOrderInvocation("z1,z2", tools::sendBackward, "sendBackward", "sent backward");
        assertZOrderInvocation("z1,z2", tools::bringForward, "bringForward", "brought forward");
        assertZOrderInvocation("z1,z2", tools::sendToBack, "sendToBack", "sent to back");
        assertZOrderInvocation("z1,z2", tools::bringToFront, "bringToFront", "brought to front");
    }

    /**
     * Vérifie de manière mutualisée qu'une méthode de z-order :
     * <ul>
     *   <li>injecte bien les IDs ;</li>
     *   <li>utilise le bon nom d'action JavaScript ;</li>
     *   <li>transmet le bon label métier à l'exécuteur ;</li>
     *   <li>retourne la réponse finale attendue.</li>
     * </ul>
     *
     * @param ids IDs des shapes passés à la méthode
     * @param method référence vers la méthode publique à tester
     * @param expectedAction action JavaScript attendue
     * @param expectedLabel libellé métier attendu
     */
    private void assertZOrderInvocation(
        String ids,
        Function<String, String> method,
        String expectedAction,
        String expectedLabel
    ) {
        ExecutionCapture capture = stubExecutorReturning(
            Map.of("ids", List.of("z1", "z2"))
        );

        try (MockedStatic<JsScriptLoader> js = mockStatic(JsScriptLoader.class)) {
            js.when(() -> JsScriptLoader.load("tools/layout/z-order.js"))
                .thenReturn("/*Z_ORDER*/");

            String result = method.apply(ids);

            assertEquals(expectedLabel, capture.label);
            assertContains(capture.code, "const shapeIds = ['z1', 'z2'];");
            assertContains(capture.code, "const action = '" + expectedAction + "';");
            assertContains(capture.code, "/*Z_ORDER*/");

            assertEquals(
                ToolResponseBuilder.multiShapeOperation(expectedLabel, List.of("z1", "z2")),
                result
            );
        }
    }

    /**
     * Prépare un mock d'exécution capable :
     * <ul>
     *   <li>de capturer le code JavaScript généré ;</li>
     *   <li>de capturer le label métier transmis à l'exécuteur ;</li>
     *   <li>d'exécuter immédiatement le formatter avec un {@link TaskResult} simulé.</li>
     * </ul>
     *
     * <p>Cela permet de tester à la fois la construction de la requête et
     * l'interprétation de la réponse sans dépendre d'une vraie exécution Penpot.</p>
     *
     * @param resultData donnée simulée renvoyée par {@link TaskResult#getData()}
     * @return structure contenant le code JavaScript généré et le label transmis
     */
    private ExecutionCapture stubExecutorReturning(Object resultData) {
        ExecutionCapture capture = new ExecutionCapture();

        TaskResult taskResult = mock(TaskResult.class);
        when(taskResult.getData()).thenReturn(Optional.ofNullable(resultData));

        doAnswer(invocation -> {
            capture.code = invocation.getArgument(0);
            capture.label = invocation.getArgument(1);

            Function<TaskResult, String> formatter = invocation.getArgument(2);
            return formatter.apply(taskResult);
        }).when(toolExecutor).execute(
            anyString(),
            anyString(),
            ArgumentMatchers.<Function<TaskResult, String>>any()
        );

        return capture;
    }

    /**
     * Vérifie qu'un fragment attendu est bien présent dans le code JavaScript généré.
     *
     * @param actual code généré
     * @param expectedFragment fragment attendu
     */
    private void assertContains(String actual, String expectedFragment) {
        assertTrue(
            actual.contains(expectedFragment),
            () -> "\nFragment attendu:\n" + expectedFragment + "\n\nCode généré:\n" + actual
        );
    }

    /**
     * Petit conteneur utilitaire utilisé pour récupérer, depuis le mock de l'exécuteur,
     * le code JavaScript généré et le label métier transmis par le composant testé.
     */
    private static final class ExecutionCapture {
        private String code;
        private String label;
    }
}