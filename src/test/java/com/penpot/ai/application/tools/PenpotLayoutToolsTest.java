package com.penpot.ai.application.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.application.tools.support.PenpotToolExecutor;
import com.penpot.ai.application.tools.support.ToolResponseBuilder;
import com.penpot.ai.core.domain.TaskResult;

/**
 * Tests d'intégration Spring de {@link PenpotLayoutTools}.
 *
 * <p>Ces tests valident le comportement du composant dans un contexte Spring réel avec :</p>
 * <ul>
 *   <li>une vraie instanciation du bean {@link PenpotLayoutTools} ;</li>
 *   <li>un {@link ObjectMapper} réel ;</li>
 *   <li>le chargement réel des scripts JavaScript via les ressources de classe ;</li>
 *   <li>un {@link PenpotToolExecutor} doublé côté test pour capturer le code généré
 *       et exécuter immédiatement le formatter métier.</li>
 * </ul>
 *
 * <p>Le but n'est pas de retester tous les détails unitaires déjà couverts, mais de vérifier
 * les scénarios d'intégration utiles : câblage Spring, construction du code, lecture des
 * réponses, et robustesse face aux différents formats de {@link TaskResult}.</p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    PenpotLayoutTools.class,
    PenpotLayoutToolsTest.TestConfig.class
})
class PenpotLayoutToolsTest {

    @TestConfiguration
    static class TestConfig {

        /**
         * Fournit un {@link ObjectMapper} réel au bean testé.
         *
         * @return instance standard de {@link ObjectMapper}
         */
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        /**
         * Fournit un double de {@link PenpotToolExecutor} afin de contrôler
         * les résultats renvoyés par l'exécution dans les tests.
         *
         * @return mock Mockito de {@link PenpotToolExecutor}
         */
        @Bean
        PenpotToolExecutor penpotToolExecutor() {
            return mock(PenpotToolExecutor.class);
        }
    }

    @Autowired
    private PenpotLayoutTools tools;

    @Autowired
    private PenpotToolExecutor toolExecutor;

    /**
     * Réinitialise le mock d'exécuteur avant chaque test afin d'éviter toute pollution
     * entre les scénarios.
     */
    @BeforeEach
    void setUp() {
        reset(toolExecutor);
    }

    /**
     * Vérifie qu'un alignement horizontal valide :
     * <ul>
     *   <li>utilise bien le bean Spring réel ;</li>
     *   <li>charge effectivement les ressources JavaScript du classpath ;</li>
     *   <li>nettoie la liste d'IDs ;</li>
     *   <li>normalise l'expression française {@code "centre horizontal"} ;</li>
     *   <li>formate correctement la réponse à partir d'une chaîne JSON.</li>
     * </ul>
     */
    @Test
    void alignShapes_horizontalAlias_buildsCodeAndFormatsJsonResponse() {
        ExecutionCapture capture = stubExecutorReturning(
            "{\"ids\":[\"id-1\",\"\",null,\"id-2\"]}"
        );

        String result = tools.alignShapes(" id-1 , , id-2 ", "centre horizontal");

        assertEquals("aligned", capture.label);
        assertContains(capture.code, "const shapeIds = ['id-1', 'id-2'];");
        assertContains(capture.code, "const alignment = 'center';");
        assertContains(capture.code, "const resolvedShapes = shapeIds.map(id => page.getShapeById(id)).filter(Boolean);");

        assertEquals(
            ToolResponseBuilder.multiShapeOperation("aligned", List.of("id-1", "id-2")),
            result
        );
    }

    /**
     * Vérifie qu'un alignement vertical avec synonymes français :
     * <ul>
     *   <li>passe par la normalisation métier ;</li>
     *   <li>produit la variable JavaScript attendue ;</li>
     *   <li>interprète correctement une réponse imbriquée dans le champ {@code result}.</li>
     * </ul>
     */
    @Test
    void alignShapes_verticalAlias_readsNestedResults() {
        ExecutionCapture capture = stubExecutorReturning(
            Map.of(
                "result",
                "{\"results\":[{\"id\":\"g1\"},{\"id\":\"\"},{\"foo\":\"x\"},null,{\"id\":\"g2\"}]}"
            )
        );

        String result = tools.alignShapes("g1,g2", "milieu vertical");

        assertEquals("aligned", capture.label);
        assertContains(capture.code, "const shapeIds = ['g1', 'g2'];");
        assertContains(capture.code, "const alignment = 'center';");

        assertEquals(
            ToolResponseBuilder.multiShapeOperation("aligned", List.of("g1", "g2")),
            result
        );
    }

    /**
     * Vérifie que les entrées invalides d'alignement sont rejetées avant tout appel
     * à l'exécuteur.
     */
    @Test
    void alignShapes_invalidAlignment_returnsErrorWithoutCallingExecutor() {
        String result = tools.alignShapes("id1,id2", "diagonal");

        assertEquals(
            ToolResponseBuilder.error("Invalid alignment: diagonal"),
            result
        );
        verifyNoInteractions(toolExecutor);
    }

    /**
     * Vérifie qu'une distribution verticale valide :
     * <ul>
     *   <li>construit le bon préambule JavaScript ;</li>
     *   <li>lit les IDs depuis une réponse de type {@code Map} ;</li>
     *   <li>retourne la bonne réponse métier.</li>
     * </ul>
     */
    @Test
    void distributeShapes_vertical_buildsCodeAndReadsIdsFromMap() {
        ExecutionCapture capture = stubExecutorReturning(
            Map.of("ids", List.of("v1", "v2", "v3"))
        );

        String result = tools.distributeShapes("v1,v2,v3", "vertical");

        assertEquals("distributed", capture.label);
        assertContains(capture.code, "const shapeIds = ['v1', 'v2', 'v3'];");
        assertContains(capture.code, "const resolvedShapes = shapeIds.map(id => page.getShapeById(id)).filter(Boolean);");

        assertEquals(
            ToolResponseBuilder.multiShapeOperation("distributed", List.of("v1", "v2", "v3")),
            result
        );
    }

    /**
     * Vérifie que les axes invalides sont rejetés immédiatement sans exécution.
     */
    @Test
    void distributeShapes_invalidAxis_returnsErrorWithoutCallingExecutor() {
        String result = tools.distributeShapes("a,b,c", "diagonal");

        assertEquals(
            ToolResponseBuilder.error("Invalid axis: diagonal"),
            result
        );
        verifyNoInteractions(toolExecutor);
    }

    /**
     * Vérifie que l'ajout dans un board :
     * <ul>
     *   <li>injecte correctement le {@code boardId} ;</li>
     *   <li>nettoie les IDs transmis ;</li>
     *   <li>retourne uniquement les IDs valides fournis par le résultat.</li>
     * </ul>
     */
    @Test
    void addShapeToBoard_injectsBoardIdAndFormatsReturnedIds() {
        ExecutionCapture capture = stubExecutorReturning(
            Map.of("ids", Arrays.asList("s1", "", null, "s2"))
        );

        String result = tools.addShapeToBoard(" s1 , , s2 ", "board-123");

        assertEquals("moved to board", capture.label);
        assertContains(capture.code, "const shapeIds = ['s1', 's2'];");
        assertContains(capture.code, "const boardId = 'board-123';");

        assertEquals(
            ToolResponseBuilder.multiShapeOperation("moved to board", List.of("s1", "s2")),
            result
        );
    }

    /**
     * Vérifie que l'extraction d'une shape de son parent reste robuste lorsque
     * la donnée retournée ressemble à du JSON mais n'est pas parsable.
     *
     * <p>Le composant ne doit pas tomber en erreur et doit simplement produire
     * une réponse cohérente avec une liste vide.</p>
     */
    @Test
    void removeShapeFromParent_handlesMalformedJsonGracefully() {
        ExecutionCapture capture = stubExecutorReturning("{oops}");

        String result = tools.removeShapeFromParent("id1,id2");

        assertEquals("extracted", capture.label);
        assertContains(capture.code, "const shapeIds = ['id1', 'id2'];");

        assertEquals(
            ToolResponseBuilder.multiShapeOperation("extracted", List.of()),
            result
        );
    }

    /**
     * Vérifie que le groupement :
     * <ul>
     *   <li>convertit un nom vide en {@code null} côté JavaScript ;</li>
     *   <li>tolère une liste d'IDs absente ;</li>
     *   <li>retombe sur {@code unknown} quand {@code groupId} est vide ou blanc.</li>
     * </ul>
     */
    @Test
    void groupShapes_blankNameAndBlankGroupId_usesNullNameAndFallback() {
        ExecutionCapture capture = stubExecutorReturning(
            Map.of("groupId", "   ")
        );

        String result = tools.groupShapes(null, "   ");

        assertEquals("group shapes", capture.label);
        assertContains(capture.code, "const shapeIds = [];");
        assertContains(capture.code, "const groupName = null;");

        assertEquals(
            ToolResponseBuilder.groupCreated("unknown"),
            result
        );
    }

    /**
     * Vérifie que le groupement avec nom explicite retourne l'identifiant créé
     * lorsqu'il est fourni dans une réponse JSON.
     */
    @Test
    void groupShapes_withName_returnsCreatedGroupId() {
        ExecutionCapture capture = stubExecutorReturning(
            "{\"groupId\":\"group-42\"}"
        );

        String result = tools.groupShapes("id1,id2", "Team Alpha");

        assertEquals("group shapes", capture.label);
        assertContains(capture.code, "const shapeIds = ['id1', 'id2'];");
        assertContains(capture.code, "const groupName = 'Team Alpha';");

        assertEquals(
            ToolResponseBuilder.groupCreated("group-42"),
            result
        );
    }

    /**
     * Vérifie que la duplication :
     * <ul>
     *   <li>applique les offsets par défaut quand ils ne sont pas fournis ;</li>
     *   <li>utilise le préambule mono-shape ;</li>
     *   <li>retombe sur {@code unknown} si aucun {@code cloneId} exploitable n'est renvoyé.</li>
     * </ul>
     */
    @Test
    void cloneShape_withoutOffsets_usesDefaultsAndFallback() {
        ExecutionCapture capture = stubExecutorReturning(null);

        String result = tools.cloneShape("shape-1", null, null);

        assertEquals("clone shape", capture.label);
        assertContains(capture.code, "const shapeId = 'shape-1';");
        assertContains(capture.code, "const offsetX = 20;");
        assertContains(capture.code, "const offsetY = 20;");

        assertEquals(
            ToolResponseBuilder.shapeCloned("unknown"),
            result
        );
    }

    /**
     * Vérifie que la duplication sait récupérer un {@code cloneId} imbriqué
     * dans un champ {@code result}.
     */
    @Test
    void cloneShape_withCustomOffsets_readsNestedCloneId() {
        ExecutionCapture capture = stubExecutorReturning(
            Map.of("result", Map.of("cloneId", "clone-7"))
        );

        String result = tools.cloneShape("shape-9", 45, 60);

        assertEquals("clone shape", capture.label);
        assertContains(capture.code, "const shapeId = 'shape-9';");
        assertContains(capture.code, "const offsetX = 45;");
        assertContains(capture.code, "const offsetY = 60;");

        assertEquals(
            ToolResponseBuilder.shapeCloned("clone-7"),
            result
        );
    }

    /**
     * Vérifie que le dégroupement sait lire les IDs utiles depuis le champ
     * {@code results} en ignorant les entrées nulles, incomplètes ou vides.
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

        String result = tools.ungroupShapes("u1,u2");

        assertEquals("ungrouped", capture.label);
        assertContains(capture.code, "const shapeIds = ['u1', 'u2'];");

        assertEquals(
            ToolResponseBuilder.multiShapeOperation("ungrouped", List.of("u1", "u2")),
            result
        );
    }

    /**
     * Vérifie de manière consolidée les quatre opérations de z-order.
     *
     * <p>Chaque appel doit :</p>
     * <ul>
     *   <li>injecter les bons IDs ;</li>
     *   <li>produire la bonne action JavaScript ;</li>
     *   <li>retourner le bon label métier.</li>
     * </ul>
     */
    @Test
    void zOrder_operations_useExpectedActionsAndLabels() {
        assertZOrderInvocation("z1,z2", tools::sendBackward, "sendBackward", "sent backward");
        assertZOrderInvocation("z1,z2", tools::bringForward, "bringForward", "brought forward");
        assertZOrderInvocation("z1,z2", tools::sendToBack, "sendToBack", "sent to back");
        assertZOrderInvocation("z1,z2", tools::bringToFront, "bringToFront", "brought to front");
    }

    /**
     * Configure le mock d'exécution pour capturer le code JavaScript généré,
     * le libellé métier, puis exécuter immédiatement le formatter sur un
     * {@link TaskResult} simulé.
     *
     * @param resultData donnée renvoyée par {@link TaskResult#getData()}
     * @return objet de capture contenant le code et le label reçus par l'exécuteur
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
     * Vérifie de manière mutualisée une opération publique de z-order.
     *
     * @param ids IDs des shapes
     * @param method méthode publique à tester
     * @param expectedAction action JavaScript attendue
     * @param expectedLabel label métier attendu
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

        String result = method.apply(ids);

        assertEquals(expectedLabel, capture.label);
        assertContains(capture.code, "const shapeIds = ['z1', 'z2'];");
        assertContains(capture.code, "const action = '" + expectedAction + "';");

        assertEquals(
            ToolResponseBuilder.multiShapeOperation(expectedLabel, List.of("z1", "z2")),
            result
        );

        reset(toolExecutor);
    }

    /**
     * Vérifie qu'un fragment attendu est bien présent dans le code JavaScript généré.
     *
     * @param actual code JavaScript généré
     * @param expectedFragment fragment attendu
     */
    private void assertContains(String actual, String expectedFragment) {
        assertTrue(
            actual.contains(expectedFragment),
            () -> "\nFragment attendu:\n" + expectedFragment + "\n\nCode généré:\n" + actual
        );
    }

    /**
     * Petit conteneur utilisé pour récupérer le code JavaScript et le label
     * transmis à l'exécuteur par le composant testé.
     */
    private static final class ExecutionCapture {
        private String code;
        private String label;
    }
}