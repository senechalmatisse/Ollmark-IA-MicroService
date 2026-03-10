package com.penpot.ai.application.tools.support;

import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpot.ai.application.service.SnapshotCollector;
import com.penpot.ai.core.domain.ExecuteCodeCommand;
import com.penpot.ai.core.domain.snapshot.ShapeModification;
import com.penpot.ai.core.domain.snapshot.ShapeState;
import com.penpot.ai.core.ports.in.ExecuteCodeUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Aspect qui intercepte les tools Penpot pour alimenter le SnapshotCollector.
 *
 * 
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SnapshotAspect {

    private final SnapshotCollector snapshotCollector;
    private final ExecuteCodeUseCase executeCodeUseCase;
    private final ObjectMapper objectMapper;

    /**
     * ThreadLocal pour propager le conversationId
     * depuis le thread HTTP vers le thread Spring AI boundedElastic.
     */
   private static final InheritableThreadLocal<String> CURRENT_CONVERSATION_ID = new InheritableThreadLocal<>();

    /** Appelé par ConversationChatUseCaseImpl avant chaque génération */
    public static void setConversationId(String conversationId) {
        CURRENT_CONVERSATION_ID.set(conversationId);
        
    }

    /** Appelé par ConversationChatUseCaseImpl après saveSnapshot */
    public static void clearConversationId() {
        CURRENT_CONVERSATION_ID.remove();
    }

    public static String getConversationId() {
        return CURRENT_CONVERSATION_ID.get();
    }

    
    // Interception CREATE
   

    // @AfterReturning(
    //     pointcut = "execution(* com.penpot.ai.application.tools.Penpot*Tools.create*(..))",
    //     returning = "result"
    // )
    // public void captureCreatedShapeId(JoinPoint jp, Object result) {
    //     if (result == null) return;

    //     String conversationId = CURRENT_CONVERSATION_ID.get();
    //     if (conversationId == null) {
    //         log.warn("[Snapshot] No conversationId in ThreadLocal — shape not captured for {}",
    //                 jp.getSignature().getName());
    //         return;
    //     }

    //     String uuid = extractUuidFromResponse(result.toString());
    //     if (uuid != null) {
    //         snapshotCollector.registerCreatedShape(conversationId, uuid);
    //         log.debug("[Snapshot] Created shape captured: {} from {}",
    //                 uuid, jp.getSignature().getName());
    //     }
    // }

   
    // Interception MODIFY
    

    @Around(
        "execution(* com.penpot.ai.application.tools.Penpot*Tools.move*(..))"       +
        " || execution(* com.penpot.ai.application.tools.Penpot*Tools.resize*(..))" +
        " || execution(* com.penpot.ai.application.tools.Penpot*Tools.rotate*(..))" +
        " || execution(* com.penpot.ai.application.tools.Penpot*Tools.changeColor*(..))" +
        " || execution(* com.penpot.ai.application.tools.Penpot*Tools.changeOpacity*(..))" +
        " || execution(* com.penpot.ai.application.tools.Penpot*Tools.updateText*(..))" +
        " || execution(* com.penpot.ai.application.tools.Penpot*Tools.applyStyle*(..))"
    )
    public Object captureModification(ProceedingJoinPoint pjp) throws Throwable {
        String conversationId = CURRENT_CONVERSATION_ID.get();
        String toolName = pjp.getSignature().getName();
        Object[] args = pjp.getArgs();
        String shapeId = args.length > 0 && args[0] != null ? args[0].toString() : null;

        if (conversationId == null || shapeId == null) {
            return pjp.proceed();
        }

        ShapeState stateBefore = readShapeState(shapeId);
        Object result = pjp.proceed();

        if (stateBefore != null) {
            snapshotCollector.registerModification(conversationId,
                    ShapeModification.of(shapeId, toolName, stateBefore));
            log.debug("[Snapshot] Modification captured: tool={}, shapeId={}", toolName, shapeId);
        }

        return result;
    }

    // =========================================================================
    // Lecture état Penpot
    // =========================================================================

    @SuppressWarnings("unchecked")
    private ShapeState readShapeState(String shapeId) {
        try {
            var result = executeCodeUseCase.execute(
                    ExecuteCodeCommand.of(ShapeState.buildReadJs(shapeId))
            );
            if (!result.isSuccess()) return null;

            String json = result.getData().map(Object::toString).orElse(null);
            if (json == null || json.equals("null")) return null;

            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            return ShapeState.builder()
                    .id(shapeId)
                    .x(toDouble(map.get("x")))
                    .y(toDouble(map.get("y")))
                    .width(toDouble(map.get("width")))
                    .height(toDouble(map.get("height")))
                    .rotation(toDouble(map.get("rotation")))
                    .opacity(toDouble(map.get("opacity")))
                    .fills((List<Map<String, Object>>) map.get("fills"))
                    .content((String) map.get("content"))
                    .fontSize(toInt(map.get("fontSize")))
                    .fontWeight((String) map.get("fontWeight"))
                    .build();
        } catch (Exception e) {
            log.warn("[Snapshot] Failed to read state for {}: {}", shapeId, e.getMessage());
            return null;
        }
    }

    // =========================================================================
    // Utilitaires
    // =========================================================================

    private String extractUuidFromResponse(String response) {
    if (response == null) return null;

    // Format 1 — SHAPE_ID: uuid
    if (response.contains("SHAPE_ID:")) {
        String[] parts = response.split("SHAPE_ID:");
        if (parts.length > 1) return parts[1].trim().split("\\s")[0];
    }

    // Format 2 — JSON { "result": "uuid", "log": "" }  ← cas réel de ton projet
    if (response.contains("\"result\"")) {
        try {
            Map<String, Object> map = objectMapper.readValue(response, Map.class);
            Object result = map.get("result");
            if (result != null) {
                String uuid = result.toString().trim();
                if (isValidUuid(uuid)) return uuid;
            }
        } catch (Exception e) {
            log.warn("[Snapshot] Could not parse JSON response: {}", e.getMessage());
        }
    }

    // Format 3 — UUID brut
    if (isValidUuid(response.trim())) return response.trim();

    return null;
}

private boolean isValidUuid(String value) {
    return value != null &&
           value.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
}
   

    private Double toDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return null; }
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return null; }
    }
     
}
