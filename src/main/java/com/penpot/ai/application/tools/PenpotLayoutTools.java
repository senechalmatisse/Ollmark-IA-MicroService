package com.penpot.ai.application.tools;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.*;
import org.springframework.stereotype.Component;

import com.penpot.ai.application.tools.support.*;
import com.penpot.ai.core.domain.TaskResult;
import com.penpot.ai.shared.util.JsStringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tools de manipulation de layout et d'organisation spatiale pour Penpot.
 *
 * <h2>Responsabilités</h2>
 * <p>Regroupe toutes les opérations portant sur la structure et
 * l'organisation visuelle des shapes dans un document Penpot :</p>
 * <ul>
 *   <li><b>Déplacement</b> — rattachement d'une shape à une board cible</li>
 *   <li><b>Suppression</b> — retrait d'une shape de son parent</li>
 *   <li><b>Duplication</b> — clonage avec offset de position</li>
 *   <li><b>Alignement</b> — alignement multi-shapes sur un axe (left, center, right, top, middle, bottom)</li>
 *   <li><b>Distribution</b> — espacement uniforme entre shapes sur un axe</li>
 *   <li><b>Groupement</b> — création et dissolution de groupes</li>
 *   <li><b>Z-order</b> — contrôle de l'ordre d'empilement visuel</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PenpotLayoutTools {

    private final PenpotToolExecutor toolExecutor;

    private static final String UNKNOWN_ERROR = "Unknown error";

    /**
     * Ensemble des valeurs d'alignement reconnues par {@link #alignShapes}.
     * Toute valeur absente de cet ensemble est rejetée avec un message d'erreur.
     */
    private static final Set<String> VALID_ALIGNMENTS =
        Set.of("left", "center", "right", "top", "middle", "bottom");

    /**
     * Déplace une ou plusieurs shapes dans une board cible en les re-parentant.
     *
     * @param shapeIds identifiants des shapes à déplacer, séparés par des virgules ;
     *                 passer {@code "selection"} pour utiliser la sélection active
     * @param boardId  identifiant de la board de destination
     * @return réponse JSON standardisée décrivant les shapes déplacées,
     *         ou un message d'erreur si la board est introuvable
     */
    @Tool(description = """
        Move one or more shapes into a target board (frame).
        Useful to nest elements inside an existing frame or artboard.
    """)
    public String addShapeToBoard(
        @ToolParam(description = "One or more shape IDs to move, separated by commas.") String shapeIds,
        @ToolParam(description = """
            UUID of the target board that will receive the shapes.
            The board must already exist on the current page.
        """) String boardId
    ) {
        log.info("Tool called: addShapeToBoard({}, {})", shapeIds, boardId);
        List<String> ids = resolveIds(shapeIds);
        String code =
            String.format("""
            const board = penpot.currentPage.getShapeById('%s');
            if (!board) throw new Error('Board not found');
            """, boardId)
            + PenpotJsSnippets.collectShapesOrFallback(ids, "MoveToBoard")
            + """
            shapes.forEach(s=>{
                if(typeof board.appendChild === 'function')
                    board.appendChild(s);
                else if(typeof s.setParent === 'function')
                    s.setParent(board);
            });
            const _ids = shapes.map(s=>s.id);
            return "OK_MULTISHAPE:moved_to_board:"+_ids.length+":"+_ids.join(",");
            """;
        return executeMultiShapeOk(code, "moved to board");
    }

    /**
     * Supprime une ou plusieurs shapes de leur parent dans la page courante.
     *
     * @param shapeIds identifiants des shapes à supprimer, séparés par des virgules ;
     *                 passer {@code "selection"} pour utiliser la sélection active
     * @return réponse JSON standardisée listant les IDs supprimés
     */
    @Tool(description = """
        Remove one or more shapes from their parent element on the current page.
    """)
    public String removeShapeFromParent(
        @ToolParam(description = "One or more shape IDs to remove, separated by commas.") String shapeIds
    ) {
        log.info("Tool called: removeShapeFromParent({})", shapeIds);
        List<String> ids = resolveIds(shapeIds);
        String code =
            PenpotJsSnippets.collectShapesOrFallback(ids, "Remove")
            + """
            shapes.forEach(s=>{
                if (s && typeof s.remove === 'function') s.remove();
            });
            const _ids = shapes.map(s=>s.id);
            return "OK_MULTISHAPE:removed:"+_ids.length+":"+_ids.join(",");
            """;
        return executeMultiShapeOk(code, "removed");
    }

    /**
     * Clone une shape et applique un décalage de position au clone résultant.
     *
     * <p>La duplication est effectuée sur la première shape résolue depuis
     * {@code shapeId}. Si aucun offset n'est fourni, un décalage de
     * {@code (20, 20)} pixels est appliqué par défaut pour éviter de superposer
     * le clone à l'original.</p>
     *
     * @param shapeId identifiant de la shape à cloner
     * @param offsetX décalage horizontal en pixels appliqué au clone ;
     *                {@code null} pour utiliser la valeur par défaut (20)
     * @param offsetY décalage vertical en pixels appliqué au clone ;
     *                {@code null} pour utiliser la valeur par défaut (20)
     * @return réponse JSON standardisée contenant l'identifiant du clone créé
     */
    @Tool(description = """
        Duplicate (clone) a shape and offset the copy by a given number of pixels.
        Use this tool to quickly duplicate UI elements, icons, cards, or any shape
        while positioning the copy slightly away from the source.
    """)
    public String cloneShape(
        @ToolParam(description = "UUID of the shape to clone.") String shapeId,
        @ToolParam(required = false, description = """
            Horizontal offset in pixels applied to the cloned shape relative to the original.
            Positive values move the clone to the right, negative to the left (default: 20).
        """) Integer offsetX,
        @ToolParam(required = false, description = """
            Vertical offset in pixels applied to the cloned shape relative to the original.
            Positive values move the clone downward, negative upward (default: 20).
        """) Integer offsetY
    ) {
        log.info("Tool called: cloneShape({}, {}, {})", shapeId, offsetX, offsetY);
        int dx = offsetX != null ? offsetX : 20;
        int dy = offsetY != null ? offsetY : 20;
        List<String> ids = resolveIds(shapeId);
        String code =
            PenpotJsSnippets.findFirstShapeOrFallback(ids)
            + String.format("""
            const clone = shape.clone();
            if(!clone) throw new Error("Clone failed");
            clone.x = shape.x + %d;
            clone.y = shape.y + %d;
            const cid = clone?.id ?? "unknown";
            return "OK_CLONE:"+cid;
            """, dx, dy);
        return executeCloneOk(code);
    }

    /**
     * Aligne plusieurs shapes selon un mode d'alignement donné.
     *
     * <p>Le mode d'alignement est normalisé avant validation, ce qui permet
     * d'accepter certaines variantes françaises (ex : {@code "gauche"} → {@code "left"}).
     * Au moins deux shapes sont nécessaires pour que l'alignement soit effectif.</p>
     *
     * <p>Modes supportés :</p>
     * <ul>
     *   <li>{@code left} — aligne les bords gauches sur le bord gauche le plus à gauche</li>
     *   <li>{@code center} — centre horizontalement sur la moyenne des centres X</li>
     *   <li>{@code right} — aligne les bords droits sur le bord droit le plus à droite</li>
     *   <li>{@code top} — aligne les bords supérieurs sur le bord le plus haut</li>
     *   <li>{@code middle} — centre verticalement sur la moyenne des centres Y</li>
     *   <li>{@code bottom} — aligne les bords inférieurs sur le bord le plus bas</li>
     * </ul>
     *
     * @param shapeIds  identifiants des shapes à aligner, séparés par des virgules ;
     *                  passer {@code "selection"} pour utiliser la sélection active
     * @param alignment valeur d'alignement demandée (en anglais ou variante française)
     * @return réponse JSON standardisée décrivant les shapes alignées,
     *         ou un message d'erreur si le mode est invalide ou si moins de deux shapes
     *         sont fournies
     */
    @Tool(description = "Align two or more shapes along a common axis or edge.")
    public String alignShapes(
        @ToolParam(description = "Two or more shape IDs to align, separated by commas.") String shapeIds,
        @ToolParam(description = """
            Alignment mode to apply. Accepted values:
            "left", "center", "right" for horizontal alignment,
            "top", "middle", "bottom" for vertical alignment.
        """) String alignment
    ) {
        log.info("Tool called: alignShapes({}, {})", shapeIds, alignment);
        String normalized = normalizeAlignment(alignment);

        if (!VALID_ALIGNMENTS.contains(normalized)) {
            log.info("alignShapes -> invalid alignment: raw={}, normalized={}", alignment, normalized);
            return ToolResponseBuilder.error("Invalid alignment: " + alignment);
        }

        List<String> ids = resolveIds(shapeIds);
        String code =
            PenpotJsSnippets.collectShapesOrFallback(ids, "Align") +
            "if (shapes.length < 2) throw new Error('Need at least 2 shapes');\n" +
            buildAlignmentLogic(normalized) +
            """
            const _ids = shapes.map(s => s.id);
            return "OK_MULTISHAPE:aligned:" + _ids.length + ":" + _ids.join(",");
            """;
        return executeMultiShapeOk(code, "aligned");
    }

    /**
     * Distribue plusieurs shapes de manière homogène sur un axe.
     *
     * <p>L'espacement entre chaque paire de shapes adjacentes est rendu uniforme.
     * Les shapes aux extrémités ne sont pas déplacées. Au moins trois shapes
     * sont nécessaires pour que la distribution soit significative.</p>
     *
     * @param shapeIds identifiants des shapes à distribuer, séparés par des virgules ;
     *                 passer {@code "selection"} pour utiliser la sélection active
     * @param axis     axe de distribution : {@code "horizontal"} ou {@code "vertical"}
     * @return réponse JSON standardisée décrivant les shapes distribuées,
     *         ou un message d'erreur si l'axe est invalide ou si moins de trois shapes
     *         sont fournies
     */
    @Tool(description = """
        Distribute three or more shapes evenly along a horizontal or vertical axis.
    """)
    public String distributeShapes(
        @ToolParam(description = "Three or more shape IDs to distribute, separated by commas.") String shapeIds,
        @ToolParam(description = """
            Axis along which to distribute shapes evenly.
            Accepted values: "horizontal" (left to right) or "vertical" (top to bottom).
        """) String axis
    ) {
        log.info("Tool called: distributeShapes({}, {})", shapeIds, axis);
        if (axis == null ||
                (!axis.equalsIgnoreCase("horizontal")
                        && !axis.equalsIgnoreCase("vertical"))) {
            log.info("distributeShapes -> invalid axis: {}", axis);
            return ToolResponseBuilder.error("Invalid axis: " + axis);
        }

        List<String> ids = resolveIds(shapeIds);
        String code =
            PenpotJsSnippets.collectShapesOrFallback(ids, "Distribute") +
            "if (shapes.length < 3) throw new Error('Need at least 3 shapes');\n" +
            buildDistributionLogic(axis) +
            """
            const _ids = shapes.map(s => s.id);
            return "OK_MULTISHAPE:distributed:" + _ids.length + ":" + _ids.join(",");
            """;
        return executeMultiShapeOk(code, "distributed");
    }

    /**
     * Regroupe plusieurs shapes dans un même groupe Penpot.
     *
     * <p>Au moins deux shapes sont requises. Un nom optionnel peut être attribué
     * au groupe. Si aucun nom n'est fourni, le nom par défaut de Penpot est conservé.</p>
     *
     * @param shapeIds  identifiants des shapes à regrouper, séparés par des virgules ;
     *                  passer {@code "selection"} pour utiliser la sélection active
     * @param groupName nom optionnel à attribuer au groupe ; {@code null} ou blanc pour
     *                  laisser Penpot attribuer un nom automatiquement
     * @return réponse JSON standardisée contenant l'identifiant du groupe créé
     */
    @Tool(description = """
        Group two or more shapes into a single Penpot group.
        Use this tool to:
            - Bundle related UI components (icon + label) into a reusable group
            - Organize sections of a design for easier manipulation
            - Prepare shapes before moving them into a board together
    """)
    public String groupShapes(
        @ToolParam(description = "Two or more shape IDs to group together, separated by commas.") String shapeIds,
        @ToolParam(description = "Name for the resulting group.", required = false) String groupName
    ) {
        log.info("Tool called: groupShapes({}, {})", shapeIds, groupName);
        List<String> ids = resolveIds(shapeIds);
        StringBuilder code = new StringBuilder(PenpotJsSnippets.collectShapesOrFallback(ids, "Group"));

        code.append("""
            if (shapes.length < 2)
                throw new Error('Need at least 2 shapes');
            const group = penpot.group(shapes);
        """);

        if (groupName != null && !groupName.isBlank()) {
            code.append(String.format(
                "group.name='%s';%n",
                JsStringUtils.jsSafe(groupName)
            ));
        }

        code.append("""
            const gid = group?.id ?? "unknown";
            return "OK_GROUP:" + gid + ":" + shapes.length;
        """);
        return executeGroupOk(code.toString());
    }

    /**
     * Dissocie un ou plusieurs groupes Penpot, restituant leurs enfants au parent.
     *
     * @param groupIds identifiants des groupes à dissocier, séparés par des virgules ;
     *                 passer {@code "selection"} pour utiliser la sélection active
     * @return réponse JSON standardisée décrivant les groupes dissociés,
     *         ou un message d'erreur si aucun groupe valide n'est trouvé
     */
    @Tool(description = """
        Ungroup one or more Penpot groups, releasing their children back to the parent layer.
    """)
    public String ungroupShapes(
        @ToolParam(description = "One or more group IDs to ungroup, separated by commas.") String groupIds
    ) {
        log.info("Tool called: ungroupShapes({})", groupIds);
        List<String> ids = resolveIds(groupIds);
        String code =
            PenpotJsSnippets.collectShapesOrFallback(ids, "Ungroup") +
            """
            const groups = shapes.filter(s =>
                (s.type && s.type.toLowerCase() === 'group') || s.isGroup === true
            );
            if (groups.length === 0) throw new Error('No groups found');
            penpot.ungroup(groups);
            const _ids = groups.map(g => g.id);
            return "OK_MULTISHAPE:ungrouped:" + _ids.length + ":" + _ids.join(",");
            """;
        return executeMultiShapeOk(code, "ungrouped");
    }

    /**
     * Recule d'un cran une ou plusieurs shapes dans l'ordre d'empilement.
     * 
     * TODO : Les derniers tests n'ont pas fonctionnés
     * 
     * @param shapeIds identifiants des shapes à reculer, séparés par des virgules ;
     *                 passer {@code "selection"} pour utiliser la sélection active
     * @return réponse JSON standardisée décrivant les shapes réordonnées
     */
    @Tool(description = """
        Move one or more shapes one step backward in the layer stack (z-order).
        The shape will appear behind the element that was directly below it.
    """)
    public String sendShapeBackward(
        @ToolParam(description = "One or more shape IDs to send backward, separated by commas.") String shapeIds
    ) {
        log.info("Tool called: sendShapeBackward({})", shapeIds);
        return executeMultiShapeOk(
            buildZOrderCode(resolveIds(shapeIds), "sendBackward"), "sent backward");
    }

    /**
     * Avance d'un cran une ou plusieurs shapes dans l'ordre d'empilement.
     *
     * TODO : Les derniers tests n'ont pas fonctionnés
     * 
     * @param shapeIds identifiants des shapes à avancer, séparés par des virgules ;
     *                 passer {@code "selection"} pour utiliser la sélection active
     * @return réponse JSON standardisée décrivant les shapes réordonnées
     */
    @Tool(description = """
        Move one or more shapes one step forward in the layer stack (z-order).
        The shape will appear in front of the element that was directly above it.
    """)
    public String sendShapeFrontward(
        @ToolParam(description = "One or more shape IDs to bring forward, separated by commas.") String shapeIds
    ) {
        log.info("Tool called: sendShapeFrontward({})", shapeIds);
        return executeMultiShapeOk(
            buildZOrderCode(resolveIds(shapeIds), "bringForward"), "brought forward");
    }

    /**
     * Envoie tout au fond de la pile d'empilement une ou plusieurs shapes.
     *
     * TODO : Les derniers tests n'ont pas fonctionnés
     * 
     * @param shapeIds identifiants des shapes à envoyer à l'arrière-plan,
     *                 séparés par des virgules ;
     *                 passer {@code "selection"} pour utiliser la sélection active
     * @return réponse JSON standardisée décrivant les shapes réordonnées
     */
    @Tool(description = """
        Send one or more shapes to the very back of the layer stack (z-order).
        The shapes will appear behind all other elements in their parent.
    """)
    public String sendShapeToTheBack(
        @ToolParam(description = "One or more shape IDs to send to the back, separated by commas.") String shapeIds
    ) {
        log.info("Tool called: sendShapeToTheBack({})", shapeIds);
        return executeMultiShapeOk(
            buildZOrderCode(resolveIds(shapeIds), "sendToBack"), "sent to back");
    }

    /**
     * Place tout au premier plan une ou plusieurs shapes dans l'ordre d'empilement.
     *
     * TODO : Les derniers tests n'ont pas fonctionnés
     * 
     * @param shapeIds identifiants des shapes à amener au premier plan,
     *                 séparés par des virgules ;
     *                 passer {@code "selection"} pour utiliser la sélection active
     * @return réponse JSON standardisée décrivant les shapes réordonnées
     */
    @Tool(description = """
        Bring one or more shapes to the very front of the layer stack (z-order).
        The shapes will appear on top of all other elements in their parent.
    """)
    public String sendShapeToTheFront(
        @ToolParam(description = "One or more shape IDs to bring to the front, separated by commas.") String shapeIds
    ) {
        log.info("Tool called: sendShapeToTheFront({})", shapeIds);
        return executeMultiShapeOk(
            buildZOrderCode(resolveIds(shapeIds), "bringToFront"), "brought to front");
    }

    /**
     * Génère un script JavaScript robuste pour réordonner des shapes dans la pile d'empilement.
     *
     * <h3>Stratégie</h3>
     * <ol>
     *   <li>Les shapes sont regroupées par parent via une {@code Map} pour garantir
     *       un comportement cohérent lors du changement d'ordre.</li>
     *   <li>Pour chaque groupe de shapes partageant le même parent, la méthode
     *       privilégie {@code setParentIndex()} lorsqu'elle est disponible.</li>
     *   <li>En l'absence de {@code setParentIndex()}, le script tombe en repli sur
     *       la méthode native Penpot correspondant au mode demandé.</li>
     *   <li>Un micro-déplacement ({@code x += 0.001; x -= 0.001}) est appliqué
     *       après l'opération pour forcer le rafraîchissement visuel de Penpot.</li>
     * </ol>
     *
     * @param ids  liste des IDs des shapes à réordonner ;
     *             une liste vide signifie que la sélection courante sera utilisée
     * @param mode mode de réordonnancement parmi {@code bringToFront},
     *             {@code sendToBack}, {@code bringForward}, {@code sendBackward}
     * @return script JavaScript complet prêt à être exécuté dans le moteur Penpot
     */
    private String buildZOrderCode(List<String> ids, String mode) {
        return PenpotJsSnippets.collectShapesOrFallback(ids, "ZOrder") +
            """
            if (!shapes || shapes.length === 0)
                throw new Error('No shapes to reorder');

            const page = penpot.currentPage;

            function forceRedraw(shape){
                if(!shape) return;
                shape.x += 0.001;
                shape.x -= 0.001;
            }

            function reorder(parent, items){
                const kids = parent.children ?? [];
                const len = kids.length;
                items.forEach(s=>{
                    if(typeof s.setParentIndex === "function"){
                        const idx = kids.findIndex(k=>k.id===s.id);
                            if("%s"==="bringToFront")
                                s.setParentIndex(len-1);
                            else if("%s"==="sendToBack")
                                s.setParentIndex(0);
                            else if("%s"==="bringForward")
                                s.setParentIndex(Math.min(len-1,idx+1));
                            else if("%s"==="sendBackward")
                                s.setParentIndex(Math.max(0,idx-1));
                        } else {
                            if(typeof s["%s"]==="function")
                                s["%s"]();
                        }
                    });
                }

                const byParent=new Map();
                shapes.forEach(s=>{
                    const pid=s?.parent?.id ?? "__root";
                    if(!byParent.has(pid)) byParent.set(pid,[]);
                    byParent.get(pid).push(s);
                });

                byParent.forEach((arr,pid)=>{
                    const parent=pid==="__root" ? null : page.getShapeById(pid);
                    if(parent) reorder(parent,arr);
                });

                forceRedraw(shapes[0]);
                const _ids = shapes.map(s=>s.id);
                return "OK_MULTISHAPE:%s:"+_ids.length+":"+_ids.join(",");
            """.formatted(mode, mode, mode, mode, mode, mode, mode);
    }

    /**
     * Exécute un script retournant un marqueur {@code OK_MULTISHAPE} et construit
     * la réponse JSON standardisée correspondante.
     *
     * <p>En cas d'erreur d'exécution côté Penpot, une réponse d'erreur normalisée
     * est retournée. En cas de succès, les IDs extraits du marqueur sont transmis
     * à {@link ToolResponseBuilder#multiShapeOperation}.</p>
     *
     * @param code  script JavaScript à exécuter
     * @param label libellé métier de l'opération pour le logging et la réponse
     * @return réponse JSON standardisée décrivant l'opération multi-shapes
     */
    private String executeMultiShapeOk(String code, String label) {
        log.debug("Executing multi-shape op '{}' (code length: {})",
            label, code != null ? code.length() : 0);
        return toolExecutor.execute(code, label, result -> {
            if (!result.isSuccess())
                return ToolResponseBuilder.error(result.getError().orElse(UNKNOWN_ERROR));
            String ok = extractOkString(result);
            List<String> ids = parseOkMultiShapeIds(ok);
            return ToolResponseBuilder.multiShapeOperation(label, ids);
        });
    }

    /**
     * Exécute un script retournant un marqueur {@code OK_GROUP} et construit
     * la réponse JSON de création de groupe.
     *
     * @param code script JavaScript à exécuter
     * @return réponse JSON standardisée contenant l'ID du groupe créé,
     *         ou une erreur en cas d'échec
     */
    private String executeGroupOk(String code) {
        log.debug("Executing group op (code length: {})", code != null ? code.length() : 0);
        return toolExecutor.execute(code, "group", result -> {
            if (!result.isSuccess())
                return ToolResponseBuilder.error(result.getError().orElse(UNKNOWN_ERROR));
            String ok = extractOkString(result);
            String gid = parseOkGroupId(ok);
            return ToolResponseBuilder.groupCreated(gid);
        });
    }

    /**
     * Exécute un script retournant un marqueur {@code OK_CLONE} et construit
     * la réponse JSON de duplication.
     *
     * @param code script JavaScript à exécuter
     * @return réponse JSON standardisée contenant l'ID du clone créé,
     *         ou une erreur en cas d'échec
     */
    private String executeCloneOk(String code) {
        log.debug("Executing clone op (code length: {})", code != null ? code.length() : 0);
        return toolExecutor.execute(code, "clone", result -> {
            if (!result.isSuccess())
                return ToolResponseBuilder.error(result.getError().orElse(UNKNOWN_ERROR));
            String ok = extractOkString(result);
            String cid = parseOkCloneId(ok);
            return ToolResponseBuilder.shapeCloned(cid);
        });
    }

    /**
     * Extrait la chaîne brute de succès depuis un {@link TaskResult}.
     *
     * <p>Le résultat peut être une {@link String} directe ou encapsulé dans une
     * {@link Map} sous la clé {@code "result"}. Cette méthode centralise
     * l'extraction pour homogénéiser le parsing aval.</p>
     *
     * @param result résultat d'exécution du tool
     * @return la chaîne brute si trouvée, {@code null} sinon
     */
    private String extractOkString(TaskResult result) {
        Object data = result.getData().orElse(null);
        if (data instanceof String s) return s;
        if (data instanceof Map<?, ?> m) {
            Object r = m.get("result");
            if (r instanceof String s) return s;
        }
        return null;
    }

    /**
     * Extrait la liste des IDs depuis un marqueur {@code OK_MULTISHAPE}.
     *
     * <p>Format attendu : {@code OK_MULTISHAPE:operation:count:id1,id2,id3}</p>
     *
     * @param ok chaîne brute renvoyée par le script JavaScript
     * @return liste des IDs extraits, ou liste vide si le format est invalide
     */
    private List<String> parseOkMultiShapeIds(String ok) {
        if (ok == null || !ok.startsWith("OK_MULTISHAPE")) return List.of();
        String[] parts = ok.split(":", 4);
        if (parts.length < 4) return List.of();
        return Arrays.stream(parts[3].split(","))
            .map(String::trim)
            .toList();
    }

    /**
     * Extrait l'ID du groupe depuis un marqueur {@code OK_GROUP}.
     *
     * <p>Format attendu : {@code OK_GROUP:groupId:count}</p>
     *
     * @param ok chaîne brute renvoyée par le script JavaScript
     * @return l'ID du groupe si le format est valide, {@code "unknown"} sinon
     */
    private String parseOkGroupId(String ok) {
        if (ok == null || !ok.startsWith("OK_GROUP")) return "unknown";
        return ok.split(":")[1];
    }

    /**
     * Extrait l'ID du clone depuis un marqueur {@code OK_CLONE}.
     *
     * <p>Format attendu : {@code OK_CLONE:cloneId}</p>
     *
     * @param ok chaîne brute renvoyée par le script JavaScript
     * @return l'ID du clone si le format est valide, {@code "unknown"} sinon
     */
    private String parseOkCloneId(String ok) {
        if (ok == null || !ok.startsWith("OK_CLONE")) return "unknown";
        return ok.split(":")[1];
    }

    /**
     * Normalise une valeur d'alignement saisie par l'utilisateur vers une valeur interne.
     *
     * <p>Gère plusieurs variantes lexicales, notamment des expressions françaises
     * et des formulations plus naturelles (ex : "axe x", "centre horizontal").
     * Les valeurs reconnues sont mappées vers l'un des modes internes :
     * {@code left}, {@code center}, {@code right}, {@code top}, {@code middle}, {@code bottom}.</p>
     *
     * @param alignment valeur brute fournie par l'utilisateur ou le modèle IA
     * @return valeur normalisée attendue par {@link #buildAlignmentLogic},
     *         ou chaîne vide si l'entrée est {@code null}
     */
    private String normalizeAlignment(String alignment) {
        if (alignment == null) return "";
        String a = alignment.toLowerCase().trim();

        if (a.contains("axe x") || a.contains("en x")
                || a.contains("horizontal center")
                || a.contains("centre horizontal")) return "center";
        if (a.contains("axe y") || a.contains("en y")
                || a.contains("vertical center")
                || a.contains("milieu vertical")) return "middle";

        return switch (a) {
            case "centre"  -> "center";
            case "milieu"  -> "middle";
            case "haut"    -> "top";
            case "bas"     -> "bottom";
            case "gauche"  -> "left";
            case "droite"  -> "right";
            default        -> a;
        };
    }

    /**
     * Construit le fragment JavaScript appliquant un mode d'alignement sur un tableau
     * {@code shapes} déjà disponible dans le scope du script.
     *
     * <p>Les alignements horizontaux ({@code left}, {@code center}, {@code right})
     * modifient la propriété {@code x} des shapes. Les alignements verticaux
     * ({@code top}, {@code middle}, {@code bottom}) modifient {@code y}.</p>
     *
     * @param alignment valeur d'alignement normalisée (parmi {@link #VALID_ALIGNMENTS})
     * @return fragment JavaScript inline prêt à être concaténé dans le script final
     * @throws IllegalArgumentException si la valeur d'alignement n'est pas reconnue
     */
    private String buildAlignmentLogic(String alignment) {
        return switch (alignment) {
            case "left"   -> "const x = Math.min(...shapes.map(s => s.x)); shapes.forEach(s => s.x = x);";
            case "center" -> "const c = shapes.reduce((a,s) => a + s.x + s.width / 2, 0) / shapes.length; shapes.forEach(s => s.x = c - s.width / 2);";
            case "right"  -> "const r = Math.max(...shapes.map(s => s.x + s.width)); shapes.forEach(s => s.x = r - s.width);";
            case "top"    -> "const y = Math.min(...shapes.map(s => s.y)); shapes.forEach(s => s.y = y);";
            case "middle" -> "const m = shapes.reduce((a,s) => a + s.y + s.height / 2, 0) / shapes.length; shapes.forEach(s => s.y = m - s.height / 2);";
            case "bottom" -> "const b = Math.max(...shapes.map(s => s.y + s.height)); shapes.forEach(s => s.y = b - s.height);";
            default -> throw new IllegalArgumentException("Unsupported alignment: " + alignment);
        };
    }

    /**
     * Construit le fragment JavaScript appliquant une distribution uniforme des shapes
     * sur l'axe demandé.
     *
     * <p>Le code généré :</p>
     * <ol>
     *   <li>Trie les shapes par position sur l'axe cible.</li>
     *   <li>Calcule l'espace total entre la première et la dernière shape.</li>
     *   <li>Soustrait la somme des largeurs (ou hauteurs) pour obtenir l'espace disponible.</li>
     *   <li>Divise cet espace par {@code n - 1} pour obtenir l'espacement uniforme.</li>
     *   <li>Repositionne chaque shape intermédiaire.</li>
     * </ol>
     *
     * @param axis axe de distribution ({@code "horizontal"} ou {@code "vertical"})
     * @return fragment JavaScript inline prêt à être concaténé dans le script final
     */
    private String buildDistributionLogic(String axis) {
        if (axis.equalsIgnoreCase("horizontal"))
            return """
            shapes.sort((a, b) => a.x - b.x);
            const first = shapes[0];
            const last = shapes[shapes.length - 1];
            const total = (last.x + last.width) - first.x;
            const w = shapes.reduce((s, x) => s + x.width, 0);
            const gap = (total - w) / (shapes.length - 1);
            let cur = first.x;
            shapes.forEach(s => { s.x = cur; cur += s.width + gap; });
            """;
        return """
            shapes.sort((a, b) => a.y - b.y);
            const first = shapes[0];
            const last = shapes[shapes.length - 1];
            const total = (last.y + last.height) - first.y;
            const h = shapes.reduce((s, x) => s + x.height, 0);
            const gap = (total - h) / (shapes.length - 1);
            let cur = first.y;
            shapes.forEach(s => { s.y = cur; cur += s.height + gap; });
            """;
    }

    /**
     * Transforme une chaîne brute d'identifiants en liste Java exploitable.
     *
     * <p>Les cas suivants sont traités :</p>
     * <ul>
     *   <li>{@code null}, blanc ou {@code "selection"} → liste vide (utiliser la sélection active)</li>
     *   <li>ID unique → liste à un élément</li>
     *   <li>IDs multiples séparés par des virgules → liste nettoyée et filtrée</li>
     * </ul>
     *
     * @param raw chaîne brute fournie par le modèle IA ou l'utilisateur
     * @return liste des IDs individuels nettoyés ;
     *         liste vide si la sélection active doit être utilisée
     */
    private List<String> resolveIds(String raw) {
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("selection"))
            return List.of();
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
}