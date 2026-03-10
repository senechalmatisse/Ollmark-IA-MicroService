package com.penpot.ai.shared.exception;


/**
 * Levée quand aucun snapshot n'est trouvé pour une conversation ou un ID donné.
 */
/**
 * Exception thrown when a requested snapshot cannot be found.
 * 
 * <p>This exception is raised during operations that attempt to retrieve or access
 * a snapshot that does not exist in the system.
 * 
 * @author Penpot AI
 * @version 1.0
 * @see PenpotAiException
 */
public class SnapshotNotFoundException extends PenpotAiException {

    public SnapshotNotFoundException(String message) {
        super(message);
    }
}
