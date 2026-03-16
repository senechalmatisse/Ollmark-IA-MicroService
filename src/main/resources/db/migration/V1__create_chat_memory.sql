-- ─────────────────────────────────────────────
-- Table Spring AI Chat Memory 
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS spring_ai_chat_memory (
    conversation_id UUID            NOT NULL,
    content         TEXT                    NOT NULL,
    type            VARCHAR(100)            NOT NULL,
    "timestamp"     TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_spring_ai_chat_memory_conversation_id
    ON spring_ai_chat_memory (conversation_id);