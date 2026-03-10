package com.penpot.ai.shared.exception;


/**
 * Levée quand aucun snapshot n'est trouvé pour une conversation ou un ID donné.
 */
public class SnapshotNotFoundException extends PenpotAiException {

    public SnapshotNotFoundException(String message) {
        super(message);
    }
}
