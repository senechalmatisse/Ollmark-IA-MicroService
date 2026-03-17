-- ─────────────────────────────────────────────
--  PROJECTS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS projects (
    id   UUID         NOT NULL DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_projects PRIMARY KEY (id)
);

-- ─────────────────────────────────────────────
-- CONVERSATIONS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS conversations (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    project_id      UUID,
    conversation_id UUID NOT NULL,
    user_id         UUID,
    CONSTRAINT pk_conversations     PRIMARY KEY (id),
    CONSTRAINT uk_conversations_cid UNIQUE (conversation_id),
    CONSTRAINT fk_conversations_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_conversations_project_id
    ON conversations (project_id);

-- ─────────────────────────────────────────────
--  MESSAGES
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS messages (
    id                   UUID        NOT NULL DEFAULT gen_random_uuid(),
    project_id            UUID      NOT NULL,
    conversation_id      UUID        NOT NULL,
    content_user         TEXT,
    content_assistant    TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_messages PRIMARY KEY (id),
    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_id
    ON messages (conversation_id);

-- ─────────────────────────────────────────────
--  AI_MODEL_CONFIG
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ai_model_config (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    project_id      UUID         NOT NULL,
    prompt_content  TEXT,
    model_name      VARCHAR(255),
    provider        VARCHAR(255),
    parameters      JSONB,
    model_api_key   TEXT,
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ai_model_config PRIMARY KEY (id),
    CONSTRAINT fk_ai_model_config_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE CASCADE
);