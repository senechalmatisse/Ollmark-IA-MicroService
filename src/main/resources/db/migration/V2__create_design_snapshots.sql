-- Migration : table design_snapshots avec support créations ET modifications
-- Fichier : V2__create_design_snapshots.sql

CREATE TABLE IF NOT EXISTS design_snapshots (
    id                  VARCHAR(36)     PRIMARY KEY,
    conversation_id     VARCHAR(255)    NOT NULL,
    user_message        VARCHAR(2000),

    -- UUIDs des shapes créées par l'IA → undo = remove()
    created_shape_ids   JSONB           NOT NULL DEFAULT '[]',

    -- État "before" des shapes modifiées → undo = restore
    -- Structure : [{shapeId, toolName, before: {x,y,w,h,fills,...}}]
    modifications       JSONB           NOT NULL DEFAULT '[]',

    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    undone              BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_snapshot_conversation
    ON design_snapshots(conversation_id);

CREATE INDEX idx_snapshot_created_at
    ON design_snapshots(created_at DESC);

-- Index partiel : requêtes sur les snapshots actifs uniquement
CREATE INDEX idx_snapshot_active
    ON design_snapshots(conversation_id, undone)
    WHERE undone = FALSE;

-- =========================================================================
-- H2 (tests) : remplacer JSONB par JSON
-- =========================================================================
-- created_shape_ids   JSON  NOT NULL DEFAULT '[]',
-- modifications       JSON  NOT NULL DEFAULT '[]',
