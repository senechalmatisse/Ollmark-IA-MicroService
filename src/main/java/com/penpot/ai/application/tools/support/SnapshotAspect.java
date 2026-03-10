/**
 * Aspect for intercepting Penpot tool operations to populate the SnapshotCollector.
 * 
 * This aspect uses Spring AOP to capture shape modifications and maintain snapshot state
 * across different execution contexts. It manages conversation context propagation via
 * InheritableThreadLocal to track interactions across HTTP and async (boundedElastic) threads.
 * 
 * <h2>Key Responsibilities:</h2>
 * <ul>
 *   <li>Intercepts shape modification operations (move, resize, rotate, etc.)</li>
 *   <li>Captures shape state before modifications using ExecuteCodeUseCase</li>
 *   <li>Registers modifications with SnapshotCollector for audit/replay purposes</li>
 *   <li>Manages conversation context propagation across thread boundaries</li>
 * </ul>
 * 
 * <h2>Conversation Context Management:</h2>
 * The aspect uses an InheritableThreadLocal to propagate conversationId from the HTTP
 * request thread to the Spring AI boundedElastic thread pool. This ensures shape modifications
 * can be correctly associated with their conversation context.
 * 
 * @see SnapshotCollector
 * @see ExecuteCodeUseCase
 * @see ShapeState
 * @see ShapeModification
 */
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
 * Aspect for capturing and managing shape modifications in Penpot AI tools.
 * 
 * This aspect intercepts method calls on PenpotTools classes that modify shapes
 * (move, resize, rotate, changeColor, changeOpacity, updateText, applyStyle)
 * and captures their state changes for snapshot tracking.
 * 
 * Uses InheritableThreadLocal to propagate conversationId from HTTP threads
 * to Spring AI boundedElastic threads, enabling proper snapshot association
 * across thread boundaries.
 * 
 * <p><strong>Thread Safety:</strong> InheritableThreadLocal ensures conversationId
 * is available in child threads spawned by Spring AI's reactive operations.</p>
 * 
 * @see ShapeModification
 * @see SnapshotCollector
 * @see ExecuteCodeUseCase
 */







/**
 * Around advice that intercepts shape modification methods in PenpotTools.
 * 
 * Captures the shape state before method execution, proceeds with the modification,
 * then registers the change with SnapshotCollector if state was successfully read.
 * 
 * Requires both conversationId and shapeId to be present; otherwise proceeds
 * without capturing the modification.
 * 
 * @param pjp the proceeding join point containing method information
 * @return the result of the intercepted method
 * @throws Throwable if the intercepted method throws an exception
 */

/**
 * Reads the current state of a shape from Penpot via ExecuteCodeUseCase.
 * 
 * Executes JavaScript code to fetch shape properties (position, size, rotation,
 * opacity, fills, content, font properties) and deserializes the JSON response
 * into a ShapeState object.
 * 
 * @param shapeId the unique identifier of the shape whose state should be read
 * @return a ShapeState object containing all shape properties, or null if
 *         the shape cannot be read or an error occurs
 */

/**
 * Extracts a UUID from various response formats returned by Penpot.
 * 
 * Supports three formats:
 * <ul>
 *   <li>Format 1: "SHAPE_ID: {uuid}" - legacy format</li>
 *   <li>Format 2: JSON with "result" field - current production format</li>
 *   <li>Format 3: Raw UUID string - fallback format</li>
 * </ul>
 * 
 * @param response the response string to parse
 * @return a valid UUID string, or null if no valid UUID can be extracted
 */

/**
 * Validates that a string conforms to the standard UUID v4 format.
 * 
 * @param value the string to validate
 * @return true if the value matches the UUID pattern, false otherwise
 */

/**
 * Converts an object to a Double value with null-safe handling.
 * 
 * Handles Number types directly and attempts string parsing for other types.
 * 
 * @param val the object to convert
 * @return the Double value, or null if conversion fails or input is null
 */

/**
 * Converts an object to an Integer value with null-safe handling.
 * 
 * Handles Number types directly and attempts string parsing for other types.
 * 
 * @param val the object to convert
 * @return the Integer value, or null if conversion fails or input is null
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
    /**
    * Sets the current conversation ID for the calling thread and all child threads.
    * 
    * Must be called by ConversationChatUseCaseImpl before each code generation
    * to ensure snapshot modifications are associated with the correct conversation.
    * 
    * @param conversationId the unique identifier for the current conversation
    * @throws IllegalArgumentException if conversationId is null or empty
    */
    public static void setConversationId(String conversationId) {
        CURRENT_CONVERSATION_ID.set(conversationId);
        
    }

    /** Appelé par ConversationChatUseCaseImpl après saveSnapshot */
    /**
    * Clears the conversation ID from the current thread's InheritableThreadLocal.
    * 
    * Must be called by ConversationChatUseCaseImpl after snapshot is saved
    * to prevent memory leaks and conversation ID pollution in thread pool scenarios.
    */
    public static void clearConversationId() {
        CURRENT_CONVERSATION_ID.remove();
    }
    /**
    * Retrieves the conversation ID from the current thread's InheritableThreadLocal.
    * 
    * @return the current conversation ID, or null if not set
    */
    public static String getConversationId() {
        return CURRENT_CONVERSATION_ID.get();
    }

    /**
     * Intercepte les méthodes de modification de shape dans les PenpotTools.
     * Capture l'état avant modification et enregistre la modification dans le SnapshotCollector.
     */
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
