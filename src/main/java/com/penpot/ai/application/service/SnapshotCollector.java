package com.penpot.ai.application.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.penpot.ai.core.domain.snapshot.ShapeModification;

import lombok.extern.slf4j.Slf4j;

/**
 * Collecteur d'événements de design pendant une génération IA.
 *
 *
 */
@Slf4j
@Component
public class SnapshotCollector {

    /** Map conversationId , UUIDs des shapes créées */
    private final Map<String, List<String>> createdShapes = new ConcurrentHashMap<>();

    /** Map conversationId , modifications de shapes existantes */
    private final Map<String, List<ShapeModification>> modifications = new ConcurrentHashMap<>();


    /** Enregistre une shape créée par l'IA pour une conversation donnée */
    public void registerCreatedShape(String conversationId, String shapeId) {
        if (conversationId == null || shapeId == null || shapeId.isBlank()) return;
        createdShapes.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(shapeId);
        log.debug("[Snapshot] Registered created shape {} for conversation {}", shapeId, conversationId);
    }

    /** Enregistre une modification pour une conversation donnée */
    public void registerModification(String conversationId, ShapeModification modification) {
        if (conversationId == null || modification == null) return;
        modifications.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(modification);
        log.debug("[Snapshot] Registered modification {} for conversation {}", modification.shapeId(), conversationId);
    }

   
    
    public List<String> getCreatedShapeIds(String conversationId) {
        return Collections.unmodifiableList(
            createdShapes.getOrDefault(conversationId, List.of())
        );
    }

    
    public List<ShapeModification> getModifications(String conversationId) {
        return Collections.unmodifiableList(
            modifications.getOrDefault(conversationId, List.of())
        );
    }

    public boolean hasEvents(String conversationId) {
        return !getCreatedShapeIds(conversationId).isEmpty()
            || !getModifications(conversationId).isEmpty();
    }

    

    /** Réinitialise les données d'une conversation après sauvegarde */
    public void clear(String conversationId) {
        createdShapes.remove(conversationId);
        modifications.remove(conversationId);
        log.debug("[Snapshot] Cleared collector for conversation {}", conversationId);
    }
}
